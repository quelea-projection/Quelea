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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.quelea.displayable.TextSection;
import org.quelea.utils.QueleaProperties;

/**
 * A list displaying the different sections in the song.
 *
 * @author Michael
 */
public class SelectLyricsList extends JList<TextSection> {

    private final Color originalSelectionColour;
    private boolean oneLineMode;
    
    /**
     * Used for displaying summaries of items in the service in the schedule list.
     */
    private class SelectLyricsRenderer extends JLabel implements ListCellRenderer<TextSection> {

        private boolean displayChords;

        public SelectLyricsRenderer(boolean displayChords) {
            this.displayChords = displayChords;
        }

        /**
         * @inheritDoc
         */
        @Override
        public Component getListCellRendererComponent(JList<? extends TextSection> list, TextSection value, int index, boolean isSelected, boolean cellHasFocus) {
            setBorder(new EmptyBorder(5, 5, 5, 5));
            StringBuilder labelHTML = new StringBuilder();
            labelHTML.append("<html>");
            if (!value.getTitle().trim().equals("")) {
                labelHTML.append("<font color=\"white\"><span style=\"background-color:blue; width:100%;\">&nbsp;");
                labelHTML.append(value.getTitle());
                labelHTML.append("&nbsp;</span></font><br/>");
            }
            for (String line : value.getText(displayChords, false)) {
                labelHTML.append(line);
                if (oneLineMode) {
                    char lastChar = labelHTML.substring(labelHTML.length()-1, labelHTML.length()).charAt(0);
                    if(lastChar!=','&&lastChar!=';') {
                        labelHTML.append(";");
                    }
                    labelHTML.append(" ");
                }
                else {
                    labelHTML.append("<br/>");
                }
            }
            labelHTML.append("</html>");
            setText(labelHTML.toString());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    /**
     * Create a new schedule list.
     */
    public SelectLyricsList() {
        super(new DefaultListModel<TextSection>());
        oneLineMode = QueleaProperties.get().getOneLineMode();
        Color inactiveColor = QueleaProperties.get().getInactiveSelectionColor();
        if (inactiveColor == null) {
            originalSelectionColour = getSelectionBackground();
        }
        else {
            originalSelectionColour = inactiveColor;
        }
        setSelectionBackground(originalSelectionColour);
        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (getModel().getSize() > 0) {
                    setSelectionBackground(QueleaProperties.get().getActiveSelectionColor());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                setSelectionBackground(originalSelectionColour);
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new SelectLyricsRenderer(false));
    }

    /**
     * Set whether this list should use one line mode.
     * @param val true if it should be in one line mode, false otherwise.
     */
    public void setOneLineMode(boolean val) {
        if (this.oneLineMode == val) {
            return;
        }
        this.oneLineMode = val;
        int selectedIndex = getSelectedIndex();
        List<TextSection> elements = new ArrayList<>(getModel().size());
        for (int i = 0; i < getModel().size(); i++) {
            elements.add(getModel().get(i));
        }
        getModel().clear();
        for (TextSection section : elements) {
            getModel().addElement(section);
        }
        setSelectedIndex(selectedIndex);
        ensureIndexIsVisible(selectedIndex);
    }

    /**
     * @return a DefaultListModel that backs this lyrics list.
     * @inheritDoc
     */
    @Override
    public DefaultListModel<TextSection> getModel() {
        return (DefaultListModel<TextSection>) super.getModel();
    }
}
