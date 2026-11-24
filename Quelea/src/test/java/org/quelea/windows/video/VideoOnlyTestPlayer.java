package org.quelea.windows.video;

/** Creates a player without opening an audio device in video-only tests. */
public final class VideoOnlyTestPlayer {
    private VideoOnlyTestPlayer() {
    }

    public static VidDisplay create() {
        return new VidDisplay(false);
    }
}
