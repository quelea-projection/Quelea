/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
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
package org.quelea.windows.main.menus;

import javafx.scene.control.MenuBar;

/**
 * Quelea's main menu bar. Contains all the sub-menus.
 * @author Michael
 */
public class MainMenuBar extends MenuBar {
    
    private FileMenu fileMenu;
    private ScheduleMenu scheduleMenu;
    private DatabaseMenu databaseMenu;
    private ToolsMenu toolsMenu;
    private HelpMenu helpMenu;
    
    public MainMenuBar() {
        fileMenu = new FileMenu();
        getMenus().add(fileMenu);
        
        scheduleMenu = new ScheduleMenu();
        getMenus().add(scheduleMenu);
        
        databaseMenu = new DatabaseMenu();
        getMenus().add(databaseMenu);
        
        toolsMenu = new ToolsMenu();
        getMenus().add(toolsMenu);
        
        helpMenu = new HelpMenu();
        getMenus().add(helpMenu);
    }
    
}
