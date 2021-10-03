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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.quelea.planningcenter.PlanningCenterClient;
import org.quelea.planningcenter.model.services.Attachment;
import org.quelea.planningcenter.model.services.AttachmentActivity;
import org.quelea.planningcenter.model.services.Media;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 *
 * @author Bronson
 */
public class PlanningCenterOnlineParser {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private PlanningCenterClient planningCenterClient;
    private final HttpClient httpClient;

    public PlanningCenterOnlineParser() {
        httpClient = HttpClients.createDefault();
    }

    public void setClient(PlanningCenterClient client) {
        this.planningCenterClient = client;
    }

    public PlanningCenterClient getPlanningCenterClient() {
        return planningCenterClient;
    }

    // Download file from url to fileName, putting the file into the download directory
    // if the file exists it wont be downloaded
    // will give the file a temporary name until the download is fully complete at
    // which point it will rename to indicate the file is downloaded properly
    public String downloadFile(Media media, Attachment attachment, String fileName, ProgressBar progressBar, LocalDateTime lastUpdated) {
        try {
            QueleaProperties props = QueleaProperties.get();
            String fullFileName = FilenameUtils.concat(props.getDownloadPath(), fileName);
            File file = new File(fullFileName);
            if (file.exists()) {
                long lastModified = file.lastModified();
                if (lastUpdated == null || lastUpdated.atZone(ZoneOffset.UTC).toInstant().toEpochMilli() <= lastModified) {
                    LOGGER.log(Level.INFO, "{0} exists, using existing file", file.getAbsolutePath());
                    return file.getAbsolutePath();
                }

                // file is going to get overridden as it failed the timestamp check
                if (!file.delete()) {
                    // deletion of exiting file failed! just use the existing file then
                    LOGGER.log(Level.INFO, "Couldn''t delete existing file: {0}", file.getAbsolutePath());
                    return file.getAbsolutePath();
                }
            }

            String partFullFileName = fullFileName + ".part";
            File partFile = new File(partFullFileName);

            AttachmentActivity attachmentActivity = planningCenterClient.services().media(media.getId()).attachment(attachment.getId()).api().open().execute().body().get();
            HttpResponse response = httpClient.execute(new HttpGet(attachmentActivity.getAttachmentUrl()));
            HttpEntity entity = response.getEntity();
            if (entity != null) {

                long contentLength = entity.getContentLength();

                InputStream is = entity.getContent();
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(partFile))) {
                    Long totalBytesRead = 0L;

                    byte[] buffer = new byte[1024 * 1024];
                    int count;
                    while ((count = is.read(buffer)) != -1) {
                        bos.write(buffer, 0, count);

                        totalBytesRead += count;
                        progressBar.setProgress((double) totalBytesRead / (double) contentLength);
                    }
                }

                EntityUtils.consume(entity);
            }

            boolean success = partFile.renameTo(file);
            if (success && lastUpdated != null) {
                file.setLastModified(lastUpdated.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()); // set file timestamp to same as on PCO
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error", e);
        }

        return "";
    }
}
