package org.quelea.data.displayable;

import java.io.Serializable;
import java.util.Objects;

public class IndexedDisplayable implements Serializable {

    private final Displayable displayable;
    private int index;

    public IndexedDisplayable(Displayable displayable, int index) {
        this.displayable = displayable;
        this.index = index;
    }

    public Displayable displayable() {
        return displayable;
    }

    public int index() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IndexedDisplayable that = (IndexedDisplayable) o;
        return index == that.index && Objects.equals(displayable, that.displayable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayable, index);
    }
}
