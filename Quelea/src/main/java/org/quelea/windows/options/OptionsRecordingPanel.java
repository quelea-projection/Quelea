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
package org.quelea.windows.options;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleChangeListener;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.main.widgets.NumberSpinner;
import org.quelea.windows.main.widgets.NumberTextField;

/**
 * The panel that shows the recording options
 * <p/>
 * @author Michael
 */
public class OptionsRecordingPanel extends GridPane implements PropertyPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final TextField recordingsPathTextField;
    private final DirectoryChooser recordingsChooser;
    private final Button recordingsSelectButton;
    private final CheckBox convertMp3CheckBox;

    /**
     * Create the options bible panel.
     */
    public OptionsRecordingPanel() {
        int rows = 0;
        setVgap(5);
        setHgap(10);
        setPadding(new Insets(5));

        Label recordingsPathLabel = new Label(LabelGrabber.INSTANCE.getLabel("recordings.path"));
        GridPane.setConstraints(recordingsPathLabel, 1, rows);
        getChildren().add(recordingsPathLabel);
        recordingsPathTextField = new TextField();
        recordingsPathTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(recordingsPathTextField, Priority.ALWAYS);
        recordingsPathTextField.setEditable(false);
        recordingsPathLabel.setLabelFor(recordingsPathTextField);
        GridPane.setConstraints(recordingsPathTextField, 2, rows);
        getChildren().add(recordingsPathTextField);
        recordingsChooser = new DirectoryChooser();
        recordingsSelectButton = new Button(LabelGrabber.INSTANCE.getLabel("browse"));
        recordingsSelectButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                File dir = recordingsChooser.showDialog(QueleaApp.get().getMainWindow());
                if (dir != null) {
                    recordingsPathTextField.setText(dir.getAbsolutePath());
                }
            }
        });
        GridPane.setConstraints(recordingsSelectButton, 3, rows);
        getChildren().add(recordingsSelectButton);
        rows++;

        Label convertMp3Label = new Label(LabelGrabber.INSTANCE.getLabel("convert.mp3"));
        GridPane.setConstraints(convertMp3Label, 1, rows);
        getChildren().add(convertMp3Label);
        convertMp3CheckBox = new CheckBox();
        convertMp3Label.setLabelFor(convertMp3CheckBox);
        GridPane.setConstraints(convertMp3CheckBox, 2, rows);
        getChildren().add(convertMp3CheckBox);
        rows++;
        
        readProperties();

    }

    /**
     * @inheritDoc
     */
    @Override
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        recordingsPathTextField.setText(props.getRecordingsPath());
        convertMp3CheckBox.setSelected(props.getConvertRecordings());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        String recPath = getRecordingsPathTextField().getText();
        props.setRecordingsPath(recPath);
        boolean convertRecordings = convertMp3CheckBox.isSelected();
        props.setConvertRecordings(convertRecordings);
    }
    
      /**
     * Get the "recordings path" text field.
     * <p/>
     * @return the "recordings path" text field.
     */
    public TextField getRecordingsPathTextField() {
        return recordingsPathTextField;
    }

    /**
     * Get the "automatically convert recordings" checkbox.
     * <p/>
     * @return the "automatically convert recordings" checkbox.
     */
    public CheckBox getConvertRecordingsCheckBox() {
        return convertMp3CheckBox;
    }

}
