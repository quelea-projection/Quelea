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

/**
 * Handles the RemoteControlServer commands.
 *
 * @author Ben Goodwin
 */
class RCHandler {

    static void logo() {
        System.out.println("Logo");
    }

    static void black() {
        System.out.println("Black");
    }

    static void clear() {
        System.out.println("Clear");
    }

    static void next() {
        System.out.println("Next");
    }

    static void prev() {
        System.out.println("Previous");
    }

    static void nextItem() {
        System.out.println("Next schedule item");
    }

    static void prevItem() {
        System.out.println("Previous schedule item");
    }

}
