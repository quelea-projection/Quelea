package org.quelea.data.db;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.data.db.model.TextShadow;
import org.quelea.data.db.model.Theme;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.EJB3ImplicitNamingStrategy;

class LegacyVideoThemeDatabaseTest {
    @TempDir Path directory;
    private QueleaProperties previousProperties;

    @BeforeEach
    void isolateProperties() {
        previousProperties = QueleaProperties.get();
        QueleaProperties.init(directory.toString());
    }

    @AfterEach
    void restoreProperties() throws Exception {
        var instance = QueleaProperties.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, previousProperties);
    }

    @Test
    void existingHueColumnDoesNotPreventReadingOrSavingThemes() throws Exception {
        String url = "jdbc:hsqldb:mem:legacytheme" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = DriverManager.getConnection(url, "SA", "")) {
            long id;
            try (SessionFactory factory = factory(url, "create"); var session = factory.openSession()) {
                var transaction = session.beginTransaction();
                Theme theme = new Theme("Legacy", "System", "0xffffffff", "System", "0xffffffff",
                        "", "background.mp4", "", new TextShadow("0x000000ff", 0.0, 0.0, 2.0, 0.0, true),
                        false, false, false, false, true, -1, 0);
                id = (Long) session.save(theme);
                transaction.commit();
            }
            try (var statement = connection.createStatement()) {
                statement.execute("ALTER TABLE theme ADD COLUMN videoHue DOUBLE");
                statement.executeUpdate("UPDATE theme SET videoHue = 0.35 WHERE id = " + id);
            }
            try (SessionFactory factory = factory(url, "update"); var session = factory.openSession()) {
                var transaction = session.beginTransaction();
                Theme legacy = session.get(Theme.class, id);
                VideoBackground background = Assertions.assertInstanceOf(VideoBackground.class, ThemeDTO.getDTO(legacy).getBackground());
                Assertions.assertEquals("background.mp4", background.getString());
                Assertions.assertTrue(background.getStretch());
                legacy.setName("Updated");
                transaction.commit();
            }
            try (var statement = connection.createStatement();
                    var rows = statement.executeQuery("SELECT name, videoHue FROM theme WHERE id = " + id)) {
                Assertions.assertTrue(rows.next());
                Assertions.assertEquals("Updated", rows.getString(1));
                Assertions.assertEquals(0.35, rows.getDouble(2), 0.0001);
            }
        }
    }

    private SessionFactory factory(String url, String schemaMode) {
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.url", url);
        cfg.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        cfg.setProperty("hibernate.hbm2ddl.auto", schemaMode);
        cfg.setImplicitNamingStrategy(new EJB3ImplicitNamingStrategy());
        cfg.addAnnotatedClass(Theme.class);
        cfg.addAnnotatedClass(TextShadow.class);
        return cfg.buildSessionFactory();
    }
}
