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
package org.quelea.windows.library;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.print.Printer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.AddSongActionHandler;
import org.quelea.windows.main.actionhandlers.EditSongDBActionHandler;
import org.quelea.windows.main.actionhandlers.ExportPDFSongActionHandler;
import org.quelea.windows.main.actionhandlers.PreviewSongActionHandler;
import org.quelea.windows.main.actionhandlers.RemoveSongDBActionHandler;
import org.quelea.windows.style.QueleaIcon;

/**
 * The popup menu that displays when someone right clicks on a song in the
 * library.
 *
 * @author Michael
 */
public class LibraryPopupMenu extends ContextMenu {

    private final MenuItem addToSchedule;
    private final MenuItem copyToSchedule;
    private final MenuItem preview;
    private final MenuItem editDB;
    private final MenuItem removeFromDB;
    private final MenuItem exportToPDF;
    private final MenuItem print;

    /**
     * Create and initialise the popup menu.
     */
    public LibraryPopupMenu() {
        addToSchedule = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.add.to.schedule.text"), QueleaIcon.ADD.create());
        addToSchedule.setOnAction(new AddSongActionHandler(true));
        copyToSchedule = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.copy.to.schedule.text"), QueleaIcon.ADD.create());
        copyToSchedule.setOnAction(new AddSongActionHandler(false));
        preview = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.preview.song.text"), QueleaIcon.PREVIEW.create());
        preview.setOnAction(new PreviewSongActionHandler());
        editDB = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.edit.song.text"), QueleaIcon.EDIT.create());
        editDB.setOnAction(new EditSongDBActionHandler());
        removeFromDB = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.remove.song.text"), QueleaIcon.DELETE.create());
        removeFromDB.setOnAction(new RemoveSongDBActionHandler());
        exportToPDF = new MenuItem(LabelGrabber.INSTANCE.getLabel("export.pdf.button"), QueleaIcon.PDF.create());
        exportToPDF.setOnAction(new ExportPDFSongActionHandler());
        print = new MenuItem(LabelGrabber.INSTANCE.getLabel("library.print.song.text"), QueleaIcon.PRINT.create());
        print.setOnAction(t -> {
            final SongDisplayable song = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValues().get(0);
            if (song != null) {
                if (song.hasChords()) {
                    Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("printing.options.text"), LabelGrabber.INSTANCE.getLabel("print.chords.question")).addYesButton(t12 -> song.setPrintChords(true)).addNoButton(t1 -> song.setPrintChords(false)).build().showAndWait();
                }
                Printer.getInstance().print(song);
            }
        });

        getItems().add(addToSchedule);
        getItems().add(copyToSchedule);
        getItems().add(preview);
        getItems().add(editDB);
        getItems().add(removeFromDB);
        getItems().add(exportToPDF);
        getItems().add(print);
    }

    public void setMultipleSelected(boolean multipleSelected) {
        addToSchedule.setDisable(false);
        copyToSchedule.setDisable(false);
        preview.setDisable(multipleSelected);
        editDB.setDisable(multipleSelected);
        removeFromDB.setDisable(false);
        exportToPDF.setDisable(multipleSelected);
        print.setDisable(multipleSelected);
    }
}
