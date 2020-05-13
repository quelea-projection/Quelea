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

import com.dlsc.formsfx.model.structure.DoubleField;
import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.LanguageFile;
import org.quelea.services.languages.LanguageFileManager;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.options.customprefs.PercentSliderControl;

import java.util.Arrays;
import java.util.HashMap;

import static org.quelea.services.utils.QueleaPropertyKeys.*;
import static org.quelea.windows.options.PreferencesDialog.getPositionSelector;

/**
 * A panel where the general options in the program are set.
 * <p/>
 *
 * @author Arvid
 */
public class OptionsGeneralPanel {
    private BooleanProperty showSmallSongProperty;
    private DoubleProperty smallSongSizeSpinnerProperty;
    private DoubleField smallSongSizeControllerField;
    private BooleanProperty showSmallBibleProperty;
    private DoubleProperty smallBibleSizeSpinnerProperty;
    private DoubleField smallBibleSizeControllerField;
    private IntegerProperty thumbnailSizeProperty;
    private IntegerProperty maxFontSizeProperty;
    private IntegerProperty additionalSpacingProperty;
    private IntegerProperty maxCharsProperty;
    private ObservableList<LanguageFile> languageItemsList;
    private ObjectProperty<LanguageFile> languageSelectionProperty;
    private ObjectProperty<String> applicationThemeProperty;
    private ObservableList<String> applicationThemeList;
    private ObjectProperty<String> dbSongPreviewProperty;
    private ObservableList<String> dbSongPreviewList;
    private HashMap<Field, ObservableValue> bindings;

