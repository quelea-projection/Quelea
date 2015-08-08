/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.data;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.quelea.services.utils.LoggerUtils;

/**
 * Responsible for getting the title of a Youtube video.
 *
 * @author Michael
 */
public class YoutubeInfo {

    private String urlStr;
    private String vidId;
    private List<NameValuePair> params;
    private boolean initParams;
    private WritableImage preview;
    private String scrapedTitle;
    private Logger LOGGER = LoggerUtils.getLogger();

    public YoutubeInfo(String url) {
        this.urlStr = url;
        int startInx = urlStr.indexOf("v=") + 2;
        if (urlStr.contains("v=") && urlStr.length() >= startInx + 11) {
            vidId = urlStr.substring(startInx, startInx + 11);
        } else {
            vidId = null;
        }
    }

    private YoutubeInfo() {
    }

    public static YoutubeInfo fromTitle(final String title) {
        YoutubeInfo info = new YoutubeInfo();
        info.params = new ArrayList<>();
        info.params.add(new NameValuePair() {

            @Override
            public String getName() {
                return "title";
            }

            @Override
            public String getValue() {
                return title;
            }
        });
        info.initParams = true;
        return info;
    }

    public String getLocation() {
        return urlStr;
    }

    public void initParams() {
        if (initParams || vidId == null) {
            return;
        }
        initParams = true;
        try {
            String detailsStr = "https://youtube.com/get_video_info?video_id=" + vidId;
            URL url = new URL(detailsStr);
            InputStreamReader in = new InputStreamReader(url.openStream());
            StringBuilder line = new StringBuilder();
            int part;
            while ((part = in.read()) != -1) {
                line.append((char) part);
            }
            String paramStr = "http://youtube.com/x?" + line;
            params = URLEncodedUtils.parse(new URI(paramStr), "UTF-8");
            if (getTitle() == null || getTitle().isEmpty()) {
                scrapedTitle = getScrapedTitle();
            }
        } catch (IOException | URISyntaxException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get youtube data", ex);
        }
    }

    private String getScrapedTitle() {
        if (scrapedTitle != null) {
            return scrapedTitle;
        }
        if (vidId == null) {
            return null;
        }
        try {
            URL scrapeurl = new URL("https://www.youtube.com/watch?v=" + vidId);
            BufferedReader in = new BufferedReader(new InputStreamReader(scrapeurl.openStream()));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("<title>")) {
                    scrapedTitle = line.substring(line.indexOf("<title>") + "<title>".length(), line.indexOf("</title>"));
                }
            }
            in.close();
            if (scrapedTitle.toLowerCase().endsWith(" - youtube")) {
                scrapedTitle = scrapedTitle.substring(0, scrapedTitle.length() - " - YouTube".length());
            }
            return scrapedTitle;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Couldn't get scraped title", ex);
            return null;
        }
    }

    /**
     * Attempt to get the title from a Youtube video.
     *
     * @return the title of the Youtube video
     */
    public String getTitle() {
        String title = getParam("title");
        if (title != null && !title.isEmpty()) {
            return title;
        } else if (scrapedTitle != null) {
            return scrapedTitle;
        }
        return "";
    }

    private String getParam(String paramName) {
        if (params == null) {
            return null;
        }
        for (NameValuePair param : params) {
            if (param.getName().equals(paramName)) {
                return param.getValue();
            }
        }
        return null;
    }

    public Image getPreviewImage() {
        if (vidId == null) {
            return null;
        }
        try {
            if (preview == null) {
                URL imgURL = new URL("http://img.youtube.com/vi/" + vidId + "/0.jpg");
                BufferedImage bi = ImageIO.read(imgURL);
                preview = new WritableImage(bi.getWidth(), bi.getHeight());
                SwingFXUtils.toFXImage(bi, preview);
            }
            return preview;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Couldn't get youtube preview image");
            return null;
        }
    }

}
