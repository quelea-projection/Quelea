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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Bronson
 */


public class PlanningCenterOnlineParser {
    
    public PlanningCenterOnlineParser() {
    
    }
    
    public static boolean login(String email, String password) {
        String args = String.format("email=%s&password=%s&submit=login", email, password);
        String url = "https://accounts.planningcenteronline.com/login";
        String result = post(url, args);
        if (result == null) {
            return false;
        }
        
        // check for a bad login page
        if (result.contains("<title>Login - Accounts</title>")) {
            return false;
        }
        
        return true;
    }
    
    private static String post(String urlString, String urlParameters) {
        
        // ensure cookie handler setup
        if (CookieHandler.getDefault() != null) {
            CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
        }
        
        BufferedReader reader = null;
        try {
            byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
            int    postDataLength = postData.length;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            
            conn.setDoOutput( true );
            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setUseCaches( false );
            try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
               wr.write( postData );
            }

            StringBuilder strBuilder = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String str;
                while ((str = in.readLine()) != null) {
                    strBuilder.append(str).append("\n");
                }
                String resultStr = strBuilder.toString();
                //String[] jsonMap = json.split(",");
                /*
                for(String s : jsonMap) {
                    if(s.split(": ")[0].contains("country") && s.split(": ")[1].equals("\"US\"")) {
                        useUK = false;
                        LOGGER.log(Level.INFO, "Using US version of weareworship.com");
                    }
                }*/
                return resultStr;
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
        
        return null;
    }
         
    
}
