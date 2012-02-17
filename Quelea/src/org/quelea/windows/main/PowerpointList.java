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
import java.awt.image.BufferedImage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import org.quelea.powerpoint.PresentationSlide;
import org.quelea.utils.QueleaProperties;

/**
 * A JList for specifically displaying powerpoint slides.
 * @author Michael
 */
public class PowerpointList extends JList<PresentationSlide> {

    private Color originalSelectionColour;

    /**
     * Create a new powerpoint list.
     */
    public PowerpointList() {
        setModel(new DefaultListModel<PresentationSlide>());
        setCellRenderer(new CustomCellRenderer());
        Color inactiveColor = QueleaProperties.get().getInactiveSelectionColor();
        if (inactiveColor == null) {
            originalSelectionColour = getSelectionBackground();
        }
        else {
            originalSelectionColour = inactiveColor;
        }
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
    }

    /**
     * Clear all current slides and set the slides in the list.
     * @param slides the slides to put in the list.
     */
    public void setSlides(PresentationSlide[] slides) {
        DefaultListModel<PresentationSlide> model = (DefaultListModel<PresentationSlide>)getModel();
        model.clear();
        for (PresentationSlide slide : slides) {
            model.addElement(slide);
        }
    }

    /**
     * Get the current image in use at the specified width / height.
     * @param width the width of the image.
     * @param height the height of the image.
     * @return the selected slide image at the given dimensions.
     */
    public BufferedImage getCurrentImage(int width, int height) {
        if (getSelectedValue() == null) {
            return new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        }
        return getModel().getElementAt(getSelectedIndex()).getImage(width, height);
    }

    /**
     * The custom cell renderer for the JList behind the panel.
     */
    private static class CustomCellRenderer extends DefaultListCellRenderer {

        /**
         * Get the component to display in the list using the custom renderer.
         * @param list the list to apply the renderer to.
         * @param value the value to render.
         * @param index the currently selected index.
         * @param isSelected true if the value object is selected.
         * @param cellHasFocus true if the cell has focus, false otherwise.
         * @return the component to display as this cell.
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            CustomCellRenderer ret = (CustomCellRenderer) super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
            ret.setBorder(new EmptyBorder(10, 5, 10, 5));
            ret.setIcon(new ImageIcon(((PresentationSlide) value).getImage( list.getWidth() > 400 ? 390 : list.getWidth() - 10, 200)));
            return ret;
        }
    }
}
