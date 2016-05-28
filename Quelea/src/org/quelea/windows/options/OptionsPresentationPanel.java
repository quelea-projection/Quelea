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
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * The panel that shows the presentation options
 * <p/>
 * @author Arvid
 */
public class OptionsPresentationPanel extends GridPane implements PropertyPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Button selectButton;
    private final Button ppSelectButton;
    private final DirectoryChooser ooChooser;
    private final FileChooser powerPointChooser;
    private final CheckBox useOOCheckBox;
    private final CheckBox usePowerPointCheckBox;
    private boolean usePowerPoint;
    private final TextField ooPathTextField;
    private final TextField ppPathTextField;

    /**
     * Create the options presentation panel.
     */
    public OptionsPresentationPanel() {
        int rows = 0;
        setVgap(5);
        setHgap(10);
        setPadding(new Insets(5));
        QueleaProperties props = QueleaProperties.get();

        Label useOOLabel = new Label(LabelGrabber.INSTANCE.getLabel("use.oo.label"));
        GridPane.setConstraints(useOOLabel, 1, rows);
        getChildren().add(useOOLabel);
        useOOCheckBox = new CheckBox();
        useOOCheckBox.selectedProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (useOOCheckBox.isSelected()) {
                    ooPathTextField.setDisable(false);
                    selectButton.setDisable(false);
                    selectButton.setDisable(false);
                    ppPathTextField.setDisable(true);
                    ppSelectButton.setDisable(true);
                    usePowerPointCheckBox.setSelected(false);
                    props.setUsePP(false);
                } else {
                    ooPathTextField.setDisable(true);
                    selectButton.setDisable(true);
                }
            }
        });
        useOOLabel.setLabelFor(useOOCheckBox);
        GridPane.setConstraints(useOOCheckBox, 2, rows);
        getChildren().add(useOOCheckBox);
        rows++;

        Label ooPathLabel = new Label(LabelGrabber.INSTANCE.getLabel("oo.path"));
        GridPane.setConstraints(ooPathLabel, 1, rows);
        getChildren().add(ooPathLabel);
        ooPathTextField = new TextField();
        ooPathTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(ooPathTextField, Priority.ALWAYS);
        ooPathTextField.setEditable(false);
        ooPathLabel.setLabelFor(ooPathTextField);
        GridPane.setConstraints(ooPathTextField, 2, rows);
        getChildren().add(ooPathTextField);
        ooChooser = new DirectoryChooser();
        selectButton = new Button(LabelGrabber.INSTANCE.getLabel("browse"));
        selectButton.setDisable(true);
        selectButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                File dir = ooChooser.showDialog(QueleaApp.get().getMainWindow());
                if (dir != null) {
                    ooPathTextField.setText(dir.getAbsolutePath());
                }
            }
        });
        GridPane.setConstraints(selectButton, 3, rows);
        getChildren().add(selectButton);
        rows++;

        Label usePPLabel = new Label(LabelGrabber.INSTANCE.getLabel("use.pp.label"));
        GridPane.setConstraints(usePPLabel, 1, rows);

        usePowerPointCheckBox = new CheckBox();
        usePowerPointCheckBox.selectedProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (usePowerPointCheckBox.isSelected()) {
                    if (Utils.isWindows()) {
                        ppPathTextField.setDisable(false);
                        ppSelectButton.setDisable(false);
                    }
                    ooPathTextField.setDisable(true);
                    selectButton.setDisable(true);
                    useOOCheckBox.setSelected(false);
                    props.setUseOO(false);
                } else {
                    ppPathTextField.setDisable(true);
                    ppSelectButton.setDisable(true);
                }
            }
        });
        usePPLabel.setLabelFor(usePowerPointCheckBox);
        GridPane.setConstraints(usePowerPointCheckBox, 2, rows);
        if (!Utils.isLinux()) {
            getChildren().add(usePPLabel);
            getChildren().add(usePowerPointCheckBox);
            rows++;
        }

        Label ppPathLabel = new Label(LabelGrabber.INSTANCE.getLabel("pp.path.label"));
        GridPane.setConstraints(ppPathLabel, 1, rows);

        ppPathTextField = new TextField();
        ppPathTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(ppPathTextField, Priority.ALWAYS);
        ppPathTextField.setEditable(false);
        ppPathLabel.setLabelFor(ppPathTextField);
        GridPane.setConstraints(ppPathTextField, 2, rows);

        powerPointChooser = new FileChooser();
        powerPointChooser.setInitialDirectory(new File("C:\\"));
        powerPointChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PowerPoint", "POWERPNT.EXE", "PPTVIEW.EXE"));
        ppSelectButton = new Button(LabelGrabber.INSTANCE.getLabel("browse"));
        ppSelectButton.setDisable(true);
        ppSelectButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                File dir = powerPointChooser.showOpenDialog(QueleaApp.get().getMainWindow());
                if (dir != null) {
                    ppPathTextField.setText(dir.getAbsolutePath());
                }
            }
        });
        GridPane.setConstraints(ppSelectButton, 3, rows);
        if (Utils.isWindows()) {
            getChildren().add(ppPathLabel);
            getChildren().add(ppPathTextField);
            getChildren().add(ppSelectButton);
            rows++;
        }

        readProperties();
    }

    /**
     * Reset the mechanism for determining if the user has changed the PowerPoint 
     * settings. Call before showing the options dialog.
     */
    public void resetPresentationChanged() {
        usePowerPoint = usePowerPointCheckBox.isSelected();
    }

    /**
     * Determine if the user has changed the presentation method since the last
     * call of resetLanguageChanged().
     * @return true if the settings have been changed, false otherwise.
     */
    public boolean hasPPChanged() {
        return usePowerPointCheckBox.isSelected() != usePowerPoint;
    }

    /**
     * @inheritDoc
     */
    @Override
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        useOOCheckBox.setSelected(props.getUseOO());
        ooPathTextField.setText(props.getOOPath());
        usePowerPointCheckBox.setSelected(props.getUsePP());
        ppPathTextField.setText(props.getPPPath());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        boolean useOO = getUseOOCheckBox().isSelected();
        props.setUseOO(useOO);
        String ooPath = getOOPathTextField().getText();
        props.setOOPath(ooPath);
        boolean usePP = getUsePPCheckBox().isSelected();
        props.setUsePP(usePP);
        String ppPath = getPPPathTextField().getText();
        props.setPPPath(ppPath);
    }
    
    /**
     * Get the "use openoffice" checkbox.
     * <p/>
     * @return the "use openoffice" checkbox.
     */
    public CheckBox getUseOOCheckBox() {
        return useOOCheckBox;
    }

    /**
     * Get the "openoffice path" text field.
     * <p/>
     * @return the "openoffice path" text field.
     */
    public TextField getOOPathTextField() {
        return ooPathTextField;
    }

    /**
     * Get the "use PowerPoint" checkbox.
     * <p/>
     * @return the "use PowerPoint" checkbox.
     */
    public CheckBox getUsePPCheckBox() {
        return usePowerPointCheckBox;
    }

    /**
     * Get the "PowerPoint path" text field.
     * <p/>
     * @return the "PowerPoint path" text field.
     */
    public TextField getPPPathTextField() {
        return ppPathTextField;
    }

}
