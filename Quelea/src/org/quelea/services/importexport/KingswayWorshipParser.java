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

import com.neovisionaries.i18n.LocaleCode;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import org.javafx.dialog.Dialog;
import org.javafx.dialog.InputDialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * Parse songs imported from the kingsway importer.
 * <p/>
 * @author Michael and Ben
 */
public class KingswayWorshipParser implements SongParser {

    private static final int CONSECUTIVE_ERROR_THRESHOLD = 20;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final String UK = "http://www.weareworship.com/uk/songs/song-library/showsong/";

    /**
     * Rough number of songs in the library at present. This is used to update
     * the progress bar.
     */
    private static final int ROUGH_NUM_SONGS = 3620;
    private int errorCount = 0;
    private boolean all;
    private int startNum, endNum;
    private boolean range;

    /**
     * Set whether we're getting all songs, or just one.
     * <p/>
     * @param all true if all songs, false if one.
     */
    public void setAll(boolean all) {
        this.all = all;
        this.range = false;
    }

    /**
     * Set the range of Id's to pull
     * <p/>
     * @param range true to show range, false to not
     */
    public void setRange(boolean range) {
        this.range = range;
        this.all = false;
    }

    /**
     * Get the songs from the kingsway online library.
     * <p/>
     * @param location not used.
     * @param statusPanel the status panel. May be null.
     * @return a list of all the songs found.
     * @throws IOException if something went wrong.
     */
    @Override
    public List<SongDisplayable> getSongs(File location, final StatusPanel statusPanel) throws IOException {
        errorCount = 0;
        List<SongDisplayable> ret = new ArrayList<>();
        String pageText;
        if (statusPanel != null) {
            Platform.runLater(() -> {
                statusPanel.getProgressBar().setProgress(0);
            });
        }
        if (all) {
            startNum = getStart();
            endNum = ROUGH_NUM_SONGS + 20;
//            System.out.println("all");
        } else if (range) {
            String results = KingswayRangeInputDialog.getUserInput();
            startNum = Integer.parseInt(results.split(",")[0]);
            endNum = Integer.parseInt(results.split(",")[1]);
//            System.out.println("range");
        } else {
            String entry = InputDialog.getUserInput(LabelGrabber.INSTANCE.getLabel("song.id.selector"), LabelGrabber.INSTANCE.getLabel("song.id.selector"));
            try {
                startNum = Integer.parseInt(entry);
                endNum = startNum;
            } catch (NumberFormatException nfe) {
                return null;
            }
//            System.out.println("single");
        }

        int index = startNum;
        while (index <= endNum && (pageText = getPageText(UK, index)) != null) {
//            System.out.println("Starting");
            int percentage = (int) (((double) index / (double) ((all) ? ROUGH_NUM_SONGS : endNum) * 100));
            LOGGER.log(Level.INFO, "Kingsway import percent complete: {0}", percentage);
            if (statusPanel != null) {
                statusPanel.getProgressBar().setProgress((double) percentage / 100);
            }
            SongDisplayable song = null;
            try {
                song = parseSong(pageText, index);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error importing song", ex);
            }
            if (song != null) {
                ret.add(song);
            }
            if (song == null) {
                errorCount++;
            } else {
                errorCount = 0;
            }
            if (errorCount > CONSECUTIVE_ERROR_THRESHOLD) {
                LOGGER.log(Level.INFO, "Giving up importing at position {0}, reached {1} consecutive errors", new Object[]{index, CONSECUTIVE_ERROR_THRESHOLD});
                break;
            }
            index++;
        }
        int nextVal = index - errorCount + 1;
        if (nextVal < 0) {
            nextVal = 0;
        }
        QueleaProperties.get().setNextKingswaySong(nextVal);
        if (statusPanel != null) {
            Platform.runLater(() -> {
                statusPanel.getProgressBar().setProgress(-1);
            });
        }
        return ret;
    }

