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

import java.util.ArrayList;
import java.util.List;
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
import org.quelea.data.ColourBackground;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.newsong.ThemePanel;

/**
 * Called when the theme of the current item in the schedule should be edited.
 *
 * @author Ben
 */
public class EditThemeScheduleActionHandler implements EventHandler<ActionEvent> {

    private TextDisplayable selectedDisplayable;

    public EditThemeScheduleActionHandler() {
        this(null);
    }

    public EditThemeScheduleActionHandler(TextDisplayable selectedDisplayable) {
        this.selectedDisplayable = selectedDisplayable;
    }

    /**
     * Edit the theme of the currently selected item in the schedule.
     *
     * @param t the action event.
     */
    @Override
    public void handle(ActionEvent t) {
        TextDisplayable firstSelected = selectedDisplayable;
        if (selectedDisplayable == null) {
            firstSelected = (TextDisplayable) QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSelectionModel().getSelectedItem();
        }
        InlineCssTextArea wordsArea = new InlineCssTextArea();
        wordsArea.replaceText(firstSelected.getSections()[0].toString().trim());
        Button confirmButton = new Button(LabelGrabber.INSTANCE.getLabel("ok.button"), new ImageView(new Image("file:icons/ic-tick.png",16,16,false,true)));
        Button cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), new ImageView(new Image("file:icons/ic-cancel.png",16,16,false,true)));
        final Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.initOwner(QueleaApp.get().getMainWindow());
        s.resizableProperty().setValue(false);
        final BorderPane bp = new BorderPane();
        final ThemePanel tp = new ThemePanel(wordsArea, confirmButton, true);
        tp.setPrefSize(500, 500);
        if (firstSelected.getSections().length > 0) {
            tp.setTheme(firstSelected.getSections()[0].getTheme());
        }
        confirmButton.setOnAction(e -> {
            if (tp.getTheme() != null) {
                ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
                tp.updateTheme(false);
                List<Displayable> displayableList;
                if (selectedDisplayable == null) {
                    displayableList = sl.getSelectionModel().getSelectedItems();
                } else {
                    displayableList = new ArrayList<>();
                    displayableList.add(selectedDisplayable);
                }
                for (Displayable eachDisplayable : displayableList) {
                    if (eachDisplayable instanceof TextDisplayable) {
                        ((TextDisplayable) eachDisplayable).setTheme(tp.getTheme());
                        for (TextSection ts : ((TextDisplayable) eachDisplayable).getSections()) {
                            ts.setTheme(tp.getTheme());
                        }
                        if (eachDisplayable instanceof SongDisplayable) {
                            Utils.updateSongInBackground((SongDisplayable) eachDisplayable, true, false);
                        }
                    }
                }
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
            }
            s.hide();
        });
        cancelButton.setOnAction(e -> {
            s.hide();
        });
        bp.setCenter(tp);

        HBox hb = new HBox(10);
        hb.setPadding(new Insets(10));
        BorderPane.setAlignment(hb, Pos.CENTER);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(confirmButton, cancelButton);
        bp.setBottom(hb);

        Scene scene = new Scene(bp);
        if (QueleaProperties.get().getUseDarkTheme()) {
            scene.getStylesheets().add("org/modena_dark.css");
        }
        s.setScene(scene);
        s.setMinHeight(600);
        s.setMinWidth(250);
        s.showAndWait();
    }

}
