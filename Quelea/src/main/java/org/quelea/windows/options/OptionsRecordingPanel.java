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

import java.util.HashMap;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.options.customprefs.DirectorySelectorPreference;

import static org.quelea.services.utils.QueleaPropertyKeys.convertMp3Key;
import static org.quelea.services.utils.QueleaPropertyKeys.recPathKey;

/**
 * The panel that shows the recording options
 * <p/>
 *
 * @author Arvid
 */
public class OptionsRecordingPanel {
    private HashMap<Field, ObservableValue> bindings;
    private StringProperty recordingsDirectoryChooserProperty;
    private StringField recordingsDirectoryField;
    private BooleanProperty useConvertProperty;
    private Setting useConvertSetting;
    private boolean hasVLC;

    /**
     * Create the options bible panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     * @param hasVLC   true if VLC is installed
     */
    OptionsRecordingPanel(HashMap<Field, ObservableValue> bindings, boolean hasVLC) {
        this.bindings = bindings;
        this.hasVLC = hasVLC;
        recordingsDirectoryChooserProperty = new SimpleStringProperty(QueleaProperties.get().getRecordingsPath());
        recordingsDirectoryField = Field.ofStringType(recordingsDirectoryChooserProperty).render(
                new DirectorySelectorPreference(LabelGrabber.INSTANCE.getLabel("browse"), null));

        useConvertProperty = new SimpleBooleanProperty(QueleaProperties.get().getConvertRecordings());
        useConvertSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("convert.mp3"), useConvertProperty).customKey(convertMp3Key);
    }

    public Category getRecordingsTab() {
        bindings.put(useConvertSetting.getField(), new SimpleBooleanProperty(hasVLC));

        return Category.of(LabelGrabber.INSTANCE.getLabel("recordings.options.heading"), new ImageView(new Image("file:icons/setting-ic-recording.png")),
                Setting.of(LabelGrabber.INSTANCE.getLabel("recordings.path"), recordingsDirectoryField, recordingsDirectoryChooserProperty).customKey(recPathKey),
                useConvertSetting
        );
    }

}
