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

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.actionhandlers.ExitActionHandler;
import org.quelea.windows.main.actionhandlers.ExportPDFScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.NewScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.OpenScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.PrintScheduleActionHandler;
import org.quelea.windows.main.actionhandlers.SaveScheduleActionHandler;

/**
 * Quelea's file menu.
 *
 * @author Michael
 */
public class FileMenu extends Menu {

    private MenuItem newItem;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem saveAsItem;
    private MenuItem exportToPDFItem;
    private MenuItem printItem;
    private MenuItem exitItem;

    /**
     * Create the file menu.
     */
    public FileMenu() {
        super(LabelGrabber.INSTANCE.getLabel("file.menu"));

        newItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("new.schedule.button"));
        if (!Utils.isMac()) {
            newItem.setGraphic(new ImageView(new Image("file:icons/filenew.png", 20, 20, true, false)));
        }
        newItem.setOnAction(new NewScheduleActionHandler());
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        getItems().add(newItem);

        openItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("open.schedule.button"));
        if(!Utils.isMac()) {
            openItem.setGraphic(new ImageView(new Image("file:icons/fileopen.png", 20, 20, true, false)));
        }
        openItem.setOnAction(new OpenScheduleActionHandler());
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        getItems().add(openItem);

        saveItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("save.schedule.button"));
        if(!Utils.isMac()) {
            saveItem.setGraphic(new ImageView(new Image("file:icons/filesave.png", 20, 20, true, false)));
        }
        saveItem.setOnAction(new SaveScheduleActionHandler(false));
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        getItems().add(saveItem);

        saveAsItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("save.as.schedule.button"));
        if(!Utils.isMac()) {
            saveAsItem.setGraphic(new ImageView(new Image("file:icons/filesaveas.png", 20, 20, true, false)));
        }
        saveAsItem.setOnAction(new SaveScheduleActionHandler(true));
        getItems().add(saveAsItem);

        exportToPDFItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("export.schedule.pdf.button"));
        if(!Utils.isMac()) {
            exportToPDFItem.setGraphic(new ImageView(new Image("file:icons/fileexport.png", 20, 20, true, false)));
        }
        exportToPDFItem.setOnAction(new ExportPDFScheduleActionHandler());
        getItems().add(exportToPDFItem);

        printItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("print.schedule.button"));
        if(!Utils.isMac()) {
            printItem.setGraphic(new ImageView(new Image("file:icons/fileprint.png", 20, 20, true, false)));
        }
        printItem.setOnAction(new PrintScheduleActionHandler());
        printItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN));
        getItems().add(printItem);

        exitItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("exit.button"));
        if(!Utils.isMac()) {
            exitItem.setGraphic(new ImageView(new Image("file:icons/exit.png", 20, 20, true, false)));
        }
        exitItem.setOnAction(new ExitActionHandler());
        getItems().add(exitItem);
    }

}
