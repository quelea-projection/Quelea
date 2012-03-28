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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.quelea.utils.Utils;

/**
 * A status panel that denotes a background task in Quelea.
 *
 * @author Michael
 */
public class StatusPanel extends JPanel {

    private JProgressBar progressBar;
    private JLabel label;
    private JButton cancelButton;
    private StatusPanelGroup group;
    private int index;

    /**
     * Create a new status panel.
     *
     * @param group the group this panel is part of.
     * @param labelText the text to put on the label on this panel.
     * @param index the index of this panel on the group.
     */
    StatusPanel(StatusPanelGroup group, String labelText, int index) {
        this.group = group;
        this.index = index;
        label = new JLabel(labelText);
        progressBar = new JProgressBar();
        cancelButton = new JButton(Utils.getImageIcon("icons/cross.png", 15, 15));
        cancelButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(label);
        add(progressBar);
        add(cancelButton);
    }

    /**
     * Called to indicate that the task associated with this panel has finished,
     * and therefore the panel can be removed.
     */
    public void done() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                group.removePanel(index);
            }
        });
    }

    /**
     * Set the label text for this panel.
     *
     * @param text the text on this panel's label.
     */
    public void setLabelText(String text) {
        label.setText(text);
    }

    /**
     * Get the progress bar associated with this panel.
     *
     * @return the progress bar associated with this panel.
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Get the cancel button on this panel.
     *
     * @return the cancel button on this panel.
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Set whether this panel is active.
     *
     * @param active true if active, false otherwise.
     */
    public void setActive(boolean active) {
        setVisible(active);
    }
}
