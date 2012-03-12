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
package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.quelea.Application;
import org.quelea.Theme;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * A modal dialog where a theme can be edited.
 *
 * @author Michael
 */
public class EditThemeDialog extends JDialog {

    private ThemePanel panel;
    private Theme theme;
    private File themeFile;
    private JButton confirmButton;
    private JButton cancelButton;
    private JTextField nameField;

    /**
     * Create a new edit theme dialog.
     */
    public EditThemeDialog() {
        super(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("edit.theme.heading"), true);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel northPanel = new JPanel();
        add(northPanel, BorderLayout.NORTH);
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("theme.name.label")));
        nameField = new JTextField(20);
        nameField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }

            private void check() {
                confirmButton.setEnabled(!nameField.getText().trim().isEmpty());
            }
        });
        northPanel.add(nameField);
        panel = new ThemePanel();
        panel.getCanvas().setPreferredSize(new Dimension(200, 200));
        add(panel, BorderLayout.CENTER);
        confirmButton = new JButton(LabelGrabber.INSTANCE.getLabel("ok.button"), Utils.getImageIcon("icons/tick.png"));
        confirmButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                theme = panel.getTheme();
                theme.setFile(themeFile);
                theme.setThemeName(nameField.getText());
                setVisible(false);
            }
        });
        cancelButton = new JButton(LabelGrabber.INSTANCE.getLabel("cancel.button"), Utils.getImageIcon("icons/cross.png"));
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                theme = null;
                setVisible(false);
            }
        });
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        southPanel.add(confirmButton);
        southPanel.add(cancelButton);
        add(southPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Centre on parent before making visible.
     *
     * @param vis true if visible, false otherwise.
     */
    @Override
    public void setVisible(boolean vis) {
        setLocationRelativeTo(getParent());
        super.setVisible(vis);
    }

    /**
     * Get the theme from this dialog.
     *
     * @return the theme.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Set the theme on this dialog.
     *
     * @param theme the theme.
     */
    public void setTheme(Theme theme) {
        if(theme == null) {
            theme = new Theme(Theme.DEFAULT_FONT, Theme.DEFAULT_FONT_COLOR, Theme.DEFAULT_BACKGROUND);
            theme.setThemeName("");
            File file;
            int filenum = 1;
            do {
                file = new File(new File(QueleaProperties.getQueleaUserHome(), "themes"), "theme" + filenum + ".th");
                filenum++;
            } while(file.exists());
            theme.setFile(file);
        }
        themeFile = theme.getFile();
        nameField.setText(theme.getThemeName());
        panel.setTheme(theme);
    }
}
