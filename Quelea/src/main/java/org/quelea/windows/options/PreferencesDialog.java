package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.IntegerField;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.formsfx.view.controls.IntegerSliderControl;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleComboBoxControl;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import org.quelea.data.displayable.TextAlignment;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.LanguageFile;
import org.quelea.services.languages.LanguageFileManager;
import org.quelea.services.utils.Utils;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.prefs.Preferences;

public class PreferencesDialog {
    private PreferencesFx preferencesFx;


    /**
     * Create a new preference dialog.
     *
     * @author Arvid
     */
    public PreferencesDialog(Class parent) {
        StringProperty stringProperty = new SimpleStringProperty("String");
        IntegerProperty integerProperty = new SimpleIntegerProperty(12);
        DoubleProperty doubleProperty = new SimpleDoubleProperty(6.5);

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
//        PreferencesFx.of(parent, Category.of("General"));
        bindings.forEach(this::bind);
    }

    private HashMap<Field, BooleanProperty> bindings = new HashMap<>();

    private Group getDisplayGroup(String groupName, String image, boolean custom) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        IntegerProperty widthProperty = new SimpleIntegerProperty(width / 2);
        IntegerProperty heightProperty = new SimpleIntegerProperty(height / 2);
        IntegerProperty xProperty = new SimpleIntegerProperty(0);
        IntegerProperty yProperty = new SimpleIntegerProperty(0);
        BooleanProperty booleanProperty = new SimpleBooleanProperty(false);

        IntegerField sizeWith = Field.ofIntegerType(widthProperty).render(
                new IntegerSliderControl(0, width));
        IntegerField sizeHeight = Field.ofIntegerType(heightProperty).render(
                new IntegerSliderControl(0, height));
        IntegerField posX = Field.ofIntegerType(xProperty).render(
                new IntegerSliderControl(0, width));
        IntegerField posY = Field.ofIntegerType(yProperty).render(
                new IntegerSliderControl(0, height));

//        outputList = FXCollections.observableArrayList(getAvailableScreens(custom));
//        ListProperty outputList = new SimpleListProperty<>(
//                FXCollections.observableArrayList(getAvailableScreens(custom))
//        );
        SimpleComboBoxControl simpleComboBoxControl = new SimpleComboBoxControl<>();
        SingleSelectionField singleSelectionField = Field.ofSingleSelectionType(FXCollections.observableArrayList(getAvailableScreens(custom))).render(simpleComboBoxControl);
        ObjectProperty outputSelection = new SimpleObjectProperty<>(getAvailableScreens(custom).get(0));

