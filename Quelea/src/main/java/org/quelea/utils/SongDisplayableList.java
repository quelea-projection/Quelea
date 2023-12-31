package org.quelea.utils;

import org.quelea.data.displayable.SongDisplayable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to wrap a song displayable list to avoid generic type warnings on drag/drop operations.
 */
public class SongDisplayableList implements Serializable {

    private final List<SongDisplayable> songDisplayables;

    public SongDisplayableList(List<SongDisplayable> songDisplayables) {
        this.songDisplayables = new ArrayList<>(songDisplayables);
    }

    public SongDisplayableList(SongDisplayable songDisplayable) {
        this(List.of(songDisplayable));
    }

    public List<SongDisplayable> getSongDisplayables() {
        return songDisplayables;
    }
}
