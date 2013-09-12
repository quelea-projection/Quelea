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
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;

/**
 * A dialog where given songs can be selected.
 * <p/>
 * @author Michael
 */
public class SelectSongsDialog extends Stage {

    private final Button addButton;
    private final TableView<SongWrapper> table;
    private List<SongWrapper> songs;
    private final String checkboxText;
    private TableColumn<SongWrapper, String> nameColumn;
    private TableColumn<SongWrapper, String> authorColumn;
    private TableColumn<SongWrapper, Boolean> checkedColumn;

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
        mainPanel.getChildren().add(createCheckAllButton());
        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        mainPanel.getChildren().add(table);
        addButton = new Button(acceptText, new ImageView(new Image("file:icons/tick.png")));
        mainPanel.getChildren().add(addButton);

        setScene(new Scene(mainPanel));
    }

    /**
     * Create the button that checks all the boxes.
     * <p/>
     * @return the newly created check all button.
     */
    private Button createCheckAllButton() {
        Button checkButton = new Button("Select All", new ImageView(new Image("file:icons/checkbox.jpg")));
        checkButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("check.uncheck.all.text")));
        checkButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                if(songs.isEmpty()) {
                    return;
                }
                boolean val = songs.get(0).selected.get();
                for(SongWrapper wrapper : songs) {
                    wrapper.selected.set(!val);
                }
            }
        });
        return checkButton;
    }

    private static class SongWrapper {

        private SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        private SongDisplayable song;

        public SongWrapper(SongDisplayable song) {
            this.song = song;
        }
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
        this.songs = toWrapper(songs);
        table.getColumns().clear();
        nameColumn = new TableColumn<>(LabelGrabber.INSTANCE.getLabel("name.label"));
        table.getColumns().add(nameColumn);
        authorColumn = new TableColumn<>(LabelGrabber.INSTANCE.getLabel("author.label"));
        table.getColumns().add(authorColumn);
        checkedColumn = new TableColumn<>(checkboxText);
        table.getColumns().add(checkedColumn);

        nameColumn.setCellValueFactory(new Callback<CellDataFeatures<SongWrapper, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<SongWrapper, String> p) {
                return new SimpleStringProperty(p.getValue().song.getTitle());
            }
        });

        authorColumn.setCellValueFactory(new Callback<CellDataFeatures<SongWrapper, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<SongWrapper, String> p) {
                return new SimpleStringProperty(p.getValue().song.getAuthor());
            }
        });

        checkedColumn.setCellValueFactory(new Callback<CellDataFeatures<SongWrapper, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(CellDataFeatures<SongWrapper, Boolean> p) {
                return p.getValue().selected;
            }
        });

        checkedColumn.setCellFactory(new Callback<TableColumn<SongWrapper, Boolean>, TableCell<SongWrapper, Boolean>>() {
            @Override
            public TableCell<SongWrapper, Boolean> call(TableColumn<SongWrapper, Boolean> p) {
                CheckBoxTableCell<SongWrapper, Boolean> cell = new CheckBoxTableCell<>();
                cell.setEditable(true);
                return cell;
            }
        });
        checkedColumn.setEditable(true);
        table.setEditable(true);

        table.setItems(FXCollections.observableArrayList(this.songs));
    }

    /**
     * Get the list of selected songs.
     * <p/>
     * @return the list of selected songs.
     */
    public List<SongDisplayable> getSelectedSongs() {
        List<SongDisplayable> ret = new ArrayList<>();
        for(SongWrapper wrapper : songs) {
            if(wrapper.selected.get()) {
                ret.add(wrapper.song);
            }
        }
        return ret;
    }

    /**
     * Get the add button.
     * <p/>
     * @return the add button.
     */
    public Button getAddButton() {
        return addButton;
    }

    private List<SongWrapper> toWrapper(List<SongDisplayable> songs) {
        List<SongWrapper> ret = new ArrayList<>();
        for(SongDisplayable song : songs) {
            ret.add(new SongWrapper(song));
        }
        return ret;
    }

    private List<SongDisplayable> fromWrapper(List<SongWrapper> wrapperList) {
        List<SongDisplayable> ret = new ArrayList<>();
        for(SongWrapper wrap : wrapperList) {
            ret.add(wrap.song);
        }
        return ret;
    }
}
