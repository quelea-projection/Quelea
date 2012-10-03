/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
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
package org.quelea.windows.main;

import com.sun.star.awt.grid.SelectionEventType;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.displayable.TextSection;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * A list displaying the different sections in the song.
 *
 * @author Michael
 */
public class SelectLyricsList extends ListView<TextSection> {

    private static final Cursor Q_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(Utils.getImage("icons/edit32.png"), new Point(0, 0), "Q Cursor");
    private boolean oneLineMode;

    /**
     * Create a new schedule list.
     */
    public SelectLyricsList() {
        setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {

            @Override
            public void handle(javafx.scene.input.MouseEvent t) {
//                requestFocus();
//                if(!t.isControlDown()) {
//                    int index = locationToIndex(t.getX(), t.getY());
//                    selectionModelProperty().get().select(index);
//                }
            }
        });
        oneLineMode = QueleaProperties.get().getOneLineMode();
//        Color inactiveColor = QueleaProperties.get().getInactiveSelectionColor();
//        if(inactiveColor == null) {
//            originalSelectionColour = getSelectionBackground();
//        }
//        else {
//            originalSelectionColour = inactiveColor;
//        }
//        setSelectionBackground(originalSelectionColour);
//        addFocusListener(new FocusListener() {
//
//            @Override
//            public void focusGained(FocusEvent e) {
//                if(getModel().getSize() > 0) {
//                    setSelectionBackground(QueleaProperties.get().getActiveSelectionColor());
//                }
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                setSelectionBackground(originalSelectionColour);
//            }
//        });

        setOnMouseMoved(new EventHandler<javafx.scene.input.MouseEvent>() {

            @Override
            public void handle(javafx.scene.input.MouseEvent t) {
                if((t.isShiftDown() || t.isControlDown()) && !itemsProperty().get().isEmpty()) {
//                    setCursor(Q_CURSOR);
                }
                else {
//                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
//        addKeyListener(new KeyAdapter() {
//
//            @Override
//            public void keyPressed(KeyEvent ke) {
//                if((ke.isControlDown()) && !getModel().isEmpty()) {
//                    setCursor(Q_CURSOR);
//                }
//                else {
//                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent ke) {
//                if(ke.getKeyCode() == KeyEvent.VK_CONTROL) {
//                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//                }
//            }
//        });
    }

    /**
     * Set whether this list should use one line mode.
     *
     * @param val true if it should be in one line mode, false otherwise.
     */
    public void setOneLineMode(boolean val) {
        if(this.oneLineMode == val) {
            return;
        }
        this.oneLineMode = val;
        int selectedIndex = selectionModelProperty().get().getSelectedIndex();
        List<TextSection> elements = new ArrayList<>(itemsProperty().get().size());
        for(int i = 0; i < itemsProperty().get().size(); i++) {
            elements.add(itemsProperty().get().get(i));
        }
        itemsProperty().get().clear();
        for(TextSection section : elements) {
            itemsProperty().get().add(section);
        }
        selectionModelProperty().get().select(selectedIndex);
        scrollTo(selectedIndex);
    }
}
