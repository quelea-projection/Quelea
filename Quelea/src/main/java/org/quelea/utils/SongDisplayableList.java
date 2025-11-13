package org.quelea.utils;

import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.data.displayable.SongDisplayable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to wrap a song displayable list to avoid generic type warnings on drag/drop operations.
 */
public class SongDisplayableList implements Serializable {

    private final List<IndexedDisplayable> songDisplayables;

    public SongDisplayableList(List<IndexedDisplayable> songDisplayables) {
        this.songDisplayables = new ArrayList<>(songDisplayables);
    }

    public SongDisplayableList(IndexedDisplayable songDisplayable) {
        this(List.of(songDisplayable));
    }

    public List<IndexedDisplayable> getSongDisplayables() {
        return songDisplayables;
    }
}
