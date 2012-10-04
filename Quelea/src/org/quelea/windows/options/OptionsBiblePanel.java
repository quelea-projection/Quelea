/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import name.antonsmirnov.javafx.dialog.Dialog;
import org.quelea.Application;
import org.quelea.bible.Bible;
import org.quelea.bible.BibleChangeListener;
import org.quelea.bible.BibleManager;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.FileFilters;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * The panel that shows the bible options
 * <p/>
 * @author Michael
 */
public class OptionsBiblePanel extends GridPane implements PropertyPanel, BibleChangeListener {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final ComboBox<Bible> defaultBibleComboBox;

    /**
     * Create the options bible panel.
     */
    public OptionsBiblePanel() {
        setVgap(5);

        Label defaultLabel = new Label(LabelGrabber.INSTANCE.getLabel("default.bible.label"));
        GridPane.setConstraints(defaultLabel, 1, 1);
        getChildren().add(defaultLabel);
        BibleManager.get().registerBibleChangeListener(this);
        defaultBibleComboBox = new ComboBox<>();
        defaultBibleComboBox.itemsProperty().set(FXCollections.observableArrayList(BibleManager.get().getBibles()));
        defaultLabel.setLabelFor(defaultBibleComboBox);
        GridPane.setConstraints(defaultBibleComboBox, 2, 1);
        getChildren().add(defaultBibleComboBox);

        final Button addBibleButton = new Button(LabelGrabber.INSTANCE.getLabel("add.bible.label"), new ImageView(new Image("file:icons/add.png")));
        addBibleButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(FileFilters.XML_BIBLE);
                File file = chooser.showOpenDialog(Application.get().getMainWindow());
                if(file != null) {
                    try {
                        Utils.copyFile(file, new File(QueleaProperties.get().getBibleDir(), file.getName()));
                    }
                    catch(IOException ex) {
                        LOGGER.log(Level.WARNING, "Errpr copying bible file", ex);
                        Dialog.showError(LabelGrabber.INSTANCE.getLabel("bible.copy.error.heading"), LabelGrabber.INSTANCE.getLabel("bible.copy.error.text"));
                    }
                }
            }
        });
        GridPane.setConstraints(addBibleButton, 1, 3);
        getChildren().add(addBibleButton);

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
                for(Bible bible : BibleManager.get().getBibles()) {
                    defaultBibleComboBox.itemsProperty().get().add(bible);
                }
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
        for(int i = 0; i < defaultBibleComboBox.itemsProperty().get().size(); i++) {
            Bible bible = defaultBibleComboBox.itemsProperty().get().get(i);
            if(bible.getName().equals(selectedBibleName)) {
                defaultBibleComboBox.getSelectionModel().select(i);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        Bible bible = getDefaultBibleBox().getSelectionModel().getSelectedItem();
        props.setDefaultBible(bible);
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
