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
import java.net.InetSocketAddress;
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
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The remote control server, responsible for handling the mobile HTTP calls and
 * changing the correct content.
 * <p>
 * @author Ben
 */
public class RemoteControlServer {

    private static final boolean USE_CACHE = true;
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final HttpServer server;
    private boolean running;
    private String pageContent;
    private final Map<String, byte[]> fileCache;

    /**
     * Create a new mobile lyrics server on a specified port. The port must not
     * be in use.
     * <p>
     * @param port the port to use
     * @throws IOException if something goes wrong.
     */
    public RemoteControlServer(int port) throws IOException {
        fileCache = new HashMap<>();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/logo", new LogoToggleHandler());
        server.createContext("/black", new BlackToggleHandler());
        server.createContext("/clear", new ClearToggleHandler());
        server.createContext("/next", new NextSlideHandler());
        server.createContext("/prev", new PreviousSlideHandler());
        server.createContext("/nextitem", new NextItemHandler());
        server.createContext("/previtem", new PreviousItemHandler());
        server.createContext("/lyrics", new LyricsHandler());
        server.createContext("/s", new SectionHandler());
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

    //Handles 
    private class PasswordHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    //Handles logo display
    private class LogoToggleHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            he.sendResponseHeaders(200, -1);
            RCHandler.logo();
        }
    }

    //Handles black display
    private class BlackToggleHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            he.sendResponseHeaders(200, -1);
            RCHandler.black();
        }
    }

    //Handles clear display
    private class ClearToggleHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            he.sendResponseHeaders(200, -1);
            RCHandler.clear();
        }
    }

    //Handles next slide
    private class NextSlideHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            he.sendResponseHeaders(200, -1);
            RCHandler.next();
        }
    }

    //Handles previous slide
    private class PreviousSlideHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            he.sendResponseHeaders(200, -1);
            RCHandler.prev();
        }
    }

    //Handles next schedule item
    private class NextItemHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            he.sendResponseHeaders(200, -1);
            RCHandler.nextItem();
        }
    }

    //Handles previous schedule item
    private class PreviousItemHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            he.sendResponseHeaders(200, -1);
            RCHandler.prevItem();
        }
    }

    //Handles 
    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (pageContent == null || !USE_CACHE) {
                pageContent = readFile("icons/defaultrcspage.htm");
                pageContent = langStrings(pageContent);
            }
            byte[] bytes = pageContent.getBytes(Charset.forName("UTF-8"));
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }

        //Handles replacing of the language strings
        private String langStrings(String pageContent) {
            pageContent = pageContent.replace("[logo.text]", LabelGrabber.INSTANCE.getLabel("remote.logo.text"));
            pageContent = pageContent.replace("[black.text]", LabelGrabber.INSTANCE.getLabel("remote.black.text"));
            pageContent = pageContent.replace("[clear.text]", LabelGrabber.INSTANCE.getLabel("remote.clear.text"));
            pageContent = pageContent.replace("[next.text]", LabelGrabber.INSTANCE.getLabel("remote.next.text"));
            pageContent = pageContent.replace("[prev.text]", LabelGrabber.INSTANCE.getLabel("remote.prev.text"));
            pageContent = pageContent.replace("[nextitem.text]", LabelGrabber.INSTANCE.getLabel("remote.nextitem.text"));
            pageContent = pageContent.replace("[previtem.text]", LabelGrabber.INSTANCE.getLabel("remote.previtem.text"));
            return pageContent;
        }
    }

    //Handles clicking on a section
    private class SectionHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(200, -1);
            RCHandler.setLyrics(t.getRequestURI().toString());
        }
    }

    //Takes the lyrics (if they're a Song or Bible passage and inserts them into the page
    private class LyricsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = LabelGrabber.INSTANCE.getLabel("remote.empty.lyrics");
            if (QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable() instanceof SongDisplayable
                    || QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable() instanceof BiblePassage) {
                response = lyrics();
            }
            byte[] bytes = response.getBytes("UTF-8");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    /**
     * Get the HTML formatted Lyrics for display. Formatted as a table, with one
     * row and one cell per lyric slide. The current slide has class current. To
     * understand target="empty" see comment in defaultrcspage.htm
     * <p/>
     * @return All lyrics formatted in a HTML table
     */
    public String lyrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>");
        int i = 0;
        for (String lyricBlock : getLyrics()) {
            if (i == RCHandler.currentLyricSection()) {
                sb.append("<tr><td class=\"current\">");
            } else {
                sb.append("<tr><td>");
            }
            sb.append("<a href=\"/s" + i + "\" target=\"empty\">");
            sb.append(lyricBlock);
            sb.append("</a></td></tr>");
            i++;
        }
        sb.append("</table>");
        return sb.toString();
    }

    //Method returns all lyrics as an ArrayList of slides
    private List<String> getLyrics() {
        try {
            if (!checkInitialised()) {
                List<String> tmp = new ArrayList<String>();
                tmp.add("");
                return tmp;
            }
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if (running && lp.isContentShowing() && lp.getDisplayable() instanceof TextDisplayable) {
                ArrayList<String> als = new ArrayList<String>();
                for (TextSection currentSection : lp.getLyricsPanel().getLyricsList().getItems()) {
                    StringBuilder ret = new StringBuilder();
                    for (String line : currentSection.getText(false, false)) {
                        ret.append(Utils.escapeHTML(line)).append("<br/>");
                    }
                    als.add(ret.toString());
                }
                return als;
            } else {
                return null;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error getting lyrics", ex);
            return null;
        }
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
