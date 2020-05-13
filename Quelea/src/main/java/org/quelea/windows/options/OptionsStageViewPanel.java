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

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.displayable.TextAlignment;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import static org.quelea.services.utils.QueleaPropertyKeys.*;
import static org.quelea.services.utils.QueleaPropertyKeys.use24hClockKey;
import static org.quelea.windows.options.PreferencesDialog.getColorPicker;

/**
 * The panel that shows the stage view options.
 *
 * @author Arvid
 */
public class OptionsStageViewPanel {
    private HashMap<Field, ObservableValue> bindings;
    private ObservableList<String> lineAlignmentList;
    private ObjectProperty<String> alignmentSelectionProperty;

    private ObservableList<String> fontsList;
    private ObjectProperty<String> fontSelectionProperty;

    /**
     * Create the stage view options panel.
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsStageViewPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        ArrayList<String> textAlignment = new ArrayList<>();
        for (TextAlignment alignment : TextAlignment.values()) {
            textAlignment.add(alignment.toFriendlyString());
        }
        lineAlignmentList = FXCollections.observableArrayList(textAlignment);
        alignmentSelectionProperty = new SimpleObjectProperty<>(QueleaProperties.get().getStageTextAlignment());

        fontsList = FXCollections.observableArrayList(Utils.getAllFonts());
        fontSelectionProperty = new SimpleObjectProperty<>(QueleaProperties.get().getStageTextFont());
    }

    public Category getStageViewTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("stage.options.heading"), new ImageView(new Image("file:icons/setting-ic-stageview.png")),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.show.chords"), new SimpleBooleanProperty(QueleaProperties.get().getShowChords())).customKey(stageShowChordsKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.line.alignment"), lineAlignmentList, alignmentSelectionProperty).customKey(stageTextAlignmentKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.font.selection"), fontsList, fontSelectionProperty).customKey(stageFontKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.background.colour"), QueleaProperties.get().getStageBackgroundColor()).customKey(stageBackgroundColorKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.lyrics.colour"), QueleaProperties.get().getStageLyricsColor()).customKey(stageLyricsColorKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.chord.colour"), QueleaProperties.get().getStageChordColor()).customKey(stageChordColorKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("clear.stage.view"), new SimpleBooleanProperty(QueleaProperties.get().getClearStageWithMain())).customKey(clearStageviewWithMainKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("use.24h.clock"), new SimpleBooleanProperty(QueleaProperties.get().getUse24HourClock())).customKey(use24hClockKey)
        );
    }


}
