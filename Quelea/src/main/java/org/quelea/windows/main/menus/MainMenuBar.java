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

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.Window;

/**
 * Quelea's main menu bar. Contains all the sub-menus.
 * @author Michael
 */
public class MainMenuBar extends MenuBar {

    private static final PseudoClass MENU_OPEN = PseudoClass.getPseudoClass("menu-open");
    private static final String FIRST_TITLE = "first-menu";
    private static final String TITLE_POPUP = "menu-bar-popup";
    private Parent titleContainer;
    
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

        // For the tab look of the in-window menu bar (see base.css): the
        // separators between titles are hidden while a menu is open, and the
        // first title has none. (JavaFX CSS has no :first-child.)
        for (Menu menu : getMenus()) {
            menu.showingProperty().addListener((obs, wasShowing, showing) -> updateMenuOpen());
        }
        skinProperty().addListener((obs, oldSkin, newSkin) -> Platform.runLater(this::watchTitles));
        // The menu that opens under a title joins it like a tab. JavaFX gives
        // that pop-up no style class of its own, so mark it when it opens
        // (sub-menus have other owners and keep the normal look).
        Window.getWindows().addListener((ListChangeListener<Window>) c -> {
            while (c.next()) {
                for (Window window : c.getAddedSubList()) {
                    if (window instanceof ContextMenu popup && isTitle(popup.getOwnerNode())) {
                        markTitlePopup(popup);
                    }
                }
            }
        });
    }

    private boolean isTitle(Node node) {
        return node != null && node.getParent() != null && node.getParent().getParent() == this;
    }

    private static void markTitlePopup(ContextMenu popup) {
        if (!popup.getStyleClass().contains(TITLE_POPUP)) {
            popup.getStyleClass().add(TITLE_POPUP); // copied to the content when it is created
        }
        if (popup.getSkin() != null && !popup.getSkin().getNode().getStyleClass().contains(TITLE_POPUP)) {
            popup.getSkin().getNode().getStyleClass().add(TITLE_POPUP);
        }
    }

    /** Keep .first-menu on the first title, also when the skin rebuilds them. */
    private void watchTitles() {
        if (lookup(".container") instanceof Parent container && container != titleContainer) {
            titleContainer = container;
            container.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) c -> markFirstTitle());
        }
        markFirstTitle();
    }

    private void updateMenuOpen() {
        pseudoClassStateChanged(MENU_OPEN, getMenus().stream().anyMatch(Menu::isShowing));
    }

    private void markFirstTitle() {
        if (lookup(".container") instanceof Parent container) {
            var titles = container.getChildrenUnmodifiable();
            for (int i = 0; i < titles.size(); i++) {
                Node title = titles.get(i);
                title.getStyleClass().remove(FIRST_TITLE);
                if (i == 0) {
                    title.getStyleClass().add(FIRST_TITLE);
                }
            }
        }
    }
    
}
