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
public class OptionsImportExportPanel extends GridPane implements PropertyPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final TextField downloadPathTextField;
    private final DirectoryChooser downloadChooser;
    private final Button downloadSelectButton;

    /**
     * Create the options bible panel.
     */
    public OptionsImportExportPanel() {
        int rows = 0;
        setVgap(5);
        setHgap(10);
        setPadding(new Insets(5));

        Label recordingsPathLabel = new Label(LabelGrabber.INSTANCE.getLabel("download.path"));
        GridPane.setConstraints(recordingsPathLabel, 1, rows);
        getChildren().add(recordingsPathLabel);
        downloadPathTextField = new TextField();
        downloadPathTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(downloadPathTextField, Priority.ALWAYS);
        downloadPathTextField.setEditable(false);
        recordingsPathLabel.setLabelFor(downloadPathTextField);
        GridPane.setConstraints(downloadPathTextField, 2, rows);
        getChildren().add(downloadPathTextField);
        downloadChooser = new DirectoryChooser();
        downloadSelectButton = new Button(LabelGrabber.INSTANCE.getLabel("browse"));
        downloadSelectButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                File dir = downloadChooser.showDialog(QueleaApp.get().getMainWindow());
                if (dir != null) {
                    downloadPathTextField.setText(dir.getAbsolutePath());
                }
            }
        });
        GridPane.setConstraints(downloadSelectButton, 3, rows);
        getChildren().add(downloadSelectButton);
        rows++;

        readProperties();

    }

    /**
     * @inheritDoc
     */
    @Override
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        downloadPathTextField.setText(props.getDownloadPath());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        String recPath = getDownloadPathTextField().getText();
        props.setDownloadPath(recPath);
    }
    
      /**
     * Get the "download path" text field.
     * <p/>
     * @return the "download path" text field.
     */
    public TextField getDownloadPathTextField() {
        return downloadPathTextField;
    }
}
