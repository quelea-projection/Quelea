package org.quelea.display;

import org.quelea.Theme;

/**
 * Represents a section of a song, eg. a verse, chorus or bridge.
 * @author Michael
 */
public class SongSection {

    private String title;
    private String[] lyrics;
    private Theme theme;

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
     * @param theme the theme of this song section.
     */
    public SongSection(String title, String[] lyrics, Theme theme) {
        this.title = title;
        this.lyrics = lyrics;
        this.theme = theme;
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
     * Get the theme of the section.
     * @return the theme of the section.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Set the theme of the section.
     * @param theme the new theme.
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    /**
     * Get a string representation of this song section.
     * @return a string representation.
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(title).append('\n');
        for(String str : lyrics) {
            ret.append(str).append('\n');
        }
        return ret.toString();
    }

}
