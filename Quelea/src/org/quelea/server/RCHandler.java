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
package org.quelea.server;

import com.sun.net.httpserver.HttpExchange;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.powerpoint.Presentation;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.lucene.SongSearchIndex;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.library.LibraryBiblePanel;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.RecordingsHandler;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.main.schedule.ScheduleThemeNode;
import org.quelea.windows.main.toolbars.MainToolbar;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * Handles the RemoteControlServer commands.
 *
 * @author Ben Goodwin
 */
public class RCHandler {

    private static final ArrayList<String> devices = new ArrayList<>();
    private static final Logger LOGGER = LoggerUtils.getLogger();

    public static void logo() {
        Platform.runLater(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel()::toggleLogo);
    }

    public static void black() {
        Platform.runLater(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel()::toggleBlack);
    }

    public static void clear() {
        Platform.runLater(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel()::toggleClear);

    }

    public static void next() {
        Platform.runLater(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel()::advance);
    }

    public static void prev() {
        Platform.runLater(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel()::previous);

    }

    public static void nextItem() {
        Platform.runLater(() -> {
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
            current++;
            p.getSchedulePanel().getScheduleList().getSelectionModel().clearSelection();
            if (current < p.getSchedulePanel().getScheduleList().getItems().size()) {
                p.getSchedulePanel().getScheduleList().getSelectionModel().select(current);
            } else {
                p.getSchedulePanel().getScheduleList().getSelectionModel().select(p.getSchedulePanel().getScheduleList().getItems().size() - 1);
            }
            p.getPreviewPanel().goLive();
        });
    }

    public static void prevItem() {
        Platform.runLater(() -> {
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
            current--;
            p.getSchedulePanel().getScheduleList().getSelectionModel().clearSelection();
            if (current > 0) {
                p.getSchedulePanel().getScheduleList().getSelectionModel().select(current);
            } else {
                p.getSchedulePanel().getScheduleList().getSelectionModel().select(0);
            }
            p.getPreviewPanel().goLive();
        });
    }

    public static void gotoItem(String index) {
        Platform.runLater(() -> {
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            p.getSchedulePanel().getScheduleList().getSelectionModel().clearSelection();
            int item = Integer.parseInt(index.split("gotoitem")[1]);
            if (item < p.getSchedulePanel().getScheduleList().getItems().size()) {
                p.getSchedulePanel().getScheduleList().getSelectionModel().select(item);
            } else {
                p.getSchedulePanel().getScheduleList().getSelectionModel().select(p.getSchedulePanel().getScheduleList().getItems().size() - 1);
            }
            p.getPreviewPanel().goLive();
        });
    }

    public static void moveUp(String index) {
        int item = Integer.parseInt(index.split("moveup/")[1]);
        Platform.runLater(() -> {
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            p.getSchedulePanel().getScheduleList().getSelectionModel().select(item);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().moveCurrentItem(ScheduleList.Direction.UP);
        });
    }

    public static void moveDown(String index) {
        int item = Integer.parseInt(index.split("movedown/")[1]);
        Platform.runLater(() -> {
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            p.getSchedulePanel().getScheduleList().getSelectionModel().select(item);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().moveCurrentItem(ScheduleList.Direction.DOWN);
        });
    }

