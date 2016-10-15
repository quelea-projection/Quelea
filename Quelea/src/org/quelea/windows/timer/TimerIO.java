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
package org.quelea.windows.timer;

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
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Input and output a timer to a file
 * <p/>
 * @author Ben
 */
public class TimerIO {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Method to save the countdown timer as a file
     * <p/>
     * @param t the timer displayable to save
     * @param f the file to save the timer to
     */
    public static void timerToFile(TimerDisplayable t, File f) throws IOException {
        if (f.exists()) {

        } else {
            f.createNewFile();
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            bw.write(t.getXML());
        }
    }

    /**
     * Method to load the countdown timer from a file
     * <p/>
     * @param f the file to load the timer from
     * @return the timer if the operation was successful, null otherwise.
     */
    public static TimerDisplayable timerFromFile(File f) {
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
                return TimerDisplayable.parseXML(node);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                LOGGER.log(Level.WARNING, "Error grabbing timer from file", e);
                return null;
            }

        } else {
            LOGGER.log(Level.WARNING, "Can't get timer from folder: {0}", f.getAbsolutePath());
            return null;
        }
    }
}
