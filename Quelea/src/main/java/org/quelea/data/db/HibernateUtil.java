package org.quelea.data.db;

import java.io.File;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.EJB3ImplicitNamingStrategy;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 * <p/>
 * @author tomaszpio@gmail.com
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
    private static boolean init = false;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    public interface SessionCallback {

        void execute(Session session);
    }

    public static boolean init() {
        try {
            LOGGER.info("Initialising hibernate");
            final String location = new File(new File(QueleaProperties.get().getQueleaUserHome(), "database_new"), "database_new").getAbsolutePath();
            final Configuration cfg = new Configuration();
            cfg.setProperty("hibernate.connection.url", "jdbc:hsqldb:" + location);
            cfg.setProperty("hibernate.connection.dialect", "org.hibernate.dialect.HSQLDialect");
            cfg.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
            cfg.setProperty("hibernate.show_sql", "false");
            cfg.setProperty("hibernate.hbm2ddl.auto", "update");
            cfg.setProperty("hibernate.implicit_naming_strategy", "legacy-hbm");
            cfg.setProperty("hibernate.connection.characterEncoding", "utf8");
            cfg.setImplicitNamingStrategy(new EJB3ImplicitNamingStrategy());
            cfg.addAnnotatedClass(org.quelea.data.db.model.Song.class);
            cfg.addAnnotatedClass(org.quelea.data.db.model.Theme.class);
            cfg.addAnnotatedClass(org.quelea.data.db.model.TextShadow.class);
            LOGGER.info("Initialised hibernate properties");
            serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
            LOGGER.info("Initialised hibernate service registry");
            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
            LOGGER.info("Initialised hibernate session factory");
            init = true;
            writeCommitsImmediately();
            return true;
        } catch (Throwable ex) {
            LOGGER.log(Level.INFO, "Initial SessionFactory creation failed. Quelea is probably already running.", ex);
            return false;
        }
    }

    /**
     * By default the database writes commits to disk up to half a second later.
     * Quelea doesn't shut the database down on exit, so anything saved just
     * before exiting (or a crash) would be lost.
     */
    private static void writeCommitsImmediately() {
        try {
            execute(session -> session.doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET FILES WRITE DELAY FALSE");
                }
            }));
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Couldn't turn off the database write delay", ex);
        }
    }

    /**
     * Encapsulate session management operations. All db operation code should
     * be implemented in callback execute method implementation.
     * <p/>
     * @param callback
     */
    public static void execute(SessionCallback callback) {
        if (!init) {
            throw new IllegalStateException("Database must be initialised first");
        }

        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.getTransaction().begin();
            callback.execute(session);
            session.getTransaction().commit();
            session.close();
        } catch (Exception ex) {
            if (session != null) {
                session.close();
            }
            throw new IllegalStateException("Couldn't update database", ex);
        }
    }
}