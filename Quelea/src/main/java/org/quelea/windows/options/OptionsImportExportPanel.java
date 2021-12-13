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
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

/**
 * The panel that shows the recording options
 * <p/>
 *
 * @author Arvid
 */
public class OptionsImportExportPanel {
    private HashMap<Field, ObservableValue> bindings;
    private StringProperty elevantoClientIdProperty;
    private IntegerProperty planningCenterPrevDaysProperty;

    /**
     * Create the options bible panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsImportExportPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        elevantoClientIdProperty = new SimpleStringProperty(QueleaProperties.get().getElevantoClientId());
        planningCenterPrevDaysProperty = new SimpleIntegerProperty(QueleaProperties.get().getPlanningCentrePrevDays());
    }

    public Category getImportExportTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("importexport.options.heading"), new ImageView(new Image("file:icons/arrows.png")),
                Group.of(LabelGrabber.INSTANCE.getLabel("elevanto.import.heading"),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("client.id"), elevantoClientIdProperty).customKey(elevantoClientIdKey)),
                Group.of(LabelGrabber.INSTANCE.getLabel("pco.import.heading"),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("pco.days.previous.setting"), planningCenterPrevDaysProperty, 0, 730).customKey(planningCentrePrevDaysKey))
        );
    }

}
