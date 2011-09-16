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
package org.quelea.windows.options;

import org.quelea.utils.PropertyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The dialog that holds all the options the user can set.
 * @author Michael
 */
public class OptionsDialog extends JDialog {

    private final JButton okButton;
    private final JTabbedPane tabbedPane;
    private final OptionsDisplaySetupPanel displayPanel;
    private final OptionsGeneralPanel generalPanel;
    private final OptionsBiblePanel biblePanel;
    private final JFrame owner;

    /**
     * Create a new options dialog.
     * @param owner the owner of the dialog - should be the main window.
     */
    public OptionsDialog(JFrame owner) {
        super(owner, "Options", true);
        this.owner = owner;
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        generalPanel = new OptionsGeneralPanel();
        tabbedPane.add(generalPanel);
        displayPanel = new OptionsDisplaySetupPanel();
        tabbedPane.add(displayPanel);
        biblePanel = new OptionsBiblePanel();
        tabbedPane.add(biblePanel);
        add(tabbedPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < tabbedPane.getComponentCount(); i++) {
                    ((PropertyPanel) tabbedPane.getComponentAt(i)).setProperties();
                }
                setVisible(false);
            }
        });
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setResizable(false);
    }

    /**
     * When the dialog is made visible, centre it on its owner and sync the forms.
     * @param visible true if the dialog should be made visible, false otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            setLocationRelativeTo(owner);
            for(int i = 0; i < tabbedPane.getComponentCount(); i++) {
                ((PropertyPanel) tabbedPane.getComponentAt(i)).readProperties();
            }
        }
        super.setVisible(visible);
    }

    /**
     * Get the general panel used in this options dialog.
     * @return the general panel.
     */
    public OptionsGeneralPanel getGeneralPanel() {
        return generalPanel;
    }

    /**
     * Get the display panel used in this options dialog.
     * @return the display panel.
     */
    public OptionsDisplaySetupPanel getDisplayPanel() {
        return displayPanel;
    }

    /**
     * Get the bible panel used in this options dialog.
     * @return the bible panel.
     */
    public OptionsBiblePanel getBiblePanel() {
        return biblePanel;
    }

    /**
     * Get the OK button used to affirm the change in options.
     * @return the OK button.
     */
    public JButton getOKButton() {
        return okButton;
    }

}