    public static int currentLyricSection() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getCurrentIndex();
    }

    public static void setLyrics(final String index) {
        Platform.runLater(() -> {
            int num = Integer.parseInt(index.split("section")[1]);
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (lp.getDisplayable() instanceof PresentationDisplayable) {
                lp.getPresentationPanel().getPresentationPreview().select(num + 1);
            } else {
                lp.getLyricsPanel().select(num);
            }
        });
    }

    public static boolean authenticate(final String password) {
        return password.equals(QueleaProperties.get().getRemoteControlPassword());
    }

    public static void addDevice(String ip) {
        devices.add(ip);
    }

    public static boolean isLoggedOn(String ip) {
        boolean found = false;
        for (String s : devices) {
            if (s.equals(ip)) {
                found = true;
            }
        }
        return found;
    }

    public static void logout(String ip) {
        devices.remove(ip);
    }

    public static void logAllOut() {
        devices.clear();
    }

    public static boolean getLogo() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLogoed();
    }

    public static boolean getBlack() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getBlacked();
    }

    public static boolean getClear() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getCleared();
    }

    public static String videoStatus() {
        LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
        if (lp.getDisplayable() instanceof TimerDisplayable) {
            if (lp.getTimerPanel().status()) {
                return LabelGrabber.INSTANCE.getLabel("pause");
            } else {
                return LabelGrabber.INSTANCE.getLabel("play");
            }
        }
        if (VLCWindow.INSTANCE.isPlaying()) {
            return LabelGrabber.INSTANCE.getLabel("pause");
        } else {
            return LabelGrabber.INSTANCE.getLabel("play");
        }
    }

    public static void play() {
        if (VLCWindow.INSTANCE.isPlaying()) {
            VLCWindow.INSTANCE.pause();
        } else {
            VLCWindow.INSTANCE.play();
        }
        LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
        if (lp.getDisplayable() instanceof TimerDisplayable) {
            lp.getTimerPanel().togglePause();
        }
    }

    static void record() {
        MainToolbar toolbar = QueleaApp.get().getMainWindow().getMainToolbar();
        RecordingsHandler recHandler = toolbar.getRecordButtonHandler().getRecordingsHandler();
        if (toolbar.getRecordButtonHandler() != null && recHandler != null) {
            if (recHandler.getIsRecording()) {
                Utils.fxRunAndWait(() -> {
                    toolbar.stopRecording();
                });
            } else {
                Utils.fxRunAndWait(() -> {
                    toolbar.startRecording();
                });
            }
        } else {
            Utils.fxRunAndWait(() -> {
                toolbar.startRecording();
            });
        }
    }

    public static String schedule() {
        Displayable preview = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
        Displayable live = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable();

        String display = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n</head>\n";
        for (int i = 0; i < QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getItems().size(); i++) {
            Displayable d = ((Displayable) QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getItems().get(i));
            if (d.equals(preview)) {
                display += "<i>";
            }
            if (d.equals(live)) {
                display += "<b>";
            }
            display += d.getPreviewText().replace("\n", " - ");
            if (d.equals(live)) {
                display += "</b>";
            }
            if (d.equals(preview)) {
                display += "</i>";
            }
            display += "<br/>";
        }
        display += "</html>";
        return display;
    }

    public static String databaseSearch(HttpExchange he) throws UnsupportedEncodingException, IOException {
        String searchString;
        if (he.getRequestURI().toString().contains("/search/")) {
            String uri = URLDecoder.decode(he.getRequestURI().toString(), "UTF-8");
            searchString = uri.split("/search/", 2)[1];
            TreeSet<SongDisplayable> songs = new TreeSet<>();
            if (searchString == null || searchString.trim().isEmpty() || Pattern.compile("[^\\w ]", Pattern.UNICODE_CHARACTER_CLASS).matcher(searchString).replaceAll("").isEmpty()) {
                return LabelGrabber.INSTANCE.getLabel("invalid.search");
            } else {
                SongDisplayable[] titleSongs = SongManager.get().getIndex().filter(searchString, SongSearchIndex.FilterType.TITLE);
                for (SongDisplayable song : titleSongs) {
                    song.setLastSearch(searchString);
                    songs.add(song);
                }

                SongDisplayable[] lyricSongs = SongManager.get().getIndex().filter(searchString, SongSearchIndex.FilterType.BODY);
                for (SongDisplayable song : lyricSongs) {
                    song.setLastSearch(null);
                    songs.add(song);
                }

                SongDisplayable[] authorSongs = SongManager.get().getIndex().filter(searchString, SongSearchIndex.FilterType.AUTHOR);
                songs.addAll(Arrays.asList(authorSongs));
            }

            StringBuilder response = new StringBuilder();
            response.append("<!DOCTYPE html><html>");
            response.append("<head><meta charset=\"UTF-8\"></head>");
            for (SongDisplayable sd : songs) {
                response.append("<a href=\"/song/").append(sd.getID()).append("\">");
                response.append(sd.getTitle()).append(" - ").append(sd.getAuthor());
                response.append("</a>").append("<br/>");
            }
            response.append("</html>");
            return response.toString();
        } else {
            return "";
        }
    }

    public static String addSongToSchedule(HttpExchange he) {
        String songIDString;
        long songID;
        if (he.getRequestURI().toString().contains("/add/")) {
            songIDString = he.getRequestURI().toString().split("/add/", 2)[1];
            songID = Long.parseLong(songIDString);
            SongDisplayable sd = SongManager.get().getIndex().getByID(songID);

            Utils.fxRunAndWait(() -> {
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(sd);
            });

            return LabelGrabber.INSTANCE.getLabel("rcs.add.success");
        }
        return LabelGrabber.INSTANCE.getLabel("rcs.add.failed");
    }

    public static String removeItemFromSchedule(HttpExchange he) {
        String songIDString;
        int songID;
        if (he.getRequestURI().toString().contains("/remove/")) {
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            songIDString = he.getRequestURI().toString().split("/remove/", 2)[1];
            songID = Integer.parseInt(songIDString);

            Utils.fxRunAndWait(() -> {
                p.getSchedulePanel().getScheduleList().getSelectionModel().select(songID);
                QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().removeCurrentItem();
            });

            return LabelGrabber.INSTANCE.getLabel("Item removal was succsessful");
        }
        return LabelGrabber.INSTANCE.getLabel("Item removal failed");
    }

    public static String songDisplay(HttpExchange he) {
        String songIDString;
        long songID;
        if (he.getRequestURI().toString().contains("/song/")) {
            songIDString = he.getRequestURI().toString().split("/song/")[1];
            songID = Long.parseLong(songIDString);
            if (SongManager.get().getIndex().getByID(songID) != null) {
                SongDisplayable sd = SongManager.get().getIndex().getByID(songID);
                StringBuilder response = new StringBuilder();
                response.append("<!DOCTYPE html><html>");
                response.append("<head><meta charset=\"UTF-8\"></head>");
                response.append(sd.getTitle()).append("<br/>");
                response.append(sd.getAuthor()).append("<br/><br/>");
                response.append(sd.getLyrics(false, false).replaceAll("\n", "<br/>")).append("<br/><br/>");
                response.append("<a href=\"/add/").append(songID).append("\">").append(LabelGrabber.INSTANCE.getLabel("rcs.add.song")).append("</a>");
                response.append("</html>");
                return response.toString();
            }
        }
        return "";
    }

    public static String addBiblePassage(HttpExchange he) throws UnsupportedEncodingException {
        String searchString;
        if (he.getRequestURI().toString().contains("/addbible/")) {
            String uri = URLDecoder.decode(he.getRequestURI().toString(), "UTF-8");
            searchString = uri.split("/addbible/")[1];
            String translation = searchString.split("/")[0];
            String book = searchString.split("/")[1];
            String cv = searchString.split("/")[2];
            String error = LabelGrabber.INSTANCE.getLabel("rcs.add.bible.error").replace("$1", book + " " + cv);
            final LibraryBiblePanel lbp = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getBiblePanel();
            boolean success = false;
            for (int i = 0; i < lbp.getBibleSelector().getItems().size(); i++) {
                if (lbp.getBibleSelector().getItems().get(i).getBibleName().equalsIgnoreCase(translation)) {
                    final int j = i;
                    Utils.fxRunAndWait(() -> {
                        lbp.getBibleSelector().selectionModelProperty().get().clearAndSelect(j);
                    });
                    success = true;
                }
            }
            if (!success) {
                return error;
            }
            success = false;
            for (int i = 0; i < lbp.getBookSelector().getItems().size(); i++) {
                if (lbp.getBookSelector().getItems().get(i).getBookName().equalsIgnoreCase(book)) {
                    final int j = i;
                    Utils.fxRunAndWait(() -> {
                        lbp.getBookSelector().selectionModelProperty().get().clearAndSelect(j);
                    });
                    success = true;
                }
            }
            if (!success) {
                return error;
            }

            int before = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getItems().size();
            Utils.fxRunAndWait(() -> {
                lbp.getPassageSelector().setText(cv);
                lbp.getAddToSchedule().fire();
            });
            int after = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getItems().size();

            if (after > before) {
                return LabelGrabber.INSTANCE.getLabel("rcs.add.success");
            } else {
                return error;
            }
        } else {
            return "";
        }
    }

    public static String getThemes(HttpExchange he) {
        StringBuilder ret = new StringBuilder();
        for (ThemeDTO t : QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getThemeNode().getThemes()) {
            ret.append(t.getThemeName()).append("\n");
        }
        ret.append(LabelGrabber.INSTANCE.getLabel("default.theme.text")).append("\n");
        return ret.toString();
    }

    public static String setTheme(HttpExchange he) throws UnsupportedEncodingException {
        String themeName;
        if (he.getRequestURI().toString().contains("/settheme/")) {
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            String uri = URLDecoder.decode(he.getRequestURI().toString(), "UTF-8");
            themeName = uri.split("/settheme/", 2)[1];
            ScheduleThemeNode stn = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getThemeNode();

            Utils.fxRunAndWait(() -> {
                int i = 0;
                for (ThemeDTO t : stn.getThemes()) {
                    i++;
                    if (t.getThemeName().equals(themeName)) {
                        stn.selectTheme(t);
                        break;
                    } else if (i == stn.getThemes().size()) {
                        stn.selectTheme(ThemeDTO.DEFAULT_THEME);
                    }
                }
            });

            return "";
        }
        return "";
    }

    public static byte[] getPresentationSlides(HttpExchange he) {
        String targetPath = he.getRequestURI().getPath().replace("/slides", "");
        Displayable d = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable();
        if (d instanceof PresentationDisplayable) {
            Presentation pres = ((PresentationDisplayable) d).getPresentation();
            if (targetPath.contains("/")) {
                try {
                    int slide = Integer.parseInt(targetPath.replace("/slide", "").replace(".png", ""));
                    BufferedImage image = SwingFXUtils.fromFXImage(pres.getSlide(slide - 1).getImage(), null);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", output);
                    return output.toByteArray();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Error getting PowerPoint slides", ex);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("\n<html>");
                int numberOfFiles = pres.getSlides().length;
                for (int i = 0; i < numberOfFiles; i++) {
                    if (currentLyricSection() == i) {
                        sb.append("<div class=\"inner current\">");
                    }
                    sb.append("<p class=\"empty\" onclick=\"section(").append(i).append(");\">");
                    sb.append("<a href='").append("/").append("slides/slide").append(i).append("'>").append("</a>");
                    sb.append("</p>");
                }
                sb.append("\n</html>");
                return sb.toString().getBytes();
            }
        }
        return "".getBytes();
    }

    public static String listBibleTranslations(HttpExchange he) {
        StringBuilder ret = new StringBuilder();
        final LibraryBiblePanel lbp = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getBiblePanel();
        for (Bible b : lbp.getBibleSelector().getItems()) {
            if (b.getBibleName().equals(QueleaProperties.get().getDefaultBible())) {
                ret.append("*");
            }
            ret.append(b.getBibleName()).append("\n");
        }
        return ret.toString();
    }

    public static String listBibleBooks(HttpExchange he) throws UnsupportedEncodingException {
        String searchString;
        if (he.getRequestURI().toString().contains("/books/")) {
            String uri = URLDecoder.decode(he.getRequestURI().toString(), "UTF-8");
            searchString = uri.split("/books/")[1];
            final LibraryBiblePanel lbp = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getBiblePanel();
            boolean success = false;
            for (int i = 0; i < lbp.getBibleSelector().getItems().size(); i++) {
                if (lbp.getBibleSelector().getItems().get(i).getBibleName().equalsIgnoreCase(searchString)) {
                    final int j = i;
                    Utils.fxRunAndWait(() -> {
                        lbp.getBibleSelector().selectionModelProperty().get().clearAndSelect(j);
                    });
                    success = true;
                }
            }
            if (!success) {
                return "";
            }
            StringBuilder ret = new StringBuilder();
            for (BibleBook bb : lbp.getBookSelector().getItems()) {
                ret.append(bb.getBookName()).append("\n");
            }
            return ret.toString();
        }
        return "";
    }

    public static String listSongTranslations(HttpExchange he) {
        StringBuilder ret = new StringBuilder();
        final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
        int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
        SongDisplayable d = ((SongDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getItems().get(current));
        for (String b : d.getTranslations().keySet()) {
            ret.append(b).append("\n");
        }
        if (d.getTranslations().size() < 1) {
            ret.append("None");
        }
        return ret.toString();
    }

    public static String getSongTranslation(HttpExchange he) throws UnsupportedEncodingException {
        String language;
        StringBuilder ret = new StringBuilder();
        if (he.getRequestURI().toString().contains("/gettranslation/")) {
            String uri = URLDecoder.decode(he.getRequestURI().toString(), "UTF-8");
            language = uri.split("/gettranslation/", 2)[1];
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
            SongDisplayable d = ((SongDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getItems().get(current));
            for (String b : d.getTranslations().keySet()) {
                if (b.equals(language)) {
                    ret.append(d.getTranslations().get(language));
                }
            }
        }
        return ret.toString();
    }
}
