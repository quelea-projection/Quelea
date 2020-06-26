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
package org.quelea.services.importexport;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.Stage;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 *
 * @author Fabian Mathews
 */
public class ElevantoLoginDialog extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean isLoggedIn = false;
    private final ElevantoImportDialog importDialog;
    private HttpServer server;
        
    class ElevantoHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (isLoggedIn) {
                return;
            }
           
            URI uri = t.getRequestURI();
            String path = uri.getPath();
            
            // this work around send a javascript which calls a POST with the URL arguments so we can obtain them as 
            // HttpServer isnt passing them too us!
            if (path.equals("/oauth")){
                String msg = LabelGrabber.INSTANCE.getLabel("elevanto.loginsuccess.message");
                String response = "<html>\n" +
                    "<script>\n" +
                    "\n" +
                    "   function closeWindow() {\n" +
                    "        window.open('','_parent','');\n" +
                    "        window.close();\n" +
                    "   }\n" + 
                    "\n" +
                    "    function script() {\n" +
                    "        var str = window.location.href;\n" +
                    "        \n" +
                    "        var xhttp = new XMLHttpRequest();\n" +
                    "        xhttp.open(\"POST\", \"oauth_post\", true);\n" +
                    "        xhttp.setRequestHeader(\"Content-type\", \"application/x-www-form-urlencoded\");\n" +
                    "        xhttp.send(str);\n" +
                    "        console.log(str);\n" +
                    "        closeWindow();\n" +
                    "    }\n" +
                    "</script>\n" +
                    "\n" +
                    "<body onload=\"script();\">\n" +
                    msg + "\n" + 
                    "</body>\n" +
                    "</html>";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            // source: https://stackoverflow.com/questions/3409348/read-post-request-values-httphandler
            Headers reqHeaders = t.getRequestHeaders();
            String contentType = reqHeaders.getFirst("Content-Type");
            String encoding = "ISO-8859-1";
            
            // read the query string from the request body
            String qry;
            InputStream in = t.getRequestBody();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte buf[] = new byte[4096];
                for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                    out.write(buf, 0, n);
                }
                qry = new String(out.toByteArray(), encoding);
            } finally {
                in.close();
            }
            
            String urlSplit[] = qry.split("[#]");
            String paramSplit[] = urlSplit[1].split("[&]");
            
            Map <String,String> params = new HashMap<String, String>();
            for (int i = 0; i < paramSplit.length; ++i) {
                String pair[] = paramSplit[i].split("=");
                if (pair.length>1) {
                    params.put(pair[0], pair[1]);
                }else{
                    params.put(pair[0], "");
                }
            }
            
            if (params.containsKey("access_token")) {
                String accessToken = (String)params.get("access_token");
                String refreshToken = (String)params.get("refresh_token");
                int expiresIn = Integer.parseInt(params.get("expires_in"));
                
                importDialog.getParser().setAccessToken(accessToken);
                isLoggedIn = true;
                importDialog.onLogin();
                hide();
            }
            else if (params.containsKey("error")) {
                String error = (String)params.get("error");
                String errorDesc = (String)params.get("error_description");
                
                LOGGER.log(Level.WARNING, "Error logging into Elevanto", errorDesc);
                Dialog.showWarning(LabelGrabber.INSTANCE.getLabel("pco.loginerror.title"), LabelGrabber.INSTANCE.getLabel("elevanto.loginerror.warning") + ":" + errorDesc);
            }

            shutdownServer();            
        }
    }
    
    public ElevantoLoginDialog() {
        importDialog = null;
    }
    
    public ElevantoLoginDialog(ElevantoImportDialog importDlg) {
        importDialog = importDlg;
        initOwner(importDialog);
    }  
    
    private void shutdownServer() {
        server.stop(1);
    }
    
    public void start() {
        if (isLoggedIn) {
            return;
        }

        // host a local server to intercept the elevanto redirect
        // https://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
        try {
            server = HttpServer.create(new InetSocketAddress(7070), 0);
            HttpContext context = server.createContext("/", new ElevantoHttpHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error", e);
        }
        
        // redirect url: http://localhost:7070/oauth
        String url = "https://api.elvanto.com/oauth?type=user_agent&client_id=" + QueleaProperties.get().getElevantoClientId() + "&redirect_uri=http%3A%2F%2Flocalhost%3A7070%2Foauth&scope=ManageServices%2CManageSongs%2CManageCalendar";

        // https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java
        if(Desktop.isDesktopSupported() && !Utils.isLinux()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                LOGGER.log(Level.WARNING, "Error", e);
            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error", e);
            }
        }
    }
}
