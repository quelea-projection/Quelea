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
package org.quelea.services.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Responsible for uploading broken bibles for examination.
 *
 * @author Michael
 */
public class BibleUploader {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    public static BibleUploader INSTANCE = new BibleUploader();

    public void upload(File f) {
        new Thread() {
            public void run() {
                MultipartEntity entity = new MultipartEntity();
                entity.addPart("file", new FileBody(f));

                HttpPost request = new HttpPost("http://quelea.org/bibleupload/upload.php");
                request.setEntity(entity);

                HttpClient client = new DefaultHttpClient();
                try {
                    client.execute(request);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Failed uploading bible", ex);
                }
            }
        }.start();
    }

}
