package org.quelea.data.db;

import java.io.File;
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
 *
 * @author tomaszpio@gmail.com
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory;
    private static final ServiceRegistry serviceRegistry;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    public interface SessionCallback {

        void execute(Session session);
    }

    static {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml) 
            // config file.

            final String location = new File(new File(QueleaProperties.getQueleaUserHome(), "database_new"), "database_new").getAbsolutePath();
            final Configuration cfg = new Configuration();
            cfg.setProperty("hibernate.connection.url", "jdbc:hsqldb:" + location);
            cfg.setProperty("hibernate.connection.dialect", "org.hibernate.dialect.HSQLDialect");
            cfg.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
            cfg.setProperty("hibernate.show_sql", "false");
            cfg.setProperty("hibernate.hbm2ddl.auto", "update");
            cfg.addAnnotatedClass(org.quelea.data.db.model.Song.class);//@todo add reflection code which retrieve all classes from package
            cfg.addAnnotatedClass(org.quelea.data.db.model.Theme.class);
            cfg.addAnnotatedClass(org.quelea.data.db.model.TextShadow.class);
            serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();
            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            // Log the exception. 
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Encapsulate session management operations. All db operation code should be implemented
     * in callback execute method implementation.
     * 
     * @param callback 
     */
    public static void execute(SessionCallback callback) {
        final Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        callback.execute(session);
        session.getTransaction().commit();
        session.close();
    }
}
