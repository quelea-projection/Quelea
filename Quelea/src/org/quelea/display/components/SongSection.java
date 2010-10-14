package org.quelea.display.components;

/**
 * Represents a section of a song, eg. a verse, chorus or bridge.
 * @author Michael
 */
public class SongSection {

    private String title;
    private String[] lyrics;

    /**
     * Create a new song section with the specified title and lyrics.
     * @param title the title of the section.
     * @param lyrics the lyrics of the section, one line per array entry.
     */
    public SongSection(String title, String[] lyrics) {
        this.title = title;
        this.lyrics = lyrics;
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

}
