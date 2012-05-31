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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A playlist that works with an audio player.
 * @author Michael
 */
public class Playlist implements Iterable<AudioTrack> {

    private List<AudioTrack> tracks;
    private AudioTrack currentTrack;
    private List<PlaylistListener> listeners;

    /**
     * Create a new playlist.
     */
    public Playlist() {
        tracks = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    /**
     * Add a track to the playlist.
     * @param track the track to add.
     */
    public void addTrack(AudioTrack track) {
        if(track.checkOK()) {
            tracks.add(track);
        }
        for(PlaylistListener listener : listeners) {
            listener.trackAdded(track);
        }
    }

    /**
     * Remove a track from the playlist.
     * @param track the track to remove.
     */
    public void removeTrack(AudioTrack track) {
        boolean result = tracks.remove(track);
        if(result) {
            for(PlaylistListener listener : listeners) {
                listener.trackRemoved(track);
            }
        }
    }

    /**
     * Get the number of tracks in the playlist.
     * @return the number of tracks in the playlist.
     */
    public int size() {
        return tracks.size();
    }

    /**
     * Determine if the playlist is empty.
     * @return true if it's empty, false otherwise.
     */
    public boolean isEmpty() {
        return tracks.isEmpty();
    }

    /**
     * Set the current track on the playlist. If it's not already on there,
     * add it.
     * @param track the track to add.
     */
    public void setCurrentTrack(AudioTrack track) {
        if(!tracks.contains(track)) {
            addTrack(track);
        }
        AudioTrack prevTrack = currentTrack;
        currentTrack = track;
        if(prevTrack != currentTrack) {
            for(PlaylistListener listener : listeners) {
                listener.trackChanged(track);
            }
        }
    }

    /**
     * Get the currently selected track on this playlist.
     * @return the currently selected track.
     */
    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Get an iterator over this playlist object, of all the audio tracks.
     * @return an iterator over the audio tracks currently in this playlist.
     */
    @Override
    public Iterator<AudioTrack> iterator() {
        return tracks.iterator();
    }

    /**
     * Add a playlist listener to this playlist to watch for changes.
     * @param listener the listener to add.
     */
    public void addPlaylistListener(PlaylistListener listener) {
        listeners.add(listener);
    }
}
