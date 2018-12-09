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
package org.quelea.services.notice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Arvid
 */
class NoticeFileHandler {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Method to save the notice as a file.
     *
     * @param notice the notice to save
     */
    static void saveNotice(Notice notice) {
        String fileName = QueleaProperties.get().getNoticeDir().getAbsolutePath() + "/" + notice.getCreationTime() + ".qnf";
        File f = new File(fileName);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
                bw.write(notice.getXML());
            }
        } catch (IOException ex) {
            LoggerUtils.getLogger().log(Level.WARNING, "Could not save notice to file");
        }
    }

    static boolean deleteNotice(Notice notice) {
        String fileName = QueleaProperties.get().getNoticeDir().getAbsolutePath() + "/" + notice.getCreationTime() + ".qnf";
        File f = new File(fileName);
        return f.delete();
    }

    /**
     * Method to load the notice from a file
     * <p/>
     * @param f the file to load the notice from
     * @return the notice if the operation was successful, null otherwise.
     */
    static Notice noticeFromFile(File f) {
        if (f.isFile()) {
            try {
                StringBuilder contentsBuilder;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
                    contentsBuilder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        contentsBuilder.append(line).append('\n');
                    }
                }
                String contents = contentsBuilder.toString();
                contents = contents.replace(new String(new byte[]{11}), "\n");
                contents = contents.replace(new String(new byte[]{-3}), " ");
                InputStream strInputStream = new ByteArrayInputStream(contents.getBytes("UTF-8"));

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(strInputStream); //Read from our "bodged" stream.
                Node node = doc.getFirstChild();
                Notice notice = Notice.parseXML(node);
                try {
                    notice.setCreationTime(Long.parseLong(f.getName().replaceAll(".qnf", "")));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Error getting notice file name", e);
                }
                return notice;
            } catch (IOException | ParserConfigurationException | SAXException e) {
                LOGGER.log(Level.WARNING, "Error grabbing notice from file", e);
                return null;
            }

        } else {
            LOGGER.log(Level.WARNING, "Can't get notice from folder: {0}", f.getAbsolutePath());
            return null;
        }
    }
}
