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

import com.dlsc.formsfx.model.structure.DoubleField;
import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleComboBoxControl;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.quelea.data.logo.LogoPosition;
import org.quelea.data.logo.LogoSettings;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.widgets.LogoPreview;
import org.quelea.windows.options.customprefs.FileSelectorPreference;
import org.quelea.windows.options.customprefs.PercentSliderControl;

import java.util.List;
import java.util.Objects;

import static org.quelea.services.utils.QueleaPropertyKeys.logoImageLocationKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoOffsetXKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoOffsetYKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoPositionKey;
import static org.quelea.services.utils.QueleaPropertyKeys.logoSizeKey;

/**
 * The logo settings in the options dialog: which logo, how big and where,
 * with a preview. The same settings can be changed from the logo button in
 * the live panel.
 * <p>
 * These settings don't go through the options dialog's normal saving
 * (PreferencesFx saves every setting, and on closing the window it first
 * undoes the changes it recorded, which would put back values from before
 * the logo was changed from the logo button). Instead the controls are
 * filled in from the properties each time the dialog opens
 * ({@link #reload()}), and only the logo settings the user actually changed
 * are written when OK is pressed ({@link #apply()}).
 *
 * @author Arvid
 */
public class LogoOptionsGroup {

    /**
     * Changes smaller than this (half a step of the offset sliders) are just
     * rounding by the slider controls, not something the user did.
     */
    private static final double TOLERANCE = 0.0005;

    private final Group group;
    private final List<Setting> settings;
    private final StringProperty imageProperty;
    private final StringField imageField;
    private final DoubleProperty sizeProperty;
    private final DoubleField sizeField;
    private final ObjectProperty<String> positionProperty;
    private final SingleSelectionField<String> positionField;
    private final DoubleProperty offsetXProperty;
    private final DoubleField offsetXField;
    private final DoubleProperty offsetYProperty;
    private final DoubleField offsetYField;
    private final LogoPreview preview;
    /**
     * The values the controls were filled in with when the dialog opened.
     */
    private Values loaded;

    /**
     * What the logo controls hold.
     */
    record Values(String image, LogoSettings settings) {

        boolean imageDiffers(Values other) {
            return !Objects.equals(image, other.image);
        }
    }

    LogoOptionsGroup() {
        LogoSettings current = QueleaProperties.get().getLogoSettings();

        imageProperty = new SimpleStringProperty(QueleaProperties.get().getLogoImageLocation());
        imageField = Field.ofStringType(imageProperty).render(
                new FileSelectorPreference(LabelGrabber.INSTANCE.getLabel("browse"),
                        QueleaProperties.get().getImageDir().getAbsoluteFile(), FileFilters.LOGO));

        sizeProperty = new SimpleDoubleProperty(current.size());
        sizeField = Field.ofDoubleType(sizeProperty).render(
                new PercentSliderControl(LogoSettings.MIN_SIZE, LogoSettings.MAX_SIZE, 2));

        ListProperty<String> positions = new SimpleListProperty<>(FXCollections.observableArrayList());
        for (LogoPosition position : LogoPosition.values()) {
            positions.add(position.getLabel());
        }
        positionProperty = new SimpleObjectProperty<>(current.position().getLabel());
        positionField = Field.ofSingleSelectionType(positions, positionProperty).render(new SimpleComboBoxControl<>());

        offsetXProperty = new SimpleDoubleProperty(current.offsetX());
        offsetXField = Field.ofDoubleType(offsetXProperty).render(
                new PercentSliderControl(LogoSettings.MIN_OFFSET, LogoSettings.MAX_OFFSET, 3));
        offsetYProperty = new SimpleDoubleProperty(current.offsetY());
        offsetYField = Field.ofDoubleType(offsetYProperty).render(
                new PercentSliderControl(LogoSettings.MIN_OFFSET, LogoSettings.MAX_OFFSET, 3));

        preview = new LogoPreview(320);
        Label previewLabel = new Label(LabelGrabber.INSTANCE.getLabel("logo.preview.label"));
        VBox previewBox = new VBox(6, previewLabel, preview);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        previewBox.setPadding(new Insets(4, 0, 4, 20)); // Lined up with the setting names

        settings = List.of(
                Setting.of(LabelGrabber.INSTANCE.getLabel("logo.image.label"), imageField, imageProperty)
                        .customKey(logoImageLocationKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("logo.size.label"), sizeField, sizeProperty)
                        .customKey(logoSizeKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("logo.position.label"), positionField, positionProperty)
                        .customKey(logoPositionKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("logo.offset.x.label"), offsetXField, offsetXProperty)
                        .customKey(logoOffsetXKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("logo.offset.y.label"), offsetYField, offsetYProperty)
                        .customKey(logoOffsetYKey)
        );
        Setting[] all = new Setting[settings.size() + 1];
        for (int i = 0; i < settings.size(); i++) {
            all[i] = settings.get(i);
        }
        all[settings.size()] = Setting.of(previewBox);
        group = Group.of(LabelGrabber.INSTANCE.getLabel("logo.options.heading"), all);

        // The preview follows what's in the controls, before OK is pressed
        ChangeListener<Object> previewUpdater = (obs, oldValue, newValue) -> updatePreview();
        imageField.valueProperty().addListener(previewUpdater);
        sizeField.valueProperty().addListener(previewUpdater);
        positionField.selectionProperty().addListener(previewUpdater);
        offsetXField.valueProperty().addListener(previewUpdater);
        offsetYField.valueProperty().addListener(previewUpdater);

        loaded = getValues();
        updatePreview();
    }

