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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import org.quelea.Application;
import org.quelea.Theme;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.quelea.windows.newsong.EditThemeDialog;
import org.quelea.windows.newsong.ThemePanel;

/**
 * Panel that displays a preview of a particular theme. This is part of the 
 * theme select popup window.
 * @author Michael
 */
public class ThemePreviewPanel extends JPanel {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private Theme theme;
    private LyricCanvas canvas;
    private JRadioButton selectButton;
    private JButton removeButton;
    private JButton editButton;
    private EditThemeDialog themeDialog;

    /**
     * Create a new theme preview panel.
     * @param theme the theme to preview.
     */
    public ThemePreviewPanel(Theme theme) {
        this.theme = theme;
        if (theme != null) {
            canvas = new LyricCanvas(false, false);
            canvas.setTheme(theme);
            canvas.setText(ThemePanel.SAMPLE_LYRICS, new String[0]);
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        String name;
        if (theme == null) {
            name = "<html><i>"+LabelGrabber.INSTANCE.getLabel("default.theme.text")+"</i></html>";
        }
        else {
            name = theme.getThemeName();
        }
        themeDialog = new EditThemeDialog();
        selectButton = new JRadioButton(name);
        if (theme != null) {
            editButton = new JButton(Utils.getImageIcon("icons/edit32.png", 16, 16));
            editButton.setMargin(new Insets(0, 0, 0, 0));
            editButton.setBorder(new EmptyBorder(0, 0, 0, 0));
            editButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("edit.theme.tooltip"));
            editButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    themeDialog.setTheme(ThemePreviewPanel.this.theme);
                    themeDialog.setVisible(true);
                    Theme ret = themeDialog.getTheme();
                    if(ret != null) {
                        try (PrintWriter pw = new PrintWriter(ret.getFile())) {
                            pw.println(ret.toDBString());
                        }
                        catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Couldn't edit theme", ex);
                        }
                    }
                }
            });
            
            removeButton = new JButton(Utils.getImageIcon("icons/delete.png", 16, 16));
            removeButton.setMargin(new Insets(0, 0, 0, 0));
            removeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
            removeButton.setToolTipText(LabelGrabber.INSTANCE.getLabel("remove.theme.tooltip"));
            removeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
//                    int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("delete.theme.question"), LabelGrabber.INSTANCE.getLabel("delete.theme.confirm.title"), JOptionPane.YES_NO_OPTION);
//                    if (result != JOptionPane.NO_OPTION) {
//                        ThemePreviewPanel.this.theme.getFile().delete();
//                    }
                }
            });
        }
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        if (canvas != null) {
//            canvas.addMouseListener(new MouseAdapter() {
//
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    selectButton.doClick();
//                }
//            });
        }
        buttonPanel.add(selectButton);
        if (theme != null) {
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(editButton);
            buttonPanel.add(removeButton);
        }
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel canvasPanel = new JPanel();
        if (theme == null) {
            JLabel label = new JLabel("<html><h1>"+LabelGrabber.INSTANCE.getLabel("default.theme.name")+"</h1></html>");
            canvasPanel.add(label);
            canvasPanel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectButton.doClick();
                }
            });
            label.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectButton.doClick();
                }
            });
        }
        else {
            canvasPanel.setLayout(new GridLayout(1, 1, 0, 0));
//            canvasPanel.add(canvas);
        }
        canvasPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(canvasPanel);
        add(buttonPanel);
    }

    /**
     * Get the select radio button used to select this theme.
     * @return the select radio button.
     */
    public JRadioButton getSelectButton() {
        return selectButton;
    }

    /**
     * Get the theme in use on this preview panel.
     * @return the theme in use on this preview panel.
     */
    public Theme getTheme() {
        return theme;
    }
}
