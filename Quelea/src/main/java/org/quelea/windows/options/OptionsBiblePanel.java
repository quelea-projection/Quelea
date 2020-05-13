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

import java.util.ArrayList;
import java.util.HashMap;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.options.customprefs.DefaultBibleSelector;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

/**
 * The panel that shows the bible options
 * <p/>
 *
 * @author Arvid
 */
public class OptionsBiblePanel {
    private HashMap<Field, ObservableValue> bindings;
    private Setting defaultBibleSetting;
    private Setting showVerseNumSetting;
    private SimpleBooleanProperty showVerseNumProperty;
    private Setting splitVersesSetting;
    private SimpleBooleanProperty splitVersesProperty;
    private Setting useMaxVersesSetting;
    private SimpleBooleanProperty useMaxVersesProperty;
    private Setting maxVersesSetting;
    private SimpleIntegerProperty maxVersesProperty;

    /**
     * Create the options bible panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsBiblePanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        showVerseNumProperty = new SimpleBooleanProperty(QueleaProperties.get().getShowVerseNumbers());
        splitVersesProperty = new SimpleBooleanProperty(QueleaProperties.get().getBibleSplitVerses());
        useMaxVersesProperty = new SimpleBooleanProperty(QueleaProperties.get().getBibleUsingMaxChars());
        maxVersesProperty = new SimpleIntegerProperty(QueleaProperties.get().getMaxBibleVerses());

        ArrayList<String> bibles = new ArrayList<>();
        for (Bible b : BibleManager.get().getBibles()) {
            bibles.add(b.getName());
        }

        ObjectProperty<String> bibleSelection = new SimpleObjectProperty<>(QueleaProperties.get().getDefaultBible());
        SingleSelectionField<String> bibleField = Field.ofSingleSelectionType(bibles).render(new DefaultBibleSelector());
        defaultBibleSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("default.bible.label"), bibleField, bibleSelection).customKey(defaultBibleKey);
        bibleField.selectionProperty().bindBidirectional(bibleSelection);

        showVerseNumSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("show.verse.numbers"),
                showVerseNumProperty).customKey(showVerseNumbersKey);

        splitVersesSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("split.bible.verses"),
                splitVersesProperty).customKey(splitBibleVersesKey);

        useMaxVersesSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("max.items.per.slide").replace("%", LabelGrabber.INSTANCE.getLabel("verses")),
                useMaxVersesProperty).customKey(useMaxBibleCharsKey);

        maxVersesSetting = Setting.of("",
                maxVersesProperty).customKey(maxBibleVersesKey);
    }

    public Category getBiblesTab() {
        bindings.put(maxVersesSetting.getField(), useMaxVersesProperty.not());

        return Category.of(LabelGrabber.INSTANCE.getLabel("bible.options.heading"), new ImageView(new Image("file:icons/setting-ic-bible.png")),
                defaultBibleSetting,
                showVerseNumSetting,
                splitVersesSetting,
                useMaxVersesSetting,
                maxVersesSetting
        );
    }
}
