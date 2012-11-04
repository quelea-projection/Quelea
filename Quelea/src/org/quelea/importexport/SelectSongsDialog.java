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
package org.quelea.importexport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.quelea.displayable.SongDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.main.BooleanCell;

/**
 * A dialog where given songs can be selected.
 * <p/>
 * @author Michael
 */
public class SelectSongsDialog extends Stage {

    private final Button addButton;
    private final TableView<SongDisplayable> table;
    private List<SongDisplayable> songs;
    private boolean[] checkList;
    private final String checkboxText;
    private TableColumn<SongDisplayable, String> nameColumn;
    private TableColumn<SongDisplayable, String> authorColumn;
    private TableColumn<SongDisplayable, Boolean> checkedColumn;

    /**
     * Create a new imported songs dialog.
     * <p/>
     * @param owner the owner of the dialog.
     * @param text a list of lines to be shown in the dialog.
     * @param acceptText text to place on the accpet button.
     * @param checkboxText text to place in the column header for the
     * checkboxes.
     */
    public SelectSongsDialog(String[] text, String acceptText,
            String checkboxText) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(LabelGrabber.INSTANCE.getLabel("select.songs.title"));
        this.checkboxText = checkboxText;
        songs = new ArrayList<>();

        VBox mainPanel = new VBox();
        for(String str : text) {
            mainPanel.getChildren().add(new Label(str));
        }
        ToolBar options = new ToolBar();
        options.getItems().add(createCheckAllButton());
        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        mainPanel.getChildren().add(table);
        addButton = new Button(acceptText, new ImageView(new Image("file:icons/tick.png")));
        mainPanel.getChildren().add(addButton);

        setScene(new Scene(mainPanel));
    }
    
    public TableColumn<SongDisplayable, Boolean> getCheckedColumn() {
        return checkedColumn;
    }

    /**
     * Create the button that checks all the boxes.
     * <p/>
     * @return the newly created check all button.
     */
    private Button createCheckAllButton() {
        Button checkButton = new Button("", new ImageView(new Image("file:icons/checkbox.jpg")));
        checkButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("check.uncheck.all.text")));
        checkButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                if(getSongs().isEmpty()) {
                    return;
                }
                final boolean checked = checkedColumn.getCellData(0);
                for(int i = 0; i < getSongs().size(); i++) {
                    checkedColumn.setCellValueFactory(new Callback<CellDataFeatures<SongDisplayable, Boolean>, ObservableValue<Boolean>>() {
                        @Override
                        public ObservableValue<Boolean> call(CellDataFeatures<SongDisplayable, Boolean> p) {
                            return new SimpleBooleanProperty(!checked);
                        }
                    });
                }
            }
        });
        return checkButton;
    }

    /**
     * Set the songs to be shown in the dialog.
     * <p/>
     * @param songs the list of songs to be shown.
     * @param checkList a list corresponding to the song list - each position is
     * true if the checkbox should be selected, false otherwise.
     * @param defaultVal the default value to use for the checkbox if checkList
     * is null or smaller than the songs list.
     */
    public void setSongs(final List<SongDisplayable> songs, final boolean[] checkList, final boolean defaultVal) {
        Collections.sort(songs);
        this.songs = songs;
        this.checkList = checkList;

        table.getColumns().clear();
        nameColumn = new TableColumn<>(LabelGrabber.INSTANCE.getLabel("name.label"));
        table.getColumns().add(nameColumn);
        authorColumn = new TableColumn<>(LabelGrabber.INSTANCE.getLabel("author.label"));
        table.getColumns().add(authorColumn);
        checkedColumn = new TableColumn<>(checkboxText);
        table.getColumns().add(checkedColumn);

        nameColumn.setCellValueFactory(new Callback<CellDataFeatures<SongDisplayable, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<SongDisplayable, String> p) {
                return new SimpleStringProperty(p.getValue().getTitle());
            }
        });

        authorColumn.setCellValueFactory(new Callback<CellDataFeatures<SongDisplayable, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<SongDisplayable, String> p) {
                return new SimpleStringProperty(p.getValue().getAuthor());
            }
        });

        Callback<TableColumn<SongDisplayable, Boolean>, TableCell<SongDisplayable, Boolean>> booleanCellFactory =
                new Callback<TableColumn<SongDisplayable, Boolean>, TableCell<SongDisplayable, Boolean>>() {
                    @Override
                    public TableCell<SongDisplayable, Boolean> call(TableColumn<SongDisplayable, Boolean> p) {
                        return new BooleanCell<SongDisplayable>();
                    }
                };
        checkedColumn.setCellFactory(booleanCellFactory);
        checkedColumn.setCellValueFactory(new Callback<CellDataFeatures<SongDisplayable, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(CellDataFeatures<SongDisplayable, Boolean> p) {
                int position = songs.indexOf(p.getValue());
                boolean bool;
                if(checkList != null && position < checkList.length) {
                    bool = !checkList[position]; //invert
                }
                else {
                    bool = defaultVal;
                }
                return new SimpleBooleanProperty(bool);
            }
        });

        table.setItems(FXCollections.observableArrayList(songs));
    }

    /**
     * Get the check list. This list corresponds with the list of songs to
     * determine whether the checkbox by each song should be checked or not.
     * <p/>
     * @return the check list.
     */
    public boolean[] getCheckList() {
        return checkList;
    }

    /**
     * Get the song list.
     * <p/>
     * @return the list of songs.
     */
    public List<SongDisplayable> getSongs() {
        return songs;
    }

    /**
     * Get the table in this dialog.
     * <p/>
     * @return the table.
     */
    public TableView<SongDisplayable> getTable() {
        return table;
    }

    /**
     * Get the add button.
     * <p/>
     * @return the add button.
     */
    public Button getAddButton() {
        return addButton;
    }
}
