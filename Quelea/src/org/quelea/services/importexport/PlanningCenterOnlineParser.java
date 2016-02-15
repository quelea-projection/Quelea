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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
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
import org.quelea.services.utils.QueleaProperties;

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
            entity.consumeContent();
            return text;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private String get(String urlString) {
        try {
            HttpGet httget = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(httget, httpContext);

            HttpEntity entity = response.getEntity();
            String text = EntityUtils.toString(entity);
            entity.consumeContent();
            return text;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
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
            e.printStackTrace();
        }
        
        return null;
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
    
    // Arrangement data
    public JSONObject arrangement(Long arrangementId) {
        return getJson("https://planningcenteronline.com/arrangements/" + arrangementId + ".json");
    }
    
    // Media data
    public JSONObject media(Long mediaId) {
        return getJson("https://services.planningcenteronline.com/medias/" + mediaId + ".json");
    }
    
    // Download file from url to fileName, putting the file into the download directory
    // if the file exists it wont be downloaded
    // will give the file a temporary name until the download is fully complete at
    // which point it will rename to indicate the file is downloaded properly
    public String downloadFile(String url, String fileName) {
        try {
            QueleaProperties props = QueleaProperties.get();
            String fullFileName = FilenameUtils.concat(props.getDownloadPath(), fileName);
            File file = new File(fullFileName);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            
            String partFullFileName = fullFileName + ".part";
            File partFile = new File(partFullFileName);
        
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpget, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                
                int statusCode = response.getStatusLine().getStatusCode();
                long len = entity.getContentLength();

                BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(partFile));
                int inByte;
                while((inByte = bis.read()) != -1) {
                    bos.write(inByte);
                }
                bis.close();
                bos.close();
                
                entity.consumeContent();
            }
            
            boolean success = partFile.renameTo(file);
            return file.getAbsolutePath();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return "";
    }
}
