/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.data.bible.versification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;

/**
 * Tells which book a book in a bible file is. Books are named by their USFM
 * code (GEN, PSA, WIS and so on), the codes the Copenhagen Alliance data uses.
 * <p>
 * A book is recognised by its name, using the names BibleOrgSys knows, and
 * then by its Zefania book number. Because some files have wrong names and
 * numbers for the deuterocanonical books, the chapters and verses of the book
 * are compared with the standard versifications, and a book whose content
 * clearly belongs to another book is taken to be that book.
 *
 * @author Arvid
 */
public final class BookCodes {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final String RESOURCES = "/org/quelea/data/bible/versification/";
    private static final String[] SYSTEMS = {"org", "eng", "lxx", "vul", "rsc", "rso"};

    /**
     * The books of the Protestant canon in Zefania order, numbers 1 to 66.
     */
    private static final String[] PROTESTANT = {
        "GEN", "EXO", "LEV", "NUM", "DEU", "JOS", "JDG", "RUT", "1SA", "2SA", "1KI", "2KI", "1CH", "2CH",
        "EZR", "NEH", "EST", "JOB", "PSA", "PRO", "ECC", "SNG", "ISA", "JER", "LAM", "EZK", "DAN", "HOS",
        "JOL", "AMO", "OBA", "JON", "MIC", "NAM", "HAB", "ZEP", "HAG", "ZEC", "MAL", "MAT", "MRK", "LUK",
        "JHN", "ACT", "ROM", "1CO", "2CO", "GAL", "EPH", "PHP", "COL", "1TH", "2TH", "1TI", "2TI", "TIT",
        "PHM", "HEB", "JAS", "1PE", "2PE", "1JN", "2JN", "3JN", "JUD", "REV"
    };

    /**
     * The numbers most Zefania files use for the deuterocanonical books. The
     * Zefania schema doesn't define them; numbers 74 and 75 (the additions to
     * Daniel and Esther) aren't single books in the Copenhagen data and are
     * left out.
     */
    private static final Map<Integer, String> DEUTEROCANON = Map.ofEntries(
            Map.entry(67, "JDT"), Map.entry(68, "WIS"), Map.entry(69, "TOB"), Map.entry(70, "SIR"),
            Map.entry(71, "BAR"), Map.entry(72, "1MA"), Map.entry(73, "2MA"), Map.entry(76, "MAN"),
            Map.entry(77, "3MA"), Map.entry(78, "4MA"), Map.entry(79, "LJE"), Map.entry(80, "1ES"),
            Map.entry(81, "2ES"));

    private static final Map<String, String> VARIANTS = Map.of("EST", "ESG", "DAN", "DAG");

    private static final double GOOD_MATCH = 0.8;
    private static final double POOR_MATCH = 0.5;
    private static final double MARGIN = 0.2;

    private static volatile BookCodes instance;

    private final Map<String, Set<String>> codesByName = new HashMap<>();
    private final Map<String, List<int[]>> profiles = new HashMap<>();

    private BookCodes() {
        loadNames();
        loadProfiles();
    }

    /**
     * Get the book codes, loading them the first time.
     *
     * @return the book codes.
     */
    public static BookCodes get() {
        if (instance == null) {
            synchronized (BookCodes.class) {
                if (instance == null) {
                    instance = new BookCodes();
                }
            }
        }
        return instance;
    }

    /**
     * Get the code a Zefania book number usually stands for.
     *
     * @param number the book number.
     * @return the code, or null if the number has no usual meaning.
     */
    public static String getCodeForNumber(int number) {
        if (number >= 1 && number <= PROTESTANT.length) {
            return PROTESTANT[number - 1];
        }
        return DEUTEROCANON.get(number);
    }

    /**
     * Get the books a name or abbreviation can stand for.
     *
     * @param name the name of a book, in any of the languages BibleOrgSys
     * knows.
     * @return the codes of the books, in no particular order, or an empty set
     * if the name isn't known.
     */
    public Set<String> getCodesForName(String name) {
        if (name == null) {
            return Collections.emptySet();
        }
        Set<String> ret = codesByName.get(normalise(name));
        return ret == null ? Collections.emptySet() : Collections.unmodifiableSet(ret);
    }

    /**
     * Work out which book a book is.
     *
     * @param name the name of the book in the file, or null.
     * @param shortName the short name of the book in the file, or null.
     * @param number the number of the book in the file.
     * @param verseCounts the highest verse number of each chapter, in order.
     * @return the code of the book, or null if it can't be told.
     */
    public String identify(String name, String shortName, int number, int[] verseCounts) {
        Set<String> named = getCodesForName(name);
        if (named.isEmpty()) {
            named = getCodesForName(shortName);
        }
        String numbered = getCodeForNumber(number);
        boolean comparable = verseCounts.length > 1;
        String ret;
        if (named.size() == 1) {
            ret = named.iterator().next();
        } else if (named.contains(numbered)) {
            ret = numbered;
        } else if (!named.isEmpty()) {
            ret = bestOf(named, verseCounts);
        } else {
            ret = numbered;
        }
        if (ret != null && numbered != null && !ret.equals(numbered) && comparable
                && getScore(numbered, verseCounts) > getScore(ret, verseCounts)) {
            ret = numbered;
        }
        if (!comparable) {
            return ret;
        }
        String best = getBestByContent(verseCounts);
        double score = ret == null ? -1 : getScore(ret, verseCounts);
        if (best != null && !best.equals(ret) && !isVariant(best, ret)
                && (ret == null || (score >= 0 && (score < POOR_MATCH || getScore(best, verseCounts) - score >= MARGIN)))) {
            return best;
        }
        if (ret != null && named.isEmpty() && number > PROTESTANT.length && score >= 0 && score < POOR_MATCH) {
            // Files don't agree on the numbers of the deuterocanonical books
            return null;
        }
        return ret;
    }

