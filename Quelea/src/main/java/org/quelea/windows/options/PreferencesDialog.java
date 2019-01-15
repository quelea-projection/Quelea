package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.formsfx.view.controls.*;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.displayable.TextAlignment;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.LanguageFile;
import org.quelea.services.languages.LanguageFileManager;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.options.customprefs.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PreferencesDialog {
    private PreferencesFx preferencesFx;


    /**
     * Create a new preference dialog.
     *
     * @author Arvid
     */
    public PreferencesDialog(Class parent) {
        preferencesFx =
                PreferencesFx.of(new CustomStorageHandler(parent),
                        getGeneralTab(),
                        getDisplaySetupTab(),
                        getStageViewTab(),
                        getNoticesTab(),
                        getPresentationsTab(),
                        getBiblesTab(),
                        getServerTab(),
                        getRecordingsTab()


                );
        bindings.forEach(this::bind);
    }

    private Category getGeneralTab() {
        BooleanProperty checkForUpdate = new SimpleBooleanProperty(QueleaProperties.get().checkUpdate());
        BooleanProperty singleMonitorWarning = new SimpleBooleanProperty(QueleaProperties.get().showSingleMonitorWarning());
        BooleanProperty oneLineMode = new SimpleBooleanProperty(QueleaProperties.get().getOneLineMode());
        BooleanProperty autoPlayVideo = new SimpleBooleanProperty(QueleaProperties.get().getAutoPlayVideo());
        BooleanProperty advanceOnLive = new SimpleBooleanProperty(QueleaProperties.get().getAdvanceOnLive());
        BooleanProperty autoTranslate = new SimpleBooleanProperty(QueleaProperties.get().getAutoTranslate());
        BooleanProperty clearLiveOnRemove = new SimpleBooleanProperty(QueleaProperties.get().getClearLiveOnRemove());
        BooleanProperty embedMediaInSchedule = new SimpleBooleanProperty(QueleaProperties.get().getEmbedMediaInScheduleFile());
        BooleanProperty itemThemeOverride = new SimpleBooleanProperty(QueleaProperties.get().getItemThemeOverride());
        BooleanProperty previewOnImageChange = new SimpleBooleanProperty(QueleaProperties.get().getPreviewOnImageUpdate());
        BooleanProperty uniformFontSize = new SimpleBooleanProperty(QueleaProperties.get().getUseUniformFontSize());
        BooleanProperty defaultSongDBUpdate = new SimpleBooleanProperty(QueleaProperties.get().getDefaultSongDBUpdate());
        BooleanProperty showSmallSong = new SimpleBooleanProperty(QueleaProperties.get().getSmallSongTextShow());
        IntegerProperty smallSongSizeSpinner = new SimpleIntegerProperty((int) (QueleaProperties.get().getSmallSongTextSize() * 100));
        IntegerField smallSongSizeController = Field.ofIntegerType(smallSongSizeSpinner).render(
                new PercentIntegerSliderControl(0.1, 0.5));
        BooleanProperty showSmallBible = new SimpleBooleanProperty(QueleaProperties.get().getSmallBibleTextShow());
        IntegerProperty smallBibleSizeSpinner = new SimpleIntegerProperty((int) (100 * QueleaProperties.get().getSmallBibleTextSize()));
        IntegerField smallBibleSizeController = Field.ofIntegerType(smallBibleSizeSpinner).render(
                new PercentIntegerSliderControl(0.1, 0.5));

        BooleanProperty overflowSong = new SimpleBooleanProperty(QueleaProperties.get().getSongOverflow());
        BooleanProperty showVideoPanel = new SimpleBooleanProperty(QueleaProperties.get().getDisplayVideoTab());
        BooleanProperty showExtraLivePanelToolbarOptions = new SimpleBooleanProperty(QueleaProperties.get().getShowExtraLivePanelToolbarOptions());
        BooleanProperty capitalFirst = new SimpleBooleanProperty(QueleaProperties.get().checkCapitalFirst());
        IntegerProperty thumbnailSize = new SimpleIntegerProperty(QueleaProperties.get().getThumbnailSize());
        IntegerProperty maxFontSize = new SimpleIntegerProperty((int) QueleaProperties.get().getMaxFontSize());
        IntegerProperty additionalSpacing = new SimpleIntegerProperty((int) QueleaProperties.get().getAdditionalLineSpacing());
        IntegerProperty maxChars = new SimpleIntegerProperty(QueleaProperties.get().getMaxChars());

        ObservableList<LanguageFile> languageItems = FXCollections.observableArrayList(LanguageFileManager.INSTANCE.languageFiles());
        ObjectProperty<LanguageFile> languageSelection = new SimpleObjectProperty<>(LanguageFileManager.INSTANCE.getCurrentFile());

        bindings.put(smallSongSizeController, showSmallSong.not());
        bindings.put(smallBibleSizeController, showSmallBible.not());

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
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.song.position.label"), false, QueleaProperties.get().getSmallSongTextPositionV(), showSmallSong),
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.song.position.label"), true, QueleaProperties.get().getSmallSongTextPositionH(), showSmallSong),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("small.song.size.label"), smallSongSizeController, smallSongSizeSpinner),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.small.bible.text.label"), showSmallBible),
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.bible.position.label"), false, QueleaProperties.get().getSmallBibleTextPositionV(), showSmallBible),
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.bible.position.label"), true, QueleaProperties.get().getSmallBibleTextPositionH(), showSmallBible),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("small.bible.size.label"), smallBibleSizeController, smallBibleSizeSpinner),
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

    private Category getDisplaySetupTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("display.options.heading"), //, new ImageView(new Image("file:icons/monitorsettingsicon.png")),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("control.screen.label"), "file:icons/monitor.png", false),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("projector.screen.label"), "file:icons/projector.png", true),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("stage.screen.label"), "file:icons/stage.png", true)
        );
    }

    private Category getStageViewTab() {
        BooleanProperty showChordsBooleanProperty = new SimpleBooleanProperty(QueleaProperties.get().getShowChords());
        BooleanProperty clearWithMainBox = new SimpleBooleanProperty(QueleaProperties.get().getClearStageWithMain());
        BooleanProperty use24HBooleanProperty = new SimpleBooleanProperty(QueleaProperties.get().getUse24HourClock());

        ArrayList<String> textAlignment = new ArrayList<>();
        for (TextAlignment alignment : TextAlignment.values()) {
            textAlignment.add(alignment.toFriendlyString());
        }
        ObservableList<String> lineAlignment = FXCollections.observableArrayList(textAlignment);
        ObjectProperty<String> alignmentSelection = new SimpleObjectProperty<>(QueleaProperties.get().getStageTextAlignment());

        ObservableList<String> fonts = FXCollections.observableArrayList(Utils.getAllFonts());
        ObjectProperty<String> fontSelection = new SimpleObjectProperty<>(QueleaProperties.get().getStageTextFont());

        return Category.of(LabelGrabber.INSTANCE.getLabel("stage.options.heading"),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.show.chords"), showChordsBooleanProperty),//.setCustomKey(QueleaPropertyKeys.stageShowChordsKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.line.alignment"), lineAlignment, alignmentSelection),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.font.selection"), fonts, fontSelection),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.background.colour"), QueleaProperties.get().getStageBackgroundColor()),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.lyrics.colour"), QueleaProperties.get().getStageLyricsColor()),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.chord.colour"), QueleaProperties.get().getStageChordColor()),
                Setting.of(LabelGrabber.INSTANCE.getLabel("clear.stage.view"), clearWithMainBox),
                Setting.of(LabelGrabber.INSTANCE.getLabel("use.24h.clock"), use24HBooleanProperty)
        );
    }

    private Category getNoticesTab() {
        DoubleProperty noticeSpeed = new SimpleDoubleProperty(QueleaProperties.get().getNoticeSpeed());
        DoubleField noticeSpeedField = Field.ofDoubleType(noticeSpeed).render(new DoubleNoLabelPreference(2, 20, 10));
        DoubleProperty noticeSize = new SimpleDoubleProperty(QueleaProperties.get().getNoticeFontSize());
        DoubleField noticeSizeField = Field.ofDoubleType(noticeSize).render(new DoubleNoLabelPreference(20, 100, 10));

        return Category.of(LabelGrabber.INSTANCE.getLabel("notice.options.heading"),
                getPositionSelector(LabelGrabber.INSTANCE.getLabel("notice.position.text"), false, QueleaProperties.get().getNoticePosition().getText(), null),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("notice.background.colour.text"), QueleaProperties.get().getNoticeBackgroundColour()),
                Setting.of(LabelGrabber.INSTANCE.getLabel("notice.speed.text"), noticeSpeedField, noticeSpeed),
                Setting.of(LabelGrabber.INSTANCE.getLabel("notice.font.size"), noticeSizeField, noticeSize)
        );
    }

    private Category getPresentationsTab() {
        BooleanProperty useOO = new SimpleBooleanProperty(QueleaProperties.get().getUseOO());
        StringProperty directoryChooserOO = new SimpleStringProperty(QueleaProperties.get().getOOPath());
        StringField directoryFieldOO = Field.ofStringType(directoryChooserOO).render(
                new DirectorySelectorPreference(QueleaApp.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("browse"), null));
        bindings.put(directoryFieldOO, useOO.not());

        if (!Utils.isLinux()) {
            BooleanProperty usePP = new SimpleBooleanProperty(QueleaProperties.get().getUsePP());
            StringProperty directoryChooserPP = new SimpleStringProperty(QueleaProperties.get().getPPPath());
            StringField directoryFieldPP = Field.ofStringType(directoryChooserPP).render(
                    new DirectorySelectorPreference(QueleaApp.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("browse"), null));
            bindings.put(directoryFieldPP, usePP.not());

            return Category.of(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.oo.label"), useOO),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("oo.path"), directoryFieldOO, directoryChooserOO),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.pp.label"), usePP),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("pp.path"), directoryFieldPP, directoryChooserPP)
            );
        } else
            return Category.of(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.oo.label"), useOO),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("oo.path"), directoryFieldOO, directoryChooserOO));
    }

    private Category getBiblesTab() {
        ArrayList<String> bibles = new ArrayList<>();
        for (Bible b : BibleManager.get().getBibles()) {
            bibles.add(b.getName());
        }
        ObjectProperty<String> bibleSelection = new SimpleObjectProperty<>(QueleaProperties.get().getDefaultBible());
        SingleSelectionField<String> bibleField = Field.ofSingleSelectionType(bibles).render(new DefaultBibleSelector());
        bibleField.selectionProperty().bindBidirectional(bibleSelection);

        BooleanProperty showVerseNum = new SimpleBooleanProperty(QueleaProperties.get().getShowVerseNumbers());
        BooleanProperty splitBibleVerse = new SimpleBooleanProperty(QueleaProperties.get().getBibleSplitVerses());
        BooleanProperty useMaxVerses = new SimpleBooleanProperty(QueleaProperties.get().getBibleUsingMaxChars());

        IntegerProperty maxVerse = new SimpleIntegerProperty(QueleaProperties.get().getMaxBibleVerses());
        Field maxVerseField = Field.ofIntegerType(QueleaProperties.get().getMaxBibleVerses()).render(new SimpleIntegerControl());
        bindings.put(maxVerseField, useMaxVerses.not());

        return Category.of(LabelGrabber.INSTANCE.getLabel("bible.options.heading"),
                Setting.of(LabelGrabber.INSTANCE.getLabel("default.bible.label"), bibleField, bibleSelection),
                Setting.of(LabelGrabber.INSTANCE.getLabel("show.verse.numbers"), showVerseNum),
                Setting.of(LabelGrabber.INSTANCE.getLabel("split.bible.verses"), splitBibleVerse),
                Setting.of(LabelGrabber.INSTANCE.getLabel("max.items.per.slide").replace("%", LabelGrabber.INSTANCE.getLabel("verses")), useMaxVerses),
                Setting.of("", maxVerseField, maxVerse)
        );
    }

    private Category getServerTab() {
        BooleanProperty useMobileLyrics = new SimpleBooleanProperty(QueleaProperties.get().getUseMobLyrics());
        StringProperty lyricsPortNumber = new SimpleStringProperty(String.valueOf(QueleaProperties.get().getMobLyricsPort()));
        StringField mobileLyricsField = Field.ofStringType(lyricsPortNumber).render(new MobileServerPreference(true));
        bindings.put(mobileLyricsField, useMobileLyrics.not());

        BooleanProperty useMobileRemote = new SimpleBooleanProperty(QueleaProperties.get().getUseRemoteControl());
        StringProperty remotePortNumber = new SimpleStringProperty(String.valueOf(QueleaProperties.get().getRemoteControlPort()));
        StringField remoteField = Field.ofStringType(remotePortNumber).render(new MobileServerPreference(false));
        bindings.put(remoteField, useMobileRemote.not());

        StringProperty passwordProperty = new SimpleStringProperty(QueleaProperties.get().getRemoteControlPassword());

        return Category.of(LabelGrabber.INSTANCE.getLabel("server.settings.heading"),
                Group.of("Mobile Lyrics",
                        Setting.of(LabelGrabber.INSTANCE.getLabel("use.mobile.lyrics.label"), useMobileLyrics),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("port.number.label"), mobileLyricsField, lyricsPortNumber)),
                Group.of("Mobile Remote",
                        Setting.of(LabelGrabber.INSTANCE.getLabel("use.remote.control.label"), useMobileRemote),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("port.number.label"), remoteField, remotePortNumber),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("remote.control.password"), passwordProperty))
        );
    }

    private Category getRecordingsTab() {
        StringProperty recordingsDirectoryChooser = new SimpleStringProperty(QueleaProperties.get().getRecordingsPath());
        StringField recordingsDirectoryField = Field.ofStringType(recordingsDirectoryChooser).render(
                new DirectorySelectorPreference(QueleaApp.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("browse"), null));

        BooleanProperty useConvert = new SimpleBooleanProperty(QueleaProperties.get().getConvertRecordings());
        return Category.of(LabelGrabber.INSTANCE.getLabel("recordings.options.heading"),
                Setting.of(LabelGrabber.INSTANCE.getLabel("recordings.path"), recordingsDirectoryField, recordingsDirectoryChooser),
                Setting.of(LabelGrabber.INSTANCE.getLabel("convert.mp3"), useConvert)
        );
    }

    private HashMap<Field, ObservableValue> bindings = new HashMap<>();

    private Group getDisplayGroup(String groupName, String image, boolean custom) {
        BooleanProperty useCustomPosition = new SimpleBooleanProperty(false);
        if (groupName.equals(LabelGrabber.INSTANCE.getLabel("projector.screen.label")))
            useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isProjectorModeCoords());
        else if (groupName.equals(LabelGrabber.INSTANCE.getLabel("stage.screen.label")))
            useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isStageModeCoords());

        ObservableList<String> availableScreens = getAvailableScreens(custom);
        ListProperty screenListProperty = new SimpleListProperty<>(availableScreens);
        ObjectProperty<String> screenSelectProperty = new SimpleObjectProperty<>(availableScreens.get(0));
        Field customControl = Field.ofSingleSelectionType(screenListProperty, screenSelectProperty).render(
                new SimpleComboBoxControl<>());

        Group group;
        if (!custom) {
            int screen = QueleaProperties.get().getControlScreen();
            screenSelectProperty.setValue(screen > -1 ? availableScreens.get(screen) : availableScreens.get(0));
            group = Group.of(groupName,
                    Setting.of(groupName, customControl, screenSelectProperty)
            );//.addGroupNode(new ImageView(new Image(image)));}
        } else {
            int screen;
            Bounds bounds;
            if (groupName.equals(LabelGrabber.INSTANCE.getLabel("projector.screen.label"))) {
                screen = QueleaProperties.get().getProjectorScreen();
                bounds = QueleaProperties.get().getProjectorCoords();
            } else {
                screen = QueleaProperties.get().getStageScreen();
                bounds = QueleaProperties.get().getStageCoords();
            }

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();

            IntegerProperty widthProperty = new SimpleIntegerProperty((int) bounds.getWidth());
            IntegerProperty heightProperty = new SimpleIntegerProperty((int) bounds.getHeight());
            IntegerProperty xProperty = new SimpleIntegerProperty((int) bounds.getMinX());
            IntegerProperty yProperty = new SimpleIntegerProperty((int) bounds.getMinY());
            IntegerField sizeWith = Field.ofIntegerType(widthProperty).render(
                    new IntegerSliderControl(0, width));
            IntegerField sizeHeight = Field.ofIntegerType(heightProperty).render(
                    new IntegerSliderControl(0, height));
            IntegerField posX = Field.ofIntegerType(xProperty).render(
                    new IntegerSliderControl(0, width));
            IntegerField posY = Field.ofIntegerType(yProperty).render(
                    new IntegerSliderControl(0, height));

            screenSelectProperty.setValue(screen > -1 ? availableScreens.get(screen) : availableScreens.get(0));

            group = Group.of(groupName,
                    Setting.of(groupName, customControl, screenSelectProperty),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("custom.position.text"), useCustomPosition),
                    Setting.of("W", sizeWith, widthProperty),
                    Setting.of("H", sizeHeight, heightProperty),
                    Setting.of("X", posX, xProperty),
                    Setting.of("Y", posY, yProperty)
            );//.addGroupNode(new ImageView(new Image(image)));

            bindings.put(sizeWith, useCustomPosition.not());
            bindings.put(sizeHeight, useCustomPosition.not());
            bindings.put(posX, useCustomPosition.not());
            bindings.put(posY, useCustomPosition.not());
            bindings.put(customControl, useCustomPosition);
        }
        return group;
    }

    /**
     * Get a list model describing the available graphical devices.
     *
     * @return a list model describing the available graphical devices.
     */
    private ObservableList<String> getAvailableScreens(boolean none) {
        ObservableList<Screen> monitors = Screen.getScreens();

        ObservableList<String> descriptions = FXCollections.<String>observableArrayList();
        if (none) {
            descriptions.add(LabelGrabber.INSTANCE.getLabel("none.text"));
        }
        for (int i = 0; i < monitors.size(); i++) {
            descriptions.add(LabelGrabber.INSTANCE.getLabel("output.text") + " " + (i + 1));
        }
        return descriptions;
    }

    private void bind(Field field, ObservableValue<? extends Boolean> booleanProperty) {
        ((SimpleControl) field.getRenderer()).getNode().disableProperty().bind(booleanProperty);
    }

    private Setting getColorPicker(String label, Color color) {
        StringProperty property = new SimpleStringProperty(QueleaProperties.get().getStr(color));
        StringField field = Field.ofStringType(property).render(
                new ColorPickerPreference(color));
        return Setting.of(label, field, property);
    }

    private Setting getPositionSelector(String label, boolean horizontal, String selectedValue, BooleanProperty booleanBind) {
        Setting setting;
        if (horizontal)
            setting = Setting.of(label, FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("left"), LabelGrabber.INSTANCE.getLabel("right")), new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel(selectedValue)));
        else
            setting = Setting.of(label, FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("top"), LabelGrabber.INSTANCE.getLabel("bottom")), new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel(selectedValue)));
        if (booleanBind != null)
            bindings.put(setting.getField(), booleanBind.not());
        return setting;
    }

    public PreferencesFx getPreferenceDialog() {
        return preferencesFx;
    }
}
