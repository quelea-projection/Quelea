/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.SysexMessage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.quelea.SongDatabase;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;

/**
 *
 * @author Michael
 */
public class SearchIndex {

    public enum FilterType {

        TITLE, LYRICS
    }
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private Analyzer analyzer;
    private Directory index;
    private List<Song> songs;

    public SearchIndex() {
        songs = new ArrayList<>();
        analyzer = new StandardAnalyzer(Version.LUCENE_35, new HashSet<String>());
        index = new RAMDirectory();
    }

    public void addSong(Song song) {
        Document doc = new Document();
        doc.add(new Field("title", song.getTitle(), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("lyrics", song.getLyrics(false, false), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("number", Integer.toString(songs.size()), Field.Store.YES, Field.Index.ANALYZED));
        songs.add(song);
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_35, analyzer))) {
            writer.addDocument(doc);
            LOGGER.log(Level.FINE, "Added song to index: {0}", song.getTitle());
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't add value to index", ex);
        }
    }

    public void removeSong(Song song) {
    }

    public Song[] filterSongs(String queryString, FilterType type) {
        if(queryString.isEmpty()) {
            return songs.toArray(new Song[songs.size()]);
        }
        String typeStr;
        if(type==FilterType.LYRICS) {
            typeStr = "lyrics";
        }
        else if(type==FilterType.TITLE) {
            typeStr = "title";
        }
        else {
            LOGGER.log(Level.SEVERE, "Unknown type: {0}", type);
            return new Song[0];
        }
        queryString = sanctifyQuery(queryString);
        List<Song> ret;
        try (IndexSearcher searcher = new IndexSearcher(IndexReader.open(index))) {
            Query q = new ComplexPhraseQueryParser(Version.LUCENE_35, typeStr, analyzer).parse(queryString);
            TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            ret = new ArrayList<>();
            for(int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                Song song = songs.get(Integer.parseInt(d.get("number")));
                ret.add(song);
            }
            return ret.toArray(new Song[ret.size()]);
        }
        catch (ParseException | IOException ex) {
            LOGGER.log(Level.WARNING, "Invalid query string: " + queryString, ex);
            return new Song[0];
        }
    }

    private String sanctifyQuery(String query) {
        query = query.trim();
        if(query.contains(" ")) {
            query = "\"" + query + "*\"";
        }
        else {
            query = query + "*";
        }
        return query;
    }

    public static void main(String[] args) throws IOException, ParseException {

        SongDatabase.get().getSongs();

        SearchIndex si = SongDatabase.get().getIndex();
        long time = System.currentTimeMillis();
        Song[] result = si.filterSongs("majest", FilterType.TITLE);
        System.out.println(System.currentTimeMillis() - time);

        for(Song song : result) {
            System.out.println(song.getTitle());
        }

    }
}
