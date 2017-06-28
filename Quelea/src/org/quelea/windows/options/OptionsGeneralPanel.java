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

import java.text.NumberFormat;
import java.io.File;
import java.math.BigDecimal;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import org.quelea.data.powerpoint.OOPresentation;
import org.quelea.data.powerpoint.OOUtils;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.LanguageFile;
import org.quelea.services.languages.LanguageFileManager;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;
import utils.BigDecimalSpinner;

/**
 * A panel where the general options in the program are set.
 * <p/>
 * @author Michael
 */
public class OptionsGeneralPanel extends GridPane implements PropertyPanel {

    QueleaProperties props = QueleaProperties.get();
    private final CheckBox startupUpdateCheckBox;
    private final CheckBox capitalFirstCheckBox;
    private final CheckBox oneMonitorWarnCheckBox;
    private final CheckBox oneLineModeCheckBox;
    private final CheckBox autoTranslateCheckBox;
    private final CheckBox clearLiveOnRemoveCheckBox;
    private final CheckBox embedMediaInScheduleCheckBox;
    private final CheckBox autoPlayVidCheckBox;
    private final CheckBox advanceOnLiveCheckBox;
    private final CheckBox previewOnImageChangeCheckBox;
    private final CheckBox uniformFontSizeCheckBox;
    private final CheckBox defaultSongDBUpdateCheckBox;
    private final ComboBox<LanguageFile> languageFileComboBox;
    private final Slider thumbnailSizeSlider;
    private final Slider maximumFontSizeSlider;
    private final Slider additionalLineSpacingSlider;
    private final Slider maxCharsSlider;
//    private final Slider minLinesSlider;
    private LanguageFile currentLanguageFile;
    private final CheckBox showSmallSongTextBox;
    private final CheckBox showSmallBibleTextBox;
    private final ComboBox<String> smallBibleTextVPositionCombo;
    private final ComboBox<String> smallBibleTextHPositionCombo;
    private final ComboBox<String> smallSongTextVPositionCombo;
    private final ComboBox<String> smallSongTextHPositionCombo;
    private final BigDecimalSpinner smallSongSizeSpinner;
    private final BigDecimalSpinner smallBibleSizeSpinner;
    private final CheckBox overflowSongCheckBox;
    private final CheckBox showVideoPanelCheckBox;

