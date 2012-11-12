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
package org.quelea;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A schedule that consists of a number of displayable objects displayed by
 * Quelea.
 *
 * @author Michael
 */
public class Schedule implements Iterable<Displayable>, Printable {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final List<Displayable> displayables;
    private File file;
    private boolean modified;

    /**
     * Create a new schedule.
     */
    public Schedule() {
        displayables = new ArrayList<>();
        modified = false;
    }

    /**
     * Determine if this schedule has been modified since it was last saved.
     *
     * @return true if it's been modified, false if it hasn't.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Set the file that this schedule should be saved to.
     *
     * @param file the file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Get the file where this schedule is being saved.
     *
     * @return the file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Clear all the displayables in this schedule.
     */
    public void clear() {
        displayables.clear();
        modified = true;
    }

    /**
     * Add a displayable to this schedule.
     *
     * @param displayable the displayable to add.
     */
    public void add(Displayable displayable) {
        displayables.add(displayable);
        modified = true;
    }

    /**
     * Write this schedule to a file.
     *
     * @return true if the write was successful, false otherwise.
     */
    public boolean writeToFile() {
        if(file == null) {
            return false;
        }
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
            final int BUFFER = 2048;
            byte data[] = new byte[BUFFER];
            try {
                zos.putNextEntry(new ZipEntry("schedule.xml"));
                zos.write(getXML().getBytes());
                zos.closeEntry();
                Set<String> entries = new HashSet<>();
                for(Displayable displayable : displayables) {
                    for(File displayableFile : displayable.getResources()) {
                        String base = ".";
                        String path = displayableFile.getAbsolutePath();
                        String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();
                        String zipPath = "resources/" + relative;
                        if(!entries.contains(zipPath)) {
                            entries.add(zipPath);
                            ZipEntry entry = new ZipEntry(zipPath);
                            zos.putNextEntry(entry);
                            FileInputStream fi = new FileInputStream(displayableFile);
                            try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER)) {
                                int count;
                                while((count = origin.read(data, 0, BUFFER)) != -1) {
                                    zos.write(data, 0, count);
                                }
                                zos.closeEntry();
                            }
                        }
                    }
                }
                modified = false;
                return true;
            }
            finally {
                zos.close();
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't write the schedule to file", ex);
            return false;
        }
    }

    /**
     * Generate a schedule object from a saved file.
     *
     * @param file the file where the schedule is saved.
     * @return the schedule object.
     */
    public static Schedule fromFile(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            final int BUFFER = 2048;
            try {
                Schedule ret = parseXML(zipFile.getInputStream(zipFile.getEntry("schedule.xml")));
                if(ret==null) {
                    return null;
                }
                ret.setFile(file);
                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                while(enumeration.hasMoreElements()) {
                    ZipEntry entry = enumeration.nextElement();
                    if(!entry.getName().startsWith("resources/")) {
                        continue;
                    }
                    try (BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry))) {
                        int count;
                        byte data[] = new byte[BUFFER];
                        File writeFile = new File(entry.getName().substring("resources/".length()));
                        if(writeFile.exists()) {
                            continue;
                        }
                        FileOutputStream fos = new FileOutputStream(writeFile);
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
                            while((count = is.read(data, 0, BUFFER))
                                    != -1) {
                                dest.write(data, 0, count);
                            }
                            dest.flush();
                        }
                    }
                }
                ret.modified = false;
                return ret;
            }
            finally {
                zipFile.close();
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't read the schedule from file", ex);
            return null;
        }
    }

    /**
     * Get this schedule as XML.
     *
     * @return XML describing this schedule.
     */
    private String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<schedule>");
        for(Displayable displayable : displayables) {
            xml.append(displayable.getXML());
        }
        xml.append("</schedule>");
        return xml.toString();
    }

    /**
     * Parse some given XML from an inputstream to create a schedule.
     *
     * @param inputStream the inputstream where the xml is being read from.
     * @return the schedule.
     */
    private static Schedule parseXML(InputStream inputStream) {
        try {
            /*
             * TODO: This should solve a problem some people were having with 
             * entering schedules - though I'm not really sure *why* they're 
             * having this problem (it seems to be that there's some funny 
             * characters that end up in the XML file which shouldn't be there.
             * Character encoding bug perhaps? Oh joy.
             * 
             * Start bodge.
             */
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder contentsBuilder = new StringBuilder();
            String line;
            while((line=reader.readLine())!=null) {
                contentsBuilder.append(line).append('\n');
            }
            String contents = contentsBuilder.toString();
            contents = contents.replace(new String(new byte[]{11}), "\n");
            contents = contents.replace(new String(new byte[]{-3}), " ");
            InputStream strInputStream = new ByteArrayInputStream(contents.getBytes());
            /*
             * End bodge.
             */
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(strInputStream); //Read from our "bodged" stream.
            NodeList nodes = doc.getFirstChild().getChildNodes();
            Schedule newSchedule = new Schedule();
            for(int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String name = node.getNodeName();
                if(name.equalsIgnoreCase("song")) {
                    newSchedule.add(SongDisplayable.parseXML(node));
                }
                else if(name.equalsIgnoreCase("passage")) {
                    newSchedule.add(BiblePassage.parseXML(node));
                }
                else if(name.equalsIgnoreCase("fileimage")) {
                    newSchedule.add(ImageDisplayable.parseXML(node));
                }
                else if(name.equalsIgnoreCase("filevideo")) {
                    newSchedule.add(VideoDisplayable.parseXML(node));
                }
                else if(name.equalsIgnoreCase("filepresentation")) {
                    newSchedule.add(PresentationDisplayable.parseXML(node));
                }
            }
            newSchedule.modified = false;
            return newSchedule;
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the schedule", ex);
            return null;
        }
    }

    /**
     * Get an iterator over the displayables in the schedule.
     *
     * @return the iterator.
     */
    @Override
    public Iterator<Displayable> iterator() {
        return displayables.iterator();
    }
    
    /**
     * Get the displayable at the given index.
     * @param index the index to get the displayable at.
     * @return the displayable at the given index.
     */
    public Displayable getDisplayable(int index) {
        return displayables.get(index);
    }
    
    /**
     * Get the size of this schedule.
     * @return the schedule size.
     */
    public int getSize() {
        return displayables.size();
    }
    
    /**
     * Determine whether this schedule is empty.
     * @return true if it's empty, false otherwise.
     */
    public boolean isEmpty() {
        return getSize()==0;
    }

    /**
     * Print the schedule.
     *
     * @param graphics graphics to paint on.
     * @param pageFormat page format.
     * @param pageIndex starting index.
     * @return PAGE_EXISTS if the page exists, NO_SUCH_PAGE otherwise.
     * @throws PrinterException if something went wrong
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if(pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setFont(new Font("Verdana", 0, 36));
        g2d.setColor(Color.BLUE);
        g2d.drawString("Order of service", 20, 60);
        g2d.setFont(new Font("Arial", 0, 14));
        g2d.setColor(Color.BLACK);
        int offset = 130;
        for(Displayable displayable : displayables) {
            g2d.drawString(displayable.getPrintText(), 20, offset);
            offset += 50;
        }
        return PAGE_EXISTS;
    }
}
