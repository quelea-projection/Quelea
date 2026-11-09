package org.quelea.server.v2;

import java.io.File;
import java.util.Collection;
import java.util.List;
import javafx.scene.Node;
import org.quelea.data.displayable.Displayable;

/**
 * A minimal displayable for tests. Two instances with the same title are
 * equal, like two copies of one song, so tests can check that ids follow
 * identity and not equals.
 */
final class FakeDisplayable implements Displayable {

    private final String title;

    FakeDisplayable(String title) {
        this.title = title;
    }

    @Override
    public boolean supportClear() {
        return true;
    }

    @Override
    public String getXML() {
        return "<fake>" + title + "</fake>";
    }

    @Override
    public Node getPreviewIcon() {
        return null;
    }

    @Override
    public String getPreviewText() {
        return title;
    }

    @Override
    public Collection<File> getResources() {
        return List.of();
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FakeDisplayable other && other.title.equals(title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}
