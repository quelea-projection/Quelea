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
 * MERCHANTABILITYs or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import com.dlsc.preferencesfx.model.Setting;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.options.customprefs.*;

import java.util.HashMap;

public class PreferencesDialog extends Stage {
    private PreferencesFx preferencesFx;
    private final BorderPane mainPane;
    private final Button okButton;
    private final OptionsGeneralPanel generalPanel;
    private final OptionsDisplaySetupPanel displayPanel;
    private final OptionsNoticePanel noticePanel;
    private final OptionsPresentationPanel presentationPanel;
    private final OptionsBiblePanel biblePanel;
    private final OptionsStageViewPanel stageViewPanel;
    private final OptionsServerSettingsPanel optionsServerSettingsPanel;
    private final OptionsRecordingPanel recordingPanel;
    private HashMap<Field, ObservableValue> bindings = new HashMap<>();

    /**
     * Create a new preference dialog.
     *
     * @author Arvid
     */
    public PreferencesDialog(Class parent, boolean hasVLC) {
        setTitle(LabelGrabber.INSTANCE.getLabel("options.title"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(QueleaApp.get().getMainWindow());
        getIcons().add(new Image("file:icons/options.png", 16, 16, false, true));
        mainPane = new BorderPane();

        generalPanel = new OptionsGeneralPanel(bindings);
        displayPanel = new OptionsDisplaySetupPanel(bindings);
        stageViewPanel = new OptionsStageViewPanel(bindings);
        noticePanel = new OptionsNoticePanel(bindings);
        presentationPanel = new OptionsPresentationPanel(bindings);
        biblePanel = new OptionsBiblePanel(bindings);
        optionsServerSettingsPanel = new OptionsServerSettingsPanel(bindings);
        recordingPanel = new OptionsRecordingPanel(bindings, hasVLC);

        preferencesFx =
                PreferencesFx.of(new PreferenceStorageHandler(parent),
                        generalPanel.getGeneralTab(),
                        displayPanel.getDisplaySetupTab(),
                        stageViewPanel.getStageViewTab(),
                        noticePanel.getNoticesTab(),
                        presentationPanel.getPresentationsTab(),
                        biblePanel.getBiblesTab(),
                        optionsServerSettingsPanel.getServerTab(),
                        recordingPanel.getRecordingsTab()
                );

        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        BorderPane.setMargin(okButton, new Insets(5));
        okButton.setOnAction((ActionEvent t) -> {
            preferencesFx.saveSettings();
            if (displayPanel.isDisplayChange())
                updatePos();
            displayPanel.setDisplayChange(false);
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getThemeNode().refresh();
            hide();
        });
        BorderPane.setAlignment(okButton, Pos.CENTER);

        mainPane.setBottom(okButton);
        mainPane.setMinWidth(1005);
        mainPane.setMinHeight(600);
        mainPane.setCenter(preferencesFx.getView().getCenter());

        setScene(new Scene(mainPane));

        getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> callBeforeHiding());

        bindings.forEach(this::bind);
    }

    private void bind(Field field, ObservableValue<? extends Boolean> booleanProperty) {
        ((SimpleControl) field.getRenderer()).getNode().disableProperty().bind(booleanProperty);
    }

    static Setting getColorPicker(String label, Color color) {
        StringProperty property = new SimpleStringProperty(QueleaProperties.get().getStr(color));
        StringField field = Field.ofStringType(property).render(
                new ColorPickerPreference(color));
        return Setting.of(label, field, property);
    }

    static Setting getPositionSelector(String label, boolean horizontal, String selectedValue, BooleanProperty booleanBind, HashMap<Field, ObservableValue> bindings) {
        Setting setting;
        if (horizontal)
            setting = Setting.of(label, FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("left"), LabelGrabber.INSTANCE.getLabel("right")), new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel(selectedValue.toLowerCase())));
        else
            setting = Setting.of(label, FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("top.text.position"), LabelGrabber.INSTANCE.getLabel("bottom.text.position")), new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel(selectedValue.toLowerCase())));
        if (booleanBind != null)
            bindings.put(setting.getField(), booleanBind.not());
        return setting;
    }

    private void updatePos() {
        DisplayStage appWindow = QueleaApp.get().getProjectionWindow();
        DisplayStage stageWindow = QueleaApp.get().getStageWindow();
        if (appWindow == null) {
            appWindow = new DisplayStage(QueleaProperties.get().getProjectorCoords(), false);
        }
        final DisplayStage fiLyricWindow = appWindow; //Fudge for AIC
        Platform.runLater(() -> {
            fiLyricWindow.setAreaImmediate(QueleaProperties.get().getProjectorCoords());
            if (!QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getHide().isSelected()) {
                fiLyricWindow.show();
            }

            // non-custom positioned windows are fullscreen
            if (!QueleaProperties.get().isProjectorModeCoords()) {
                if (QueleaProperties.get().getProjectorScreen() == -1)
                    fiLyricWindow.hide();
                else
                    fiLyricWindow.setFullScreenAlwaysOnTop(true);
            }
        });
        if (stageWindow == null) {
            stageWindow = new DisplayStage(QueleaProperties.get().getStageCoords(), true);
        }
        final DisplayStage fiStageWindow = stageWindow; //Fudge for AIC
        Platform.runLater(() -> {
            fiStageWindow.setAreaImmediate(QueleaProperties.get().getStageCoords());
            if (!QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getHide().isSelected()) {
                fiStageWindow.show();
            }
            if (QueleaProperties.get().getStageScreen() == -1 && !QueleaProperties.get().isStageModeCoords())
                fiStageWindow.hide();
        });
    }

    private void callBeforeHiding() {
        preferencesFx.discardChanges();
    }

    public OptionsServerSettingsPanel getOptionsServerSettingsPanel() {
        return optionsServerSettingsPanel;
    }

    public OptionsDisplaySetupPanel getDisplaySetupPanel() {
        return displayPanel;
    }
}
