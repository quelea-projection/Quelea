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
import javafx.scene.input.KeyEvent;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.newsong.SongEntryWindow;

import java.util.ArrayList;

/**
 * Manage adding the shortcuts for the application. This is responsible for
 * adding the sort of shortcuts that do things such as focus the user on a
 * particular node. Shortcuts for buttons or menu items are declared in the same
 * place as the corresponding node.
 * <p/>
 *
 * @author Michael
 */
public class ShortcutManager {
    private ArrayList<String> keyCodeBuilder = new ArrayList<>();

    /**
     * Add in the shortcuts.
     *
     * @param mainWindow the main window
     */
    public void addShortcuts(final MainWindow mainWindow) {
        final MainPanel mainPanel = QueleaApp.get().getMainWindow().getMainPanel();
        mainPanel.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!keyCodeBuilder.contains(event.getCode().getName()))
                keyCodeBuilder.add(event.getCode().getName());
            if (checkCombination(QueleaProperties.get().getAdvanceKeys())) {
                event.consume();
            } else if (checkCombination(QueleaProperties.get().getPreviousKeys())) {
                event.consume();
            }
        });

        mainPanel.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (checkCombination(QueleaProperties.get().getSearchKeys())) {
                mainPanel.getLibraryPanel().getTabPane().getSelectionModel().select(0);
                mainPanel.getLibraryPanel().getLibrarySongPanel().getSearchBox().requestFocus();
            } else if (checkCombination(QueleaProperties.get().getLogoKeys())) {
                mainWindow.getMainPanel().getLivePanel().toggleLogo();
            } else if (checkCombination(QueleaProperties.get().getBlackKeys())) {
                mainWindow.getMainPanel().getLivePanel().toggleBlack();
            } else if (checkCombination(QueleaProperties.get().getClearKeys())) {
                mainWindow.getMainPanel().getLivePanel().toggleClear();
            } else if (checkCombination(QueleaProperties.get().getHideKeys())) {
                mainWindow.getMainPanel().getLivePanel().toggleHide();
            } else if (checkCombination(QueleaProperties.get().getAdvanceKeys())) {
                mainWindow.getMainPanel().getLivePanel().advance();
            } else if (checkCombination(QueleaProperties.get().getPreviousKeys())) {
                mainWindow.getMainPanel().getLivePanel().previous();
            } else if (checkCombination(QueleaProperties.get().getNewSongKeys())) {
                SongEntryWindow songEntryWindow = QueleaApp.get().getMainWindow().getSongEntryWindow();
                songEntryWindow.resetNewSong();
                songEntryWindow.show();
            } else if (checkCombination(QueleaProperties.get().getScheduleFocusKeys())) {
                mainPanel.getSchedulePanel().getScheduleList().getListView().requestFocus();
            } else if (checkCombination(QueleaProperties.get().getBibleFocusKeys())) {
                mainPanel.getLibraryPanel().getTabPane().getSelectionModel().select(1);
                mainPanel.getLibraryPanel().getBiblePanel().getBookSelector().requestFocus();
            }
            keyCodeBuilder.clear();
        });
    }

    private boolean checkCombination(String[] keyCombination) {
        if (keyCombination.length != keyCodeBuilder.size())
            return false;
        for (String kc : keyCombination) {
            if (!keyCodeBuilder.contains(kc)) {
                return false;
            }
        }
        return true;
    }

    public static KeyCodeCombination getKeyCodeCombination(String[] keys) {
        ArrayList<KeyCombination.Modifier> modifiers = new ArrayList<>();
        String character = "";
        for (String s : keys) {
            if (s.contains("Ctrl"))
                modifiers.add(KeyCombination.CONTROL_DOWN);
            else if (s.contains("Alt"))
                modifiers.add(KeyCombination.ALT_DOWN);
            else if (s.contains("Shift"))
                modifiers.add(KeyCombination.SHIFT_DOWN);
            else if (s.contains("Shortcut"))
                modifiers.add(KeyCombination.SHORTCUT_DOWN);
            else
                character = s;
        }
        if (modifiers.size() == 4)
            return new KeyCodeCombination(KeyCode.getKeyCode(character), modifiers.get(0), modifiers.get(1), modifiers.get(2), modifiers.get(3));
        if (modifiers.size() == 3)
            return new KeyCodeCombination(KeyCode.getKeyCode(character), modifiers.get(0), modifiers.get(1), modifiers.get(2));
        if (modifiers.size() == 2)
            return new KeyCodeCombination(KeyCode.getKeyCode(character), modifiers.get(0), modifiers.get(1));
        if (modifiers.size() == 1)
            return new KeyCodeCombination(KeyCode.getKeyCode(character), modifiers.get(0));
        return new KeyCodeCombination(KeyCode.getKeyCode(character));
    }
}
