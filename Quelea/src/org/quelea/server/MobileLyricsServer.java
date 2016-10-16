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
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.data.bible.BibleBook;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.library.LibraryBiblePanel;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The mobile lyrics server, responsible for handling the mobile HTTP calls and
 * pushing out the correct content.
 * <p>
 * @author Michael
 */
public class MobileLyricsServer {

    private static final boolean USE_CACHE = true;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final HttpServer server;
    private boolean running;
    private String pageContent;
    private final Map<String, byte[]> fileCache;
    private String text = "";

    /**
     * Create a new mobile lyrics server on a specified port. The port must not
     * be in use.
     * <p>
     * @param port the port to use
     * @throws IOException if something goes wrong.
     */
    public MobileLyricsServer(int port) throws IOException {
        fileCache = new HashMap<>();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/lyrics", new LyricsHandler());
        server.createContext("/chords", new ChordsHandler());
        server.createContext("/title", new TitleHandler());
        server.createContext("/songtranslations", new SongTranslationsHandler());
        server.createContext("/gettranslation", new SongTranslationsHandler());
        server.createContext("/livetext", new LiveTextHandler());
        server.createContext("/jscolor.js", new FileHandler("icons/jscolor.js"));
        server.createContext("/arrow.gif", new FileHandler("icons/arrow.gif"));
        server.createContext("/gear.png", new FileHandler("icons/gear.png"));
        server.createContext("/translate.png", new FileHandler("icons/translate_on.png"));
        server.createContext("/cross.gif", new FileHandler("icons/cross.gif"));
        server.createContext("/hs.png", new FileHandler("icons/hs.png"));
        server.createContext("/hv.png", new FileHandler("icons/hv.png"));
        server.setExecutor(null);
    }

    /**
     * Start the server.
     */
    public void start() {
        if (server != null) {
            server.start();
            running = true;
        }
    }

    /**
     * Stop the server. If the server is stopped, it cannot be restarted - a new
     * server must be created.
     */
    public void stop() {
        if (server != null) {
            running = false;
            server.stop(0);
        }
    }

