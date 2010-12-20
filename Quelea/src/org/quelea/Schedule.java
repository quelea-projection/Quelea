package org.quelea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quelea.display.Displayable;
import org.quelea.display.Song;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael
 */
public class Schedule implements Iterable<Displayable> {

    private final List<Displayable> displayables;
    private File file;

    public Schedule() {
        displayables = new ArrayList<Displayable>();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void clear() {
        displayables.clear();
    }

    public void add(Displayable displayable) {
        displayables.add(displayable);
    }

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
            return false;
        }
    }

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
            //TODO: Error
            return null;
        }
    }

    private String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<schedule>");
        for (Displayable displayable : displayables) {
            xml.append(displayable.getXML());
        }
        xml.append("</schedule>");
        return xml.toString();
    }

    public static Schedule parseXML(InputStream inputStream) {
//        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
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
                else {
                    throw new RuntimeException("Invalid node name: " + name);
                }
            }
            return newSchedule;
        }
        catch (ParserConfigurationException ex) {
            return null;
        }
        catch (SAXException ex) {
            return null;
        }
        catch (IOException ex) {
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
