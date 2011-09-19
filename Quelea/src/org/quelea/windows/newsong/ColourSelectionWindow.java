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
package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.quelea.utils.Utils;

/**
 * A colour selection window where the user can select a colour they require.
 * @author Michael
 */
public class ColourSelectionWindow extends JDialog {

    private final ColorWheel wheel;
    private final JSlider brightness;
    private final JButton confirmButton;
    private final JButton cancelButton;

    /**
     * Create and initialise the colour selection window.
     * @param owner the owner of this dialog.
     */
    public ColourSelectionWindow(Window owner) {
        super(owner, "Select colour", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        setResizable(false);
        wheel = new ColorWheel(new ColorModel(), Color.WHITE);
        add(wheel, BorderLayout.CENTER);
        brightness = new JSlider(JSlider.VERTICAL, 0, 100, 100);
        brightness.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                wheel.getModel().setBrightness((float) (brightness.getValue() / 100.0));
                wheel.repaint();
            }
        });
        add(brightness, BorderLayout.WEST);
        JPanel bottomPanel = new JPanel();
        confirmButton = new JButton("Select Colour", Utils.getImageIcon("icons/tick.png"));
        cancelButton = new JButton("Cancel", Utils.getImageIcon("icons/cross.png"));
        addHideListeners();
        bottomPanel.add(confirmButton);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Add the listeners to the JButtons that hide the window when the user has selected a colour.
     */
    private void addHideListeners() {
        final ActionListener hideListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        confirmButton.addActionListener(hideListener);
        cancelButton.addActionListener(hideListener);
    }

    /**
     * Get the confirm button on the new song window.
     * @return the confirm button.
     */
    public JButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * Get the cancel button on the new song window.
     * @return the cancel button.
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Get the colour that the user has selected.
     * @return the selected colour.
     */
    public Color getSelectedColour() {
        return wheel.getModel().getColor();
    }

    /**
     * Set the colour to be displayed in this colour selection window.
     * @param colour the colour to be displayed.
     */
    public void setSelectedColour(Color colour) {
        wheel.getModel().setColor(colour);
        ColorModel model = new ColorModel();
        model.setColor(colour);
        brightness.setValue((int) (model.getBrightness() * 100));
    }
}