    /**
     * Create a new general panel.
     */
    public OptionsGeneralPanel() {
        int rows = 0;
        setVgap(5);
        setHgap(10);
        setPadding(new Insets(5));

//        Label spacer = new Label("");
//        GridPane.setConstraints(spacer, 1, rows);
//        getChildren().add(spacer);
//        rows++;
        Label userOptions = new Label(LabelGrabber.INSTANCE.getLabel("user.options.options"));
        userOptions.setFont(Font.font(userOptions.getFont().getFamily(), FontWeight.BOLD, userOptions.getFont().getSize()));
        GridPane.setConstraints(userOptions, 1, rows);
        getChildren().add(userOptions);
        rows++;

        Label interfaceLanguageLabel = new Label(LabelGrabber.INSTANCE.getLabel("interface.language.label"));
        GridPane.setConstraints(interfaceLanguageLabel, 1, rows);
        getChildren().add(interfaceLanguageLabel);
        languageFileComboBox = new ComboBox<>();
        for (LanguageFile file : LanguageFileManager.INSTANCE.languageFiles()) {
            languageFileComboBox.getItems().add(file);
        }
        interfaceLanguageLabel.setLabelFor(languageFileComboBox);
        GridPane.setConstraints(languageFileComboBox, 2, rows);
        getChildren().add(languageFileComboBox);
        rows++;

        Label startupLabel = new Label(LabelGrabber.INSTANCE.getLabel("check.for.update.label"));
        GridPane.setConstraints(startupLabel, 1, rows);
        getChildren().add(startupLabel);
        startupUpdateCheckBox = new CheckBox();
        startupLabel.setLabelFor(startupUpdateCheckBox);
        GridPane.setConstraints(startupUpdateCheckBox, 2, rows);
        getChildren().add(startupUpdateCheckBox);
        rows++;

        Label warnLabel = new Label(LabelGrabber.INSTANCE.getLabel("1.monitor.warn.label"));
        GridPane.setConstraints(warnLabel, 1, rows);
        getChildren().add(warnLabel);
        oneMonitorWarnCheckBox = new CheckBox();
        warnLabel.setLabelFor(oneMonitorWarnCheckBox);
        GridPane.setConstraints(oneMonitorWarnCheckBox, 2, rows);
        getChildren().add(oneMonitorWarnCheckBox);
        rows++;

        Label oneLineModeLabel = new Label(LabelGrabber.INSTANCE.getLabel("one.line.mode.label"));
        GridPane.setConstraints(oneLineModeLabel, 1, rows);
        getChildren().add(oneLineModeLabel);
        oneLineModeCheckBox = new CheckBox();
        oneLineModeLabel.setLabelFor(oneLineModeCheckBox);
        GridPane.setConstraints(oneLineModeCheckBox, 2, rows);
        getChildren().add(oneLineModeCheckBox);
        rows++;

        Label autoPlayVidLabel = new Label(LabelGrabber.INSTANCE.getLabel("autoplay.vid.label"));
        GridPane.setConstraints(autoPlayVidLabel, 1, rows);
        getChildren().add(autoPlayVidLabel);
        autoPlayVidCheckBox = new CheckBox();
        autoPlayVidLabel.setLabelFor(autoPlayVidCheckBox);
        GridPane.setConstraints(autoPlayVidCheckBox, 2, rows);
        getChildren().add(autoPlayVidCheckBox);
        rows++;

        Label advanceOnLiveLabel = new Label(LabelGrabber.INSTANCE.getLabel("advance.on.live.label"));
        GridPane.setConstraints(advanceOnLiveLabel, 1, rows);
        getChildren().add(advanceOnLiveLabel);
        advanceOnLiveCheckBox = new CheckBox();
        advanceOnLiveLabel.setLabelFor(advanceOnLiveCheckBox);
        GridPane.setConstraints(advanceOnLiveCheckBox, 2, rows);
        getChildren().add(advanceOnLiveCheckBox);
        rows++;

        Label overflowSongLabel = new Label(LabelGrabber.INSTANCE.getLabel("overflow.song.label"));
        GridPane.setConstraints(overflowSongLabel, 1, rows);
        getChildren().add(overflowSongLabel);
        overflowSongCheckBox = new CheckBox();
        overflowSongLabel.setLabelFor(overflowSongCheckBox);
        GridPane.setConstraints(overflowSongCheckBox, 2, rows);
        getChildren().add(overflowSongCheckBox);
        rows++;

        advanceOnLiveCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> a, Boolean b, Boolean c) -> {
            checkOverflowEnable();
        });

        Label previewOnImageChangeLabel = new Label(LabelGrabber.INSTANCE.getLabel("preview.on.image.change.label"));
        GridPane.setConstraints(previewOnImageChangeLabel, 1, rows);
        getChildren().add(previewOnImageChangeLabel);
        previewOnImageChangeCheckBox = new CheckBox();
        previewOnImageChangeLabel.setLabelFor(previewOnImageChangeCheckBox);
        GridPane.setConstraints(previewOnImageChangeCheckBox, 2, rows);
        getChildren().add(previewOnImageChangeCheckBox);
        rows++;

        Label showVideoPanelLabel = new Label(LabelGrabber.INSTANCE.getLabel("show.video.library.panel"));
        GridPane.setConstraints(showVideoPanelLabel, 1, rows);
        getChildren().add(showVideoPanelLabel);
        showVideoPanelCheckBox = new CheckBox();
        showVideoPanelLabel.setLabelFor(showVideoPanelCheckBox);
        GridPane.setConstraints(showVideoPanelCheckBox, 2, rows);
        getChildren().add(showVideoPanelCheckBox);
        rows++;

        Label autoTranslateLabel = new Label(LabelGrabber.INSTANCE.getLabel("auto.translate.label"));
        GridPane.setConstraints(autoTranslateLabel, 1, rows);
        getChildren().add(autoTranslateLabel);
        autoTranslateCheckBox = new CheckBox();
        autoTranslateLabel.setLabelFor(autoTranslateCheckBox);
        GridPane.setConstraints(autoTranslateCheckBox, 2, rows);
        getChildren().add(autoTranslateCheckBox);
        rows++;

        Label defaultSongDBUpdateLabel = new Label(LabelGrabber.INSTANCE.getLabel("copy.song.db.default"));
        GridPane.setConstraints(defaultSongDBUpdateLabel, 1, rows);
        getChildren().add(defaultSongDBUpdateLabel);
        defaultSongDBUpdateCheckBox = new CheckBox();
        startupLabel.setLabelFor(defaultSongDBUpdateCheckBox);
        GridPane.setConstraints(defaultSongDBUpdateCheckBox, 2, rows);
        getChildren().add(defaultSongDBUpdateCheckBox);
        rows++;

        Label clearLiveOnRemoveLabel = new Label(LabelGrabber.INSTANCE.getLabel("clear.live.on.remove.schedule") + " ");
        GridPane.setConstraints(clearLiveOnRemoveLabel, 1, rows);
        getChildren().add(clearLiveOnRemoveLabel);
        clearLiveOnRemoveCheckBox = new CheckBox();
        clearLiveOnRemoveLabel.setLabelFor(clearLiveOnRemoveCheckBox);
        GridPane.setConstraints(clearLiveOnRemoveCheckBox, 2, rows);
        getChildren().add(clearLiveOnRemoveCheckBox);
        rows++;

        Label embedMediaLabel = new Label(LabelGrabber.INSTANCE.getLabel("embed.media.in.schedule") + " ");
        GridPane.setConstraints(embedMediaLabel, 1, rows);
        getChildren().add(embedMediaLabel);
        embedMediaInScheduleCheckBox = new CheckBox();
        embedMediaLabel.setLabelFor(embedMediaInScheduleCheckBox);
        GridPane.setConstraints(embedMediaInScheduleCheckBox, 2, rows);
        getChildren().add(embedMediaInScheduleCheckBox);
        rows++;

        Label showSmallSongTextLabel = new Label(LabelGrabber.INSTANCE.getLabel("show.small.song.text.label"));
        GridPane.setConstraints(showSmallSongTextLabel, 1, rows);
        getChildren().add(showSmallSongTextLabel);
        showSmallSongTextBox = new CheckBox();
        smallSongTextVPositionCombo = new ComboBox<>();
        smallSongTextHPositionCombo = new ComboBox<>();
        smallSongSizeSpinner = new BigDecimalSpinner(new BigDecimal("0.01"), new BigDecimal("0.5"), new BigDecimal("0.01"), NumberFormat.getPercentInstance());
        smallSongSizeSpinner.setMaxWidth(70);
        smallSongSizeSpinner.setMinWidth(70);
        HBox hboxSmallSong = new HBox();
        hboxSmallSong.alignmentProperty().setValue(Pos.CENTER_LEFT);
        smallSongTextVPositionCombo.getItems().addAll(LabelGrabber.INSTANCE.getLabel("top"), LabelGrabber.INSTANCE.getLabel("bottom"));
        smallSongTextHPositionCombo.getItems().addAll(LabelGrabber.INSTANCE.getLabel("left"), LabelGrabber.INSTANCE.getLabel("right"));
        hboxSmallSong.getChildren().addAll(showSmallSongTextBox, smallSongTextVPositionCombo, smallSongTextHPositionCombo, smallSongSizeSpinner);
        GridPane.setConstraints(hboxSmallSong, 2, rows);
        getChildren().add(hboxSmallSong);
        showSmallSongTextLabel.setLabelFor(hboxSmallSong);
        rows++;

        Label showSmallBibleTextLabel = new Label(LabelGrabber.INSTANCE.getLabel("show.small.bible.text.label"));
        GridPane.setConstraints(showSmallBibleTextLabel, 1, rows);
        getChildren().add(showSmallBibleTextLabel);
        showSmallBibleTextBox = new CheckBox();
        smallBibleTextVPositionCombo = new ComboBox<>();
        smallBibleTextHPositionCombo = new ComboBox<>();
        smallBibleSizeSpinner = new BigDecimalSpinner(new BigDecimal("0.01"), new BigDecimal("0.5"), new BigDecimal("0.01"), NumberFormat.getPercentInstance());
        smallBibleSizeSpinner.setMaxWidth(70);
        smallBibleSizeSpinner.setMinWidth(70);
        HBox hboxSmallBible = new HBox();
        hboxSmallBible.alignmentProperty().setValue(Pos.CENTER_LEFT);
        smallBibleTextVPositionCombo.getItems().addAll(LabelGrabber.INSTANCE.getLabel("top"), LabelGrabber.INSTANCE.getLabel("bottom"));
        smallBibleTextHPositionCombo.getItems().addAll(LabelGrabber.INSTANCE.getLabel("left"), LabelGrabber.INSTANCE.getLabel("right"));
        hboxSmallBible.getChildren().addAll(showSmallBibleTextBox, smallBibleTextVPositionCombo, smallBibleTextHPositionCombo, smallBibleSizeSpinner);
        GridPane.setConstraints(hboxSmallBible, 2, rows);
        getChildren().add(hboxSmallBible);
        showSmallSongTextLabel.setLabelFor(hboxSmallBible);
        rows++;

        showSmallBibleTextBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                smallBibleTextHPositionCombo.setDisable(false);
                smallBibleTextVPositionCombo.setDisable(false);
            } else {
                smallBibleTextHPositionCombo.setDisable(true);
                smallBibleTextVPositionCombo.setDisable(true);
            }
        });

        showSmallSongTextBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                smallSongTextHPositionCombo.setDisable(false);
                smallSongTextVPositionCombo.setDisable(false);
            } else {
                smallSongTextHPositionCombo.setDisable(true);
                smallSongTextVPositionCombo.setDisable(true);
            }
        });
        
        Label thumbnailSizeLabel = new Label(LabelGrabber.INSTANCE.getLabel("thumbnail.size.label"));
        GridPane.setConstraints(thumbnailSizeLabel, 1, rows);
        getChildren().add(thumbnailSizeLabel);
        thumbnailSizeSlider = new Slider(100, 500, 200);
        thumbnailSizeSlider.setMajorTickUnit(50);
        thumbnailSizeSlider.setMinorTickCount(0);
        thumbnailSizeSlider.setShowTickMarks(true);
        thumbnailSizeSlider.setSnapToTicks(true); 
        thumbnailSizeSlider.setBlockIncrement(50);
        
        GridPane.setConstraints(thumbnailSizeSlider, 2, rows);
        getChildren().add(thumbnailSizeSlider);
        thumbnailSizeLabel.setLabelFor(thumbnailSizeSlider);
        final Label thumbnailSizeValue = new Label(Integer.toString((int) thumbnailSizeSlider.getValue()));
        GridPane.setConstraints(thumbnailSizeValue, 3, rows);
        getChildren().add(thumbnailSizeValue);
        thumbnailSizeValue.setLabelFor(thumbnailSizeSlider);
        thumbnailSizeSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                thumbnailSizeValue.setText(Integer.toString((int) thumbnailSizeSlider.getValue()));
            }
        });
        rows++;

        Label spacer1 = new Label("");
        GridPane.setConstraints(spacer1, 1, rows);
        getChildren().add(spacer1);
        rows++;

        Label textOptions = new Label(LabelGrabber.INSTANCE.getLabel("text.options.options"));
        textOptions.setFont(Font.font(textOptions.getFont().getFamily(), FontWeight.BOLD, textOptions.getFont().getSize()));
        GridPane.setConstraints(textOptions, 1, rows);
        getChildren().add(textOptions);
        rows++;

        Label capitalFirstLabel = new Label(LabelGrabber.INSTANCE.getLabel("capitalise.start.line.label"));
        GridPane.setConstraints(capitalFirstLabel, 1, rows);
        getChildren().add(capitalFirstLabel);
        capitalFirstCheckBox = new CheckBox();
        startupLabel.setLabelFor(capitalFirstCheckBox);
        GridPane.setConstraints(capitalFirstCheckBox, 2, rows);
        getChildren().add(capitalFirstCheckBox);
        rows++;

        Label uniformFontSizeLabel = new Label(LabelGrabber.INSTANCE.getLabel("uniform.font.size.label"));
        GridPane.setConstraints(uniformFontSizeLabel, 1, rows);
        getChildren().add(uniformFontSizeLabel);
        uniformFontSizeCheckBox = new CheckBox();
        startupLabel.setLabelFor(uniformFontSizeCheckBox);
        GridPane.setConstraints(uniformFontSizeCheckBox, 2, rows);
        getChildren().add(uniformFontSizeCheckBox);
        rows++;

        Label maxFontSizeLabel = new Label(LabelGrabber.INSTANCE.getLabel("max.font.size.label"));
        GridPane.setConstraints(maxFontSizeLabel, 1, rows);
        getChildren().add(maxFontSizeLabel);
        maximumFontSizeSlider = new Slider(12, 300, 100);
        GridPane.setConstraints(maximumFontSizeSlider, 2, rows);
        getChildren().add(maximumFontSizeSlider);
        maxFontSizeLabel.setLabelFor(maximumFontSizeSlider);
        final Label maxFontSizeValue = new Label(Integer.toString((int) maximumFontSizeSlider.getValue()));
        GridPane.setConstraints(maxFontSizeValue, 3, rows);
        getChildren().add(maxFontSizeValue);
        maxFontSizeValue.setLabelFor(maximumFontSizeSlider);
        maximumFontSizeSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                maxFontSizeValue.setText(Integer.toString((int) maximumFontSizeSlider.getValue()));
            }
        });
        rows++;

        Label additionalLineSpacingLabel = new Label(LabelGrabber.INSTANCE.getLabel("additional.line.spacing.label"));
        GridPane.setConstraints(additionalLineSpacingLabel, 1, rows);
        getChildren().add(additionalLineSpacingLabel);
        additionalLineSpacingSlider = new Slider(0, 50, 10);
        GridPane.setConstraints(additionalLineSpacingSlider, 2, rows);
        getChildren().add(additionalLineSpacingSlider);
        maxFontSizeLabel.setLabelFor(additionalLineSpacingSlider);
        final Label additionalLineSpacingValue = new Label(Integer.toString((int) additionalLineSpacingSlider.getValue()));
        GridPane.setConstraints(additionalLineSpacingValue, 3, rows);
        getChildren().add(additionalLineSpacingValue);
        additionalLineSpacingValue.setLabelFor(additionalLineSpacingSlider);
        additionalLineSpacingSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                additionalLineSpacingValue.setText(Integer.toString((int) additionalLineSpacingSlider.getValue()));
            }
        });
        rows++;

        Label maxCharsLabel = new Label(LabelGrabber.INSTANCE.getLabel("max.chars.line.label"));
        GridPane.setConstraints(maxCharsLabel, 1, rows);
        getChildren().add(maxCharsLabel);
        maxCharsSlider = new Slider(10, 160, 45);
        GridPane.setConstraints(maxCharsSlider, 2, rows);
        getChildren().add(maxCharsSlider);
        maxCharsLabel.setLabelFor(maxCharsSlider);
        final Label maxCharsValue = new Label(Integer.toString((int) maxCharsSlider.getValue()));
        GridPane.setConstraints(maxCharsValue, 3, rows);
        getChildren().add(maxCharsValue);
        maxCharsValue.setLabelFor(maxCharsSlider);
        maxCharsSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                maxCharsValue.setText(Integer.toString((int) maxCharsSlider.getValue()));
            }
        });
        rows++;