    /**
     * Determine if the server is running.
     * <p>
     * @return true if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return running;
    }

    private String sortLabels(String content) {
        content = content.replace("[loading.text]", LabelGrabber.INSTANCE.getLabel("loading.text"));
        content = content.replace("[font.colour.label]", LabelGrabber.INSTANCE.getLabel("font.colour.label"));
        content = content.replace("[background.colour.label]", LabelGrabber.INSTANCE.getLabel("background.colour.label"));
        content = content.replace("[change.graphics.label]", LabelGrabber.INSTANCE.getLabel("change.graphics.label"));
        content = content.replace("[show.chords.label]", LabelGrabber.INSTANCE.getLabel("stage.show.chords"));
        content = content.replace("[default.translation]", LabelGrabber.INSTANCE.getLabel("default.translation.label"));
        content = content.replace("[select.language]", LabelGrabber.INSTANCE.getLabel("translation.choice.title"));
        return content;
    }

    private class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            if (pageContent == null || !USE_CACHE) {
                pageContent = readFile("server/defaultpage.htm");
                pageContent = sortLabels(pageContent);
            }
            byte[] bytes = pageContent.getBytes(Charset.forName("UTF-8"));
            t.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }

    }

    private class FileHandler implements HttpHandler {

        private String file;

        public FileHandler(String file) {
            this.file = file;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            byte[] ret = fileCache.get(file);
            if (ret == null) {
                ret = Files.readAllBytes(Paths.get(file));
                if (USE_CACHE) {
                    fileCache.put(file, ret);
                }
            }
            t.sendResponseHeaders(200, ret.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(ret);
            }
        }

    }

    private class LyricsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response;
            if (t.getRequestURI().toString().contains("all")) {
                response = allLyrics();
            } else {
                response = getLyrics(false);
            }
            byte[] bytes = response.getBytes("UTF-8");
            t.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class ChordsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = getLyrics(true);
            byte[] bytes = response.getBytes("UTF-8");
            t.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class LiveTextHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = getText();
            byte[] bytes = response.getBytes("UTF-8");
            t.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class TitleHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = getTitle();
            byte[] bytes = response.getBytes("UTF-8");
            t.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class SongTranslationsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = "";
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (running && lp.getDisplayable() instanceof TextDisplayable) {
                if (he.getRequestURI().toString().contains("/songtranslations")) {
                    response = listSongTranslations(he);
                } else {
                    response = getSongTranslation(he);
                }
                if (getLyrics(false).equals("")) {
                    response = "";
                }
            }
            he.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            he.sendResponseHeaders(200, response.getBytes(Charset.forName("UTF-8")).length);
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes(Charset.forName("UTF-8")));
            os.close();
        }
    }

    private String getLyrics(boolean chords) {
        try {
            if (!checkInitialised()) {
                return "";
            }
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (running && lp.isContentShowing() && lp.getDisplayable() instanceof TextDisplayable) {
                TextSection currentSection = lp.getLyricsPanel().getLyricsList().getSelectionModel().getSelectedItem();
                StringBuilder ret = new StringBuilder();
                for (String line : currentSection.getText(chords, false)) {
                    if (lp.getDisplayable() instanceof BiblePassage) {
                        ret.append("<span class=\"bible\">").append(line);
                    } else if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.CHORDS) {
                        ret.append("<span class=\"chord\">").append(line.replace(" ", "&#160;"));
                    } else if (new LineTypeChecker(line).getLineType() == LineTypeChecker.Type.TITLE) {
                        ret.append("<span class=\"title\">").append(line);
                    } else {
                        ret.append("<span class=\"lyric\">").append(line);
                    }
                    ret.append("</span>").append("<br/>");
                }
//                if(chords) {
//                    return ret.toString().replace(" ", "&#160;");
//                }
                return ret.toString();
            } else {
                return "";
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error getting lyrics", ex);
            return "";
        }
    }

    public String allLyrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"outer\">");
        int i = 0;
        for (String lyricBlock : getAllLyrics()) {
            if (i == RCHandler.currentLyricSection()) {
                sb.append("<div class=\"inner current\">");
            } else {
                sb.append("<div class=\"inner\">");
            }
            sb.append(lyricBlock);
            sb.append("</div>");
            i++;
        }
        sb.append("</div>");
        return sb.toString();
    }

    //Method returns all lyrics as an ArrayList of slides
    private List<String> getAllLyrics() {
        try {
            if (!checkInitialised()) {
                List<String> tmp = new ArrayList<>();
                tmp.add("");
                return tmp;
            }
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (running && lp.getDisplayable() instanceof TextDisplayable) {
                ArrayList<String> als = new ArrayList<>();
                lp.getLyricsPanel().getLyricsList().getItems().stream().map((currentSection) -> {
                    StringBuilder ret = new StringBuilder();
                    for (String line : currentSection.getText(false, false)) {
                        ret.append("<span class=\"lyric\">").append(line).append("</span>").append("<br/>");
                    }
                    return ret;
                }).forEach((ret) -> {
                    als.add(ret.toString());
                });
                return als;
            } else {
                String response = "<i>" + LabelGrabber.INSTANCE.getLabel("remote.empty.lyrics") + "</i>";
                ArrayList<String> als = new ArrayList<>();
                als.add(response);
                return als;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error getting lyrics", ex);
            return null;
        }
    }

    private String getTitle() {
        try {
            if (!checkInitialised()) {
                return "";
            }
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (running && lp.isContentShowing() && lp.getDisplayable() instanceof TextDisplayable) {
                String response = lp.getDisplayable().getPreviewText();
                if (lp.getDisplayable() instanceof BiblePassage) {
                    final LibraryBiblePanel lbp = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getBiblePanel();
                    int chapterPos = 0;

                    for (int i = 1; i < response.length(); i++) {
                        char c = response.charAt(i);
                        if (Character.isDigit(c)) {
                            chapterPos = i - 1;
                            break;
                        }
                    }
                    String bible = response.substring(response.indexOf("\n") + 1);
                    String book = response.substring(0, chapterPos);
                    int bookNumber = 0;
                    int bibleNumber = 0;

                    for (int i = 0; i < lbp.getBibleSelector().getItems().size(); i++) {
                        if (lbp.getBibleSelector().getItems().get(i).toString().toLowerCase().contains(bible.toLowerCase())) {
                            bibleNumber = i;
                        }
                    }

                    BibleBook[] books = lbp.getBibleSelector().getItems().get(bibleNumber).getBooks();
                    for (int i = 0; i < books.length; i++) {
                        if (books[i].getBookName().equalsIgnoreCase(book)) {
                            bookNumber = i + 1;
                        }
                    }

                    response = bookNumber + "<br/>" + response;
                }
                return response;
            } else {
                return "";
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error getting title", ex);
            return "";
        }
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
        StringBuilder lyrics = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        if (he.getRequestURI().toString().contains("/gettranslation/")) {
            String uri = URLDecoder.decode(he.getRequestURI().toString(), "UTF-8");
            language = uri.split("/gettranslation/", 2)[1];
            final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
            int currentSong = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
            SongDisplayable d = ((SongDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getItems().get(currentSong));
            for (String b : d.getTranslations().keySet()) {
                if (b.equals(language)) {
                    lyrics.append(d.getTranslations().get(language));
                }
            }
            String[] translation = lyrics.toString().split("\n\n");

            int i = 0;
            for (String currentSlide : translation) {
                if (i == RCHandler.currentLyricSection()) {
                    sb.append("<div class=\"inner current\">");
                    sb.append(currentSlide);
                    sb.append("</div>");
                }
                i++;
            }
        }
        return sb.toString().replaceAll("\n", "<br/>");
    }

    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    /**
     * A bunch of checks to check whether the live panel that we grab the lyrics
     * from has fully initialised. Rather hacky but works for now at least.
     * <p>
     * @return true if we're initialised properly and ok to continue, false if
     * not.
     */
    private boolean checkInitialised() {
        if (QueleaApp.get() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow().getMainPanel() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getLyricsList() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getLyricsList().getSelectionModel() == null) {
            return false;
        }
        if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getLyricsList().getSelectionModel().getSelectedItem() == null) {
            return false;
        }
        return true;
    }

    /**
     * Read a file and return it as a string.
     * <p>
     * @param path the path to read the file from.
     * @return a string with the file contents.
     * @throws IOException if something goes wrong.
     */
    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return Charset.forName("UTF-8").decode(ByteBuffer.wrap(encoded)).toString();
    }

}
