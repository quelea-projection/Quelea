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
package org.quelea.windows.lyrics;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.quelea.data.displayable.TextSection;
import org.quelea.services.utils.QueleaProperties;

/**
 * List cell for SelectLyricsList
 *
 * @author Michael
 */
public class LyricListCell extends ListCell<TextSection> {

    private final VBox layout;
    private final Text header;
    private final Text lyrics;
    private boolean selected, focused;
    private SelectLyricsList selectLyricsList;

    public LyricListCell(SelectLyricsList sll) {
        selectLyricsList = sll;
        layout = new VBox(3);
        header = new Text();
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 11.5));
        lyrics = new Text();
        lyrics.getStyleClass().add("text");
        layout.getChildren().addAll(header, lyrics);
//        lyrics.setFill(Color.BLACK);
        selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            selected = newValue;
            updateState();
        });
        setOnMouseEntered((MouseEvent t) -> {
            updateState();
        });
        setOnMouseExited((MouseEvent t) -> {
            updateState();
        });
        focused = sll.focusedProperty().get();
        sll.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            focused = newValue;
            updateState();
        });
    }

    @Override
    public void updateItem(TextSection section, boolean empty) {
        super.updateItem(section, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setContent(section);
        }
    }

    private void setContent(TextSection section) {
        setText(null);
        String[] text = section.getText(false, false);
        StringBuilder builder = new StringBuilder();
        final boolean oneLineMode = QueleaProperties.get().getOneLineMode();
        for (String str : text) {
            str = FormattedText.stripFormatTags(str);
            builder.append(str);
            if (oneLineMode) {
                builder.append(" ");
            } else {
                builder.append("\n");
            }
        }
        String str = builder.toString().trim();
        if (!oneLineMode) {
            lyrics.wrappingWidthProperty().bind(selectLyricsList.widthProperty().subtract(15));
        }
        lyrics.setText(str);
        String title = section.getTitle();
        if (title == null || title.isEmpty()) {
            layout.getChildren().remove(header);
        } else {
            if (!layout.getChildren().contains(header)) {
                layout.getChildren().add(0, header);
            }
            header.setText(section.getTitle());
            if (section.getTitle().toLowerCase().startsWith("chorus")) {
                header.setFill(QueleaProperties.get().getUseDarkTheme() ? Color.RED : Color.DARKRED);
            } else if (section.getTitle().toLowerCase().startsWith("verse")) {
                header.setFill(QueleaProperties.get().getUseDarkTheme() ? Color.LIGHTBLUE : Color.DARKBLUE);
            } else {
                header.setFill(QueleaProperties.get().getUseDarkTheme() ? Color.LIGHTGREEN : Color.DARKGREEN);
            }
        }
        lyrics.getStyleClass().add("cell-text");
        setGraphic(layout);
    }

    private void updateState() {
        if (!QueleaProperties.get().getUseDarkTheme()) {
            if (selected && focused) {
                lyrics.setFill(Color.WHITE);
                setStyle("-fx-background-color:#0093ff;");
            } else if (selected) {
                lyrics.setFill(Color.BLACK);
                setStyle("-fx-background-color:#D3D3D3;");
            } else {
                lyrics.setFill(Color.BLACK);
                setStyle("-fx-background-color:none;");
            }
        }
    }
}
