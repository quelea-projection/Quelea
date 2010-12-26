package org.quelea;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A schedule that consists of a number of displayable objects displayed by
 * Quelea.
 * @author Michael
 */
public class Schedule implements Iterable<Displayable> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final List<Displayable> displayables;
    private File file;

    /**
     * Create a new schedule.
     */
    public Schedule() {
        displayables = new ArrayList<Displayable>();
    }

    /**
     * Set the file that this schedule should be saved to.
     * @param file the file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Get the file where this schedule is being saved.
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
    }

    /**
     * Add a displayable to this schedule.
     * @param displayable the displayable to add.
     */
    public void add(Displayable displayable) {
        displayables.add(displayable);
    }

    /**
     * Write this schedule to a file.
     * @return true if the write was successful, false otherwise.
     */
    public boolean writeToFile() {
        if(file==null) {
            return false;
        }
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
            try {
                zos.putNextEntry(new ZipEntry("schedule.xml"));
                zos.write(getXML().getBytes());
                zos.closeEntry();
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
     * @param file the file where the schedule is saved.
     * @return the schedule object.
     */
    public static Schedule fromFile(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            try {
                Schedule ret = parseXML(zipFile.getInputStream(zipFile.getEntry("schedule.xml")));
                ret.setFile(file);
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
     * @return XML describing this schedule.
     */
    private String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<schedule>");
        for (Displayable displayable : displayables) {
            xml.append(displayable.getXML());
        }
        xml.append("</schedule>");
        return xml.toString();
    }

    /**
     * Parse some given XML from an inputstream to create a schedule.
     * @param inputStream the inputstream where the xml is being read from.
     * @return the schedule.
     */
    private static Schedule parseXML(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            NodeList nodes = doc.getFirstChild().getChildNodes();
            Schedule newSchedule = new Schedule();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String name = node.getNodeName();
                if (name.equalsIgnoreCase("song")) {
                    newSchedule.add(Song.parseXML(node));
                }
            }
            return newSchedule;
        }
        catch (ParserConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the schedule", ex);
            return null;
        }
        catch (SAXException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the schedule", ex);
            return null;
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the schedule", ex);
            return null;
        }
    }

    /**
     * Get an iterator over the displayables in the schedule.
     * @return the iterator.
     */
    public Iterator<Displayable> iterator() {
        return displayables.iterator();
    }
}
