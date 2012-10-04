/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.importexport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JFrame;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.quelea.displayable.Song;
import org.quelea.languages.LabelGrabber;

/**
 * A dialog where given songs can be selected.
 * @author Michael
 */
public class SelectSongsDialog extends Stage {

    private final Button addButton;
    private final TableView table;
    private List<Song> songs;
    private boolean[] checkList;
    private final String checkboxText;
    private TableModel model;

    /**
     * Create a new imported songs dialog.
     * @param owner the owner of the dialog.
     * @param text a list of lines to be shown in the dialog.
     * @param acceptText text to place on the accpet button.
     * @param checkboxText text to place in the column header for the
     * checkboxes.
     */
    public SelectSongsDialog(JFrame owner, String[] text, String acceptText,
                             String checkboxText) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(LabelGrabber.INSTANCE.getLabel("select.songs.title"));
        this.checkboxText = checkboxText;
        songs = new ArrayList<>();
        
        HBox rootPane = new HBox();
        VBox mainPanel = new VBox();
        for(String str : text) {
            mainPanel.getChildren().add(new Label(str));
        }
        table = new TableView();
        mainPanel.getChildren().add(table);
        
        ToolBar options = new ToolBar();
        options.getItems().add(createCheckAllButton());
        addButton = new Button(acceptText, new ImageView(new Image("file:icons/tick.png")));
        mainPanel.getChildren().add(addButton);
        rootPane.getChildren().add(mainPanel);
        rootPane.getChildren().add(options);
        
        setScene(new Scene(rootPane));
    }

    /**
     * Create the button that checks all the boxes.
     * @return the newly created check all button.
     */
    private Button createCheckAllButton() {
        Button checkButton = new Button("",new ImageView(new Image("file:icons/checkbox.jpg")));
        checkButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("check.uncheck.all.text")));
        checkButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                if(getSongs().isEmpty()) {
                    return;
                }
                boolean val = !(Boolean) getTable().getValueAt(0, 2);
                for(int i = 0; i < getSongs().size(); i++) {
                    getTable().setValueAt(val, i, 2);
                }
            }
        });
        return checkButton;
    }

    /**
     * Set the songs to be shown in the dialog.
     * @param songs         the list of songs to be shown.
     * @param checkList     a list corresponding to the song list - each position is true if the checkbox should be
     *                      selected, false otherwise.
     * @param defaultVal    the default value to use for the checkbox if checkList is null or smaller than the songs
     *                      list.
     */
    public void setSongs(List<Song> songs, boolean[] checkList, boolean defaultVal) {
        Collections.sort(songs);
        this.songs = songs;
        this.checkList = checkList;
//        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
//        table.setRowSorter(sorter);
        
        table.getColumns().clear();
        TableColumn nameColumn = new TableColumn(LabelGrabber.INSTANCE.getLabel("name.label"));
        table.getColumns().add(nameColumn);
        TableColumn authorColumn = new TableColumn(LabelGrabber.INSTANCE.getLabel("author.label"));
        table.getColumns().add(authorColumn);
        TableColumn checkedColumn = new TableColumn(checkboxText);
        table.getColumns().add(checkedColumn);
        
        table.getColumnModel().getColumn(0).setHeaderValue(LabelGrabber.INSTANCE.getLabel("name.label"));
        table.getColumnModel().getColumn(1).setHeaderValue(LabelGrabber.INSTANCE.getLabel("author.label"));
        table.getColumnModel().getColumn(2).setHeaderValue(checkboxText);
        table.getColumnModel().getColumn(2).setCellEditor(table.getDefaultEditor(Boolean.class));
        table.getColumnModel().getColumn(2).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        for(int i = 0; i < songs.size(); i++) {
            table.getModel().setValueAt(songs.get(i).getTitle(), i, 0);
            table.getModel().setValueAt(songs.get(i).getAuthor(), i, 1);
            boolean val;
            if(checkList != null && i < checkList.length) {
                val = !checkList[i]; //invert
            }
            else {
                val = defaultVal;
            }
            table.getModel().setValueAt(val, i, 2);
        }
    }

    /**
     * Get the check list. This list corresponds with the list of songs to determine whether the checkbox by each song
     * should be checked or not.
     * @return the check list.
     */
    public boolean[] getCheckList() {
        return checkList;
    }

    /**
     * Get the song list.
     * @return the list of songs.
     */
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * Get the table in this dialog.
     * @return the table.
     */
    public TableView getTable() {
        return table;
    }

    /**
     * Get the add button.
     * @return the add button.
     */
    public Button getAddButton() {
        return addButton;
    }
}
