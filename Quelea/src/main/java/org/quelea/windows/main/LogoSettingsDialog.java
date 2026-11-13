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
package org.quelea.windows.main;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.quelea.data.logo.LogoPosition;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.widgets.LogoPreview;
import org.quelea.windows.style.StyleManager;

import java.io.File;

/**
 * A small window, opened from the logo button, for choosing the logo and how
 * big it is and where it goes. Changes are shown on the logo straight away,
 * so it can be lined up while looking at the projector. The same settings are
 * in the options dialog, on the display tab.
 *
 * @author Arvid
 */
public class LogoSettingsDialog extends Stage {

    private final Runnable onChange;
    private final LogoPreview preview;
    private final TextField imageField;
    private final Slider sizeSlider;
    private final Label sizeValue;
    private final ComboBox<LogoPosition> positionBox;
    private final Slider offsetXSlider;
    private final Label offsetXValue;
    private final Slider offsetYSlider;
    private final Label offsetYValue;
    /**
     * True while the controls are being filled in from the properties, so that
     * doesn't count as the user changing them.
     */
    private boolean updating;
    /**
     * True if settings were changed in memory but not saved yet (while a
     * slider was being dragged).
     */
    private boolean unsaved;

    /**
     * Create the logo settings dialog.
     *
     * @param owner    the window the dialog belongs to.
     * @param onChange called whenever a setting changes, to update the logo.
     */
    public LogoSettingsDialog(Window owner, Runnable onChange) {
        this.onChange = onChange;
        initOwner(owner);
        // Not modal, so the logo button can be used while it's open
        initModality(Modality.NONE);
        setTitle(LabelGrabber.INSTANCE.getLabel("logo.settings.title"));
        Utils.addIconsToStage(this);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 18, 16, 18));
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setHalignment(HPos.RIGHT);
        ColumnConstraints controlColumn = new ColumnConstraints();
        controlColumn.setHgrow(Priority.ALWAYS);
        controlColumn.setMinWidth(260);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setMinWidth(55);
        grid.getColumnConstraints().addAll(labelColumn, controlColumn, valueColumn);

        imageField = new TextField();
        imageField.setEditable(false);
        imageField.setId("logo-image");
        Button browseButton = new Button(LabelGrabber.INSTANCE.getLabel("browse"));
        browseButton.setOnAction(e -> chooseImage());
        HBox imageBox = new HBox(5, imageField, browseButton);
        HBox.setHgrow(imageField, Priority.ALWAYS);
        preview = new LogoPreview(320);
        GridPane.setHalignment(preview, HPos.CENTER);
        grid.add(preview, 0, 0, 3, 1);

        addRow(grid, 1, "logo.image.label", imageBox, null);

        sizeSlider = new Slider(LogoSettings.MIN_SIZE * 100, LogoSettings.MAX_SIZE * 100, 100);
        sizeSlider.setId("logo-size");
        sizeValue = new Label();
        addRow(grid, 2, "logo.size.label", sizeSlider, sizeValue);

        positionBox = new ComboBox<>();
        positionBox.getItems().addAll(LogoPosition.values());
        positionBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(LogoPosition position) {
                return position == null ? "" : position.getLabel();
            }

            @Override
            public LogoPosition fromString(String label) {
                return LogoPosition.fromLabel(label);
            }
        });
        positionBox.setMaxWidth(Double.MAX_VALUE);
        positionBox.setId("logo-position");
        positionBox.valueProperty().addListener((obs, oldPos, newPos) -> settingsChanged(true));
        addRow(grid, 3, "logo.position.label", positionBox, null);

        offsetXSlider = new Slider(LogoSettings.MIN_OFFSET * 100, LogoSettings.MAX_OFFSET * 100, 0);
        offsetXSlider.setId("logo-offset-x");
        offsetXValue = new Label();
        addRow(grid, 4, "logo.offset.x.label", offsetXSlider, offsetXValue);

        offsetYSlider = new Slider(LogoSettings.MIN_OFFSET * 100, LogoSettings.MAX_OFFSET * 100, 0);
        offsetYSlider.setId("logo-offset-y");
        offsetYValue = new Label();
        addRow(grid, 5, "logo.offset.y.label", offsetYSlider, offsetYValue);

        for (Slider slider : new Slider[]{sizeSlider, offsetXSlider, offsetYSlider}) {
            slider.setBlockIncrement(0.5);
            // Show the change live while dragging, but only save once let go
            slider.valueProperty().addListener((obs, oldVal, newVal) -> settingsChanged(!slider.isValueChanging()));
            slider.valueChangingProperty().addListener((obs, wasChanging, changing) -> {
                if (!changing) {
                    settingsChanged(true);
                }
            });
        }

        Button resetButton = new Button(LabelGrabber.INSTANCE.getLabel("logo.reset.button"));
        resetButton.setId("logo-reset");
        resetButton.setOnAction(e -> {
            show(LogoSettings.DEFAULT);
            settingsChanged(true);
        });
        Button closeButton = new Button(LabelGrabber.INSTANCE.getLabel("close.button"));
        closeButton.setOnAction(e -> hide());
        closeButton.setDefaultButton(true);
        closeButton.setCancelButton(true);
        HBox buttons = new HBox(8, resetButton, closeButton);

        BorderPane root = new BorderPane(grid);
        root.setBottom(buttons);
        StyleManager.markPrimaryButton(closeButton);

        setOnShowing(e -> reload());
        setOnHidden(e -> {
            if (unsaved) {
                settingsChanged(true);
            }
        });

        Scene scene = new Scene(root);
        StyleManager.apply(scene);
        setScene(scene);
        reload();
    }

    private static void addRow(GridPane grid, int row, String labelKey, javafx.scene.Node control, Label value) {
        Label label = new Label(LabelGrabber.INSTANCE.getLabel(labelKey));
        label.setLabelFor(control);
        grid.add(label, 0, row);
        if (value == null) {
            grid.add(control, 1, row, 2, 1);
        } else {
            grid.add(control, 1, row);
            grid.add(value, 2, row);
        }
    }

    /**
     * Fill in the controls from the current settings (they may have been
     * changed in the options dialog).
     */
    public final void reload() {
        imageField.setText(QueleaProperties.get().getLogoImageLocation());
        show(QueleaProperties.get().getLogoSettings());
        unsaved = false;
        preview.updateAspectRatio();
        updatePreview();
    }

    private void updatePreview() {
        preview.show(imageField.getText(), getSettings());
    }

    private void show(LogoSettings settings) {
        updating = true;
        try {
            sizeSlider.setValue(settings.size() * 100);
            positionBox.setValue(settings.position());
            offsetXSlider.setValue(settings.offsetX() * 100);
            offsetYSlider.setValue(settings.offsetY() * 100);
            updateValueLabels();
        } finally {
            updating = false;
        }
    }

    private void updateValueLabels() {
        sizeValue.setText(String.format("%.0f %%", sizeSlider.getValue()));
        offsetXValue.setText(String.format("%+.1f %%", offsetXSlider.getValue()));
        offsetYValue.setText(String.format("%+.1f %%", offsetYSlider.getValue()));
    }

    private LogoSettings getSettings() {
        return new LogoSettings(sizeSlider.getValue() / 100, positionBox.getValue(),
                offsetXSlider.getValue() / 100, offsetYSlider.getValue() / 100);
    }

    private void settingsChanged(boolean save) {
        if (updating) {
            return;
        }
        updateValueLabels();
        QueleaProperties.get().setLogoSettings(getSettings(), save);
        unsaved = !save;
        updatePreview();
        onChange.run();
    }

    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(FileFilters.LOGO);
        File current = new File(QueleaProperties.get().getLogoImageLocation()).getAbsoluteFile();
        if (current.getParentFile() != null && current.getParentFile().isDirectory()) {
            chooser.setInitialDirectory(current.getParentFile());
        } else {
            chooser.setInitialDirectory(QueleaProperties.get().getImageDir().getAbsoluteFile());
        }
        File file = chooser.showOpenDialog(this);
        if (file != null) {
            QueleaProperties.get().setLastDirectory(file.getParentFile());
            QueleaProperties.get().setLogoImage(file.getAbsolutePath());
            imageField.setText(file.getAbsolutePath());
            updatePreview();
            onChange.run();
        }
    }
}