    /**
     * Create the options general panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsGeneralPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        showSmallSongProperty = new SimpleBooleanProperty(QueleaProperties.get().getSmallSongTextShow());
        smallSongSizeSpinnerProperty = new SimpleDoubleProperty(QueleaProperties.get().getSmallSongTextSize());
        smallSongSizeControllerField = Field.ofDoubleType(smallSongSizeSpinnerProperty).render(
                new PercentSliderControl(0.01, 0.5, 10));

        showSmallBibleProperty = new SimpleBooleanProperty(QueleaProperties.get().getSmallBibleTextShow());
        smallBibleSizeSpinnerProperty = new SimpleDoubleProperty(QueleaProperties.get().getSmallBibleTextSize());
        smallBibleSizeControllerField = Field.ofDoubleType(smallBibleSizeSpinnerProperty).render(
                new PercentSliderControl(0.01, 0.5, 10));

        thumbnailSizeProperty = new SimpleIntegerProperty(QueleaProperties.get().getThumbnailSize());
        maxFontSizeProperty = new SimpleIntegerProperty((int) QueleaProperties.get().getMaxFontSize());
        additionalSpacingProperty = new SimpleIntegerProperty((int) QueleaProperties.get().getAdditionalLineSpacing());
        maxCharsProperty = new SimpleIntegerProperty(QueleaProperties.get().getMaxChars());

        languageItemsList = FXCollections.observableArrayList(LanguageFileManager.INSTANCE.languageFiles());
        languageSelectionProperty = new SimpleObjectProperty<>(LanguageFileManager.INSTANCE.getCurrentFile());

        applicationThemeList = FXCollections.observableArrayList(Arrays.asList(
                LabelGrabber.INSTANCE.getLabel("default.theme.label"), LabelGrabber.INSTANCE.getLabel("dark.theme.label"))
        );
        applicationThemeProperty = new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel("default.theme.label"));

        dbSongPreviewList = FXCollections.observableArrayList(Arrays.asList(
                LabelGrabber.INSTANCE.getLabel("db.song.preview.label.control"), LabelGrabber.INSTANCE.getLabel("db.song.preview.label.databasepreview"),
                LabelGrabber.INSTANCE.getLabel("db.song.preview.label.previewpane"))
        );
        dbSongPreviewProperty = new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel("db.song.preview.label.control"));
    }

    public Category getGeneralTab() {
        bindings.put(smallSongSizeControllerField, showSmallSongProperty.not());
        bindings.put(smallBibleSizeControllerField, showSmallBibleProperty.not());

        return Category.of(LabelGrabber.INSTANCE.getLabel("general.options.heading"), new ImageView(new Image("file:icons/setting-ic-general.png")))
                .subCategories(
                        Category.of(LabelGrabber.INSTANCE.getLabel("interface.options.options"),
                                Group.of(LabelGrabber.INSTANCE.getLabel("general.interface.options"),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("interface.language.label"), languageItemsList, languageSelectionProperty).customKey(languageFileKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("interface.theme.label"), applicationThemeList, applicationThemeProperty).customKey(darkThemeKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("db.song.preview.label"), dbSongPreviewList, dbSongPreviewProperty).customKey(dbSongPreviewKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.video.library.panel"), new SimpleBooleanProperty(QueleaProperties.get().getDisplayVideoTab())).customKey(videoTabKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.extra.live.panel.toolbar.options.label"), new SimpleBooleanProperty(QueleaProperties.get().getShowExtraLivePanelToolbarOptions())).customKey(showExtraLivePanelToolbarOptionsKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("thumbnail.size.label"), thumbnailSizeProperty, 100, 1000).customKey(thumbnailSizeKey)
                                ),
                                Group.of(LabelGrabber.INSTANCE.getLabel("small.song.text.options"),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.small.song.text.label"), showSmallSongProperty).customKey(showSmallSongTextKey),
                                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.song.position.label"), false, QueleaProperties.get().getSmallSongTextPositionV(), showSmallSongProperty, bindings).customKey(smallSongTextVPositionKey),
                                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.song.position.label"), true, QueleaProperties.get().getSmallSongTextPositionH(), showSmallSongProperty, bindings).customKey(smallSongTextHPositionKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("small.song.size.label"), smallSongSizeControllerField, smallSongSizeSpinnerProperty).customKey(smallSongTextSizeKey)
                                ),
                                Group.of(LabelGrabber.INSTANCE.getLabel("small.bible.text.options"),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.small.bible.text.label"), showSmallBibleProperty).customKey(showSmallBibleTextKey),
                                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.bible.position.label"), false, QueleaProperties.get().getSmallBibleTextPositionV(), showSmallBibleProperty, bindings).customKey(smallBibleTextVPositionKey),
                                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.bible.position.label"), true, QueleaProperties.get().getSmallBibleTextPositionH(), showSmallBibleProperty, bindings).customKey(smallBibleTextHPositionKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("small.bible.size.label"), smallBibleSizeControllerField, smallBibleSizeSpinnerProperty).customKey(smallBibleTextSizeKey)
                                )
                        ),
                        Category.of(LabelGrabber.INSTANCE.getLabel("user.options.options"),
                                Group.of(LabelGrabber.INSTANCE.getLabel("general.user.options"),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("check.for.update.label"), new SimpleBooleanProperty(QueleaProperties.get().checkUpdate())).customKey(checkUpdateKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("1.monitor.warn.label"), new SimpleBooleanProperty(QueleaProperties.get().showSingleMonitorWarning())).customKey(singleMonitorWarningKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("auto.translate.label"), new SimpleBooleanProperty(QueleaProperties.get().getAutoTranslate())).customKey(autoTranslateKey)
                                ),
                                Group.of(LabelGrabber.INSTANCE.getLabel("theme.options"),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("allow.item.theme.override.global"), new SimpleBooleanProperty(QueleaProperties.get().getItemThemeOverride())).customKey(itemThemeOverrideKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("preview.on.image.change.label"), new SimpleBooleanProperty(QueleaProperties.get().getPreviewOnImageUpdate())).customKey(previewOnImageChangeKey)
                                ),
                                Group.of(LabelGrabber.INSTANCE.getLabel("schedule.options"),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("one.line.mode.label"), new SimpleBooleanProperty(QueleaProperties.get().getOneLineMode())).customKey(oneLineModeKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("autoplay.vid.label"), new SimpleBooleanProperty(QueleaProperties.get().getAutoPlayVideo())).customKey(autoplayVidKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("advance.on.live.label"), new SimpleBooleanProperty(QueleaProperties.get().getAdvanceOnLive())).customKey(advanceOnLiveKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("overflow.song.label"), new SimpleBooleanProperty(QueleaProperties.get().getSongOverflow())).customKey(songOverflowKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("copy.song.db.default"), new SimpleBooleanProperty(QueleaProperties.get().getDefaultSongDBUpdate())).customKey(defaultSongDbUpdateKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("clear.live.on.remove.schedule"), new SimpleBooleanProperty(QueleaProperties.get().getClearLiveOnRemove())).customKey(clearLiveOnRemoveKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("embed.media.in.schedule"), new SimpleBooleanProperty(QueleaProperties.get().getEmbedMediaInScheduleFile())).customKey(scheduleEmbedMediaKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("slide.transition.label"), new SimpleBooleanProperty(QueleaProperties.get().getUseSlideTransition())).customKey(useSlideTransitionKey)
                                )

                        ),
                        Category.of(LabelGrabber.INSTANCE.getLabel("text.options.options"),
                                Group.of(
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("capitalise.start.line.label"), new SimpleBooleanProperty(QueleaProperties.get().checkCapitalFirst())).customKey(capitalFirstKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("uniform.font.size.label"), new SimpleBooleanProperty(QueleaProperties.get().getUseUniformFontSize())).customKey(uniformFontSizeKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("max.font.size.label"), maxFontSizeProperty, 12, 300).customKey(maxFontSizeKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("additional.line.spacing.label"), additionalSpacingProperty, 0, 50).customKey(additionalLineSpacingKey),
                                        Setting.of(LabelGrabber.INSTANCE.getLabel("max.chars.line.label"), maxCharsProperty, 10, 160).customKey(maxCharsKey)
                                )
                        )
                ).expand();
    }

}
