package org.quelea.windows.help;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.Version;
import org.quelea.services.utils.VersionType;
import org.quelea.testutil.FxTestSupport;
import org.quelea.windows.style.StyleManager;
import org.quelea.windows.style.UiTheme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quelea.testutil.FxTestSupport.onFx;

class AboutDialogTest {

    @Test
    void versionIncludesReleaseChannelWhenApplicable() {
        assertEquals("2027.0 CI", AboutDialog.displayVersion(new Version("2027.0", VersionType.CI)));
        assertEquals("2027.0 Beta", AboutDialog.displayVersion(new Version("2027.0", VersionType.BETA)));
        assertEquals("2027.0", AboutDialog.displayVersion(new Version("2027.0", VersionType.RELEASE)));
    }

    @Test
    void contentAndFooterUseThemeSurface() throws Exception {
        FxTestSupport.assumeAvailable();
        onFx(() -> {
            UiTheme previous = StyleManager.getTheme();
            AboutDialog dialog = null;
            try {
                for (UiTheme theme : new UiTheme[]{UiTheme.LIGHT, UiTheme.DARK}) {
                    StyleManager.applyTheme(theme);
                    if (dialog == null) {
                        dialog = new AboutDialog();
                        dialog.show();
                    }
                    var root = dialog.getScene().getRoot();
                    root.applyCss();
                    root.layout();
                    WritableImage image = root.snapshot(null, null);
                    Color surface = theme == UiTheme.LIGHT ? Color.web("#ffffff") : Color.web("#24272c");
                    assertEquals(surface, image.getPixelReader().getColor(2, 2));
                    assertEquals(surface, image.getPixelReader().getColor(2, (int) image.getHeight() - 3));
                }
            } finally {
                if (dialog != null) {
                    dialog.close();
                }
                StyleManager.applyTheme(previous);
            }
            return null;
        });
    }
}
