package org.quelea.data.db.model;

import java.util.HashMap;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Song table mapping
 *
 * @author tomaszpio@gmail.com
 */
@Entity
@Table(name = "songs")
public class Song {

    private static final int STRING_LENGTH = DBConstants.STRING_LENGTH;
    private long id;
    private String title;
    private String author;
    private String lyrics;
    private String ccli;
    private String copyright;
    private String year;
    private String publisher;
    private String key;
    private String capo;
    private String info;
    private Theme theme;
    private HashMap<String, String> translations;
    private String sequence;

    public Song() {
    }

    public Song(String title, String author, String lyrics, String ccli, String copyright,
                String year, String publisher, String key, String capo, String info,
                Theme theme, HashMap<String, String> translations, String sequence) {
        this.title = title;
        this.author = author;
        this.lyrics = lyrics;
        this.ccli = ccli;
        this.copyright = copyright;
        this.year = year;
        this.publisher = publisher;
        this.key = key;
        this.capo = capo;
        this.info = info;
        this.theme = theme;
        this.translations = translations;
        this.sequence = sequence;
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    @Column(name = "title", nullable = false, length = STRING_LENGTH)
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the author
     */
    @Column(name = "author", length = STRING_LENGTH)
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the lyrics
     */
    @Column(name = "lyrics", length = STRING_LENGTH)
    public String getLyrics() {
        return lyrics;
    }

    /**
     * @param lyrics the lyrics to set
     */
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * @return the theme
     */
    @ManyToOne(cascade = CascadeType.ALL)
    public Theme getTheme() {
        return theme;
    }

    /**
     * @param theme the theme to set
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    /**
     * @return the ccli
     */
    @Column(name = "ccli", length = STRING_LENGTH)
    public String getCcli() {
        return ccli;
    }

    /**
     * @param ccli the ccli to set
     */
    public void setCcli(String ccli) {
        this.ccli = ccli;
    }

    /**
     * @return the copyright
     */
    @Column(name = "copyright", length = STRING_LENGTH)
    public String getCopyright() {
        return copyright;
    }

    /**
     * @param copyright the copyright to set
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    /**
     * @return the year
     */
    @Column(name = "year", length = STRING_LENGTH)
    public String getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the publisher
     */
    @Column(name = "publisher", length = STRING_LENGTH)
    public String getPublisher() {
        return publisher;
    }

    /**
     * @param publisher the publisher to set
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * @return the key
     */
    @Column(name = "key", length = STRING_LENGTH)
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the capo
     */
    @Column(name = "capo", length = STRING_LENGTH)
    public String getCapo() {
        return capo;
    }

    /**
     * @param capo the capo to set
     */
    public void setCapo(String capo) {
        this.capo = capo;
    }

    /**
     * @return the info
     */
    @Column(name = "info", length = STRING_LENGTH)
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }

    @Lob
    @Column(name="translations", length=STRING_LENGTH)
    public HashMap<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(HashMap<String, String> translations) {
        this.translations = translations;
    }

    /**
     * @return the sequence
     */
    @Column(name = "songsequence", length = STRING_LENGTH)
    public String getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }


}
