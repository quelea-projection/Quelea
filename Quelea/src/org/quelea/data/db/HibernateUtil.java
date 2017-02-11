package org.quelea.data.db;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

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
            final String location = new File(new File(QueleaProperties.getQueleaUserHome(), "database_new"), "database_new").getAbsolutePath();
            final Configuration cfg = new Configuration();
            cfg.setProperty("hibernate.connection.url", "jdbc:hsqldb:" + location);
            cfg.setProperty("hibernate.connection.dialect", "org.hibernate.dialect.HSQLDialect");
            cfg.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
            cfg.setProperty("hibernate.show_sql", "false");
            cfg.setProperty("hibernate.hbm2ddl.auto", "update");
            cfg.setProperty("hibernate.connection.characterEncoding", "utf8");
            cfg.addAnnotatedClass(org.quelea.data.db.model.Song.class);//@todo add reflection code which retrieve all classes from package
            cfg.addAnnotatedClass(org.quelea.data.db.model.Theme.class);
            cfg.addAnnotatedClass(org.quelea.data.db.model.TextShadow.class);
            LOGGER.info("Initialised hibernate properties");
            serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();
            LOGGER.info("Initialised hibernate service registry");
            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
            LOGGER.info("Initialised hibernate session factory");
            init = true;
            return true;
        } catch (Throwable ex) {
            LOGGER.log(Level.INFO, "Initial SessionFactory creation failed. Quelea is probably already running.", ex);
            return false;
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
