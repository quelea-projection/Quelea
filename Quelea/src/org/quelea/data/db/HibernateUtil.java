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
            serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();
            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
            init = true;
            return true;
        }
        catch(Throwable ex) {
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
        if(!init) {
            throw new IllegalStateException("Database must be initialised first");
        }
        final Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        callback.execute(session);
        session.getTransaction().commit();
        session.close();
    }
}
