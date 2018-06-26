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
package org.quelea.windows.main;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.quelea.services.utils.Utils;

/**
 * A status panel that denotes a background task in Quelea.
 * <p/>
 * @author Michael
 */
public class StatusPanel extends HBox {

    private ProgressBar progressBar;
    private Label label;
    private Button cancelButton;
    private StatusPanelGroup group;
    private int index;

    /**
     * Create a new status panel.
     * <p/>
     * @param group the group this panel is part of.
     * @param labelText the text to put on the label on this panel.
     * @param index the index of this panel on the group.
     */
    StatusPanel(StatusPanelGroup group, String labelText, int index) {
        setAlignment(Pos.CENTER);
        setSpacing(5);
        this.group = group;
        this.index = index;
        label = new Label(labelText);
        label.setAlignment(Pos.CENTER);
        label.setMaxHeight(Double.MAX_VALUE);
        HBox.setMargin(label, new Insets(5));
        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE); //Allow progress bar to fill space.
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        cancelButton = new Button("", new ImageView(new Image("file:icons/cross.png", 13, 13, false, true)));
        Utils.setToolbarButtonStyle(cancelButton);
        cancelButton.setAlignment(Pos.CENTER);
        getChildren().add(label);
        getChildren().add(progressBar);
        getChildren().add(cancelButton);
    }
    
    /**
     * Remove the cancel button from this status bar.
     */
    public void removeCancelButton() {
        getChildren().remove(cancelButton);
    }
    
    /**
     * Convenience method to set the progress of the progress bar. Thread safe.
     * @param progress the progress to set the bar to, between 0-1.
     */
    public void setProgress(final double progress) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });
    }
    
    private double progressVal = 0;
    /**
     * Convenience method to get the current progress. Thread safe.
     * @return the current progress.
     */
    public double getProgress() {
        progressVal = 0;
        Utils.fxRunAndWait(new Runnable() {

            @Override
            public void run() {
                progressVal = progressBar.getProgress();
            }
        });
        return progressVal;
    }

    /**
     * Called to indicate that the task associated with this panel has finished,
     * and therefore the panel can be removed.
     */
    public void done() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                group.removePanel(index);
            }
        });
    }

    /**
     * Set the label text for this panel.
     * <p/>
     * @param text the text on this panel's label.
     */
    public void setLabelText(String text) {
        label.setText(text);
    }

    /**
     * Get the progress bar associated with this panel.
     * <p/>
     * @return the progress bar associated with this panel.
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Get the cancel button on this panel.
     * <p/>
     * @return the cancel button on this panel.
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Set whether this panel is active.
     * <p/>
     * @param active true if active, false otherwise.
     */
    public void setActive(boolean active) {
        setVisible(active);
    }
}
