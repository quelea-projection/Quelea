/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.widgets.NumberSpinner;
import org.quelea.windows.main.widgets.NumberTextField;

/**
 * A panel used to represent a single type of display that the user can then
 * select the output for.
 *
 * @author Michael
 */
public class SingleDisplayPanel extends VBox {

    private final boolean none;
    private final ComboBox<String> outputSelect;
    private CheckBox custom;
    private NumberTextField customX;
    private NumberTextField customY;
    private NumberTextField customWidth;
    private NumberTextField customHeight;

    /**
     * Create a new single display panel.
     *
     * @param caption the bit of text at the top describing the display.
     * @param iconLocation the location of the icon to use.
     * @param none true if "none" (i.e. no output) should be an option, false
     * otherwise.
     * @param customPos true if a custom position should be allowed for this
     * display panel.
     */
    public SingleDisplayPanel(String caption, String iconLocation, boolean none,
            boolean customPos) {
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);
        this.none = none;
        Label captionLabel = new Label(caption);
        getChildren().add(captionLabel);
        Label iconLabel = new Label("",new ImageView(new Image("file:"+iconLocation, 80, 80, false, true)));
        getChildren().add(iconLabel);
        outputSelect = new ComboBox<>(getAvailableScreens(none));
        getChildren().add(outputSelect);
        if(customPos) {
            outputSelect.setDisable(true);
            custom = new CheckBox(LabelGrabber.INSTANCE.getLabel("custom.position.text"));
            custom.setSelected(true);
            custom.selectedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    if(custom.isSelected()) {
                        outputSelect.setDisable(true);
                        customX.setDisable(false);
                        customY.setDisable(false);
                        customWidth.setDisable(false);
                        customHeight.setDisable(false);
                    }
                    else {
                        outputSelect.setDisable(false);
                        customX.setDisable(true);
                        customY.setDisable(true);
                        customWidth.setDisable(true);
                        customHeight.setDisable(true);
                    }
                }
            });
            getChildren().add(custom);

            GridPane posPanel = new GridPane();
            posPanel.setVgap(5);
            customX = new NumberTextField(0);
            customY = new NumberTextField(0);
            customWidth = new NumberTextField(0);
            customHeight = new NumberTextField(0);
            
            Label xLabel = new Label("X:");
            GridPane.setConstraints(xLabel, 1, 1);
            posPanel.getChildren().add(xLabel);
            GridPane.setConstraints(customX, 2, 1);
            posPanel.getChildren().add(customX);
            Label yLabel = new Label(" Y:");
            GridPane.setConstraints(yLabel, 3, 1);
            posPanel.getChildren().add(yLabel);
            GridPane.setConstraints(customY, 4, 1);
            posPanel.getChildren().add(customY);
            Label wLabel = new Label("W:");
            GridPane.setConstraints(wLabel, 1, 2);
            posPanel.getChildren().add(wLabel);
            GridPane.setConstraints(customWidth, 2, 2);
            posPanel.getChildren().add(customWidth);
            Label hLabel = new Label(" H:");
            GridPane.setConstraints(hLabel, 3, 2);
            posPanel.getChildren().add(hLabel);
            GridPane.setConstraints(customHeight, 4, 2);
            posPanel.getChildren().add(customHeight);
            posPanel.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(posPanel);
        }
    }

    /**
     * Get the output screen currently selected in the dialog, or -1 if none is
     * selected.
     *
     * @return the output screen currently selected in the dialog
     */
    public int getOutputScreen() {
        int screenNum;
        if(none) {
            screenNum = outputSelect.getSelectionModel().getSelectedIndex() - 1;
        }
        else {
            screenNum = outputSelect.getSelectionModel().getSelectedIndex();
        }
        return screenNum;
    }

    /**
     * Determine the output bounds that should be used.
     *
     * @return the output bounds as a rectangle, or null if "none" is selected.
     */
    public Bounds getOutputBounds() {
        if(custom != null && custom.isSelected()) {
            return getCoords();
        }
        ObservableList<Screen> monitors = Screen.getScreens();
        
        int screen = getOutputScreen();
        if(screen < 0) {
            return null;
        }
        else {
            return Utils.getBoundsFromRect2D(monitors.get(screen).getBounds());
        }
    }

    /**
     * Determine whether the panel is set to a custom position.
     *
     * @return the bounds for the custom position.
     */
    public boolean customPosition() {
        return custom.isSelected();
    }

    /**
     * Get the bounds currently selected on the dialog.
     *
     * @return the bounds currently selected.
     */
    public Bounds getCoords() {
        return new BoundingBox(customX.getNumber(), customY.getNumber(), customWidth.getNumber(), customHeight.getNumber());
    }

    /**
     * Set the bounds to display on the panel.
     *
     * @param bounds the bounds to display.
     */
    public void setCoords(Bounds bounds) {
        if(custom != null) {
            custom.setSelected(true);
        }
        customX.setNumber((int) bounds.getMinX());
        customY.setNumber((int) bounds.getMinY());
        customWidth.setNumber((int) bounds.getWidth());
        customHeight.setNumber((int) bounds.getHeight());
    }

    /**
     * Set the screen to select on the combo box.
     *
     * @param num the index (0 based) of the screen to select.
     */
    public void setScreen(int num) {
        if(custom != null) {
            custom.setSelected(false);
        }
        int maxIndex = outputSelect.itemsProperty().get().size() - 1;
        if(none) {
            int index = num + 1;
            if(index > maxIndex) {
                index = 0;
            }
            outputSelect.getSelectionModel().select(index);
        }
        else {
            int index = num;
            if(index > maxIndex) {
                index = 0;
            }
            outputSelect.getSelectionModel().select(index);
        }
    }

    /**
     * Update the display panel with the monitor information.
     */
    public void update() {
        outputSelect.itemsProperty().set(getAvailableScreens(none));
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
        if(none) {
            descriptions.add(LabelGrabber.INSTANCE.getLabel("none.text"));
        }
        for(int i = 0; i < monitors.size(); i++) {
            descriptions.add(LabelGrabber.INSTANCE.getLabel("output.text") + " " + (i + 1));
        }
        return descriptions;
    }

}
