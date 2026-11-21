package org.quelea.windows.video;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.net.URI;
import java.util.Objects;

/**
 * Plays the logo video on the projection screen, looping and without sound.
 * Must be used on the FX thread.
 */
public class VidLogoDisplay {

    public static final VidLogoDisplay INSTANCE = new VidLogoDisplay();

    private final ReadOnlyObjectWrapper<Image> image = new ReadOnlyObjectWrapper<>(new WritableImage(1, 1));
    /** Created on first use, like any other video display (see {@link VidDisplay}). */
    private VidDisplay display;
    private URI uri;

    private VidLogoDisplay() {
    }

    public ReadOnlyObjectProperty<? extends Image> imageProperty() {
        return image.getReadOnlyProperty();
    }

    public void setURI(URI uri) {
        if (!Objects.equals(this.uri, uri)) {
            this.uri = uri;
            if (display == null) {
                // Video only: a muted pipeline would still open an audio output.
                display = new VidDisplay(false);
                display.setLoop(true);
                image.bind(display.imageProperty());
            }
            display.setURI(uri);
            display.play();
        }
    }
}
