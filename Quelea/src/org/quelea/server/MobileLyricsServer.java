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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The mobile lyrics server, responsible for handling the mobile HTTP calls and
 * pushing out the correct content.
 * <p>
 * @author Michael
 */
public class MobileLyricsServer {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final HttpServer server;
    private boolean running;
    private String pageContent;

    /**
     * Create a new mobile lyrics server on a specified port. The port must not
     * be in use.
     * <p>
     * @param port the port to use
     * @throws IOException if something goes wrong.
     */
    public MobileLyricsServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/lyrics", new LyricsHandler());
        server.createContext("/chords", new ChordsHandler());
        server.setExecutor(null);
    }

    /**
     * Start the server.
     */
    public void start() {
        if(server != null) {
            server.start();
            running = true;
        }
    }

    /**
     * Stop the server. If the server is stopped, it cannot be restarted - a new
     * server must be created.
     */
    public void stop() {
        if(server != null) {
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
        return content;
    }

    private class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
//            if(pageContent == null) {
                pageContent = readFile("icons/defaultpage.htm");
                pageContent = sortLabels(pageContent);
//            }
            t.sendResponseHeaders(200, pageContent.length());
            try(OutputStream os = t.getResponseBody()) {
                os.write(pageContent.getBytes());
            }
        }

    }

    private class LyricsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = getLyrics(false);
            t.sendResponseHeaders(200, response.length());
            try(OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
        }
    }

    private class ChordsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = getLyrics(true);
            t.sendResponseHeaders(200, response.length());
            try(OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
        }
    }
    
    private String getLyrics(boolean chords) {
        try {
            if(!checkInitialised()) {
                return "";
            }
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
            if(running && lp.isContentShowing() && lp.getDisplayable() instanceof TextDisplayable) {
                TextSection currentSection = lp.getLyricsPanel().getLyricsList().getSelectionModel().getSelectedItem();
                StringBuilder ret = new StringBuilder();
                for(String line : currentSection.getText(chords, false)) {
                    ret.append(Utils.escapeHTML(line)).append("<br/>");
                }
                return ret.toString().replace(" ", "&#160;");
            }
            else {
                return "";
            }
        }
        catch(Exception ex) {
            LOGGER.log(Level.WARNING, "Error getting lyrics", ex);
            return "";
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
        if(QueleaApp.get() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow().getMainPanel() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getLyricsList() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getLyricsList().getSelectionModel() == null) {
            return false;
        }
        if(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getLyricsList().getSelectionModel().getSelectedItem() == null) {
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
