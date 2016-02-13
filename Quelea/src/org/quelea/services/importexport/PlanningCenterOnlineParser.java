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
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Bronson
 */


public class PlanningCenterOnlineParser {
    
    private final DefaultHttpClient httpClient;
    private final BasicCookieStore cookieStore;
    private final HttpContext httpContext;
    
    public PlanningCenterOnlineParser() {
        
        // need to handle cookies
        CookieManager cm = ((CookieManager)CookieHandler.getDefault());
        if (cm == null) {
            CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
        }
        else {
            cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        }
        
        httpClient = new DefaultHttpClient();
        cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
    public boolean login(String email, String password) {
        String args = String.format("email=%s&password=%s&submit=login", email, password);
        String url = "https://accounts.planningcenteronline.com/login";
        String result = post(url, args, email, password);
        if (result == null) {
            return false;
        }
        
        // check for a bad login page
        if (result.contains("<title>Login - Accounts</title>")) {
            return false;
        }
        
        return true;
    }
    
    private String post(String urlString, String urlParameters, String email, String password) {
        try {         
            HttpPost httpost = new HttpPost(urlString);
            List<NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("email", email));
            nvps.add(new BasicNameValuePair("password", password));
            httpost.setEntity(new UrlEncodedFormEntity(nvps));
            
            HttpResponse response = httpClient.execute(httpost, httpContext);
                       
            Header[] cookieList = response.getHeaders("Set-Cookie");
            for (Header cookieHeader : cookieList) {
                String cookieStr = cookieHeader.getValue();
                
                HttpCookie httpCookie = HttpCookie.parse(cookieStr).get(0);
                BasicClientCookie cookie = new BasicClientCookie(httpCookie.getName(), httpCookie.getValue());
                cookie.setPath(httpCookie.getPath());
                cookie.setDomain(httpCookie.getDomain());
                
                Date today = new Date();
                Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));
                cookie.setExpiryDate(tomorrow);
                cookieStore.addCookie(cookie);
            }
            
            HttpEntity entity = response.getEntity();
            String text = EntityUtils.toString(entity);
            /*
            if (response1.getStatusLine().getStatusCode() == 302) {
                Header[] locationList = response1.getHeaders("Location");
                if (locationList.length > 0)
                {
                    String newUrl = locationList[0].getValue();
                    
                    try {
                        HttpGet httget = new HttpGet(newUrl);
                        response1 = httpClient.execute(httget, httpContext);
                        
                        entity = response1.getEntity();
                        text = EntityUtils.toString(entity);
                    } catch (Exception e) {
                        int nothing = 0;
                        ++nothing;
                    }
                }
            }
            */
            
            return text;
        } 
        catch (Exception e) {
        }
        
        return null;
    }
    
    private String get(String urlString) {
        try {
            HttpGet httget = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(httget, httpContext);

            HttpEntity entity = response.getEntity();
            String text = EntityUtils.toString(entity);
            return text;
        } catch (Exception e) {
            return null;
        }
        
        /*
        // debug cookies
        CookieManager cm = ((CookieManager)CookieHandler.getDefault());
        CookieStore cookies = cm.getCookieStore();
        
        
        try {
            StringBuilder content = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            int response = conn.getResponseCode();
            if (response == HttpURLConnection.HTTP_MOVED_PERM || response == HttpURLConnection.HTTP_MOVED_TEMP) {
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
            return null;
        }*/
    }
    
    private JSONObject getJson(String urlString) {
        try {
            String jsonStr = get(urlString);
            
            // fix up arrays JSONParser wont handle
            if (jsonStr.startsWith("[") && jsonStr.endsWith("]"))
            {
                jsonStr = "{\"array\":" + jsonStr + "}";
            }
            
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            return json;
        }
        catch(Exception e) {
            return null;
        }
    }
    
    // Contains organisation data - service types
    public JSONObject organisation() {        
        return getJson("https://services.planningcenteronline.com/organization.json");
    }
    
    // Contains all plans for a certain service type
    public JSONObject serviceTypePlans(Long serviceTypeId) {
        return getJson("https://planningcenteronline.com/service_types/" + serviceTypeId + "/plans.json");
    }
    
    // Plan data
    public JSONObject plan(Long planId) {
        return getJson("https://planningcenteronline.com/plans/" + planId + ".json?include_slides=true");
    }
}
