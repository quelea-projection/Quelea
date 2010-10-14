package org.quelea.display.components;

import java.util.ArrayList;
import java.util.List;

/**
 * A song that contains a number of sections (verses, choruses, etc.)
 * @author Michael
 */
public class Song implements Displayable {

    private String title;
    private String author;
    private List<SongSection> sections;

    /**
     * Create a new, empty song.
     */
    public Song(String title, String author) {
        this.title = title;
        this.author = author;
        sections = new ArrayList<SongSection>();
    }

    /**
     * Get the title of this song.
     * @return the title of this song.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the author of this song.
     * @return the author of the song.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Add a section to this song.
     * @param section the section to add.
     */
    public void addSection(SongSection section) {
        sections.add(section);
    }

    /**
     * Get an array of all the sections in this song.
     * @return the song sections.
     */
    public SongSection[] getSections() {
        return sections.toArray(new SongSection[sections.size()]);
    }

}
