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
package org.quelea.data.displayable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.w3c.dom.Node;

/**
 * A displayable that's an webpage.
 *
 * @author Arvid
 */
public class WebDisplayable implements Displayable {

    public static final int ICON_WIDTH = 60;
    public static final int ICON_HEIGHT = 60;
    private String url;
    private final WebView webView;
    private final WebEngine webEngine;
    private double zoomLevel = 1;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new web displayable.
     *
     * @param url the url for the displayable.
     */
    public WebDisplayable(String url) {
        this.url = url;
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webView.setCursor(Cursor.NONE);
        webView.setZoom(zoomLevel);
        webEngine.load(getUrl());
        webEngine.getLoadWorker().exceptionProperty().addListener((ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1) -> {
            LOGGER.log(Level.WARNING, "Website loading exception: ", t1);
        });
    }

    /**
     * Get the displayable url.
     *
     * @return the displayable image.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Parse some XML representing this object and return the object it
     * represents.
     *
     * @param node the XML node representing this object.
     * @return the object as defined by the XML.
     */
    public static WebDisplayable parseXML(Node node) {
        return new WebDisplayable(node.getTextContent());
    }

    /**
     * Get the XML that forms this image displayable.
     *
     * @return the XML.
     */
    @Override
    public String getXML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<url>");
        ret.append(Utils.escapeXML(url));
        ret.append("</url>");
        return ret.toString();
    }

    /**
     * Get the preview icon for this displayable (30x30.)
     *
     * @return the preview icon.
     */
    @Override
    public ImageView getPreviewIcon() {
        ImageView small = new ImageView(new Image("file:icons/website.png"));
        return small;
    }

    /**
     * Get the preview text for the web displayable.
     *
     * @return the file name.
     */
    @Override
    public String getPreviewText() {
        return url;
    }

    /**
     * Get any resources this displayable needs
     *
     * @return null, nothing is needed.
     */
    @Override
    public Collection<File> getResources() {
        return null;
    }

    /**
     * Webpages don't support clearing of text so false, always.
     *
     * @return false, always.
     */
    @Override
    public boolean supportClear() {
        return false;
    }

    @Override
    public void dispose() {
        // Do nothing to be able to keep the page loaded in the background
    }

    public void updatePreview() {

    }

    /**
     * Get the webview for web displayables.
     *
     * @return the webview
     */
    public WebView getWebView() {
        return webView;
    }

    /**
     * Get the webengine for web displayables.
     *
     * @return the webengine
     */
    public WebEngine getWebEngine() {
        return webEngine;
    }

    public void back() {
        if (webView.getEngine().getHistory().getCurrentIndex() != 0) {
            webView.getEngine().getHistory().go(-1);
        }
    }

    public void forward() {
        if (webView.getEngine().getHistory().getCurrentIndex() < webView.getEngine().getHistory().getEntries().size() - 1) {
            webView.getEngine().getHistory().go(+1);
        }
    }

    public void reload() {
        webEngine.reload();
    }

    public void setUrl(String url) {
        url = sanitiseUrl(url);
        this.url = url;
        webEngine.load(url);
    }

    public static String sanitiseUrl(String unsafeUrl) {
        try {
            URI uri = new URI(unsafeUrl);
            if ( uri.getScheme() == null ){
                uri = new URI("http://"+unsafeUrl);
            }
            return uri.toString();
        }catch(URISyntaxException e){
            LOGGER.warning("Failed to parse url \""+unsafeUrl+"\" "+e.getMessage());
            return "https://quelea.org/";
        }
    }

    public void zoom(boolean zoomIn) {
        if (!zoomIn) {
            zoomLevel = webView.getZoom() * 0.9;
        } else {
            zoomLevel = webView.getZoom() * 1.1;
        }
        webView.setZoom(zoomLevel);
    }

}
