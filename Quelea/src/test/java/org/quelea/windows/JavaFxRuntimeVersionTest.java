package org.quelea.windows;

import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import javafx.beans.Observable;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.media.Media;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the JavaFX classes actually loaded, not just Gradle's resolved dependencies. */
class JavaFxRuntimeVersionTest {
    @Test
    void allJavaFxModulesComeFromVersion27() throws Exception {
        String runtimeVersion = loadedJavaFxVersion();
        assertTrue(runtimeVersion.matches("27(?:[.+-].*)?"), () -> "Loaded JavaFX version '"
                + runtimeVersion + "' instead of JavaFX 27; a JDK with an older bundled JavaFX may"
                + " be shadowing the Gradle dependencies");
        String baseModuleVersion = loadedDescriptor(Observable.class).rawVersion().orElse("");
        assertTrue(!baseModuleVersion.isEmpty(), "Loaded JavaFX base module has no version");
        Map<String, Class<?>> modules = Map.of(
                "javafx.base", Observable.class,
                "javafx.graphics", Node.class,
                "javafx.controls", Control.class,
                "javafx.fxml", FXMLLoader.class,
                "javafx.media", Media.class,
                "javafx.swing", JFXPanel.class,
                "javafx.web", WebView.class);
        for (var entry : modules.entrySet()) {
            ModuleDescriptor descriptor = loadedDescriptor(entry.getValue());
            assertEquals(entry.getKey(), descriptor.name(), "Wrong module for " + entry.getValue());
            String version = descriptor.rawVersion().orElse("");
            assertEquals(baseModuleVersion, version, () -> entry.getKey() + " was loaded from a"
                    + " different JavaFX build than javafx.base");
        }
    }

    private static String loadedJavaFxVersion() throws Exception {
        Properties properties = new Properties();
        Module module = Observable.class.getModule();
        if (module.isNamed()) {
            try (InputStream in = module.getResourceAsStream("javafx.properties")) {
                if (in != null) {
                    properties.load(in);
                } else {
                    // Some bundled JavaFX distributions compile the version
                    // into VersionInfo and do not ship javafx.properties.
                    Class<?> info = Class.forName("com.sun.javafx.runtime.VersionInfo", true,
                            Observable.class.getClassLoader());
                    return (String) info.getMethod("getVersion").invoke(null);
                }
            }
        } else {
            try (JarFile jar = new JarFile(classJar(Observable.class).toFile())) {
                var entry = jar.getJarEntry("javafx.properties");
                assertNotNull(entry, "No javafx.properties in the loaded javafx.base JAR");
                try (InputStream in = jar.getInputStream(entry)) {
                    properties.load(in);
                }
            }
        }
        return properties.getProperty("javafx.version", "");
    }

    private static ModuleDescriptor loadedDescriptor(Class<?> type) throws Exception {
        ModuleDescriptor descriptor = type.getModule().getDescriptor();
        if (descriptor != null) {
            return descriptor;
        }
        // JavaFX can also be loaded from the classpath. Read the descriptor
        // from this class's own JAR, rather than a possibly shadowed resource
        // elsewhere on the classpath. No toolkit or display is required.
        Path location = classJar(type);
        try (JarFile jar = new JarFile(location.toFile())) {
            var moduleInfo = jar.getJarEntry("module-info.class");
            assertNotNull(moduleInfo, () -> "No module descriptor in " + location);
            try (InputStream in = jar.getInputStream(moduleInfo)) {
                return ModuleDescriptor.read(in);
            }
        }
    }

    private static Path classJar(Class<?> type) throws Exception {
        URL location = type.getProtectionDomain().getCodeSource().getLocation();
        return Path.of(location.toURI());
    }
}
