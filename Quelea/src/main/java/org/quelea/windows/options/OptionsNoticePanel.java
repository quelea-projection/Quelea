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
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;

import static org.quelea.services.utils.QueleaPropertyKeys.*;
import static org.quelea.services.utils.QueleaPropertyKeys.noticeFontSizeKey;
import static org.quelea.windows.options.PreferencesDialog.getColorPicker;
import static org.quelea.windows.options.PreferencesDialog.getPositionSelector;

/**
 * The panel that shows the notice options
 * <p/>
 *
 * @author Arvid
 */
public class OptionsNoticePanel {
    private HashMap<Field, ObservableValue> bindings;
    private DoubleProperty noticeSpeed;
    private DoubleProperty noticeSize;

    /**
     * Create the options bible panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsNoticePanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        noticeSpeed = new SimpleDoubleProperty(QueleaProperties.get().getNoticeSpeed());
        noticeSize = new SimpleDoubleProperty(QueleaProperties.get().getNoticeFontSize());
    }

    public Category getNoticesTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("notice.options.heading"), new ImageView(new Image("file:icons/setting-ic-notice.png")),
                getPositionSelector(LabelGrabber.INSTANCE.getLabel("notice.position.text"), false, QueleaProperties.get().getNoticePosition().getText(), null, bindings).customKey(noticePositionKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("notice.background.colour.text"), QueleaProperties.get().getNoticeBackgroundColour()).customKey(noticeBackgroundColourKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("notice.speed.text"), noticeSpeed, 2, 20, 1).customKey(noticeSpeedKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("notice.font.size"), noticeSize, 20, 100, 1).customKey(noticeFontSizeKey)
        );
    }
}
