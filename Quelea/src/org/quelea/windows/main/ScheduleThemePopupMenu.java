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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.quelea.Theme;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.TextDisplayable;
import org.quelea.displayable.TextSection;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class ScheduleThemePopupMenu extends JPopupMenu {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private ScheduleList schedule;
    private Theme tempTheme;
    private ButtonGroup group;

    public ScheduleThemePopupMenu(final ScheduleList schedule) {
        group = new ButtonGroup();
        this.schedule = schedule;
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
                addMenuItem(theme.getThemeName(), theme);
            }
        }
        addMenuItem("<html><i>Default themes</i></html>", null);
    }
    
    public Theme getTempTheme() {
        return tempTheme;
    }
    
    public void updateTheme() {
        setTheme(tempTheme);
    }
    
    private void addMenuItem(String name, final Theme theme) {
        JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(name);
        if(theme==null) {
            themeItem.setSelected(true);
        }
        group.add(themeItem);
        themeItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tempTheme = theme;
                setTheme(theme);
            }
        });
        add(themeItem);
    }

    private void setTheme(Theme theme) {
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
}
