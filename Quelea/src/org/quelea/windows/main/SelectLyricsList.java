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
package org.quelea.windows.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import org.quelea.displayable.TextSection;
import org.quelea.utils.QueleaProperties;

/**
 * A list displaying the different sections in the song.
 * @author Michael
 */
public class SelectLyricsList extends JList<TextSection> {

    private final Color originalSelectionColour;

    /**
     * Used for displaying summaries of items in the service in the schedule list.
     */
    private static class SelectLyricsRenderer extends JLabel implements ListCellRenderer<TextSection> {

        private boolean displayChords;

        public SelectLyricsRenderer(boolean displayChords) {
            this.displayChords = displayChords;
        }

        /**
         * @inheritDoc
         */
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
                labelHTML.append("<br/>");
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
     * Create a new schedule list with a given model.
     * @param model the model to display.
     */
    public SelectLyricsList() {
        super(new DefaultListModel<TextSection>());
        originalSelectionColour = getSelectionBackground();
        addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (getModel().getSize() > 0) {
                    setSelectionBackground(QueleaProperties.get().getActiveSelectionColor());
                }
            }

            public void focusLost(FocusEvent e) {
                setSelectionBackground(originalSelectionColour);
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new SelectLyricsRenderer(false));
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