//        Label minLinesLabel = new Label(LabelGrabber.INSTANCE.getLabel("min.emulated.lines.label") + " (" + LabelGrabber.INSTANCE.getLabel("advanced.label") + ")");
//        GridPane.setConstraints(minLinesLabel, 1, rows);
//        getChildren().add(minLinesLabel);
//        minLinesSlider = new Slider(1, 20, 0);
//        GridPane.setConstraints(minLinesSlider, 2, rows);
//        getChildren().add(minLinesSlider);
//        minLinesLabel.setLabelFor(minLinesSlider);
//        final Label minLinesValue = new Label(Integer.toString((int) minLinesSlider.getValue()));
//        GridPane.setConstraints(minLinesValue, 3, rows);
//        getChildren().add(minLinesValue);
//        minLinesValue.setLabelFor(minLinesSlider);
//        minLinesSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
//                minLinesValue.setText(Integer.toString((int) minLinesSlider.getValue()));
//            }
//        });
//        rows++;
        readProperties();
    }

    /**
     * Reset the mechanism for determining if the user has changed the interface
     * language. Call before showing the options dialog.
     */
    public void resetLanguageChanged() {
        currentLanguageFile = languageFileComboBox.getValue();
    }

    /**
     * Determine if the user has changed the interface language since the last
     * call of resetLanguageChanged().
     */
    public boolean hasLanguageChanged() {
        return !languageFileComboBox.getValue().equals(currentLanguageFile);
    }
    
    private void checkOverflowEnable() {
        if (advanceOnLiveCheckBox.isSelected()) {
            overflowSongCheckBox.setDisable(false);
        } else {
            overflowSongCheckBox.setDisable(true);
            overflowSongCheckBox.setSelected(false);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public final void readProperties() {
        languageFileComboBox.setValue(LanguageFileManager.INSTANCE.getCurrentFile());
        startupUpdateCheckBox.setSelected(props.checkUpdate());
        capitalFirstCheckBox.setSelected(props.checkCapitalFirst());
        oneMonitorWarnCheckBox.setSelected(props.showSingleMonitorWarning());
        uniformFontSizeCheckBox.setSelected(props.getUseUniformFontSize());
        defaultSongDBUpdateCheckBox.setSelected(!props.getDefaultSongDBUpdate());
        oneLineModeCheckBox.setSelected(props.getOneLineMode());
        autoTranslateCheckBox.setSelected(props.getAutoTranslate());
        autoPlayVidCheckBox.setSelected(props.getAutoPlayVideo());
        advanceOnLiveCheckBox.setSelected(props.getAdvanceOnLive());
        overflowSongCheckBox.setSelected(props.getSongOverflow());
        showVideoPanelCheckBox.setSelected(props.getDisplayVideoTab());
        previewOnImageChangeCheckBox.setSelected(props.getPreviewOnImageUpdate());
        clearLiveOnRemoveCheckBox.setSelected(props.getClearLiveOnRemove());
        embedMediaInScheduleCheckBox.setSelected(props.getEmbedMediaInScheduleFile());
        maxCharsSlider.setValue(props.getMaxChars());
//        minLinesSlider.setValue(props.getMinLines());
        showSmallSongTextBox.setSelected(props.getSmallSongTextShow());
        showSmallBibleTextBox.setSelected(props.getSmallBibleTextShow());
        smallBibleTextVPositionCombo.getSelectionModel().select(props.getSmallBibleTextPositionV().equals("top") ? 0 : 1);
        smallBibleTextHPositionCombo.getSelectionModel().select(props.getSmallBibleTextPositionH().equals("left") ? 0 : 1);
        smallBibleSizeSpinner.setNumber(new BigDecimal(props.getSmallBibleTextSize()));
        smallSongTextVPositionCombo.getSelectionModel().select(props.getSmallSongTextPositionV().equals("top") ? 0 : 1);
        smallSongTextHPositionCombo.getSelectionModel().select(props.getSmallSongTextPositionH().equals("left") ? 0 : 1);
        smallSongSizeSpinner.setNumber(new BigDecimal(props.getSmallSongTextSize()));
        additionalLineSpacingSlider.setValue(props.getAdditionalLineSpacing());
        maximumFontSizeSlider.setValue(props.getMaxFontSize());
        thumbnailSizeSlider.setValue(props.getThumbnailSize());
        checkOverflowEnable();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        props.setLanguageFile(languageFileComboBox.getValue().getFile().getName());
        boolean checkUpdate = getStartupUpdateCheckBox().isSelected();
        props.setCheckUpdate(checkUpdate);
        boolean showWarning = getOneMonitorWarningCheckBox().isSelected();
        props.setSingleMonitorWarning(showWarning);
        boolean checkCapital = getCapitalFirstCheckBox().isSelected();
        props.setCapitalFirst(checkCapital);
        boolean useUniformFontSize = uniformFontSizeCheckBox.isSelected();
        props.setUseUniformFontSize(useUniformFontSize);
        boolean defaultSongDBUpdate = !defaultSongDBUpdateCheckBox.isSelected();
        props.setDefaultSongDBUpdate(defaultSongDBUpdate);
        boolean clearLive = clearLiveOnRemoveCheckBox.isSelected();
        props.setClearLiveOnRemove(clearLive);
        boolean embedMedia = embedMediaInScheduleCheckBox.isSelected();
        props.setEmbedMediaInScheduleFile(embedMedia);
        boolean oneLineMode = getOneLineModeCheckBox().isSelected();
        props.setOneLineMode(oneLineMode);
        boolean autoTranslate = getAutoTranslateCheckBox().isSelected();
        props.setAutoTranslate(autoTranslate);
        boolean autoPlayVid = autoPlayVidCheckBox.isSelected();
        props.setAutoPlayVideo(autoPlayVid);
        boolean autoAdvance = advanceOnLiveCheckBox.isSelected();
        props.setAdvanceOnLive(autoAdvance);
        boolean overflow = overflowSongCheckBox.isSelected();
        props.setSongOverflow(overflow);
        boolean videoTab = showVideoPanelCheckBox.isSelected();
        props.setDisplayVideoTab(videoTab);
        boolean previewChange = previewOnImageChangeCheckBox.isSelected();
        props.setPreviewOnImageUpdate(previewChange);
        //One line mode needs to be updated manually
        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().updateOneLineMode();
        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().updateOneLineMode();
        int maxCharsPerLine = (int) getMaxCharsSlider().getValue();
        props.setMaxChars(maxCharsPerLine);
//        int minLines = (int) getMinLinesSlider().getValue();
//        props.setMinLines(minLines);
        boolean showSmallSongText = showSmallSongTextBox.isSelected();
        props.setSmallSongTextShow(showSmallSongText);
        boolean showSmallBibleText = showSmallBibleTextBox.isSelected();
        props.setSmallBibleTextShow(showSmallBibleText);
        int smallBibleTextVPosition = smallBibleTextVPositionCombo.getSelectionModel().getSelectedIndex();
        props.setSmallBibleTextPositionV(smallBibleTextVPosition == 0 ? "top" : "bottom");
        int smallBibleTextHPosition = smallBibleTextHPositionCombo.getSelectionModel().getSelectedIndex();
        props.setSmallBibleTextPositionH(smallBibleTextHPosition == 0 ? "left" : "right");
        double smallBibleSize = (smallBibleSizeSpinner.getNumber().doubleValue());
        props.setSmallBibleTextSize(smallBibleSize);
        int smallSongTextVPosition = smallSongTextVPositionCombo.getSelectionModel().getSelectedIndex();
        props.setSmallSongTextPositionV(smallSongTextVPosition == 0 ? "top" : "bottom");
        int smallSongTextHPosition = smallSongTextHPositionCombo.getSelectionModel().getSelectedIndex();
        props.setSmallSongTextPositionH(smallSongTextHPosition == 0 ? "left" : "right");
        double smallSongSize = (smallSongSizeSpinner.getNumber().doubleValue());
        props.setSmallSongTextSize(smallSongSize);
        props.setMaxFontSize(maximumFontSizeSlider.getValue());
        props.setAdditionalLineSpacing(additionalLineSpacingSlider.getValue());
        props.setThumbnailSize((int)thumbnailSizeSlider.getValue());
        
        //Initialise presentation
        if (!OOPresentation.isInit()) {
            OOUtils.attemptInit();
        }
    }

    /**
     * Get the max chars slider.
     * <p/>
     * @return the max chars slider.
     */
    public Slider getMaxCharsSlider() {
        return maxCharsSlider;
    }

//    /**
//     * Get the min lines slider.
//     * <p/>
//     * @return the min lines slider.
//     */
//    public Slider getMinLinesSlider() {
//        return minLinesSlider;
//    }
    /**
     * Get the startup readProperties checkbox.
     * <p/>
     * @return the startup readProperties checkbox.
     */
    public CheckBox getStartupUpdateCheckBox() {
        return startupUpdateCheckBox;
    }

    /**
     * Get the capitalise first character in each line checkbox.
     * <p/>
     * @return the capitalise first character in each line checkbox.
     */
    public CheckBox getCapitalFirstCheckBox() {
        return capitalFirstCheckBox;
    }

    /**
     * Get the "one monitor warning" checkbox.
     * <p/>
     * @return the "one monitor warning" checkbox.
     */
    public CheckBox getOneMonitorWarningCheckBox() {
        return oneMonitorWarnCheckBox;
    }

    /**
     * Get the "one line mode" checkbox.
     * <p/>
     * @return the "one line mode" checkbox.
     */
    public CheckBox getOneLineModeCheckBox() {
        return oneLineModeCheckBox;
    }

    /**
     * Get the "auto translate" checkbox.
     * <p/>
     * @return the "auto translate" checkbox.
     */
    public CheckBox getAutoTranslateCheckBox() {
        return autoTranslateCheckBox;
    }

    /**
     * Get the "use small song text" checkbox.
     * <p/>
     * @return the "use small song text" checkbox.
     */
    public CheckBox getShowSmallSongTextCheckBox() {
        return showSmallSongTextBox;
    }

    /**
     * Get the "use small bible text" checkbox.
     * <p/>
     * @return the "use small bible text" checkbox.
     */
    public CheckBox getShowSmallBibleTextCheckBox() {
        return showSmallBibleTextBox;
    }

    /**
     * Get the "use small Bible text" checkbox.
     * <p/>
     * @return the "use small Bible text" checkbox.
     */
    public ComboBox getSmallBibleTextVPositionComboBox() {
        return smallBibleTextVPositionCombo;
    }

    /**
     * Get the "use small Bible text" checkbox.
     * <p/>
     * @return the "use small Bible text" checkbox.
     */
    public ComboBox getSmallBibleTextHPositionComboBox() {
        return smallBibleTextHPositionCombo;
    }

    /**
     * Get the "use small Song text" checkbox.
     * <p/>
     * @return the "use small Song text" checkbox.
     */
    public ComboBox getSmallSongTextVPositionComboBox() {
        return smallSongTextVPositionCombo;
    }

    /**
     * Get the "use small Song text" checkbox.
     * <p/>
     * @return the "use small Song text" checkbox.
     */
    public ComboBox getSmallSongTextHPositionComboBox() {
        return smallSongTextHPositionCombo;
    }
}
