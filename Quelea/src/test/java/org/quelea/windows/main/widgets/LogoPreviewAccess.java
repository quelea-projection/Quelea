package org.quelea.windows.main.widgets;

import javafx.geometry.Rectangle2D;

import static org.quelea.testutil.FxTestSupport.onFx;

/**
 * Lets tests in other packages look inside a {@link LogoPreview}.
 */
public final class LogoPreviewAccess {

    private LogoPreviewAccess() {
    }

    public static Rectangle2D logoBounds(LogoPreview preview) {
        return preview.getLogoImage().getLogoBounds();
    }

    public static double width(LogoPreview preview) {
        return preview.getPrefWidth();
    }

    public static double height(LogoPreview preview) {
        return preview.getPrefHeight();
    }

    /**
     * Wait (off the FX thread) until the preview shows an image.
     */
    public static boolean waitForImage(LogoPreview preview) throws Exception {
        long end = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < end) {
            if (onFx(() -> preview.getLogoImage().getShownImage()) != null) {
                return true;
            }
            Thread.sleep(50);
        }
        return false;
    }
}
