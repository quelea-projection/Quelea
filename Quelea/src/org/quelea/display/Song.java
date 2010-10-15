package org.quelea.display;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.quelea.Background;

/**
 * A song that contains a number of sections (verses, choruses, etc.)
 * @author Michael
 */
public class Song implements Displayable {

    private String title;
    private String author;
    private List<SongSection> sections;
    private Background background;

    /**
     * Create a new, empty song.
     * @param title the title of the song.
     * @param author the author of the song.
     */
    public Song(String title, String author) {
        this(title, author, new Background(Color.BLACK));
    }

    /**
     * Create a new, empty song.
     * @param title the title of the song.
     * @param author the author of the song.
     * @param background the default background of the song.
     */
    public Song(String title, String author, Background background) {
        this.title = title;
        this.author = author;
        this.background = background;
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
        if(section.getBackground()==null) {
            section.setBackground(background);
        }
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
