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
    private int count500 = 0;

    @Override
    public List<Song> getSongs(File location) throws IOException {
        List<Song> ret = new ArrayList<>();
        int i = 1;
        String pageText;
        while ((pageText = getPageText(i)) != null) {
            Song song = null;
            try {
                song = parseSong(pageText);
            }
            catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
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
        int startIndex = text.indexOf("<h1>SONG LIBRARY</h1>") + "<h1>SONG LIBRARY</h1>".length();
        int endIndex = text.indexOf("<a class=", startIndex);
        if (endIndex == -1) {
            endIndex = text.indexOf("</div>", startIndex);
        }
        String songHtml = text.substring(startIndex, endIndex).trim();
        songHtml = songHtml.replace("&#39;", "'");
        songHtml = songHtml.replace("&#160;", " ");
        songHtml = songHtml.replace("&nbsp;", " ");
        songHtml = songHtml.replace(Character.toString((char)160), " ");
        songHtml = songHtml.replace("&amp;", "&");
        songHtml = songHtml.replace("&quot;", "\"");
        songHtml = songHtml.replace("&lt;", "<");
        songHtml = songHtml.replace("&gt;", ">");
        songHtml = songHtml.replace("&lsquo;", "'");
        songHtml = songHtml.replace("&rsquo;", "'");
        songHtml = songHtml.replace("&copy;", "©");

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
        songHtml = songHtml.replaceAll("\\<.*?>", "");
        songHtml = songHtml.trim();

        int i = songHtml.length() - 1;
        while (i > 1) {
            if (songHtml.charAt(i) == '\n' && songHtml.charAt(i - 1) == '\n') {
                break;
            }
            i--;
        }
        songHtml = songHtml.substring(0, i);
        songHtml = songHtml.trim();
        
        StringBuilder lyrics = new StringBuilder();
        for(String str : songHtml.split("\n")) {
            lyrics.append(str.trim()).append('\n');
        }

        Song ret = new Song(title, author);
        ret.setLyrics(lyrics.toString());
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
            count500=0;
            return content.toString();
        }
        catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            if (ex.getMessage().contains("500")) {
                count500++;
                if(count500>10) {
                    LOGGER.log(Level.INFO, "Too many 500's, giving up");
                    count500=0;
                    return null;
                }
                return "";
            }
            return null;
        }

    }
}
