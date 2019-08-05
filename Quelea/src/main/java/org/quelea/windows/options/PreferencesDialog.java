package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.formsfx.view.controls.*;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.displayable.TextAlignment;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.LanguageFile;
import org.quelea.services.languages.LanguageFileManager;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayStage;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.options.customprefs.*;

import java.util.ArrayList;
import java.util.HashMap;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

public class PreferencesDialog extends Stage {
    private PreferencesFx preferencesFx;
    private final BorderPane mainPane;
    private final Button okButton;
    private boolean displayChange = false;

    /**
     * Create a new preference dialog.
     *
     * @author Arvid
     */
    public PreferencesDialog(Class parent) {
        setTitle(LabelGrabber.INSTANCE.getLabel("options.title"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(QueleaApp.get().getMainWindow());

        getIcons().add(new Image("file:icons/options.png", 16, 16, false, true));
        mainPane = new BorderPane();

        preferencesFx =
                PreferencesFx.of(new PreferenceStorageHandler(parent),
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
        mainPane.setCenter(preferencesFx.getView());
        okButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        BorderPane.setMargin(okButton, new Insets(5));
        okButton.setOnAction((ActionEvent t) -> {
            preferencesFx.saveSettings();
            if (displayChange)
                updatePos();
            displayChange = false;
            QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getThemeNode().refresh();
            hide();
        });
        BorderPane.setAlignment(okButton, Pos.CENTER);
        mainPane.setBottom(okButton);
        setScene(new Scene(mainPane));
    }

    private Category getGeneralTab() {
        BooleanProperty showSmallSong = new SimpleBooleanProperty(QueleaProperties.get().getSmallSongTextShow());
        IntegerProperty smallSongSizeSpinner = new SimpleIntegerProperty((int) (QueleaProperties.get().getSmallSongTextSize() * 100));
        IntegerField smallSongSizeController = Field.ofIntegerType(smallSongSizeSpinner).render(
                new PercentIntegerSliderControl(0.1, 0.5));

        BooleanProperty showSmallBible = new SimpleBooleanProperty(QueleaProperties.get().getSmallBibleTextShow());
        IntegerProperty smallBibleSizeSpinner = new SimpleIntegerProperty((int) (100 * QueleaProperties.get().getSmallBibleTextSize()));
        IntegerField smallBibleSizeController = Field.ofIntegerType(smallBibleSizeSpinner).render(
                new PercentIntegerSliderControl(0.1, 0.5));

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
                        Setting.of(LabelGrabber.INSTANCE.getLabel("interface.language.label"), languageItems, languageSelection).customKey(languageFileKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("check.for.update.label"), new SimpleBooleanProperty(QueleaProperties.get().checkUpdate())).customKey(checkUpdateKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("1.monitor.warn.label"), new SimpleBooleanProperty(QueleaProperties.get().showSingleMonitorWarning())).customKey(singleMonitorWarningKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("one.line.mode.label"), new SimpleBooleanProperty(QueleaProperties.get().getOneLineMode())).customKey(oneLineModeKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("autoplay.vid.label"), new SimpleBooleanProperty(QueleaProperties.get().getAutoPlayVideo())).customKey(autoplayVidKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("advance.on.live.label"), new SimpleBooleanProperty(QueleaProperties.get().getAdvanceOnLive())).customKey(advanceOnLiveKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("overflow.song.label"), new SimpleBooleanProperty(QueleaProperties.get().getSongOverflow())).customKey(songOverflowKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("preview.on.image.change.label"), new SimpleBooleanProperty(QueleaProperties.get().getPreviewOnImageUpdate())).customKey(previewOnImageChangeKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.video.library.panel"), new SimpleBooleanProperty(QueleaProperties.get().getDisplayVideoTab())).customKey(videoTabKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("auto.translate.label"), new SimpleBooleanProperty(QueleaProperties.get().getAutoTranslate())).customKey(autoTranslateKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("copy.song.db.default"), new SimpleBooleanProperty(QueleaProperties.get().getDefaultSongDBUpdate())).customKey(defaultSongDbUpdateKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("clear.live.on.remove.schedule"), new SimpleBooleanProperty(QueleaProperties.get().getClearLiveOnRemove())).customKey(clearLiveOnRemoveKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("embed.media.in.schedule"), new SimpleBooleanProperty(QueleaProperties.get().getEmbedMediaInScheduleFile())).customKey(scheduleEmbedMediaKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("allow.item.theme.override.global"), new SimpleBooleanProperty(QueleaProperties.get().getItemThemeOverride())).customKey(itemThemeOverrideKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.small.song.text.label"), showSmallSong).customKey(showSmallSongTextKey),
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.song.position.label"), false, QueleaProperties.get().getSmallSongTextPositionV(), showSmallSong).customKey(smallSongTextVPositionKey),
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.song.position.label"), true, QueleaProperties.get().getSmallSongTextPositionH(), showSmallSong).customKey(smallSongTextHPositionKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("small.song.size.label"), smallSongSizeController, smallSongSizeSpinner).customKey(smallSongTextSizeKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.small.bible.text.label"), showSmallBible).customKey(showSmallBibleTextKey),
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.bible.position.label"), false, QueleaProperties.get().getSmallBibleTextPositionV(), showSmallBible).customKey(smallBibleTextVPositionKey),
                        getPositionSelector(LabelGrabber.INSTANCE.getLabel("small.bible.position.label"), true, QueleaProperties.get().getSmallBibleTextPositionH(), showSmallBible).customKey(smallBibleTextHPositionKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("small.bible.size.label"), smallBibleSizeController, smallBibleSizeSpinner).customKey(smallBibleTextSizeKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("thumbnail.size.label"), thumbnailSize, 100, 500).customKey(thumbnailSizeKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("show.extra.live.panel.toolbar.options.label"), new SimpleBooleanProperty(QueleaProperties.get().getShowExtraLivePanelToolbarOptions())).customKey(showExtraLivePanelToolbarOptionsKey)
                ),
                Group.of(LabelGrabber.INSTANCE.getLabel(LabelGrabber.INSTANCE.getLabel("text.options.options")),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("capitalise.start.line.label"), new SimpleBooleanProperty(QueleaProperties.get().checkCapitalFirst())).customKey(capitalFirstKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("uniform.font.size.label"), new SimpleBooleanProperty(QueleaProperties.get().getUseUniformFontSize())).customKey(uniformFontSizeKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("max.font.size.label"), maxFontSize, 12, 300).customKey(maxFontSizeKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("additional.line.spacing.label"), additionalSpacing, 0, 50).customKey(additionalLineSpacingKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("max.chars.line.label"), maxChars, 10, 160).customKey(maxCharsKey)
                )
        );
    }

    private Category getDisplaySetupTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("display.options.heading"), new ImageView(new Image("file:icons/monitorsettingsicon.png")), //, new ImageView(new Image("file:icons/monitorsettingsicon.png")),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("control.screen.label"), "file:icons/monitor.png", false),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("projector.screen.label"), "file:icons/projector.png", true),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("stage.screen.label"), "file:icons/stage.png", true)
        );
    }

    private Category getStageViewTab() {
        ArrayList<String> textAlignment = new ArrayList<>();
        for (TextAlignment alignment : TextAlignment.values()) {
            textAlignment.add(alignment.toFriendlyString());
        }
        ObservableList<String> lineAlignment = FXCollections.observableArrayList(textAlignment);
        ObjectProperty<String> alignmentSelection = new SimpleObjectProperty<>(QueleaProperties.get().getStageTextAlignment());

        ObservableList<String> fonts = FXCollections.observableArrayList(Utils.getAllFonts());
        ObjectProperty<String> fontSelection = new SimpleObjectProperty<>(QueleaProperties.get().getStageTextFont());

        return Category.of(LabelGrabber.INSTANCE.getLabel("stage.options.heading"), new ImageView(new Image("file:icons/stageviewsettingsicon.png")),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.show.chords"), new SimpleBooleanProperty(QueleaProperties.get().getShowChords())).customKey(stageShowChordsKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.line.alignment"), lineAlignment, alignmentSelection).customKey(stageTextAlignmentKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.font.selection"), fonts, fontSelection).customKey(stageFontKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.background.colour"), QueleaProperties.get().getStageBackgroundColor()).customKey(stageBackgroundColorKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.lyrics.colour"), QueleaProperties.get().getStageLyricsColor()).customKey(stageLyricsColorKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("stage.chord.colour"), QueleaProperties.get().getStageChordColor()).customKey(stageChordColorKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("clear.stage.view"), new SimpleBooleanProperty(QueleaProperties.get().getClearStageWithMain())).customKey(clearStageviewWithMainKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("use.24h.clock"), new SimpleBooleanProperty(QueleaProperties.get().getUse24HourClock())).customKey(use24hClockKey)
        );
    }

    private Category getNoticesTab() {
        DoubleProperty noticeSpeed = new SimpleDoubleProperty(QueleaProperties.get().getNoticeSpeed());
        DoubleField noticeSpeedField = Field.ofDoubleType(noticeSpeed).render(new DoubleNoLabelPreference(2, 20, 10));
        DoubleProperty noticeSize = new SimpleDoubleProperty(QueleaProperties.get().getNoticeFontSize());
        DoubleField noticeSizeField = Field.ofDoubleType(noticeSize).render(new DoubleNoLabelPreference(20, 100, 10));

        return Category.of(LabelGrabber.INSTANCE.getLabel("notice.options.heading"), new ImageView(new Image("file:icons/noticessettingsicon.png")),
                getPositionSelector(LabelGrabber.INSTANCE.getLabel("notice.position.text"), false, QueleaProperties.get().getNoticePosition().getText(), null).customKey(noticePositionKey),
                getColorPicker(LabelGrabber.INSTANCE.getLabel("notice.background.colour.text"), QueleaProperties.get().getNoticeBackgroundColour()).customKey(noticeBackgroundColourKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("notice.speed.text"), noticeSpeedField, noticeSpeed).customKey(noticeSpeedKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("notice.font.size"), noticeSizeField, noticeSize).customKey(noticeFontSizeKey)
        );
    }

    private Category getPresentationsTab() {
        BooleanProperty useOO = new SimpleBooleanProperty(QueleaProperties.get().getUseOO());
        StringProperty directoryChooserOO = new SimpleStringProperty(QueleaProperties.get().getOOPath());
        StringField directoryFieldOO = Field.ofStringType(directoryChooserOO).render(
                new DirectorySelectorPreference(LabelGrabber.INSTANCE.getLabel("browse"), null));
        bindings.put(directoryFieldOO, useOO.not());

        if (!Utils.isLinux()) {
            BooleanProperty usePP = new SimpleBooleanProperty(QueleaProperties.get().getUsePP());
            StringProperty directoryChooserPP = new SimpleStringProperty(QueleaProperties.get().getPPPath());
            StringField directoryFieldPP = Field.ofStringType(directoryChooserPP).render(
                    new DirectorySelectorPreference(LabelGrabber.INSTANCE.getLabel("browse"), null));
            bindings.put(directoryFieldPP, usePP.not());

            return Category.of(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"), new ImageView(new Image("file:icons/presentationssettingsicon.png")),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.oo.label"), useOO).customKey(useOoKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("oo.path"), directoryFieldOO, directoryChooserOO).customKey(ooPathKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.pp.label"), usePP).customKey(usePpKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("pp.path"), directoryFieldPP, directoryChooserPP).customKey(ppPathKey)
            );
        } else
            return Category.of(LabelGrabber.INSTANCE.getLabel("presentation.options.heading"), new ImageView(new Image("file:icons/presentationssettingsicon.png")),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("use.oo.label"), useOO).customKey(useOoKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("oo.path"), directoryFieldOO, directoryChooserOO).customKey(ooPathKey)
            );
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

        return Category.of(LabelGrabber.INSTANCE.getLabel("bible.options.heading"), new ImageView(new Image("file:icons/biblesettingsicon.png")),
                Setting.of(LabelGrabber.INSTANCE.getLabel("default.bible.label"), bibleField, bibleSelection).customKey(defaultBibleKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("show.verse.numbers"), showVerseNum).customKey(showVerseNumbersKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("split.bible.verses"), splitBibleVerse).customKey(splitBibleVersesKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("max.items.per.slide").replace("%", LabelGrabber.INSTANCE.getLabel("verses")), useMaxVerses).customKey(useMaxBibleCharsKey),
                Setting.of("", maxVerseField, maxVerse).customKey(maxBibleVersesKey)
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

        return Category.of(LabelGrabber.INSTANCE.getLabel("server.settings.heading"), new ImageView(new Image("file:icons/serversettingsicon.png")),
                Group.of(LabelGrabber.INSTANCE.getLabel("mobile.lyrics.heading"),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("use.mobile.lyrics.label"), useMobileLyrics).customKey(useMobLyricsKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("port.number.label"), mobileLyricsField, lyricsPortNumber).customKey(mobLyricsPortKey)
                ),
                Group.of(LabelGrabber.INSTANCE.getLabel("mobile.remote.heading"),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("use.remote.control.label"), useMobileRemote).customKey(useRemoteControlKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("port.number.label"), remoteField, remotePortNumber).customKey(remoteControlPortKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("remote.control.password"), passwordProperty).customKey(remoteControlPasswordKey)
                )
        );
    }

    private Category getRecordingsTab() {
        StringProperty recordingsDirectoryChooser = new SimpleStringProperty(QueleaProperties.get().getRecordingsPath());
        StringField recordingsDirectoryField = Field.ofStringType(recordingsDirectoryChooser).render(
                new DirectorySelectorPreference(LabelGrabber.INSTANCE.getLabel("browse"), null));

        BooleanProperty useConvert = new SimpleBooleanProperty(QueleaProperties.get().getConvertRecordings());
        return Category.of(LabelGrabber.INSTANCE.getLabel("recordings.options.heading"), new ImageView(new Image("file:icons/recordingssettingsicon.png")),
                Setting.of(LabelGrabber.INSTANCE.getLabel("recordings.path"), recordingsDirectoryField, recordingsDirectoryChooser).customKey(recPathKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("convert.mp3"), useConvert).customKey(convertMp3Key)
        );
    }

    private HashMap<Field, ObservableValue> bindings = new HashMap<>();

    private Group getDisplayGroup(String groupName, String image, boolean custom) {
        BooleanProperty useCustomPosition = new SimpleBooleanProperty(false);
        if (groupName.equals(LabelGrabber.INSTANCE.getLabel("projector.screen.label"))) {
            useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isProjectorModeCoords());
        } else if (groupName.equals(LabelGrabber.INSTANCE.getLabel("stage.screen.label"))) {
            useCustomPosition = new SimpleBooleanProperty(QueleaProperties.get().isStageModeCoords());
        }
        useCustomPosition.addListener(e -> {
            displayChange = true;
        });

        ObservableList<String> availableScreens = getAvailableScreens(custom);
        ListProperty<String> screenListProperty = new SimpleListProperty<>(availableScreens);
        ObjectProperty<String> screenSelectProperty = new SimpleObjectProperty<>(availableScreens.get(0));
        Field customControl = Field.ofSingleSelectionType(screenListProperty, screenSelectProperty).render(
                new SimpleComboBoxControl<>());

        availableScreens.addListener((ListChangeListener<? super String>) e -> {
            displayChange = true;
        });

        screenSelectProperty.addListener(e -> {
            displayChange = true;
        });

        Group group;
        if (!custom) {
            int screen = QueleaProperties.get().getControlScreen();
            screenSelectProperty.setValue(screen > -1 ? availableScreens.get(screen) : availableScreens.get(0));
            group = Group.of(groupName,
                    Setting.of(groupName, customControl, screenSelectProperty).customKey(controlScreenKey)
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

            IntegerProperty widthProperty = new SimpleIntegerProperty((int) bounds.getWidth());
            IntegerProperty heightProperty = new SimpleIntegerProperty((int) bounds.getHeight());
            IntegerProperty xProperty = new SimpleIntegerProperty((int) bounds.getMinX());
            IntegerProperty yProperty = new SimpleIntegerProperty((int) bounds.getMinY());
            IntegerField sizeWith = Field.ofIntegerType(widthProperty).render(
                    new SimpleIntegerControl());
            IntegerField sizeHeight = Field.ofIntegerType(heightProperty).render(
                    new SimpleIntegerControl());
            IntegerField posX = Field.ofIntegerType(xProperty).render(
                    new SimpleIntegerControl());
            IntegerField posY = Field.ofIntegerType(yProperty).render(
                    new SimpleIntegerControl());

            widthProperty.addListener(e -> {
                displayChange = true;
            });

            heightProperty.addListener(e -> {
                displayChange = true;
            });

            xProperty.addListener(e -> {
                displayChange = true;
            });

            yProperty.addListener(e -> {
                displayChange = true;
            });

            screenSelectProperty.setValue(screen > -1 ? availableScreens.get(screen) : availableScreens.get(0));
            boolean projectorGroup = groupName.equals(LabelGrabber.INSTANCE.getLabel("projector.screen.label"));

            group = Group.of(groupName,
                    Setting.of(groupName, customControl, screenSelectProperty)
                            .customKey(projectorGroup ? projectorScreenKey : stageScreenKey),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("custom.position.text"), useCustomPosition)
                            .customKey(projectorGroup ? projectorModeKey : stageModeKey),
                    Setting.of("W", sizeWith, widthProperty)
                            .customKey(projectorGroup ? projectorWCoordKey : stageWCoordKey),
                    Setting.of("H", sizeHeight, heightProperty)
                            .customKey(projectorGroup ? projectorHCoordKey : stageHCoordKey),
                    Setting.of("X", posX, xProperty)
                            .customKey(projectorGroup ? projectorXCoordKey : stageXCoordKey),
                    Setting.of("Y", posY, yProperty)
                            .customKey(projectorGroup ? projectorYCoordKey : stageYCoordKey)
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
            setting = Setting.of(label, FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("left"), LabelGrabber.INSTANCE.getLabel("right")), new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel(selectedValue.toLowerCase())));
        else
            setting = Setting.of(label, FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("top.text.position"), LabelGrabber.INSTANCE.getLabel("bottom.text.position")), new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel(selectedValue.toLowerCase())));
        if (booleanBind != null)
            bindings.put(setting.getField(), booleanBind.not());
        return setting;
    }

    private void updatePos() {
        DisplayStage appWindow = QueleaApp.get().getProjectionWindow();
        DisplayStage stageWindow = QueleaApp.get().getStageWindow();
        if (appWindow == null) {
            appWindow = new DisplayStage(QueleaProperties.get().getProjectorCoords(), false);
        }
        final DisplayStage fiLyricWindow = appWindow; //Fudge for AIC
        Platform.runLater(() -> {
            fiLyricWindow.setAreaImmediate(QueleaProperties.get().getProjectorCoords());
            if (!QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getHide().isSelected()) {
                fiLyricWindow.show();
            }

            // non-custom positioned windows are fullscreen
            if (!QueleaProperties.get().isProjectorModeCoords()) {
                if (QueleaProperties.get().getProjectorScreen() == -1)
                    fiLyricWindow.hide();
                else
                    fiLyricWindow.setFullScreenAlwaysOnTop(true);
            }
        });
        if (stageWindow == null) {
            stageWindow = new DisplayStage(QueleaProperties.get().getStageCoords(), true);
        }
        final DisplayStage fiStageWindow = stageWindow; //Fudge for AIC
        Platform.runLater(() -> {
            fiStageWindow.setAreaImmediate(QueleaProperties.get().getStageCoords());
            if (!QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getHide().isSelected()) {
                fiStageWindow.show();
            }
            if (QueleaProperties.get().getStageScreen() == -1 && !QueleaProperties.get().isStageModeCoords())
                fiStageWindow.hide();
        });
    }
}
