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
package org.quelea.server;

import org.quelea.windows.main.QueleaApp;

/**
 * Handles the RemoteControlServer commands.
 *
 * @author Ben Goodwin
 */
class RCHandler {

    static void logo() {
        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleLogo();
    }

    static void black() {
        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleBlack();
    }

    static void clear() {
        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleClear();
    }

    static void next() {
        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
    }

    static void prev() {
        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
    }

    static void nextItem() {
        System.out.println("Next schedule item");
    }

    static void prevItem() {
        System.out.println("Previous schedule item");
    }

}