    /**
     * Determine whether two books are the same book with or without the Greek
     * additions, which the content can't always tell apart.
     */
    private static boolean isVariant(String a, String b) {
        return b != null && (VARIANTS.getOrDefault(a, "").equals(b) || VARIANTS.getOrDefault(b, "").equals(a));
    }

    /**
     * Get the one of some books whose content fits a book best.
     *
     * @return the code, or null if the content can't tell them apart.
     */
    private String bestOf(Set<String> codes, int[] verseCounts) {
        String ret = null;
        double best = 0;
        boolean tie = false;
        for (String code : codes) {
            double score = getScore(code, verseCounts);
            if (score > best) {
                best = score;
                ret = code;
                tie = false;
            } else if (score == best && score > 0) {
                tie = true;
            }
        }
        return tie ? null : ret;
    }

    /**
     * Find the book whose content clearly fits a book best.
     *
     * @param verseCounts the highest verse number of each chapter, in order.
     * @return the code, or null if no book fits well or two books fit about
     * as well.
     */
    String getBestByContent(int[] verseCounts) {
        String best = null;
        double bestScore = 0;
        double secondScore = 0;
        for (String code : profiles.keySet()) {
            double score = getScore(code, verseCounts);
            if (score > bestScore) {
                secondScore = bestScore;
                bestScore = score;
                best = code;
            } else if (score > secondScore) {
                secondScore = score;
            }
        }
        if (best == null || bestScore < GOOD_MATCH || bestScore - secondScore < MARGIN) {
            return null;
        }
        return best;
    }

    /**
     * Get how well the chapters and verses of a book fit a book, in the
     * standard versification that fits best.
     *
     * @param code the code of the book.
     * @param verseCounts the highest verse number of each chapter, in order.
     * @return the share of chapters with about the same number of verses,
     * from 0 to 1, or -1 if there's nothing to compare with.
     */
    public double getScore(String code, int[] verseCounts) {
        List<int[]> list = profiles.get(code);
        if (list == null) {
            return -1;
        }
        double ret = 0;
        for (int[] profile : list) {
            int chapters = Math.max(profile.length, verseCounts.length);
            int close = 0;
            for (int i = 0; i < Math.min(profile.length, verseCounts.length); i++) {
                if (Math.abs(profile[i] - verseCounts[i]) <= Math.max(1, profile[i] / 10)) {
                    close++;
                }
            }
            ret = Math.max(ret, close / (double) chapters);
        }
        return ret;
    }

    /**
     * Normalise a book name so that small differences in writing it don't
     * matter: case, accents, punctuation, spaces and Roman numbers.
     *
     * @param name the name.
     * @return the normalised name.
     */
    static String normalise(String name) {
        String ret = Normalizer.normalize(name, Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
        ret = ret.replaceFirst("^(iii|3rd|third)\\b\\.?", "3")
                .replaceFirst("^(ii|2nd|second)\\b\\.?", "2")
                .replaceFirst("^(i|1st|first)\\b\\.?", "1");
        return ret.replaceAll("[^\\p{L}\\d]", "");
    }

    private void loadNames() {
        try (BufferedReader reader = open("book-names.tsv")) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\t");
                for (int i = 0; i < parts.length; i++) {
                    String key = normalise(parts[i]);
                    if (!key.isEmpty()) {
                        codesByName.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(parts[0]);
                    }
                }
            }
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Couldn't read the book names", ex);
        }
    }

    private void loadProfiles() {
        for (String system : SYSTEMS) {
            try (Reader reader = open(system + ".json")) {
                JsonObject maxVerses = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("maxVerses");
                for (Map.Entry<String, JsonElement> book : maxVerses.entrySet()) {
                    JsonArray chapters = book.getValue().getAsJsonArray();
                    int[] counts = new int[chapters.size()];
                    for (int i = 0; i < counts.length; i++) {
                        counts[i] = Integer.parseInt(chapters.get(i).getAsString().trim());
                    }
                    profiles.computeIfAbsent(book.getKey(), k -> new ArrayList<>()).add(counts);
                }
            } catch (IOException | RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Couldn't read the versification " + system, ex);
            }
        }
    }

    /**
     * Open one of the versification resources.
     *
     * @param name the file name.
     * @return a reader for the file.
     * @throws IOException if the file is missing.
     */
    static BufferedReader open(String name) throws IOException {
        InputStream stream = BookCodes.class.getResourceAsStream(RESOURCES + name);
        if (stream == null) {
            throw new IOException("Missing resource " + name);
        }
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
