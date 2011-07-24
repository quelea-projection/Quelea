package org.quelea;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import org.quelea.displayable.BiblePassage;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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
import org.quelea.displayable.ImageDisplayable;
import org.quelea.displayable.VideoDisplayable;

/**
 * A schedule that consists of a number of displayable objects displayed by Quelea.
 * @author Michael
 */
public class Schedule implements Iterable<Displayable>, Printable {

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
        if (file == null) {
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
                Set<String> entries = new HashSet<String>();
                for (Displayable displayable : displayables) {
                    for (File displayableFile : displayable.getResources()) {
                        String base = ".";
                        String path = displayableFile.getAbsolutePath();
                        String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();
                        String zipPath = "resources/" + relative;
                        if (!entries.contains(zipPath)) {
                            entries.add(zipPath);
                            ZipEntry entry = new ZipEntry(zipPath);
                            zos.putNextEntry(entry);
                            FileInputStream fi = new FileInputStream(displayableFile);
                            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
                            int count;
                            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                                zos.write(data, 0, count);
                            }
                            zos.closeEntry();
                        }
                    }
                }
                return true;
            } finally {
                zos.close();
            }
        } catch (IOException ex) {
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
            final int BUFFER = 2048;
            try {
                Schedule ret = parseXML(zipFile.getInputStream(zipFile.getEntry("schedule.xml")));
                ret.setFile(file);
                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                while (enumeration.hasMoreElements()) {
                    ZipEntry entry = enumeration.nextElement();
                    if (!entry.getName().startsWith("resources/")) {
                        continue;
                    }
                    BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    File writeFile = new File(entry.getName().substring("resources/".length()));
                    if (writeFile.exists()) {
                        continue;
                    }
                    FileOutputStream fos = new FileOutputStream(writeFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER))
                            != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
                return ret;
            } finally {
                zipFile.close();
            }
        } catch (IOException ex) {
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
                } else if (name.equalsIgnoreCase("passage")) {
                    newSchedule.add(BiblePassage.parseXML(node));
                } else if (name.equalsIgnoreCase("fileimage")) {
                    newSchedule.add(ImageDisplayable.parseXML(node));
                } else if (name.equalsIgnoreCase("filevideo")) {
                    newSchedule.add(VideoDisplayable.parseXML(node));
                }
            }
            return newSchedule;
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the schedule", ex);
            return null;
        } catch (SAXException ex) {
            LOGGER.log(Level.WARNING, "Couldn't parse the schedule", ex);
            return null;
        } catch (IOException ex) {
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

    /**
     * Print the schedule.
     * @param graphics graphics to paint on.
     * @param pageFormat page format.
     * @param pageIndex starting index.
     * @return PAGE_EXISTS if the page exists, NO_SUCH_PAGE otherwise.
     * @throws PrinterException  if something went wrong
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setFont(new Font("Verdana", 0, 36));
        g2d.setColor(Color.BLUE);
        g2d.drawString("Order of service", 20, 60);
        g2d.setFont(new Font("Arial", 0, 14));
        g2d.setColor(Color.BLACK);
        int offset = 130;
        for (Displayable displayable : displayables) {
            g2d.drawString(displayable.getPrintText(), 20, offset);
            offset += 50;
        }
        return PAGE_EXISTS;
    }
}
