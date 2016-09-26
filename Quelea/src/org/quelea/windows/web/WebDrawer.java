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
package org.quelea.windows.web;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.WebDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;

/**
 * Responsible for drawing an webpage onto the DisplayCanvas.
 * <p/>
 * @author Arvid
 */
public class WebDrawer extends DisplayableDrawer {

    private WebView webView;
    private WebEngine webEngine;
    private WebDisplayable d;

    @Override
    public void draw(Displayable displayable) {
        clear();
        d = (WebDisplayable) displayable;
        webView = d.getWebView();
        webEngine = d.getWebEngine();
        if (!getCanvas().isStageView()) {
            if (!d.getUrl().startsWith("http")) {
                d.setUrl("http://" + d.getUrl());
            } else {
                if (webEngine.getTitle() == null) {
                    webEngine.load(d.getUrl());
                }
            }
            addWebView(webView);
        } else {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setFitWidth(getCanvas().getWidth());
            Image image = Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor());
            imageView.setImage(image);
            getCanvas().getChildren().add(imageView);
        }

    }

    @Override
    public void clear() {
        if (getCanvas().getChildren() != null) {
            getCanvas().clearNonPermanentChildren();
        }
    }

    @Override
    public void requestFocus() {
        webView.requestFocus();
    }

    public Image getPreview(int x, int y) {
        WritableImage previewImage = new WritableImage(x, y);
        QueleaApp.get().getProjectionWindow().getCanvas().snapshot(new SnapshotParameters(), previewImage);
        BufferedImage bi = SwingFXUtils.fromFXImage((WritableImage) previewImage, null);
        SwingFXUtils.toFXImage(bi, previewImage);
        return previewImage;
    }

    public void back() {
        d.back();
    }

    public void forward() {
        d.forward();
    }

    public void reload() {
        d.reload();
    }

    public void setUrl(String url) {
        d.setUrl(url);
    }

    public String getURL() {
        if (webEngine != null) {
            return webEngine.getLocation();
        } else {
            return "";
        }
    }

    public boolean isLoading() {
        return webEngine.getLoadWorker().isRunning();
    }

    public void addWebView(WebView wv) {
        if (!getCanvas().isStageView()) {
            getCanvas().getChildren().add(wv);
        }
    }

    public String getErrorMessage() {
        try {
            return webEngine.getLoadWorker().getException().getMessage();
        } catch (NullPointerException e) {

        }
        return "";
    }
}
