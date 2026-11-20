/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main.widgets;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.utils.QueleaProperties;

import java.io.File;
import java.util.List;

/**
 * A small, projector shaped preview of the logo, for editing the logo
 * settings before they're saved. Uses the same {@link LogoImage} (and so the
 * same layout and SVG rendering) as the projector, with the same black
 * background.
 *
 * @author Arvid
 */
public class LogoPreview extends StackPane {

    private final LogoImage logo;
    private final double previewWidth;

    /**
     * Create a logo preview.
     *
     * @param width the width of the preview; the height follows from the
     *              projector's aspect ratio.
     */
    public LogoPreview(double width) {
        this.previewWidth = width;
        logo = LogoImage.createPreview();
        getStyleClass().add("logo-preview");
        getChildren().add(logo);
        updateAspectRatio();
    }

    /**
     * Match the shape of the projector again (it may have been changed).
     */
    public final void updateAspectRatio() {
        double height = Math.round(previewWidth / projectorAspectRatio());
        setMinSize(previewWidth, height);
        setPrefSize(previewWidth, height);
        setMaxSize(previewWidth, height);
        logo.setMinSize(previewWidth, height);
        logo.setPrefSize(previewWidth, height);
        logo.setMaxSize(previewWidth, height);
    }

    /**
     * Show the given logo with the given settings.
     *
     * @param location the logo's file path, as in the properties; shows just
     *                 black if it isn't an existing file.
     * @param settings the size and position.
     */
    public void show(String location, LogoSettings settings) {
        String uri = null;
        if (location != null && !location.trim().isEmpty()) {
            File file = new File(location.trim());
            if (file.isFile()) {
                uri = file.toURI().toString();
            }
        }
        logo.showLogo(uri, settings);
    }

    /**
     * @return the logo image in this preview (for testing).
     */
    LogoImage getLogoImage() {
        return logo;
    }

    /**
     * @return width / height of the projector output, or 16:9 if there isn't
     * one.
     */
    public static double projectorAspectRatio() {
        QueleaProperties props = QueleaProperties.get();
        double width = 0;
        double height = 0;
        if (props.isProjectorModeCoords()) {
            Bounds bounds = props.getProjectorCoords();
            width = bounds.getWidth();
            height = bounds.getHeight();
        } else {
            int screen = props.getProjectorScreen();
            List<Screen> screens = Screen.getScreens();
            if (screen >= 0 && screen < screens.size()) {
                Rectangle2D bounds = screens.get(screen).getBounds();
                width = bounds.getWidth();
                height = bounds.getHeight();
            }
        }
        if (width > 0 && height > 0) {
            return width / height;
        }
        return 16.0 / 9.0;
    }
}
