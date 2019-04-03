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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.quelea.data.bible.BibleChapter;
import org.quelea.services.utils.LoggerUtils;

/**
 * Search index used for indexing the bibles.
 * @author Michael
 */
public class BibleSearchIndex implements SearchIndex<BibleChapter> {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private Analyzer analyzer;
    private Directory index;
    private Map<Integer, BibleChapter> chapters;

    /**
     * Create a new empty search index.
     */
    public BibleSearchIndex() {
        chapters = new HashMap<>();
        try {
            analyzer = CustomAnalyzer.builder()
                    .withTokenizer(StandardTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(ASCIIFoldingFilterFactory.class)
                    .build();
            index = new MMapDirectory(Files.createTempDirectory("quelea-mmap-bible").toAbsolutePath());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't create song search index");
            throw new RuntimeException("Couldn't create song search index", ex);
        }
    }
    
    @Override
    public int size() {
        return chapters.size();
    }

    /**
     * Add a bible chapter to the index.
     *
     * @param chapter the chapter to add.
     */
    @Override
    public void add(BibleChapter chapter) {
        List<BibleChapter> list = new ArrayList<>();
        list.add(chapter);
        addAll(list);
    }

    /**
     * Add a number of chapters to the index. This is much more efficient than
     * calling add() repeatedly because it just uses one writer rather than
     * opening and closing one for each individual operation.
     *
     * @param bibleList the list of chapters to add.
     */
    @Override
    public void addAll(Collection<? extends BibleChapter> bibleList) {
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer))) {
            for(BibleChapter chapter : bibleList) {
                Document doc = new Document();
                doc.add(new TextField("text", chapter.getText(), Field.Store.NO));
                doc.add(new TextField("number", Integer.toString(chapter.getID()), Field.Store.YES));
                writer.addDocument(doc);
                chapters.put(chapter.getID(), chapter);
                LOGGER.log(Level.FINE, "Added bible chapter to index: {0}", chapter.getID());
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't add value to index", ex);
        }
    }

    /**
     * Remove the given bible chapter from the index.
     *
     * @param chapter the chapter to remove.
     */
    @Override
    public void remove(BibleChapter chapter) {
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(analyzer))) {
            writer.deleteDocuments(new Term("number", Integer.toString(chapter.getID())));
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't remove value from index", ex);
        }
    }

    /**
     * Update the given bible chapter in the index.
     *
     * @param chapter the chapter to update.
     */
    @Override
    public void update(BibleChapter chapter) {
        remove(chapter);
        add(chapter);
    }

    /**
     * Search for bible chapters that match the given filter.
     *
     * @param queryString the query string to filter.
     * @param type ignored - may be null.
     * @return a list of all bible chapters that match the given filter.
     */
    @Override
    public BibleChapter[] filter(String queryString, FilterType type) {
        String sanctifyQueryString = SearchIndexUtils.makeLuceneQuery(queryString);
        if(chapters.isEmpty() || sanctifyQueryString.isEmpty()) {
            return chapters.values().toArray(new BibleChapter[chapters.size()]);
        }
        List<BibleChapter> ret;
        try (DirectoryReader dr = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(dr);
            BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
            Query q = new ComplexPhraseQueryParser("text", analyzer).parse(sanctifyQueryString);
            TopScoreDocCollector collector = TopScoreDocCollector.create(10000,10000);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            ret = new ArrayList<>();
            for(int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                BibleChapter chapter = chapters.get(Integer.parseInt(d.get("number")));
                ret.add(chapter);
            }
            return ret.toArray(new BibleChapter[ret.size()]);
        }
        catch (ParseException | IOException ex) {
            LOGGER.log(Level.WARNING, "Invalid query string: " + sanctifyQueryString, ex);
            return new BibleChapter[0];
        }
    }
    
    /**
     * Remove everything from this index.
     */
    @Override
    public void clear() {
        SearchIndexUtils.clearIndex(index);
    }
}
