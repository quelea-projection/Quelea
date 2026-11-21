/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.presentation.powerpoint;

/**
 * Runs a command of powerpoint.applescript, i.e. the equivalent of
 * {@code osascript powerpoint.applescript <args...>}.
 */
interface AppleScriptRunner extends AutoCloseable {

    /**
     * Run the script with the given arguments.
     *
     * @param timeoutMillis how long to wait for an answer.
     * @param args the command and its arguments.
     * @return what the script returned.
     * @throws PowerPointException if the script raised an error or timed out.
     */
    String run(long timeoutMillis, String... args) throws PowerPointException;

    @Override
    void close();
}