    Group getGroup() {
        return group;
    }

    /**
     * @return the settings in this group (without the preview), in the order
     * they're shown.
     */
    List<Setting> getSettings() {
        return settings;
    }

    /**
     * @return the preview of the logo.
     */
    LogoPreview getPreview() {
        return preview;
    }

    /**
     * Fill in the controls with the current logo settings, since they can also
     * be changed from the logo button while the options dialog is closed.
     * Called each time the options dialog is opened.
     */
    void reload() {
        LogoSettings current = QueleaProperties.get().getLogoSettings();
        imageProperty.set(QueleaProperties.get().getLogoImageLocation());
        sizeProperty.set(current.size());
        positionProperty.set(current.position().getLabel());
        offsetXProperty.set(current.offsetX());
        offsetYProperty.set(current.offsetY());
        // Read back what the controls made of it (sliders round a little)
        loaded = getValues();
        preview.updateAspectRatio();
        updatePreview();
    }

    /**
     * Save the logo settings the user changed in the dialog since it was
     * opened, and only those. Called when OK is pressed.
     *
     * @return true if anything was changed.
     */
    boolean apply() {
        Values now = getValues();
        QueleaProperties props = QueleaProperties.get();
        boolean changed = false;
        if (now.imageDiffers(loaded) && now.image() != null && !now.image().isEmpty()) {
            props.setLogoImage(now.image());
            changed = true;
        }
        LogoSettings before = loaded.settings();
        LogoSettings after = now.settings();
        LogoSettings saved = props.getLogoSettings();
        if (Math.abs(after.size() - before.size()) > TOLERANCE) {
            saved = saved.withSize(after.size());
        }
        if (after.position() != before.position()) {
            saved = saved.withPosition(after.position());
        }
        if (Math.abs(after.offsetX() - before.offsetX()) > TOLERANCE) {
            saved = saved.withOffsetX(after.offsetX());
        }
        if (Math.abs(after.offsetY() - before.offsetY()) > TOLERANCE) {
            saved = saved.withOffsetY(after.offsetY());
        }
        if (!saved.equals(props.getLogoSettings())) {
            props.setLogoSettings(saved);
            changed = true;
        }
        loaded = getValues();
        return changed;
    }

    /**
     * @return what the logo controls currently hold (including changes not
     * saved yet).
     */
    Values getValues() {
        String image = imageField.getValue() == null ? "" : imageField.getValue().trim();
        LogoPosition position = LogoPosition.fromLabel(positionField.getSelection());
        LogoSettings logo = new LogoSettings(
                sizeField.getValue() == null ? LogoSettings.DEFAULT.size() : sizeField.getValue(),
                position == null ? LogoPosition.CENTER : position,
                offsetXField.getValue() == null ? 0 : offsetXField.getValue(),
                offsetYField.getValue() == null ? 0 : offsetYField.getValue());
        return new Values(image, logo);
    }

    private void updatePreview() {
        Values values = getValues();
        preview.show(values.image(), values.settings());
    }

    /**
     * @param key a settings key.
     * @return true if it's one of the logo settings, which are loaded with
     * {@link #loadValue(String)} and saved by {@link #apply()} rather than
     * by the options dialog's storage.
     */
    static boolean isLogoKey(String key) {
        return switch (key) {
            case logoImageLocationKey, logoSizeKey, logoPositionKey, logoOffsetXKey, logoOffsetYKey -> true;
            default -> false;
        };
    }

    /**
     * Load a logo setting for the options dialog.
     *
     * @param key the settings key, see {@link #isLogoKey(String)}.
     * @return the value as the options dialog wants it: a path, a fraction,
     * or the (translated) name of a position.
     */
    static Object loadValue(String key) {
        LogoSettings logo = QueleaProperties.get().getLogoSettings();
        return switch (key) {
            case logoImageLocationKey -> QueleaProperties.get().getLogoImageLocation();
            case logoSizeKey -> logo.size();
            case logoPositionKey -> logo.position().getLabel();
            case logoOffsetXKey -> logo.offsetX();
            case logoOffsetYKey -> logo.offsetY();
            default -> throw new IllegalArgumentException("Not a logo setting: " + key);
        };
    }
}
