/*
 * Author: Bo Maryniuk <bo@suse.de>
 *
 * Copyright (c) 2013 Bo Maryniuk. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *     3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY BO MARYNIUK "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package de.suse.lib.openlyrics.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Properties of the OpenLyrics object.
 * 
 * @author bo
 */
public class OpenLyricsProperties {
    private TitleProperty title;
    private TempoProperty tempo;
    private String copyright;
    private List<String> verseOrder;
    private List<String> authors;
    private String key;
    private String publisher;
    private List<String> comments;

    /**
     * Reviewed, but skipped at least for now:
     *
     * - CCLI           Meh... Christian incarnation of some sort of RIAA?
     * - variant        No idea how to deal with this yet with chords. How to switch between variants?
     * - version        "or anything else??" How "anything else" would apply for version control?
     * - keywords       This is no need for the search engine. Keywords are extracted automatically by algorithms.
     * - songbook       Outdated approach for categorization. Nowadays all should live in one pool and just searched/tagged.
     * - theme          "Heavy metal" anyone?.. See reason for "songbook".
     * - release date   Maybe it is very important to know the song was released in 16th century.
     *
     * Constructor of the properties.
     */
    public OpenLyricsProperties() {
        this.authors = new ArrayList<String>();
        this.comments = new ArrayList<String>();
        this.title = new TitleProperty();
    }

    
    /**
     * Get tempo property.
     * 
     * @return
     */
    public TempoProperty getTempo() {
        return tempo;
    }


    /**
     * Set tempo property.
     *
     * @param tempo
     */
    public void setTempo(TempoProperty tempo) {
        this.tempo = tempo;
    }


    /**
     * Add an author of the song.
     *
     * @param author
     */
    public void addAuthor(String author) {
        this.authors.add(author);
    }


    /**
     * Get all authors of the song.
     *
     * @return
     */
    public List<String> getAuthors() {
        return Collections.unmodifiableList(this.authors);
    }


    /**
     * Add commend for the entire song.
     *
     * @param comment
     */
    public void addComment(String comment) {
        this.comments.add(comment);
    }


    /**
     * Get all comments of the entire song.
     *
     * @return
     */
    public List<String> getComments() {
        return Collections.unmodifiableList(this.comments);
    }


    /**
     * Set copyright notice of the song.
     * 
     * @param copyright
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }


    /**
     * Get copyright notice of the song.
     *
     * @return
     */
    public String getCopyright() {
        return copyright;
    }


    /**
     * Set tonality key.
     * 
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }


    /**
     * Get tonality key.
     * 
     * @return
     */
    public String getKey() {
        return key;
    }


    /**
     * Set the name of the song publisher.
     * 
     * @param publisher
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }


    /**
     * Get the name of the song publisher.
     *
     * @return
     */
    public String getPublisher() {
        return publisher;
    }


    /**
     * Add title of the song within particular locale.
     *
     * @param locale
     * @param title
     */
    public void addTitle(Locale locale, String title) {
        this.title.addTitle(locale, title);
    }


    /**
     * Get title property of the song.
     *
     * @return
     */
    public TitleProperty getTitleProperty() {
        return title;
    }


    /**
     * Add verse name for verse order.
     *
     * @param name
     */
    public void addVerse(String name) {
        if (this.verseOrder == null) {
            this.verseOrder = new ArrayList<String>();
        }

        this.verseOrder.add(name);
    }


    /**
     * Set verse order.
     * Note: allowed only once per instance.
     *
     * @param verseOrder
     */
    public final void setVerseOrder(List<String> verseOrder) {
        if (this.verseOrder == null) {
            this.verseOrder = verseOrder;
        }
    }



    /**
     * Get the whole verse order.
     * 
     * @return
     */
    public List<String> getVerseOrder() {
        return Collections.unmodifiableList(this.verseOrder != null ? this.verseOrder : new ArrayList<String>());
    }
}
