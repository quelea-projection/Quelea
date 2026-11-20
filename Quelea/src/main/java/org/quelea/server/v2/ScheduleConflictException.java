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
package org.quelea.server.v2;

/**
 * Thrown when a schedule change names a revision that is no longer current:
 * someone else changed the schedule first.
 */
public final class ScheduleConflictException extends RuntimeException {

    private final long currentRevision;

    public ScheduleConflictException(long expected, long currentRevision) {
        super("The schedule is at revision " + currentRevision + ", not " + expected);
        this.currentRevision = currentRevision;
    }

    /**
     * @return the schedule revision when the change was refused.
     */
    public long getCurrentRevision() {
        return currentRevision;
    }
}
