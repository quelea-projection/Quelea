/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.menus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.importexport.OpenLyricsExporter;
import org.quelea.services.importexport.PDFExporter;
import org.quelea.services.importexport.QSPExporter;
import org.quelea.services.importexport.SelectExportedSongsDialog;
import org.quelea.services.importexport.SongListExporter;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

/**
 * Quelea's export menu.
 * <p>
 * @author Michael
 */
public class ExportMenu extends Menu {

    private final MenuItem qspItem;
    private final MenuItem pdfItem;
    private final MenuItem listItem;
    private final MenuItem openLyricsItem;

    /**
     * Create the export menu.
     */
    public ExportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("export.heading"), new ImageView(new Image(QueleaProperties .get().getUseDarkTheme() ? "file:icons/ic-export-light.png" : "file:icons/ic-export.png", 16, 16, false, true)));

        qspItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), new ImageView(new Image("file:icons/logo16.png", 16, 16, false, true)));
        qspItem.setOnAction(evt -> {
			final List<SongDisplayable> songs = Arrays.asList(SongManager.get().getSongs());
			//TODO: Determine if number of songs is above some threshold, then display warning that Quelea might be unresponsive while dialog is built.
			SelectExportedSongsDialog dialog = new SelectExportedSongsDialog(songs, new QSPExporter());
			dialog.showAndWait(); //This line is what takes the time for a large number of songs.
		});
        getItems().add(qspItem);

        openLyricsItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olyrics.button"), new ImageView(new Image("file:icons/logo-openlyrics.png", 16, 16, false, true)));
        openLyricsItem.setOnAction(evt -> {
			final List<SongDisplayable> songs = Arrays.asList(SongManager.get().getSongs());
			//TODO: Determine if number of songs is above some threshold, then display warning that Quelea might be unresponsive while dialog is built.
			SelectExportedSongsDialog dialog = new SelectExportedSongsDialog(songs, new OpenLyricsExporter());
			dialog.showAndWait(); //This line is what takes the time for a large number of songs.
		});
        getItems().add(openLyricsItem);

        pdfItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("pdf.button"), new ImageView(new Image(QueleaProperties .get().getUseDarkTheme() ? "file:icons/ic-pdf-light.png" : "file:icons/ic-pdf.png", 16, 16, false, true)));
        pdfItem.setOnAction(evt -> {
			final List<SongDisplayable> songs = Arrays.asList(SongManager.get().getSongs());
			//TODO: Determine if number of songs is above some threshold, then display warning that Quelea might be unresponsive while dialog is built.
			SelectExportedSongsDialog dialog = new SelectExportedSongsDialog(songs, new PDFExporter());
			dialog.showAndWait(); //This line is what takes the time for a large number of songs.
		});
        getItems().add(pdfItem);

        listItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("song.list"), new ImageView(new Image(QueleaProperties .get().getUseDarkTheme() ? "file:icons/ic-list-light.png" : "file:icons/ic-list.png", 16, 16, false, true)));
        listItem.setOnAction(evt -> {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(FileFilters.CSV);
			fc.setSelectedExtensionFilter(FileFilters.CSV);
			new SongListExporter().exportToFile(fc.showSaveDialog(QueleaApp.get().getMainWindow()));
		});
        getItems().add(listItem);
    }

}