        Group group;
        if (!custom) {
            group = Group.of(groupName,
                    Setting.of(groupName, singleSelectionField, outputSelection)
            );//.addGroupNode(new ImageView(new Image(image)));}
        } else {
            group = Group.of(groupName,
                    Setting.of(groupName, singleSelectionField, outputSelection),
                    Setting.of(LabelGrabber.INSTANCE.getLabel("custom.position.text"), booleanProperty),
                    Setting.of("W", sizeWith, widthProperty),
                    Setting.of("H", sizeHeight, heightProperty),
                    Setting.of("X", posX, xProperty),
                    Setting.of("Y", posY, yProperty)
            );//.addGroupNode(new ImageView(new Image(image)));
            bindings.put(sizeWith, booleanProperty);
            bindings.put(sizeHeight, booleanProperty);
            bindings.put(posX, booleanProperty);
            bindings.put(posY, booleanProperty);
            bindings.put(singleSelectionField, booleanProperty);
//            simpleComboBoxControl.disableProperty().bind(booleanProperty);
//            bindings.put(singleSelectionField, booleanProperty);
        }
        return group;
    }

    /**
     * Get a list model describing the available graphical devices.
     *
     * @return a list model describing the available graphical devices.
     */
    private ObservableList<String> getAvailableScreens(boolean none) {
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        final GraphicsDevice[] gds = ge.getScreenDevices();

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

    private void bind(Field field, BooleanProperty booleanProperty) {
        if (field.getRenderer() instanceof SimpleComboBoxControl)
            ((SimpleComboBoxControl) field.getRenderer()).getNode().disableProperty().bind(booleanProperty);
        else
            ((SimpleControl) field.getRenderer()).getNode().disableProperty().bind(booleanProperty.not());
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
        BooleanProperty showChordsBooleanProperty = new SimpleBooleanProperty(true);
        ColorPicker backgroundColorPicker;
        ColorPicker chordColorPicker;
        ColorPicker lyricsColorPicker;
        BooleanProperty clearWithMainBox = new SimpleBooleanProperty(true);
        BooleanProperty use24HBooleanProperty = new SimpleBooleanProperty(true);
        ArrayList<String> textAlignment = new ArrayList<>();
        for (TextAlignment alignment : TextAlignment.values()) {
            textAlignment.add(alignment.toFriendlyString());
        }
        ObservableList lineAlignment = FXCollections.observableArrayList(textAlignment);
        ObservableList fonts = FXCollections.observableArrayList(Utils.getAllFonts());
        ObjectProperty alignmentSelection = new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel("left"));
        ObjectProperty fontSelection = new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel("SansSerif"));
        return Category.of(LabelGrabber.INSTANCE.getLabel("stage.options.heading"),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.show.chords"), showChordsBooleanProperty),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.line.alignment"), lineAlignment, alignmentSelection),
                Setting.of(LabelGrabber.INSTANCE.getLabel("stage.font.selection"), fonts, fontSelection),
                Setting.of(LabelGrabber.INSTANCE.getLabel("clear.stage.view"), clearWithMainBox),
                Setting.of(LabelGrabber.INSTANCE.getLabel("use.24h.clock"), use24HBooleanProperty)
        );
    }

    private Category getDisplaySetupTab() {
        return Category.of(LabelGrabber.INSTANCE.getLabel("display.options.heading"), //, new ImageView(new Image("file:icons/monitorsettingsicon.png")),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("control.screen.label"), "file:icons/monitor.png", false),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("projector.screen.label"), "file:icons/projector.png", true),
                getDisplayGroup(LabelGrabber.INSTANCE.getLabel("stage.screen.label"), "file:icons/stage.png", true)
        );
    }

    private Category getGeneralTab() {
        BooleanProperty checkForUpdate = new SimpleBooleanProperty(true);
        BooleanProperty singleMonitorWarning = new SimpleBooleanProperty(true);
        BooleanProperty oneLineMode = new SimpleBooleanProperty(false);
        BooleanProperty autoPlayVideo = new SimpleBooleanProperty(false);
        BooleanProperty advanceOnLive = new SimpleBooleanProperty(false);
        BooleanProperty autoTranslate = new SimpleBooleanProperty(true);
        BooleanProperty clearLiveOnRemove = new SimpleBooleanProperty(true);
        BooleanProperty embedMediaInSchedule = new SimpleBooleanProperty(true);
        BooleanProperty itemThemeOverride = new SimpleBooleanProperty(false);
        BooleanProperty autoPlayVid = new SimpleBooleanProperty(false);
        BooleanProperty previewOnImageChange = new SimpleBooleanProperty(false);
        BooleanProperty uniformFontSize = new SimpleBooleanProperty(true);
        BooleanProperty defaultSongDBUpdate = new SimpleBooleanProperty(false);
        BooleanProperty showSmallSong = new SimpleBooleanProperty(true);

        BooleanProperty showSmallBible = new SimpleBooleanProperty(true);
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

        ObservableList languageItems = FXCollections.observableArrayList(languages);
        ObservableList songInfoPosItems = FXCollections.observableArrayList(LabelGrabber.INSTANCE.getLabel("top"), LabelGrabber.INSTANCE.getLabel("bottom"));
        ObjectProperty languageSelection = new SimpleObjectProperty<>("English (GB)");
        ObjectProperty songPosSelection = new SimpleObjectProperty<>(LabelGrabber.INSTANCE.getLabel("top"));

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
