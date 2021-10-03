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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quelea.services.utils.LoggerUtils;

/**
 *
 * @author Fabian Mathews
 */
public class ElevantoParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final HttpClient httpClient;
    private final HttpContext httpContext;
    
    private String accessToken;

    public ElevantoParser() {
        httpClient = HttpClients.createDefault();
        httpContext = new BasicHttpContext();
    }

    public void setAccessToken(String at) {
        accessToken = at;
    }

    private String post(String urlString, JSONObject content) {
        try {
            HttpPost httpost = new HttpPost(urlString);
            httpost.setHeader("Authorization","Bearer " + accessToken);
          
            StringEntity params = new StringEntity(content.toString());
            httpost.addHeader("content-type", "application/json");
            httpost.setEntity(params);
            LOGGER.log(Level.INFO, "Params: {0}", params);
            
            HttpResponse response = httpClient.execute(httpost, httpContext);

            LOGGER.log(Level.INFO, "Response code {0}", response.getStatusLine().getStatusCode());
            for (Header header : response.getAllHeaders()) {
                LOGGER.log(Level.INFO, "Response header ({0}:{1})", new Object[]{header.getName(), header.getValue()});
            }

            HttpEntity entity = response.getEntity();
            String text = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            return text;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error", e);
        }

        return null;
    }
    
    private JSONObject postJson(String urlString, JSONObject content) {
        try {
            String jsonStr = post(urlString, content);

            // fix up arrays JSONParser wont handle
            if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                jsonStr = "{\"array\":" + jsonStr + "}";
            }

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            
            // check for error, just log them
            try {
                String status = (String)json.get("status");
                if (status.equals("fail")) {                    
                    String errorMsg = (String)((JSONObject)json.get("error")).get("message");
                    LOGGER.log(Level.WARNING, "Error", errorMsg);
                }
            }
            catch (Exception e) {
            }
            
            return json;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error", e);
        }

        return null;
    }

    // Contains organisation data - service types
    @SuppressWarnings("unchecked")
    public JSONObject getServices() {
        JSONObject json = new JSONObject();
        json.put("page", "1");
        json.put("page_size", "100");
        json.put("status", "published");
        
        // set suitable start and end date
        Date date = Calendar.getInstance().getTime();        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = df.format(date);  
  
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 14);
        date = c.getTime();
        String endDate = df.format(date);  

        json.put("start", startDate);
        json.put("end", endDate);
                
        json.put("fields", Arrays.asList("series_name", "service_times", "plans", "volunteers", "songs", "files", "notes"));
        return postJson("https://api.elvanto.com/v1/services/getAll.json", json);
    }

    // Arrangement data
    @SuppressWarnings("unchecked")
    public JSONObject arrangement(String arrangementId) {
        JSONObject json = new JSONObject();
        json.put("id", arrangementId);
        return postJson("https://api.elvanto.com/v1/songs/arrangements/getInfo.json", json);
    }
}
