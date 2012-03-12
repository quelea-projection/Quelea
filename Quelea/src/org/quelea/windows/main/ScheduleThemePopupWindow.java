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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.quelea.Application;
import org.quelea.Theme;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.TextDisplayable;
import org.quelea.displayable.TextSection;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.FadeWindow;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import org.quelea.windows.newsong.EditThemeDialog;

/**
 * A popup window that allows the user to select a theme for the current 
 * schedule. It behaves a bit like a rich menu that fades in and out, and 
 * replaces the old menu we had.
 * @author Michael
 */
public class ScheduleThemePopupWindow extends FadeWindow {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private JPanel contentPanel;
    private Theme tempTheme;
    private ScheduleList schedule;
    private EditThemeDialog themeDialog;

    /**
     * Create a new schedule theme popup window to control a particular
     * schedule.
     * @param schedule the schedule to control.
     */
    public ScheduleThemePopupWindow(final ScheduleList schedule) {
        setSpeed(0.06f);
        this.schedule = schedule;
        themeDialog = new EditThemeDialog();
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(1, 1, 0, 0));
        contentPanel.setBorder(new LineBorder(Color.BLACK, 1));
        refresh();
        add(contentPanel);
        startWatching();
    }

    /**
     * Start the watcher thread. This runs in the background and if any theme
     * changes are detected on the folder it updates itself.
     */
    private void startWatching() {
        try {
            final WatchService watcher = FileSystems.getDefault().newWatchService();
            final Path themePath = FileSystems.getDefault().getPath(new File(QueleaProperties.getQueleaUserHome(), "themes").getAbsolutePath());
            themePath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            new Thread() {

                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    while (true) {
                        WatchKey key;
                        try {
                            key = watcher.take();
                        }
                        catch (InterruptedException ex) {
                            return;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }

                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path filename = ev.context();
                            if (!filename.toFile().toString().toLowerCase().endsWith(".th")) {
                                continue;
                            }

                            if (!key.reset()) {
                                break;
                            }
                            Utils.sleep(200); //TODO: Bodge
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    refresh();
                                }
                            });

                        }
                    }
                }
            }.start();
        }
        catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception in logger thread.", ex);
        }
    }

    /**
     * Get the temporary theme to be used on the schedule, the one currently 
     * selected by the user. Or null if it's default or nothing is selected.
     * @return the user's chosen theme.
     */
    public Theme getTempTheme() {
        return tempTheme;
    }

    /**
     * Update the theme on the schedule to the current temporary theme.
     */
    public void updateTheme() {
        setTheme(tempTheme);
    }

    /**
     * Refresh all the themes in the window - remove all the old ones, go 
     * through the folder and find the themes to display.
     */
    public synchronized final void refresh() {
        List<Theme> themes;
        try {
            themes = getThemes();
        }
        catch (Exception ex) {
            return;
        }
        themes.add(null);
        final ButtonGroup group = new ButtonGroup();
        Component[] components = contentPanel.getComponents();
        for (Component component : components) {
            contentPanel.remove(component);
        }
        contentPanel.validate();
        contentPanel.repaint();
        contentPanel.setLayout(new BorderLayout());
        final JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("theme.select.text")));
        contentPanel.add(northPanel, BorderLayout.NORTH);
        final JPanel themePreviews = new JPanel();
        themePreviews.setLayout(new GridLayout((themes.size()/5)+1, 5, 5, 5));
        for (final Theme theme : themes) {
            ThemePreviewPanel panel = new ThemePreviewPanel(theme);
            panel.getSelectButton().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    tempTheme = theme;
                    setTheme(theme);
                    Application.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                }
            });
            group.add(panel.getSelectButton());
            themePreviews.add(panel);
        }
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton newThemeButton = new JButton(LabelGrabber.INSTANCE.getLabel("new.theme.text"));
        newThemeButton.addActionListener(new ActionListener() {

            /**
             * Invoked when the user wants to add a new theme.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                themeDialog.setTheme(null);
                themeDialog.setVisible(true);
                Theme ret = themeDialog.getTheme();
                if(ret != null) {
                    try (PrintWriter pw = new PrintWriter(ret.getFile())) {
                        pw.println(ret.toDBString());
                    }
                    catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't write new theme", ex);
                    }
                }
            }
        });
        buttonPanel.add(newThemeButton);
        contentPanel.add(themePreviews, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        contentPanel.validate();
        contentPanel.repaint();
    }

    /**
     * Get a list of themes currently in use on this window.
     * @return the list of themes displayed.
     */
    private List<Theme> getThemes() {
        List<Theme> themesList = new ArrayList<>();
        File themeDir = new File(QueleaProperties.getQueleaUserHome(), "themes");
        if (!themeDir.exists()) {
            themeDir.mkdir();
        }
        for (File file : themeDir.listFiles()) {
            if (file.getName().endsWith(".th")) {
                final Theme theme = Theme.parseDBString(Utils.getTextFromFile(file.getAbsolutePath(), ""));
                if (theme.equals(Theme.DEFAULT_THEME)) {
                    LOGGER.log(Level.WARNING, "Error parsing theme file: {0}", file.getAbsolutePath());
                    continue;  //error
                }
                theme.setFile(file);
                themesList.add(theme);
            }
        }
        return themesList;
    }

    /**
     * Set the schedule to a given theme.
     * @param theme the theme to set.
     */
    private void setTheme(Theme theme) {
        if (schedule == null) {
            LOGGER.log(Level.WARNING, "Null schedule, not setting theme");
            return;
        }
        for (int i = 0; i < schedule.getModel().getSize(); i++) {
            Displayable displayable = schedule.getModel().get(i);
            if (displayable instanceof TextDisplayable) {
                TextDisplayable textDisplayable = (TextDisplayable) displayable;
                for (TextSection section : textDisplayable.getSections()) {
                    section.setTempTheme(theme);
                }
            }
        }
    }

    /**
     * Testing stuff.
     * @param args 
     */
    public static void main(String[] args) {
        JWindow window = new ScheduleThemePopupWindow(null);
        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        window.pack();
        window.setVisible(true);
    }
}
