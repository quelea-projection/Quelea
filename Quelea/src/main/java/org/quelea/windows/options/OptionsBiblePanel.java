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
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.options.customprefs.BibleVersionsList;

import static org.quelea.services.utils.QueleaPropertyKeys.*;
import org.quelea.windows.style.QueleaIcon;

/**
 * The panel that shows the bible options
 * <p/>
 *
 * @author Arvid
 */
public class OptionsBiblePanel {
    private HashMap<Field<?>, ObservableValue<? extends Boolean>> bindings;
    private Setting bibleVersionsSetting;
    private Setting showVerseNumSetting;
    private SimpleBooleanProperty showVerseNumProperty;
    private Setting splitVersesSetting;
    private SimpleBooleanProperty splitVersesProperty;
    private Setting useMaxVersesSetting;
    private SimpleBooleanProperty useMaxVersesProperty;
    private Setting maxVersesSetting;
    private SimpleIntegerProperty maxVersesProperty;
    private Setting versionSeparatorSetting;

    /**
     * Create the options bible panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsBiblePanel(HashMap<Field<?>, ObservableValue<? extends Boolean>> bindings) {
        this.bindings = bindings;
        showVerseNumProperty = new SimpleBooleanProperty(QueleaProperties.get().getShowVerseNumbers());
        splitVersesProperty = new SimpleBooleanProperty(QueleaProperties.get().getBibleSplitVerses());
        useMaxVersesProperty = new SimpleBooleanProperty(QueleaProperties.get().getBibleUsingMaxChars());
        maxVersesProperty = new SimpleIntegerProperty(QueleaProperties.get().getMaxBibleVerses());

        // The default bible and the versions to show new passages in, in one list
        StringProperty bibleVersionsProperty = new SimpleStringProperty(String.join("\n", QueleaProperties.get().getBibleVersions()));
        StringField bibleVersionsField = Field.ofStringType(bibleVersionsProperty).render(new BibleVersionsList());
        bibleVersionsSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("bible.versions.label"),
                bibleVersionsField, bibleVersionsProperty).customKey(bibleVersionsKey);

        showVerseNumSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("show.verse.numbers"),
                showVerseNumProperty).customKey(showVerseNumbersKey);

        splitVersesSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("split.bible.verses"),
                splitVersesProperty).customKey(splitBibleVersesKey);

        useMaxVersesSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("max.items.per.slide").replace("%", LabelGrabber.INSTANCE.getLabel("verses")),
                useMaxVersesProperty).customKey(useMaxBibleCharsKey);

        maxVersesSetting = Setting.of("",
                maxVersesProperty).customKey(maxBibleVersesKey);


        versionSeparatorSetting = Setting.of(LabelGrabber.INSTANCE.getLabel("bible.version.separator.label"),
                new SimpleBooleanProperty(QueleaProperties.get().getBibleVersionSeparator())).customKey(bibleVersionSeparatorKey);
    }

    public Category getBiblesTab() {
        bindings.put((Field<?>) maxVersesSetting.getElement(), useMaxVersesProperty.not());

        return Category.of(LabelGrabber.INSTANCE.getLabel("bible.options.heading"), QueleaIcon.BIBLE.create(),
                bibleVersionsSetting,
                showVerseNumSetting,
                splitVersesSetting,
                useMaxVersesSetting,
                maxVersesSetting,
                versionSeparatorSetting
        );
    }
}
