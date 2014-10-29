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
package org.quelea.services.importexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.print.SongPDFPrinter;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;

/**
 * An exporter for the PDF format.
 *
 * @author Michael
 */
public class PDFExporter implements Exporter {

    public static final Logger LOGGER = LoggerUtils.getLogger();
    private boolean printChords;

    /**
     * Get the file chooser to be used.
     * <p/>
     * @return the zip file chooser..
     */
    @Override
    public FileChooser getChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.ZIP);
        return chooser;
    }

    /**
     * Export the given songs to the given file.
     *
     * @param file the zip file to export to.
     * @param songDisplayables the songs to export to the zip file.
     */
    @Override
    public void exportSongs(final File file, final List<SongDisplayable> songDisplayables) {
        Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("printing.options.text"), LabelGrabber.INSTANCE.getLabel("print.chords.export.question")).addYesButton(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                printChords = true;
            }
        }).addNoButton(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                printChords = false;
            }
        }).build().showAndWait();
        final StatusPanel panel = QueleaApp.get().getMainWindow().getMainPanel().getStatusPanelGroup().addPanel(LabelGrabber.INSTANCE.getLabel("exporting.label") + "...");
        final List<SongDisplayable> songDisplayablesThreadSafe = new ArrayList<>(songDisplayables);
        new Thread() {
            public void run() {
                final HashSet<String> names = new HashSet<>();
                try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file), Charset.forName("UTF-8"))) {
                    for (int i = 0; i < songDisplayablesThreadSafe.size(); i++) {
                        SongDisplayable song = songDisplayablesThreadSafe.get(i);
                        String name = sanitise(song.getTitle()) + ".pdf";
                        while (names.contains(name)) {
                            name = incrementExtension(name);
                        }
                        names.add(name);
                        out.putNextEntry(new ZipEntry(name));
                        out.write(getPDF(song, printChords));
                        panel.setProgress((double) i / songDisplayablesThreadSafe.size());
                    }
                    panel.done();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't export PDF songs", ex);
                }
            }
        }.start();
    }

    public static String incrementExtension(String name) {
        name = name.substring(0, name.length() - 4).trim();
        Pattern p = Pattern.compile(".*\\(([0-9]+)\\)");
        Matcher matcher = p.matcher(name);
        if (matcher.matches()) {
            int suffixLength = matcher.group(1).length() + 2;
            int nextNum = Integer.parseInt(matcher.group(1)) + 1;
            return name.substring(0, name.length() - suffixLength) + "(" + nextNum + ").pdf";
        } else {
            return name + "(2).pdf";
        }
    }
    
    public static String sanitise(String name) {
        name = name.replace(":", "");
        name = name.replace("/", "");
        name = name.replace("\\", "");
        return name;
    }

    /**
     * Get the bytes that make up a PDF file for each song.
     *
     * @param song the song to get the PDF bytes for.
     * @param printChords
     * @return the bytes that make up a PDF file for each song.
     */
    public static byte[] getPDF(SongDisplayable song, boolean printChords) {
        song.setPrintChords(printChords);
        File temp = null;
        try {
            temp = File.createTempFile("queleasong", ".pdf");
            temp.deleteOnExit();
            if (song == null) {
                return new byte[0];
            } else {
                SongPDFPrinter.INSTANCE.print(song, temp, false);
                return Files.readAllBytes(temp.toPath());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Couldn't get PDF bytes for song", ex);
            return new byte[0];
        }
    }

    @Override
    public String getStrExtension() {
        return "zip";
    }

}
