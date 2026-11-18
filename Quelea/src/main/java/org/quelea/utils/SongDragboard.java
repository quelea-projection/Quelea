package org.quelea.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.quelea.data.displayable.SongDisplayable;

/** A song drag is local to this process; the dragboard carries only its token. */
public final class SongDragboard {
    private static String token;
    private static SongDisplayableList songs;

    private SongDragboard() {
    }

    /**
     * Capture the same independent song data that the native clipboard previously
     * produced by serializing the payload. Only bytes produced here are read;
     * bytes from the dragboard are never deserialized.
     */
    public static SongDisplayableList snapshot(SongDisplayableList source) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(source);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (SongDisplayableList) in.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException("Could not copy dragged songs", ex);
        }
    }

    /** Begin a drag, replacing any stale payload from an earlier gesture. */
    public static void put(ClipboardContent content, SongDisplayableList value) {
        token = UUID.randomUUID().toString();
        songs = value;
        content.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, token);
    }

    /** Return the payload only for the current process-local drag token. */
    public static SongDisplayableList get(Clipboard clipboard) {
        Object candidate = clipboard.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT);
        return token != null && token.equals(candidate) ? songs : null;
    }

    /** Release a completed or cancelled drag without affecting a newer one. */
    public static void clear(Clipboard clipboard) {
        clearToken(clipboard.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT));
    }

    /** Release a payload if putting it on a dragboard failed. */
    public static void clear(ClipboardContent content) {
        clearToken(content.get(SongDisplayable.SONG_DISPLAYABLE_FORMAT));
    }

    private static void clearToken(Object candidate) {
        if (token != null && token.equals(candidate)) {
            token = null;
            songs = null;
        }
    }
}
