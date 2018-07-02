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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.WebDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;

/**
 * The panel displaying the preview lyrics selection - this is viewed before
 * displaying the actual lyrics on the projector.
 */
public class PreviewPanel extends LivePreviewPanel {

    private final Button liveButton;

    /**
     * Create a new preview lyrics panel.
     */
    public PreviewPanel() {
        ToolBar header = new ToolBar();
        Label headerLabel = new Label(LabelGrabber.INSTANCE.getLabel("preview.heading"));
        headerLabel.setStyle("-fx-font-weight: bold;");
        header.getItems().add(headerLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getItems().add(spacer);
        ImageView goLiveIV = new ImageView(new Image("file:icons/golivearrow.png"));
        goLiveIV.setFitHeight(16);
        goLiveIV.setFitWidth(16);
        liveButton = new Button(LabelGrabber.INSTANCE.getLabel("go.live.text"), goLiveIV);
        liveButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("go.live.text") + " (" + LabelGrabber.INSTANCE.getLabel("space.key") + ")"));
        liveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().setDisplayable(getDisplayable(), ((ContainedPanel) getCurrentPane()).getCurrentIndex());
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getCurrentPane().requestFocus();
                ListView<Displayable> list = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getListView();
                if (list.getSelectionModel().getSelectedIndex() < list.getItems().size() - 1 && QueleaProperties.get().getAdvanceOnLive()) {
                    list.getSelectionModel().clearAndSelect(list.getSelectionModel().getSelectedIndex() + 1);
                }
            }
        });
        header.getItems().add(liveButton);
        liveButton.setDisable(true);
        setTop(header);
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCharacter().equals(" ")) {
                    goLive();
                }
            }
        });
        setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.RIGHT) {
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().requestFocus();
                } else if (t.getCode() == KeyCode.LEFT) {
                    QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().requestFocus();
                }
            }
        });
    }

    /**
     * Transfer the current preview to live.
     */
    public void goLive() {
        liveButton.fire();
    }

    /**
     * Set the given displayable to be shown on the panel.
     * <p/>
     * @param d the displayable to show.
     * @param index an index that may be used or ignored depending on the
     * displayable.
     */
    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        liveButton.setDisable(false);
        if (d instanceof WebDisplayable) {
            final WebDisplayable webDisplayable = (WebDisplayable)d;
            Platform.runLater(() -> {
                if (!webDisplayable.equals(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable())) {
                    getWebPanel().addWebView(webDisplayable);
                    getWebPanel().blockButtons(false);
                } else {
                    getWebPanel().removeWebView();
                    getWebPanel().blockButtons(true);
                }
            });
        }
    }

    /**
     * Clear the preview panel.
     */
    @Override
    public void removeDisplayable() {
        super.removeDisplayable();
        liveButton.setDisable(true);
    }
}
