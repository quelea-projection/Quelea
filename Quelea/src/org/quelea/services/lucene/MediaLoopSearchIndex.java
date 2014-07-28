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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.services.utils.LoggerUtils;

/**
 * The search index of mediaLoops.
 *
 * @author Michael
 */
public class MediaLoopSearchIndex implements SearchIndex<MediaLoopDisplayable> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final Analyzer analyzer;
    private final Directory index;
    private final Map<Long, MediaLoopDisplayable> mediaLoops;

    /**
     * Create a new empty search index.
     */
    public MediaLoopSearchIndex() {
        mediaLoops = new HashMap<>();
        analyzer = new StandardAnalyzer(Version.LUCENE_35, new HashSet<String>());
        index = new RAMDirectory();
    }
    
    /**
     * Gets the size of the library of media loops
     * @return the number of media loops
     */
    @Override
    public int size() {
        return mediaLoops.size();
    }

    /**
     * Add a media Loop to the index.
     *
     * @param mediaLoop the mediaLoop to add.
     */
    @Override
    public void add(MediaLoopDisplayable mediaLoop) {
        List<MediaLoopDisplayable> mediaLoopList = new ArrayList<>();
        mediaLoopList.add(mediaLoop);
        addAll(mediaLoopList);
    }

    /**
     * Add a number of mediaLoops to the index. This is much more efficient than
     * calling add() repeatedly because it just uses one writer rather than
     * opening and closing one for each individual operation.
     *
     * @param mediaLoopList the mediaLoop list to add.
     */
    @Override
    public synchronized void addAll(Collection<? extends MediaLoopDisplayable> mediaLoopList) {
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_35, analyzer))) {
            for(MediaLoopDisplayable mediaLoop : mediaLoopList) {
                Document doc = new Document();
                doc.add(new Field("title", mediaLoop.getPreviewText(), Field.Store.NO, Field.Index.ANALYZED));
                doc.add(new Field("number", Long.toString(mediaLoop.getID()), Field.Store.YES, Field.Index.ANALYZED));
                writer.addDocument(doc);
                mediaLoops.put(mediaLoop.getID(), mediaLoop);
                LOGGER.log(Level.FINE, "Added mediaLoop to index: {0}", mediaLoop.getPreviewText());
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't add value to index", ex);
        }
    }

    /**
     * Remove the given mediaLoop from the index.
     *
     * @param mediaLoop the mediaLoop to remove.
     */
    @Override
    public synchronized void remove(MediaLoopDisplayable mediaLoop) {
        try (IndexReader reader = IndexReader.open(index, false)) {
            reader.deleteDocuments(new Term("number", Long.toString(mediaLoop.getID())));
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't remove value from index", ex);
        }
    }

    /**
     * Update the given mediaLoop in the index.
     *
     * @param mediaLoop the mediaLoop to update.
     */
    @Override
    public void update(MediaLoopDisplayable mediaLoop) {
        remove(mediaLoop);
        add(mediaLoop);
    }

    /**
     * Search for mediaLoops that match the given filter.
     *
     * @param queryString the query to use to search.
     * @param type TITLE or BODY, depending on what to search in. BODY is
     * equivalent to the lyrics, TITLE the title.
     * @return an array of mediaLoops that match the filter.
     */
    @Override
    public synchronized MediaLoopDisplayable[] filter(String queryString, FilterType type) {
        String sanctifyQueryString = SearchIndexUtils.makeLuceneQuery(queryString);
        if(mediaLoops.isEmpty() || sanctifyQueryString.isEmpty()) {
            return mediaLoops.values().toArray(new MediaLoopDisplayable[mediaLoops.size()]);
        }
        String typeStr;
         if(type == FilterType.TITLE) {
            typeStr = "title";
        }
        else {
            LOGGER.log(Level.SEVERE, "Unknown type: {0}", type);
            return new MediaLoopDisplayable[0];
        }
        List<MediaLoopDisplayable> ret;
        try (IndexSearcher searcher = new IndexSearcher(IndexReader.open(index))) {
            Query q = new ComplexPhraseQueryParser(Version.LUCENE_35, typeStr, analyzer).parse(sanctifyQueryString);
            TopScoreDocCollector collector = TopScoreDocCollector.create(10000, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            ret = new ArrayList<>();
            for(int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                final Long mediaLoopNumber = Long.parseLong(d.get("number"));
                MediaLoopDisplayable mediaLoop = mediaLoops.get(mediaLoopNumber);
                ret.add(mediaLoop);
            }
            if(type == FilterType.BODY) {
                for(MediaLoopDisplayable mediaLoop : filter(queryString, FilterType.TITLE)) {
                    ret.remove(mediaLoop);
                }
            }
            return ret.toArray(new MediaLoopDisplayable[ret.size()]);
        }
        catch (ParseException | IOException ex) {
            LOGGER.log(Level.WARNING, "Invalid query string: " + sanctifyQueryString, ex);
            return new MediaLoopDisplayable[0];
        }
    }

    /**
     * Remove everything from this index.
     */
    @Override
    public synchronized void clear() {
        SearchIndexUtils.clearIndex(index);
    }
}