    /**
     * Returns a list containing a single song of the ID given
     * <p/>
     * @param songID Kingsway song number
     * @return List of one Song if ID exists or null if not.
     */
    public List<SongDisplayable> getSong(int songID) {
        SongDisplayable song;
        String html = getPageText(UK, songID);
        try {
            if (html == null || html.equals("")) {
                return null;
            } else {
                song = parseSong(html, songID);
            }
        } catch (Exception ex) {
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
        if (start == 0) {
            return 0;
        }
        returnVal = -1;
        Platform.runLater(() -> {
            Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("check.kingsway.start.title"), LabelGrabber.INSTANCE.getLabel("check.kingsway.start"))
                    .addYesButton((ActionEvent t) -> {
                        returnVal = start;
                    }).addNoButton((ActionEvent t) -> {
                        returnVal = 0;
                    }).build().showAndWait();
        });
        while (returnVal == -1) {
            Utils.sleep(10);
        }
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
        try {
            if (html == null || html.contains("<h1>Sorry...</h1>") || html.contains("Server error") || html.trim().isEmpty()) {
                return null;
            }
            int startIndex = html.indexOf("<h1 class=\"no-border\" id=\"song-title\">");
            int endIndex = html.indexOf("<div class=\"span3\">");

            String songHtml = html.substring(startIndex, endIndex).trim();
            songHtml = songHtml.replace("&#39;", "'");
            songHtml = songHtml.replace("&#039;", "'");
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

            String title = songHtml.substring("<h1 class=\"no-border\" id=\"song-title\">".length(), songHtml.indexOf("</h1>"));

            int sindex = songHtml.indexOf("<h3>");
            songHtml = songHtml.substring(sindex).trim();
            String author = songHtml.substring(4, songHtml.indexOf("</h3>")).trim();
            if (author.toLowerCase().startsWith("by")) {
                author = author.substring(3).trim();
            }

            String copyright = "";
            String ccli = "";
            String patternString = "<p>Copyright (.*?)</p>";
            Pattern pc = Pattern.compile(patternString, Pattern.DOTALL);
            Matcher mc = pc.matcher(songHtml);
            if (mc.find() && mc.group(1) != null) {
                copyright = mc.group(1);
            }

            patternString = "<p>CCLI Number: (\\d*?)</p>";
            pc = Pattern.compile(patternString, Pattern.DOTALL);
            mc = pc.matcher(songHtml);
            if (mc.find() && mc.group(1) != null) {
                ccli = mc.group(1);
            }

            patternString = "Lyrics_(.._..)\">(.*?)</div>";
            Pattern p = Pattern.compile(patternString, Pattern.DOTALL);
            Matcher m = p.matcher(songHtml);

            if (m.find()) {
                String lyrics = sanitize(m.group(2));
                lyrics = lyrics.replace(author + "( & .*)?", "");
                lyrics = lyrics.replace(copyright, "");
                lyrics = lyrics.replace(ccli, "");

                if (lyrics.length() > 5) {
                    if (title.trim().isEmpty()) {
                        title = lyrics.substring(0, lyrics.indexOf("\n")).trim();
                    }
                    SongDisplayable ret = new SongDisplayable(title.trim(), author);
                    ret.setLyrics(lyrics);
                    if (copyright != null) {
                        ret.setCopyright(copyright);
                    }
                    if (ccli != null) {
                        ret.setCcli(ccli);
                    }

                    while (m.find()) {
                        String translationLyrics = sanitize(m.group(2));
                        String languageName;
                        if (LocaleCode.getByCode(m.group(1)) != null && LocaleCode.getByCode(m.group(1)).getLanguage() != null) {
                            languageName = LocaleCode.getByCode(m.group(1)).getLanguage().getName();
                        } else if (m.group(1).equals("cy_GB")) {
                            languageName = "Welsh";
                        } else {
                            languageName = m.group(1);
                        }
                        ret.addTranslation(languageName, translationLyrics);
                    }

                    return ret;
                } else if (!title.trim().toLowerCase().equals("test hymn")) {
                    LOGGER.log(Level.INFO, "Page {0} no lyrics found. Title: {1}", new Object[]{num, title});
                    return null;
                } else {
                    return null;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error importing song.", ex);
            return null;
        }
        return null;
    }

    /**
     * Get the raw page text for a particular page number.
     * <p/>
     * @param num the page number.
     * @return the raw text on the page.
     */
    private String getPageText(String location, int num) {
        LOGGER.log(Level.INFO, "Doing page {0} from location: " + location, num);
        try {
            StringBuilder content = new StringBuilder();
            URL url = new URL(location + num);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                url = new URL(conn.getHeaderField("Location"));
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), "UTF-8"))) {
                String str;
                while ((str = in.readLine()) != null) {
                    content.append(str).append("\n");
                }
            }
            return content.toString();
        } catch (Exception ex) {
            if (ex.getMessage().contains("500")) {
                return "";
            } else {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
            return null;
        }
    }

    private String sanitize(String group) {
        group = group.replace("\n", "");
        group = group.replace("<br />", "\n");
        group = group.replace("<BR />", "\n");
        group = group.replace("<br/>", "\n");
        group = group.replace("<BR/>", "\n");
        group = group.replace("<br>", "\n");
        group = group.replace("<BR>", "\n");
        group = group.replace("</p>", "\n");
        group = group.replace("</P>", "\n");
        group = group.replace("<p>", "\n");
        group = group.replace("<P>", "\n");
        group = group.replaceAll("<.*?>", "");
        String[] lines = group.split("\n");
        StringBuilder lyricSB = new StringBuilder();
        for (String line : lines) {
            lyricSB.append(line.trim()).append("\n");
        }
        group = lyricSB.toString();
        group = group.substring(0, group.indexOf("\n")).toLowerCase() + group.substring(group.indexOf("\n"));
        group = group.substring(0, 1).toUpperCase() + group.substring(1);
        group = group.trim();
        int i = group.length() - 1;
        for (int j = 0; j < 2; j++) {
            while (i > 1) {
                if (group.charAt(i) == '\n') {
                    while (group.charAt(i) == ' ') {
                        i--;
                    }
                    if (group.charAt(i) == '\n') {
                        i--;
                        break;
                    }
                }
                i--;
            }
        }
        return group;
    }

    /**
     * Testing.
     * <p/>
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
//        new JFXPanel();
        KingswayWorshipParser kwp = new KingswayWorshipParser();
//        kwp.range = true;
//        kwp.getSongs(null, null);
        List<SongDisplayable> song = kwp.getSong(2370);
    }
}
