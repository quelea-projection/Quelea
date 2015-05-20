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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleChangeListener;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.PropertyPanel;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;

/**
 * The panel that shows the bible options
 * <p/>
 * @author Michael
 */
public class OptionsBiblePanel extends GridPane implements PropertyPanel, BibleChangeListener {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final ComboBox<Bible> defaultBibleComboBox;
    private final CheckBox showVerseNumCheckbox;
    private final ComboBox<String> useBibleVersesBox;
//    private final NumberTextField maxItemsPerSlideBox;
//    private final Slider maxCharsSlider;
    private boolean changed;

    /**
     * Create the options bible panel.
     */
    public OptionsBiblePanel() {
        setVgap(5);
        setHgap(10);
        setPadding(new Insets(5));

        Label defaultBibleLabel = new Label(LabelGrabber.INSTANCE.getLabel("default.bible.label"));
        GridPane.setConstraints(defaultBibleLabel, 1, 1);
        getChildren().add(defaultBibleLabel);
        BibleManager.get().registerBibleChangeListener(this);
        defaultBibleComboBox = new ComboBox<>();
        defaultBibleComboBox.itemsProperty().set(FXCollections.observableArrayList(BibleManager.get().getBibles()));
        defaultBibleLabel.setLabelFor(defaultBibleComboBox);
        GridPane.setConstraints(defaultBibleComboBox, 2, 1);
        getChildren().add(defaultBibleComboBox);

        final Button addBibleButton = new Button(LabelGrabber.INSTANCE.getLabel("add.bible.label"), new ImageView(new Image("file:icons/add.png")));
        addBibleButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                FileChooser chooser = new FileChooser();
                if (QueleaProperties.get().getLastDirectory() != null) {
                    chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
                }
                chooser.getExtensionFilters().add(FileFilters.XML_BIBLE);
                File file = chooser.showOpenDialog(QueleaApp.get().getMainWindow());
                if (file != null) {
                    QueleaProperties.get().setLastDirectory(file.getParentFile());
                    try {
                        Utils.copyFile(file, new File(QueleaProperties.get().getBibleDir(), file.getName()));
                        BibleManager.get().refreshAndLoad();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Errpr copying bible file", ex);
                        Dialog.showError(LabelGrabber.INSTANCE.getLabel("bible.copy.error.heading"), LabelGrabber.INSTANCE.getLabel("bible.copy.error.text"));
                    }
                }
            }
        });
        GridPane.setConstraints(addBibleButton, 3, 1);
        getChildren().add(addBibleButton);

        Label showVerseNumLabel = new Label(LabelGrabber.INSTANCE.getLabel("show.verse.numbers"));
        GridPane.setConstraints(showVerseNumLabel, 1, 2);
        getChildren().add(showVerseNumLabel);
        showVerseNumCheckbox = new CheckBox();
        showVerseNumLabel.setLabelFor(showVerseNumCheckbox);
        GridPane.setConstraints(showVerseNumCheckbox, 2, 2);
        getChildren().add(showVerseNumCheckbox);

        Label useBibleVersesLabel = new Label(LabelGrabber.INSTANCE.getLabel("use.bible.verses"));
        GridPane.setConstraints(useBibleVersesLabel, 1, 3);
        getChildren().add(useBibleVersesLabel);
        useBibleVersesBox = new ComboBox<>();
        useBibleVersesBox.getItems().addAll(LabelGrabber.INSTANCE.getLabel("verses"), LabelGrabber.INSTANCE.getLabel("words"));
        useBibleVersesLabel.setLabelFor(useBibleVersesBox);
        GridPane.setConstraints(useBibleVersesBox, 2, 3);
        getChildren().add(useBibleVersesBox);

