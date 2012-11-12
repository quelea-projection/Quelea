/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.javafx.dialog.InputDialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.StatusPanel;

/**
 * Parse songs imported from the kingsway importer.
 * <p/>
 * @author Michael and Ben
 */
public class KingswayWorshipParser implements SongParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    /**
     * Rough number of songs in the library at present.
     */
    private static final int ROUGH_NUM_SONGS = 2600;
    private static final SongDisplayable DEFAULT = new SongDisplayable("", "");
    private int count500 = 0;
    private boolean all;

    /**
     *
     */
    public void setAll(boolean all) {
        this.all = all;
    }

    /**
     * Get the songs from the kingsway online library.
     * <p/>
     * @param location not used.
     * @param the status panel. May be null.
     * @return a list of all the songs found.
     * @throws IOException if something went wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, StatusPanel statusPanel) throws IOException {
        if(all) {
            count500 = 0;
            List<SongDisplayable> ret = new ArrayList<>();
            int i = getStart();
            String pageText;
            if(statusPanel != null) {
                statusPanel.getProgressBar().setProgress(0);
            }
            while((pageText = getPageText(i)) != null) {
                int percentage = (int) (((double) i / (double) ROUGH_NUM_SONGS) * 100);
                LOGGER.log(Level.INFO, "Kingsway import percent complete: {0}", percentage);
                if(statusPanel != null) {
                    statusPanel.getProgressBar().setProgress((double) percentage / 100);
                }
                SongDisplayable song = null;
                try {
                    song = parseSong(pageText, i);
                }
                catch(Exception ex) {
                    LOGGER.log(Level.WARNING, "Error importing song", ex);
                }
                if(song != DEFAULT) {
                    ret.add(song);
                }
                i++;
            }
            int nextVal = i - count500 + 1;
            if(nextVal < 0) {
                nextVal = 0;
            }
            QueleaProperties.get().setNextKingswaySong(nextVal);
            if(statusPanel != null) {
                statusPanel.getProgressBar().setProgress(-1);
            }
            return ret;
        }
        else {
            String entry = InputDialog.getUserInput(LabelGrabber.INSTANCE.getLabel("song.id.selector"), LabelGrabber.INSTANCE.getLabel("song.id.selector"));
            try {
                int id = Integer.parseInt(entry);
                return getSong(id);
            }
            catch(NumberFormatException nfe) {
                return null;
            }
        }
    }

    /*
     * Code by Ben Goodwin
     */
    /**
     * Returns a list containing a single song of the ID given
     * <p/>
     * @param songID Kingsway song number
     * @return List of one Song if ID exists or null if not.
     */
    public List<SongDisplayable> getSong(int songID) {
        SongDisplayable song;
        String html = getPageText(songID);
        try {
            if(html == null || html.equals("")) {
                return null;
            }
            else {
                song = parseSong(html, songID);
            }
        }
        catch(Exception ex) {
            LOGGER.log(Level.WARNING, "Error importing song: " + songID, ex);
            return null;
        }

        List<SongDisplayable> ret = new ArrayList<>();
        ret.add(song);
        return ret;
    }
    private int returnVal;

    /**
     * Work out the number to start importing at.
     * <p/>
     * @return the song number to start importing at.
     */
    private int getStart() {
        final int start = QueleaProperties.get().getNextKingswaySong();
        if(start == 0) {
            return 0;
        }
        returnVal = 0;
        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("check.kingsway.start.title"), LabelGrabber.INSTANCE.getLabel("check.kingsway.start"))
                .addYesButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                returnVal = start;
            }
        }).addNoButton(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                returnVal = 0;
            }
        }).build().showAndWait();
        return returnVal;
    }

    /**
     * Parse the given HTML to produce a song object.
     * <p/>
     * @param html the HTML to parse.
     * @param num the number of the song (debugging use only)
     * @return the song from the given HTML
     */
    private SongDisplayable parseSong(String html, int num) {
        if(html == null) {
            return null;
        }
        if(html.isEmpty()) {
            return DEFAULT;
        }
        int startIndex = html.indexOf("<h1>SONG LIBRARY</h1>") + "<h1>SONG LIBRARY</h1>".length();
        int endIndex = html.indexOf("<a class=", startIndex);
        if(endIndex == -1) {
            endIndex = html.indexOf("</div>", startIndex);
        }
        String songHtml = html.substring(startIndex, endIndex).trim();
        songHtml = songHtml.replace("&#39;", "'");
        songHtml = songHtml.replace("&#160;", " ");
        songHtml = songHtml.replace("&nbsp;", " ");
        songHtml = songHtml.replace(Character.toString((char) 160), " ");
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
        songHtml = songHtml.replace("<BR />", "\n");
        songHtml = songHtml.replace("<br/>", "\n");
        songHtml = songHtml.replace("<BR/>", "\n");
        songHtml = songHtml.replace("<br>", "\n");
        songHtml = songHtml.replace("<BR>", "\n");
        songHtml = songHtml.replace("</p>", "\n");
        songHtml = songHtml.replace("</P>", "\n");
        songHtml = songHtml.replace("<p>", "");
        songHtml = songHtml.replace("<P>", "");
        songHtml = songHtml.replaceAll("\\<.*?>", "");
        songHtml = songHtml.trim();

        int i = songHtml.length() - 1;
        for(int j = 0; j < 2; j++) {
            while(i > 1) {
                if(songHtml.charAt(i) == '\n') {
                    while(songHtml.charAt(i) == ' ') {
                        i--;
                    }
                    if(songHtml.charAt(i) == '\n') {
                        i--;
                        break;
                    }
                }
                i--;
            }
        }
        i++;
        songHtml = songHtml.substring(0, i);
        songHtml = songHtml.trim();

        StringBuilder lyrics = new StringBuilder();
        String[] strx = songHtml.split("\n");

        /*
         * Code by Ben Goodwin
         */
        //Below uncapitalises the first line of the song.
        String fl = strx[0];
        fl = fl.toLowerCase();
        fl = fl.replaceFirst(Pattern.quote(fl.substring(0, 1)), fl.substring(0, 1).toUpperCase()); //recapitalise first letter
        String[] godWords = QueleaProperties.get().getGodWords();
        for(int c = 0; c < godWords.length; c += 2) {
            fl = fl.replaceAll("(?<=\\W)" + godWords[c] + "(?=\\W)", godWords[c + 1]); //recapitalise God words
        }
        char[] y = fl.toCharArray();
        for(int i2 = 0; i2 < y.length - 2; i2++) {
            if(y[i2] == '!' || y[i2] == '.' || y[i2] == '?') {
                i2 += 2;
                y[i2] = Character.toUpperCase(y[i2]); // recaptalise after punctuation
            }
        }
        fl = new String(y);
        lyrics.append(fl.trim()).append('\n'); //add first line
        /*
         * End of code by Ben Goodwin
         */

        for(int index = 1; index < strx.length; index++) {
            lyrics.append(strx[index].trim()).append('\n');
        }

        if(title.trim().isEmpty()) {
            title = lyrics.toString().split("\n")[0];
        }

        if(lyrics.toString().length() > 5) {
            SongDisplayable ret = new SongDisplayable(title, author);
            ret.setLyrics(lyrics.toString());
            return ret;
        }
        else {
            LOGGER.log(Level.WARNING, "Page {0} no lyrics found", num);
            return null;
        }
    }

    /**
     * Get the raw page text for a particular page number.
     * <p/>
     * @param num the page number.
     * @return the raw text on the page.
     */
    private String getPageText(int num) {
        LOGGER.log(Level.INFO, "Doing page {0}", num);
        try {
            StringBuilder content = new StringBuilder();
            URL url = new URL("http://www.kingswayworship.co.uk/song-library/showsong/" + num);
            try(BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String str;
                while((str = in.readLine()) != null) {
                    content.append(str).append("\n");
                }
            }
            count500 = 0;
            return content.toString();
        }
        catch(Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            if(ex.getMessage().contains("500")) {
                count500++;
                if(count500 > 10) {
                    LOGGER.log(Level.INFO, "Too many 500's, giving up");
//                    count500 = 0;
                    return null;
                }
                return "";
            }
            return null;
        }

    }

    /**
     * Testing.
     * <p/>
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new KingswayWorshipParser().getSongs(null, null);
    }
}
