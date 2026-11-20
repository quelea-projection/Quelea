/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
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
package org.quelea.services.lucene;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.ThreadInterruptedException;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;

/**
 * The search index of songs.
 *
 * @author Michael
 */
public class SongSearchIndex implements SearchIndex<SongDisplayable> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final Pattern PUNCTUATION = Pattern.compile("[^\\w\\s]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern CCLI_QUERY = Pattern.compile("(?i)^(?:ccli\\s*[:#-]?\\s*)?[0-9][0-9\\s,.-]*$");
    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");
    private final Analyzer analyzer;
    private final Directory index;
    private final Map<Long, SongDisplayable> songs;
    /** Database duplicates hidden from ordinary searches but searchable by CCLI. */
    private final Map<Long, SongDisplayable> ccliOnlySongs;

    /**
     * Create a new empty search index.
     */
    public SongSearchIndex() {
        songs = new HashMap<>();
        ccliOnlySongs = new HashMap<>();
        try {
            analyzer = CustomAnalyzer.builder()
                    .withTokenizer(StandardTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(ASCIIFoldingFilterFactory.class)
                    .build();
            index = new MMapDirectory(Files.createTempDirectory("quelea-mmap-song").toAbsolutePath());
        }
        catch(IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't create song search index");
            throw new RuntimeException("Couldn't create song search index", ex);
        }
    }

    @Override
    public int size() {
        return songs.size();
    }

    /**
     * Add a song to the index.
     *
     * @param song the song to add.
     */
    @Override
    public void add(SongDisplayable song) {
        List<SongDisplayable> songList = new ArrayList<>();
        songList.add(song);
        addAll(songList);
    }

    /**
     * Add a number of songs to the index. This is much more efficient than
     * calling add() repeatedly because it just uses one writer rather than
     * opening and closing one for each individual operation.
     *
     * @param songList the song list to add.
     */
    @Override
    public synchronized void addAll(Collection<? extends SongDisplayable> songList) {
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer))) {
            for (SongDisplayable song : songList) {
                writer.updateDocument(idTerm(song), createDocument(song));
                songs.put(song.getID(), song);
                ccliOnlySongs.remove(song.getID());
                LOGGER.log(Level.FINE, "Added song to index: {0}", song.getTitle());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't add value to index", ex);
        }
    }

    private Document createDocument(SongDisplayable song) {
        Document doc = createCcliDocument(song);
        if (song.getTitle() != null) {
            doc.add(new TextField("title", PUNCTUATION.matcher(song.getTitle()).replaceAll(""), Field.Store.NO));
        }
        if (song.getAuthor() != null) {
            doc.add(new TextField("author", PUNCTUATION.matcher(song.getAuthor()).replaceAll(""), Field.Store.NO));
        }
        String lyrics = song.getLyrics(false, false, false);
        if (lyrics != null) {
            doc.add(new TextField("lyrics", PUNCTUATION.matcher(lyrics).replaceAll(""), Field.Store.NO));
        }
        return doc;
    }

    private Document createCcliDocument(SongDisplayable song) {
        Document doc = new Document();
        String number = song.getCcli();
        if (number != null) {
            String digits = NON_DIGIT.matcher(number).replaceAll("");
            if (!digits.isEmpty()) {
                doc.add(new StringField("ccli", digits, Field.Store.NO));
            }
        }
        doc.add(new StringField("number", Long.toString(song.getID()), Field.Store.YES));
        return doc;
    }

    /** Add hidden duplicate songs without exposing them in the existing text searches. */
    public synchronized void addCcliOnlyAll(Collection<? extends SongDisplayable> songList) {
        if (songList.isEmpty()) return;
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer))) {
            for (SongDisplayable song : songList) {
                writer.updateDocument(idTerm(song), createCcliDocument(song));
                songs.remove(song.getID());
                ccliOnlySongs.put(song.getID(), song);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't add CCLI-only songs to index", ex);
        }
    }

    public void addCcliOnly(SongDisplayable song) {
        addCcliOnlyAll(List.of(song));
    }

    private static Term idTerm(SongDisplayable song) {
        return new Term("number", Long.toString(song.getID()));
    }

    /**
     * Remove the given song from the index.
     *
     * @param song the song to remove.
     */
    @Override
    public void remove(SongDisplayable song) {
        removeAll(List.of(song));
    }

    /**
     * Remove a number of songs from the index, using just one writer.
     *
     * @param songList the songs to remove.
     */
    public synchronized void removeAll(Collection<? extends SongDisplayable> songList) {
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer))) {
            for (SongDisplayable song : songList) {
                writer.deleteDocuments(idTerm(song));
                songs.remove(song.getID());
                ccliOnlySongs.remove(song.getID());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't remove value from index", ex);
        }
    }

    /**
     * Update the given song in the index.
     *
     * @param song the song to update.
     */
    @Override
    public synchronized void update(SongDisplayable song) {
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer))) {
            writer.updateDocument(idTerm(song), createDocument(song));
            songs.put(song.getID(), song);
            ccliOnlySongs.remove(song.getID());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't update value in index", ex);
        }
    }

    /**
     * Get a song by its ID.
     *
     * @param id the id of the song.
     * @return the song with the given id.
     */
    public synchronized SongDisplayable getByID(long id) {
        SongDisplayable song = songs.get(id);
        return song != null ? song : ccliOnlySongs.get(id);
    }

    /**
     * Search for songs that match the given filter.
     *
     * @param queryString the query to use to search.
     * @param type which field to search; CCLI searches normalized song numbers.
     * @return an array of songs that match the filter.
     */
    @Override
    public synchronized SongDisplayable[] filter(String queryString, FilterType type) {
        if (type == FilterType.CCLI && (queryString == null || queryString.trim().isEmpty())) {
            return songs.values().toArray(new SongDisplayable[0]);
        }
        String sanctifyQueryString = SearchIndexUtils.makeLuceneQuery(queryString);
        if (sanctifyQueryString.trim().isEmpty()) {
            if (type == FilterType.CCLI) return new SongDisplayable[0];
            return songs.values().toArray(new SongDisplayable[songs.size()]);
        }
        if (songs.isEmpty() && (type != FilterType.CCLI || ccliOnlySongs.isEmpty())) {
            return new SongDisplayable[0];
        }
        String typeStr;
        String ccliDigits = null;
        if (type == FilterType.BODY) {
            typeStr = "lyrics";
        } else if (type == FilterType.TITLE) {
            typeStr = "title";
        } else if (type == FilterType.AUTHOR) {
            typeStr = "author";
        } else if (type == FilterType.CCLI) {
            String trimmed = queryString.trim();
            if (!CCLI_QUERY.matcher(trimmed).matches()) {
                return new SongDisplayable[0];
            }
            ccliDigits = NON_DIGIT.matcher(trimmed).replaceAll("");
            typeStr = "ccli";
        } else {
            LOGGER.log(Level.SEVERE, "Unknown type: {0}", type);
            return new SongDisplayable[0];
        }
        List<SongDisplayable> ret;
        try (DirectoryReader dr = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(dr);
            Query q = type == FilterType.CCLI
                    ? new PrefixQuery(new Term(typeStr, ccliDigits))
                    : new ComplexPhraseQueryParser(typeStr, analyzer).parse(sanctifyQueryString);
            TopScoreDocCollector collector = TopScoreDocCollector.create(1000,10000);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            ret = new ArrayList<>();
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                final Long songNumber = Long.parseLong(d.get("number"));
                SongDisplayable song = songs.get(songNumber);
                if (song == null && type == FilterType.CCLI) {
                    song = ccliOnlySongs.get(songNumber);
                }
                if (song != null) ret.add(song);
            }
            if (type == FilterType.BODY) {
                for (SongDisplayable song : filter(queryString, FilterType.TITLE)) {
                    ret.remove(song);
                }
            }
            return ret.toArray(new SongDisplayable[ret.size()]);
        }
        catch(ClosedByInterruptException|ThreadInterruptedException ex) {
            //Ignore, thread is being shut down by other character being typed
            return new SongDisplayable[0];
        }
        catch (ParseException | IOException ex) {
            LOGGER.log(Level.WARNING, "Invalid query string: " + sanctifyQueryString, ex);
            return new SongDisplayable[0];
        }
    }

    /**
     * Remove everything from this index.
     */
    @Override
    public synchronized void clear() {
        SearchIndexUtils.clearIndex(index);
        songs.clear();
        ccliOnlySongs.clear();
    }
}
