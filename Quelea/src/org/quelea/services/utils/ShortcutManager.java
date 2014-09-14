/* 
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.services.utils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;

/**
 * Manage adding the shortcuts for the application. This is responsible for
 * adding the sort of shortcuts that do things such as focus the user on a
 * particular node. Shortcuts for buttons or menu items are declared in the same
 * place as the corresponding node.
 * <p/>
 * @author Michael
 */
public class ShortcutManager {

    /**
     * Add in the shortcuts.
     * @param mainWindow the main window
     */
    public void addShortcuts(final MainWindow mainWindow) {
        final MainPanel mainPanel = QueleaApp.get().getMainWindow().getMainPanel();
        mainPanel.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), new Runnable() {
            @Override
            public void run() {
                mainPanel.getLibraryPanel().getLibrarySongPanel().getSearchBox().requestFocus();
            }
        });
        mainPanel.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F5), new Runnable() {
            @Override
            public void run() {
                mainWindow.getMainPanel().getLivePanel().toggleLogo();
            }
        });
        mainPanel.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F6), new Runnable() {
            @Override
            public void run() {
                mainWindow.getMainPanel().getLivePanel().toggleBlack();
            }
        });
        mainPanel.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F7), new Runnable() {
            @Override
            public void run() {
                mainWindow.getMainPanel().getLivePanel().toggleClear();
            }
        });
        mainPanel.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F8), new Runnable() {
            @Override
            public void run() {
                mainWindow.getMainPanel().getLivePanel().toggleHide();
            }
        });
        mainPanel.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.PAGE_DOWN), new Runnable() {
            @Override
            public void run() {
                mainWindow.getMainPanel().getLivePanel().advance();
            }
        });
        mainPanel.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.PAGE_UP), new Runnable() {
            @Override
            public void run() {
                mainWindow.getMainPanel().getLivePanel().previous();
            }
        });
    }
}
