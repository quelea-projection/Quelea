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
import org.quelea.services.utils.Utils;
import org.quelea.windows.options.customprefs.DirectorySelectorPreference;

import static org.quelea.services.utils.QueleaPropertyKeys.*;
import static org.quelea.services.utils.QueleaPropertyKeys.ooPathKey;

/**
 * The panel that shows the presentation options
 * <p/>
 *
 * @author Arvid
 */
public class OptionsPresentationPanel {
    private HashMap<Field, ObservableValue> bindings;
    private BooleanProperty useOOProperty;
    private StringProperty directoryChooserOOProperty;
    private BooleanProperty usePPProperty;
    private StringProperty directoryChooserPPProperty;

    /**
     * Create the options presentation panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsPresentationPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        useOOProperty = new SimpleBooleanProperty(QueleaProperties.get().getUseOO());
        directoryChooserOOProperty = new SimpleStringProperty(QueleaProperties.get().getOOPath());

        usePPProperty = new SimpleBooleanProperty(QueleaProperties.get().getUsePP());
        directoryChooserPPProperty = new SimpleStringProperty(QueleaProperties.get().getPPPath());
    }

    public Category getPresentationsTab() {
        StringField directoryFieldOOField = Field.ofStringType(directoryChooserOOProperty).render(
                new DirectorySelectorPreference(LabelGrabber.INSTANCE.getLabel("browse"), null));
        bindings.put(directoryFieldOOField, useOOProperty.not());

        if (!Utils.isLinux()) {
            StringField directoryFieldPPField = Field.ofStringType(directoryChooserPPProperty).render(
                    new DirectorySelectorPreference(LabelGrabber.INSTANCE.getLabel("browse"), null));
            bindings.put(directoryFieldPPField, usePPProperty.not());

            return Category.of(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"), new ImageView(new Image(QueleaProperties.get().getUseDarkTheme() ? "file:icons/setting-ic-presentation-light.png" : "file:icons/setting-ic-presentation.png")),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.oo.label"), useOOProperty).customKey(useOoKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("oo.path"), directoryFieldOOField, directoryChooserOOProperty).customKey(ooPathKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.pp.label"), usePPProperty).customKey(usePpKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("pp.path"), directoryFieldPPField, directoryChooserPPProperty).customKey(ppPathKey)
            );
        } else
            return Category.of(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"), new ImageView(new Image("file:icons/setting-ic-presentation.png")),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.oo.label"), useOOProperty).customKey(useOoKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("oo.path"), directoryFieldOOField, directoryChooserOOProperty).customKey(ooPathKey)
            );
    }

}
