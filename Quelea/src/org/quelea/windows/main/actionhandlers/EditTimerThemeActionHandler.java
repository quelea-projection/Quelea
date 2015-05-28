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
package org.quelea.windows.main.actionhandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.newsong.ThemePanel;

/**
 * Called when the theme of the current timer in the schedule should be edited.
 *
 * @author Ben
 */
public class EditTimerThemeActionHandler implements EventHandler<ActionEvent> {

    private TimerDisplayable selectedDisplayable;

    public EditTimerThemeActionHandler() {
        this(null);
    }

    public EditTimerThemeActionHandler(TimerDisplayable selectedDisplayable) {
        this.selectedDisplayable = selectedDisplayable;
    }

    /**
     * Edit the theme of the currently selected item in the schedule.
     *
     * @param t the action event.
     */
    @Override
    public void handle(ActionEvent t) {
        InlineCssTextArea wordsArea = new InlineCssTextArea();
        if(selectedDisplayable == null) {
            selectedDisplayable = (TimerDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSelectionModel().getSelectedItem();
        }
        wordsArea.replaceText(selectedDisplayable.getPretext() + selectedDisplayable.secondsToTime(selectedDisplayable.getSeconds()) + selectedDisplayable.getPosttext());
        Button confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/tick.png")));
        Button cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/cross.png")));
        final Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.initOwner(QueleaApp.get().getMainWindow());
        s.resizableProperty().setValue(false);
        s.setTitle(LabelGrabber.INSTANCE.getLabel("edit.theme.text"));
        final BorderPane bp = new BorderPane();
        final ThemePanel tp = new ThemePanel(wordsArea, confirmButton);
        tp.setPrefSize(500, 500);
        tp.setTheme(selectedDisplayable.getTheme());
        confirmButton.setOnAction((ActionEvent event) -> {
            if (tp.getTheme() != null) {
                tp.setTheme(tp.getTheme());
                selectedDisplayable.setTheme(tp.getTheme());
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                if(QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable().equals(selectedDisplayable)) {
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().refresh();
                }
            }
            s.hide();
        });
        cancelButton.setOnAction((ActionEvent event) -> {
            s.hide();
        });
        bp.setCenter(tp);

        HBox hb = new HBox(10);
        hb.setPadding(new Insets(10));
        BorderPane.setAlignment(hb, Pos.CENTER);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(confirmButton, cancelButton);
        bp.setBottom(hb);

        s.setScene(new Scene(bp));
        s.setMinHeight(600);
        s.setMinWidth(250);
        s.showAndWait();
    }

}