//        final String[] labels = LabelGrabber.INSTANCE.getLabel("max.items.per.slide").split("%");
//        final Label maxItemsPerSlideLabel = new Label("");
//        GridPane.setConstraints(maxItemsPerSlideLabel, 1, 4);
//        getChildren().add(maxItemsPerSlideLabel);
//        maxItemsPerSlideBox = new NumberTextField();
//        maxItemsPerSlideLabel.setLabelFor(maxItemsPerSlideBox);
//        GridPane.setConstraints(maxItemsPerSlideBox, 2, 4);
////        getChildren().add(maxItemsPerSlideBox);
//
        useBibleVersesBox.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            //maxItemsPerSlideLabel.setText(labels[0] + newValue + labels[1]);
            changed = true;
        });
//
//        maxItemsPerSlideBox.textProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                changed = true;
//            }
//        });

//        Label maxCharsLabel = new Label(LabelGrabber.INSTANCE.getLabel("max.chars.line.label"));
//
//        GridPane.setConstraints(maxCharsLabel, 1, 5);
//        getChildren().add(maxCharsLabel);
//        maxCharsSlider = new Slider(10, 160, 0);
//
//        GridPane.setConstraints(maxCharsSlider, 2, 5);
////        getChildren().add(maxCharsSlider);
//        maxCharsLabel.setLabelFor(maxCharsSlider);
//        final Label maxCharsValue = new Label(Integer.toString((int) maxCharsSlider.getValue()));
//
//        GridPane.setConstraints(maxCharsValue, 3, 5);
//        getChildren().add(maxCharsValue);
//        maxCharsValue.setLabelFor(maxCharsSlider);
//
//        maxCharsSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
//                maxCharsValue.setText(Integer.toString((int) maxCharsSlider.getValue()));
//            }
//        });

        readProperties();

    }

    /**
     * Update all the bibles in the panel.
     */
    @Override
    public void updateBibles() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                defaultBibleComboBox.itemsProperty().get().clear();
                for (Bible bible : BibleManager.get().getBibles()) {
                    defaultBibleComboBox.itemsProperty().get().add(bible);
                }
                readProperties();
            }
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        String selectedBibleName = props.getDefaultBible();
        for (int i = 0; i < defaultBibleComboBox.itemsProperty().get().size(); i++) {
            Bible bible = defaultBibleComboBox.itemsProperty().get().get(i);
            if (bible.getBibleName().equals(selectedBibleName)) {
                defaultBibleComboBox.getSelectionModel().select(i);
            }
        }
        showVerseNumCheckbox.setSelected(props.getShowVerseNumbers());
        useBibleVersesBox.getSelectionModel().select((props.getBibleSectionVerses()) ? 0 : 1);
//        maxItemsPerSlideBox.setNumber(props.getMaxBibleItems());
//        maxCharsSlider.setValue(props.getMaxBibleChars());

    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        Bible bible = getDefaultBibleBox().getSelectionModel().getSelectedItem();
        if (bible != null) {
            props.setDefaultBible(bible);
        }
        props.setShowVerseNumbers(showVerseNumCheckbox.isSelected());
        props.setBibleSectionVerses(useBibleVersesBox.getSelectionModel().getSelectedIndex() == 0);
//        props.setMaxBibleItems(maxItemsPerSlideBox.getNumber());
//        int maxCharsPerLine = (int) maxCharsSlider.getValue();
//        props.setMaxBibleChars(maxCharsPerLine);
        if (changed) {
            if (QueleaApp.get().getMainWindow().getMainPanel() != null) {
                ScheduleList list = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
                for (Displayable d : list.getItems()) {
                    if (d != null) {
                        if (d instanceof BiblePassage) {
                            ((BiblePassage) d).updateBibleLines();
                            int index = list.getListView().itemsProperty().get().indexOf(d);
                            if (index != -1) {
                                list.getListView().itemsProperty().get().set(index, d);
                                list.getListView().selectionModelProperty().get().select(index); //Needed for single item lists
                            }
                        }
                    }
                }
            }
            changed = false;
        }
    }

    /**
     * Get the default bible combo box.
     * <p/>
     * @return the default bible combo box.
     */
    public ComboBox<Bible> getDefaultBibleBox() {
        return defaultBibleComboBox;
    }
}
