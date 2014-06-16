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

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
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
        HttpContext rootcontext = server.createContext("/", new RootHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/tlogo", new LogoToggleHandler());
        server.createContext("/black", new BlackToggleHandler());
        server.createContext("/clear", new ClearToggleHandler());
        server.createContext("/next", new NextSlideHandler());
        server.createContext("/prev", new PreviousSlideHandler());
        server.createContext("/nextitem", new NextItemHandler());
        server.createContext("/previtem", new PreviousItemHandler());
        server.createContext("/lyrics", new LyricsHandler());
        server.createContext("/status", new StatusHandler());
        server.createContext("/s", new SectionHandler());
        rootcontext.getFilters().add(new ParameterFilter());
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
     * <p/>
     * @return true if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return running;
    }

    //Handles logo display
    private class LogoToggleHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.logo();
            } else {
                reload(he);
            }
        }
    }

    //Handles black display
    private class BlackToggleHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.black();
            } else {
                reload(he);
            }
        }
    }

    //Handles clear display
    private class ClearToggleHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.clear();
            } else {
                reload(he);
            }
        }
    }

    //Handles next slide
    private class NextSlideHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.next();
            } else {
                reload(he);
            }
        }
    }

    //Handles previous slide
    private class PreviousSlideHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.prev();
            } else {
                reload(he);
            }
        }
    }

    //Handles next schedule item
    private class NextItemHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.nextItem();
            } else {
                reload(he);
            }
        }
    }

    //Handles previous schedule item
    private class PreviousItemHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.prevItem();
            } else {
                reload(he);
            }
        }
    }
    
    //Handles button status
    private class StatusHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                String status = RCHandler.getLogo() + "," + RCHandler.getBlack() + "," + RCHandler.getClear();
                byte[] bytes = status.getBytes(Charset.forName("UTF-8"));
                he.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = he.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                reload(he);
            }
        }
    }

    //Handles clicking on a section
    private class SectionHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                he.sendResponseHeaders(200, -1);
                RCHandler.setLyrics(he.getRequestURI().toString());
            } else {
                reload(he);
            }
        }
    }

    //Handles logo display
    private class LogoutHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            RCHandler.logout(he.getRemoteAddress().getAddress().toString());
            passwordPage(he);
        }
    }

    //Handles 
    private class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            if (RCHandler.isLoggedOn(he.getRemoteAddress().getAddress().toString())) {
                if (pageContent == null || !USE_CACHE) {
                    pageContent = readFile("icons/defaultrcspage.htm");
                    pageContent = langStrings(pageContent);
                }
                byte[] bytes = pageContent.getBytes(Charset.forName("UTF-8"));
                he.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = he.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                passwordPage(he);
            }
        }
    }

    private void passwordPage(HttpExchange he) throws IOException {
        if (he.getRequestMethod().equals("POST")) {
            Map<String, Object> params = (Map<String, Object>) he.getAttribute("parameters");
            String password = (String) params.get("password");
            if (password != null && RCHandler.authenticate(password)) {
                RCHandler.addDevice(he.getRemoteAddress().getAddress().toString());
                if (pageContent == null || !USE_CACHE) {
                    pageContent = readFile("icons/defaultrcspage.htm");
                    pageContent = langStrings(pageContent);
                }
                byte[] bytes = pageContent.getBytes(Charset.forName("UTF-8"));
                he.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = he.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

        }
        String content = readFile("icons/defaultpasswordpage.htm");
        content = content.replace("[remote.login.text]", LabelGrabber.INSTANCE.getLabel("remote.login.text"));
        content = content.replace("[submit.button.text]", LabelGrabber.INSTANCE.getLabel("remote.submit.text"));
        byte[] bytes = content.getBytes(Charset.forName("UTF-8"));
        he.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = he.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void reload(HttpExchange he) throws IOException {
        byte[] bytes = readFile("icons/reloadpage.htm").getBytes("UTF-8");
        he.sendResponseHeaders(307, bytes.length);
        try (OutputStream os = he.getResponseBody()) {
            os.write(bytes);
        }
    }

    //Takes the lyrics (if they're a Song or Bible passage and inserts them into the page
    private class LyricsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "<i>" + LabelGrabber.INSTANCE.getLabel("remote.empty.lyrics") + "</i>";
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
        sb.append("<div id=\"outer\">");
        int i = 0;
        for (String lyricBlock : getLyrics()) {
            if (i == RCHandler.currentLyricSection()) {
                sb.append("<div class=\"inner current\">");
            } else {
                sb.append("<div class=\"inner\">");
            }
            sb.append("<a href=\"/s" + i + "\" target=\"empty\">");
            sb.append(lyricBlock);
            sb.append("</a></div>");
            i++;
        }
        sb.append("</div>");
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
            if (running && lp.getDisplayable() instanceof TextDisplayable) {
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
                String response = "<i>" + LabelGrabber.INSTANCE.getLabel("remote.empty.lyrics") + "</i>";
                ArrayList<String> als = new ArrayList<String>();
                als.add(response);
                return als;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error getting lyrics", ex);
            return null;
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
        pageContent = pageContent.replace("[logout.text]", LabelGrabber.INSTANCE.getLabel("remote.logout.text"));
        return pageContent;
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

    private class ParameterFilter extends Filter {

        @Override
        public String description() {
            return "Parses the requested URI for parameters";
        }

        @Override
        public void doFilter(HttpExchange exchange, Chain chain)
                throws IOException {
            parseGetParameters(exchange);
            parsePostParameters(exchange);
            chain.doFilter(exchange);
        }

        private void parseGetParameters(HttpExchange exchange)
                throws UnsupportedEncodingException {

            Map<String, Object> parameters = new HashMap<String, Object>();
            URI requestedUri = exchange.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);
            exchange.setAttribute("parameters", parameters);
        }

        private void parsePostParameters(HttpExchange exchange)
                throws IOException {

            if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parameters
                        = (Map<String, Object>) exchange.getAttribute("parameters");
                InputStreamReader isr
                        = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();
                parseQuery(query, parameters);
            }
        }

        @SuppressWarnings("unchecked")
        private void parseQuery(String query, Map<String, Object> parameters)
                throws UnsupportedEncodingException {

            if (query != null) {
                String pairs[] = query.split("[&]");

                for (String pair : pairs) {
                    String param[] = pair.split("[=]");

                    String key = null;
                    String value = null;
                    if (param.length > 0) {
                        key = URLDecoder.decode(param[0],
                                System.getProperty("file.encoding"));
                    }

                    if (param.length > 1) {
                        value = URLDecoder.decode(param[1],
                                System.getProperty("file.encoding"));
                    }

                    if (parameters.containsKey(key)) {
                        Object obj = parameters.get(key);
                        if (obj instanceof List<?>) {
                            List<String> values = (List<String>) obj;
                            values.add(value);
                        } else if (obj instanceof String) {
                            List<String> values = new ArrayList<String>();
                            values.add((String) obj);
                            values.add(value);
                            parameters.put(key, values);
                        }
                    } else {
                        parameters.put(key, value);
                    }
                }
            }
        }
    }
}
