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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.importexport.EasySlidesImportDialog;
import org.quelea.services.importexport.ImportDialog;
import org.quelea.services.importexport.KingswayImportDialog;
import org.quelea.services.importexport.OpenLPImportDialog;
import org.quelea.services.importexport.OpenSongImportDialog;
import org.quelea.services.importexport.PlainTextSongsImportDialog;
import org.quelea.services.importexport.QSPImportDialog;
import org.quelea.services.importexport.SourceImportDialog;
import org.quelea.services.importexport.SundayPlusImportDialog;
import org.quelea.services.importexport.SurvivorImportDialog;

/**
 * Quelea's import menu.
 *
 * @author Michael
 */
public class ImportMenu extends Menu {

    private final ImportDialog sImportDialog;
    private final ImportDialog qspImportDialog;
    private final ImportDialog openSongImportDialog;
    private final ImportDialog openLPImportDialog;
    private final ImportDialog sourceImportDialog;
    private final ImportDialog kingswayImportDialog;
    private final ImportDialog plainTextImportDialog;
    private final ImportDialog easySlidesImportDialog;
    private final ImportDialog sundayPlusImportDialog;
    private final MenuItem qspItem;
    private final MenuItem osItem;
    private final MenuItem spItem;
    private final MenuItem olpItem;
    private final MenuItem ssItem;
    private final MenuItem sourceItem;
    private final MenuItem plainTextItem;
    private final MenuItem easySlidesItem;
    private final Menu kingswayItem;

    /**
     * Create the import menu.
     */
    public ImportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("import.heading"), new ImageView(new Image("file:icons/left.png", 16, 16, false, true)));

        qspImportDialog = new QSPImportDialog();
        openSongImportDialog = new OpenSongImportDialog();
        openLPImportDialog = new OpenLPImportDialog();
        sImportDialog = new SurvivorImportDialog();
        sourceImportDialog = new SourceImportDialog();
        kingswayImportDialog = new KingswayImportDialog(null);
        plainTextImportDialog = new PlainTextSongsImportDialog();
        easySlidesImportDialog = new EasySlidesImportDialog();
        sundayPlusImportDialog = new SundayPlusImportDialog();

        qspItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), new ImageView(new Image("file:icons/logo16.png", 16, 16, false, true)));
        qspItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                qspImportDialog.show();
            }
        });
        getItems().add(qspItem);

        osItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("os.button"), new ImageView(new Image("file:icons/opensong.png", 16, 16, false, true)));
        osItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                openSongImportDialog.show();
            }
        });
        getItems().add(osItem);

        olpItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("olp.button"), new ImageView(new Image("file:icons/openlp.png", 16, 16, false, true)));
        olpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                openLPImportDialog.show();
            }
        });
        getItems().add(olpItem);

        spItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("sp.button"), new ImageView(new Image("file:icons/sundayplus.png", 16, 16, false, true)));
        spItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                sundayPlusImportDialog.show();
            }
        });
        getItems().add(spItem);

        ssItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("ss.button"), new ImageView(new Image("file:icons/survivor.jpg", 16, 16, false, true)));
        ssItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                sImportDialog.show();
            }
        });
        getItems().add(ssItem);

        sourceItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("source.button"), new ImageView(new Image("file:icons/source.jpg", 16, 16, false, true)));
        sourceItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                sourceImportDialog.show();
            }
        });
        getItems().add(sourceItem);

        easySlidesItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("easyslides.button"), new ImageView(new Image("file:icons/easyslides.png", 16, 16, false, true)));
        easySlidesItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                easySlidesImportDialog.show();
            }
        });
        getItems().add(easySlidesItem);

        MenuItem kingswayAll, kingswayOne;

        kingswayItem = new Menu(LabelGrabber.INSTANCE.getLabel("kingsway.button"), new ImageView(new Image("file:icons/kingsway.png", 16, 16, false, true)));
        kingswayAll = new MenuItem(LabelGrabber.INSTANCE.getLabel("kingsway.button.all"), new ImageView(new Image("file:icons/kingsway.png", 16, 16, false, true)));
        kingswayAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                kingswayImportDialog.setAll(true);
                kingswayImportDialog.show();
            }
        });

        kingswayOne = new MenuItem(LabelGrabber.INSTANCE.getLabel("kingsway.button.one"), new ImageView(new Image("file:icons/kingsway.png", 16, 16, false, true)));
        kingswayOne.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                kingswayImportDialog.setAll(false);
                kingswayImportDialog.show();
            }
        });

        getItems().add(kingswayItem);
        kingswayItem.getItems().add(kingswayAll);
        kingswayItem.getItems().add(kingswayOne);

        plainTextItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("plainText.button"), new ImageView(new Image("file:icons/text.png", 16, 16, false, true)));
        plainTextItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                plainTextImportDialog.show();
            }
        });
        getItems().add(plainTextItem);
    }
}
