package org.quelea.display;

import org.quelea.Background;

/**
 * Represents a section of a song, eg. a verse, chorus or bridge.
 * @author Michael
 */
public class SongSection {

    private String title;
    private String[] lyrics;
    private Background background;

    /**
     * Create a new song section with the specified title and lyrics.
     * @param title the title of the section.
     * @param lyrics the lyrics of the section, one line per array entry.
     */
    public SongSection(String title, String[] lyrics) {
        this(title, lyrics, null);
    }

    /**
     * Create a new song section with the specified title and lyrics.
     * @param title the title of the section.
     * @param lyrics the lyrics of the section, one line per array entry.
     * @param background the default background of this song section.
     */
    public SongSection(String title, String[] lyrics, Background background) {
        this.title = title;
        this.lyrics = lyrics;
        this.background = background;
    }

    /**
     * Get the title of the section.
     * @return the title of the section.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the lyrics of the section.
     * @return the lyrics of the section.
     */
    public String[] getLyrics() {
        return lyrics;
    }

    /**
     * Get the background of the section.
     * @return the background of the section.
     */
    public Background getBackground() {
        return background;
    }

    /**
     * Set the background of the section.
     * @param background the new background.
     */
    public void setBackground(Background background) {
        this.background = background;
    }

}
