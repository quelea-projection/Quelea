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
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.standard.QueryParserUtil;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.quelea.services.utils.LoggerUtils;

/**
 * General utility methods for search indexes.
 * <p/>
 * @author Michael
 */
public class SearchIndexUtils {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Don't make me...
     */
    private SearchIndexUtils() {
        throw new AssertionError();
    }

    /**
     * Clear the given index.
     * <p/>
     * @param index the index to clear.
     */
    public static void clearIndex(Directory index) {
        try(IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_35, new StandardAnalyzer(Version.LUCENE_35, new HashSet<String>())))) {
            writer.deleteAll();
            writer.commit();
        }
        catch(IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't clear the index", ex);
        }
    }

    /**
     * Sanitise the given query so it's "lucene-safe". Make sure it's what we
     * want as well - treat as a phrase with a partial match for the last word.
     * <p/>
     * @param query the query to sanitise.
     * @return the sanitised query.
     */
    public static String makeLuceneQuery(String query) {
        query = Pattern.compile("[^\\w ]", Pattern.UNICODE_CHARACTER_CLASS).matcher(query).replaceAll("");
//        query = QueryParserUtil.escape(query);
        query = query.trim();
        if(query.isEmpty()) {
            return query;
        }
        if(query.contains(" ")) {
            query = "\"" + query + "*\"";
        }
        else {
            query = query + "*";
        }
        return query;
    }
}
