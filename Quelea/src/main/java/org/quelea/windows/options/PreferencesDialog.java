package org.quelea.windows.options;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.LanguageFile;
import org.quelea.services.languages.LanguageFileManager;

import java.util.ArrayList;

public class PreferencesDialog {
    private PreferencesFx preferencesFx;


    /**
     * Create a new preference dialog.
     *
     * @author Arvid
     */
    public PreferencesDialog(Class parent) {

        preferencesFx =
                PreferencesFx.of(parent, // Save class (will be used to reference saved values of Settings to)
                        getGeneralTab(),
                        getDisplaySetupTab(),
                        getStageViewTab(),
                        getNoticesTab(),
                        getPresentationsTab(),
                        getBiblesTab(),
                        getServerTab(),
                        getRecordingsTab()
                ).buttonsVisibility(false);
    }

    private Category getRecordingsTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("recordings.options.heading"));
    }

    private Category getServerTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("stage.options.heading"));
    }

    private Category getBiblesTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("bible.options.heading"));
    }

    private Category getPresentationsTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"));
    }

    private Category getNoticesTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("notice.options.heading"));
    }

    private Category getStageViewTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("stage.options.heading"));
    }

    private Category getDisplaySetupTab() {
        StringProperty stringProperty = new SimpleStringProperty("String");
        BooleanProperty booleanProperty = new SimpleBooleanProperty(true);
        return Category.of(LabelGrabber.INSTANCE.getLabel("display.options.heading"),
                Setting.of("Setting title 1", stringProperty), // creates a group automatically
                Setting.of("Setting title 2", booleanProperty) // which contains both settings
        );
    }

    private Category getGeneralTab() {
        BooleanProperty checkForUpdate = new SimpleBooleanProperty(true);
        BooleanProperty singleMonitorWarning = new SimpleBooleanProperty(true);
        BooleanProperty oneLineMode = new SimpleBooleanProperty(false);
        BooleanProperty autoPlayVideo = new SimpleBooleanProperty(false);
        BooleanProperty advanceOnLive = new SimpleBooleanProperty(false);
        BooleanProperty autoTranslate = new SimpleBooleanProperty(false);
        BooleanProperty clearLiveOnRemove = new SimpleBooleanProperty(false);
        BooleanProperty embedMediaInSchedule = new SimpleBooleanProperty(false);
        BooleanProperty itemThemeOverride = new SimpleBooleanProperty(false);
        BooleanProperty autoPlayVid = new SimpleBooleanProperty(false);
        BooleanProperty previewOnImageChange = new SimpleBooleanProperty(false);
        BooleanProperty uniformFontSize = new SimpleBooleanProperty(false);
        BooleanProperty defaultSongDBUpdate = new SimpleBooleanProperty(false);
        BooleanProperty showSmallSong = new SimpleBooleanProperty(false);

        BooleanProperty showSmallBible = new SimpleBooleanProperty(false);
        BooleanProperty overflowSong = new SimpleBooleanProperty(false);
        BooleanProperty showVideoPanel = new SimpleBooleanProperty(false);
        BooleanProperty showExtraLivePanelToolbarOptions = new SimpleBooleanProperty(false);
        BooleanProperty capitalFirst = new SimpleBooleanProperty(false);
        IntegerProperty thumbnailSize = new SimpleIntegerProperty(300);
        IntegerProperty maxFontSize = new SimpleIntegerProperty(100);
        IntegerProperty additionalSpacing = new SimpleIntegerProperty(10);
        IntegerProperty maxChars = new SimpleIntegerProperty(45);
        ArrayList<String> languages = new ArrayList<>();
        for (LanguageFile file : LanguageFileManager.INSTANCE.languageFiles()) {
            languages.add(file.getLanguageName());
        }
        // Combobox, Single Selection, with ObservableList
        ObservableList languageItems = FXCollections.observableArrayList(languages);
        ObservableList songInfoPosItems = FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("top"), LabelGrabber.INSTANCE.getLabel("bottom"));
        ObjectProperty languageSelection = new SimpleObjectProperty<>("English (GB)");
        ObjectProperty songPosSelection = new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel("top"));
        showSmallSong.addListener((observable, oldValue, newValue) -> {
            overflowSong.not();
        });

        return Category.of(LabelGrabber.INSTANCE.getLabel("general.options.heading"),
                Group.of(LabelGrabber.INSTANCE.getLabel("user.options.options"),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("interface.language.label"), languageItems, languageSelection),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("check.for.update.label"), checkForUpdate),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("1.monitor.warn.label"), singleMonitorWarning),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("one.line.mode.label"), oneLineMode),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("autoplay.vid.label"), autoPlayVideo),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("advance.on.live.label"), advanceOnLive),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("overflow.song.label"), overflowSong),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("preview.on.image.change.label"), previewOnImageChange),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.video.library.panel"), showVideoPanel),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("auto.translate.label"), autoTranslate),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("copy.song.db.default"), defaultSongDBUpdate),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("clear.live.on.remove.schedule"), clearLiveOnRemove),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("embed.media.in.schedule"), embedMediaInSchedule),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("allow.item.theme.override.global"), itemThemeOverride),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.small.song.text.label"), showSmallSong),
                        Setting.of("Small song text position", songInfoPosItems, songPosSelection),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("thumbnail.size.label"), thumbnailSize, 100, 500),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.extra.live.panel.toolbar.options.label"), showExtraLivePanelToolbarOptions)
                ),
                Group.of(LabelGrabber.INSTANCE.getLabel(LabelGrabber.INSTANCE.getLabel("text.options.options")),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("capitalise.start.line.label"), capitalFirst),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("uniform.font.size.label"), uniformFontSize),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("max.font.size.label"), maxFontSize, 12, 300),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("additional.line.spacing.label"), additionalSpacing, 0, 50),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("max.chars.line.label"), maxChars, 10, 160)
                )
        );
    }

    public PreferencesFx getPreferenceDialog() {
        return preferencesFx;
    }
}
