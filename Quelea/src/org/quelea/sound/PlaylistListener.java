/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.sound;

/**
 * Listener for playlist events.
 * <p/>
 * @author Michael
 */
public interface PlaylistListener {

    /**
     * Called when the current track in the playlist has changed.
     * @param newTrack the new "current track".
     */
    void trackChanged(AudioTrack newTrack);

    /**
     * Called when a track has been added to the playlist.
     * @param newTrack the track that's been added.
     */
    void trackAdded(AudioTrack newTrack);

    /**
     * Called when a track has been removed from the playlist.
     * @param newTrack the track that's been removed.
     */
    void trackRemoved(AudioTrack newTrack);
}
