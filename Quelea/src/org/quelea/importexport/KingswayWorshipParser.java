package org.quelea.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;

/**
 *
 * @author Michael
 */
public class KingswayWorshipParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final Song DEFAULT = new Song("", "");

    @Override
    public List<Song> getSongs(File location) throws IOException {
        LOGGER.log(Level.INFO, "Making connection");
        List<Song> ret = new ArrayList<>();
        int i = 1;
        String pageText;
        while ((pageText = getPageText(i)) != null) {
            Song song = parseSong(pageText);
            if (song != DEFAULT) {
                ret.add(song);
            }
            i++;
        }
        return ret;
    }

    private Song parseSong(String text) {
        if (text == null) {
            return null;
        }
        if (text.isEmpty()) {
            return DEFAULT;
        }
        String songHtml = text.substring(text.indexOf("<h1>SONG LIBRARY</h1>") + "<h1>SONG LIBRARY</h1>".length(), text.indexOf("<a class=")).trim();

        String title = songHtml.substring(4, songHtml.indexOf("</h2>"));
        songHtml = songHtml.substring(songHtml.indexOf("</h2>") + 5);
        songHtml = songHtml.trim();

        String author = songHtml.substring(4, songHtml.indexOf("</h3>"));
        songHtml = songHtml.substring(songHtml.indexOf("</h3>") + 5);
        songHtml = songHtml.trim();

        songHtml = songHtml.replace("<br />", "\n");
        songHtml = songHtml.replace("<br/>", "\n");
        songHtml = songHtml.replace("<br>", "\n");
        songHtml = songHtml.replace("</p>", "\n");
        songHtml = songHtml.replace("<p>", "");
        songHtml = songHtml.replace("&#39;", "'");
        songHtml = songHtml.replace("&amp;", "&");
        songHtml = songHtml.replace("&quot;", "\"");
        songHtml = songHtml.replace("&lt;", "<");
        songHtml = songHtml.replace("&gt;", ">");
        songHtml = songHtml.replace("&lsquo;", "'");
        songHtml = songHtml.replace("&rsquo;", "'");
        songHtml = songHtml.replace("&copy;", "©");
        songHtml = songHtml.replaceAll("\\<.*?>", "");
        songHtml = songHtml.trim();

        int i = songHtml.length() - 1;
        while (i > 1) {
            if (songHtml.charAt(i)=='\n'&&songHtml.charAt(i-1)=='\n') {
                break;
            }
            i--;
        }
        songHtml = songHtml.substring(0, i);
        songHtml = songHtml.trim();

        Song ret = new Song(title, author);
        ret.setLyrics(songHtml);
        return ret;
    }

    private String getPageText(int num) {
        LOGGER.log(Level.INFO, "Doing page {0}", num);
        try {
            StringBuilder content = new StringBuilder();
            URL url = new URL("http://www.kingswayworship.co.uk/song-library/showsong/" + num);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String str;
                while ((str = in.readLine()) != null) {
                    content.append(str).append("\n");
                }
            }
            return content.toString();
        }
        catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            if (ex.getMessage().contains("500")) {
                return "";
            }
            return null;
        }

    }
}
