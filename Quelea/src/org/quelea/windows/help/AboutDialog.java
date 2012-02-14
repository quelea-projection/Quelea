/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * Quelea's about Dialog, displaying general features about the program.
 *
 * @author Michael
 */
public class AboutDialog extends JDialog {

    private final JFrame owner;

    /**
     * Create a new about dialog.
     *
     * @param owner the owner of the dialog (should be the main window.)
     */
    public AboutDialog(JFrame owner) {
        super(owner, LabelGrabber.INSTANCE.getLabel("help.about.title"));
        this.owner = owner;
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        add(new JLabel("<html><h1>Quelea</h1> " + LabelGrabber.INSTANCE.getLabel("help.about.version") + " " + QueleaProperties.VERSION.getVersionString() + "</html>"));
        add(new JLabel(Utils.getImageIcon("img/logo.png")));
        add(new JLabel(" "));
        add(new JLabel(LabelGrabber.INSTANCE.getLabel("help.about.line1")));
        add(new JLabel(LabelGrabber.INSTANCE.getLabel("help.about.line2")));
        add(new JLabel(" "));
        JButton closeButton = new JButton(LabelGrabber.INSTANCE.getLabel("help.about.close"));
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        add(closeButton);
        pack();
    }

    /**
     * When the dialog is made visible, centre it on its owner.
     *
     * @param visible true if the dialog should be made visible, false
     * otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            setLocationRelativeTo(owner);
        }
        super.setVisible(visible);
    }
}
