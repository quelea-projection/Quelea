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
package org.quelea.windows.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleChapter;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleReferenceParser;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.lucene.SearchIndex.FilterType;
import org.quelea.services.lucene.SearchIndexUtils;
import org.quelea.services.notice.Notice;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.AddSongActionHandler;
import org.quelea.windows.style.QueleaIcon;

/**
 * Searches everything that can be added to the schedule, and the saved
 * notices. Results come in a fixed order: a recognised bible reference first,
 * then songs, then whatever else matches.
 *
 * @author Arvid
 */
public class UniversalSearch {

    /**
     * What a result is, in the order they are offered as filters.
     */
    public enum Kind {
        SONG(QueleaIcon.LYRICS, "library.songs.heading"),
        BIBLE(QueleaIcon.BIBLE, "library.bible.heading"),
        IMAGE(QueleaIcon.IMAGES, "library.image.heading"),
        VIDEO(QueleaIcon.VIDEO, "library.video.heading"),
        TIMER(QueleaIcon.TIMER, "library.timer.heading"),
        NOTICE(QueleaIcon.NOTICES, "notices.heading");

        private final QueleaIcon icon;
        private final String label;

        Kind(QueleaIcon icon, String label) {
            this.icon = icon;
            this.label = label;
        }

        public QueleaIcon getIcon() {
            return icon;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * One thing that was found.
     *
     * @param kind what it is.
     * @param title the main text to show.
     * @param subtitle more detail, may be empty.
     * @param previewText the parts of the text to preview, may be empty.
     * @param previewStart the part to scroll to, because that's what matched.
     * @param previewFile the image or video to preview, may be null.
     * @param action what enter does: adds it to the schedule or, for a notice,
     * opens it to be confirmed. Must run on the FX thread.
     */
    public record Result(Kind kind, String title, String subtitle, List<String> previewText, int previewStart,
            File previewFile, Runnable action) {
    }

    /**
     * What to search that lives in the UI, so has to be read on the FX thread.
     *
     * @param bible the bible to look in, may be null.
     * @param timers the timers to look in.
     * @param notices the saved notices to look in.
     */
    public record Sources(Bible bible, List<TimerDisplayable> timers, List<Notice> notices) {
    }

    private static final int MAX_REFERENCES = 5;
    private static final int MAX_SONGS = 15;
    private static final int MAX_OTHERS = 5;
    /** With only one kind of result there's room to show much more of it. */
    private static final int MAX_FILTERED = 50;
    private static final Pattern NOT_A_WORD = Pattern.compile("[^\\p{L}\\p{N}]+");
    /** Shorter words match too much of a bible to be useful. */
    private static final int MIN_BIBLE_TEXT_LENGTH = 3;

    /**
     * Search for something. This can take a moment, so it shouldn't be called
     * on the FX thread.
     *
     * @param query what to search for.
     * @param filter the only kind of result to look for, null for all kinds.
     * @param sources what to look in besides the songs and the media files.
     * @return what was found, best first.
     */
    public List<Result> search(String query, Kind filter, Sources sources) {
        String trimmed = query.trim();
        int maxSongs = filter == null ? MAX_SONGS : MAX_FILTERED;
        int maxOthers = filter == null ? MAX_OTHERS : MAX_FILTERED;
        // An empty lucene query means "everything", which isn't a search here
        boolean searchable = !SearchIndexUtils.makeLuceneQuery(trimmed).isEmpty();
        List<Result> ret = new ArrayList<>();
        if (includes(filter, Kind.BIBLE)) {
            ret.addAll(searchReferences(trimmed, sources.bible()));
        }
        if (searchable && includes(filter, Kind.SONG)) {
            ret.addAll(searchSongs(trimmed, maxSongs));
        }
        if (searchable && includes(filter, Kind.BIBLE)) {
            ret.addAll(searchBibleText(trimmed, sources.bible(), maxOthers));
        }
        if (includes(filter, Kind.IMAGE)) {
            ret.addAll(searchFiles(trimmed, Kind.IMAGE, QueleaProperties.get().getImageDir(), Utils::fileIsImage, maxOthers));
        }
        if (includes(filter, Kind.VIDEO)) {
            ret.addAll(searchFiles(trimmed, Kind.VIDEO, QueleaProperties.get().getVidDir(), Utils::fileIsVideo, maxOthers));
        }
        if (includes(filter, Kind.TIMER)) {
            ret.addAll(searchTimers(trimmed, sources.timers(), maxOthers));
        }
        if (includes(filter, Kind.NOTICE)) {
            ret.addAll(searchNotices(trimmed, sources.notices(), maxOthers));
        }
        return ret;
    }

    private static boolean includes(Kind filter, Kind kind) {
        return filter == null || filter == kind;
    }

    private List<Result> searchReferences(String query, Bible bible) {
        return BibleReferenceParser.parse(query, bible).stream()
                .limit(MAX_REFERENCES)
                .map(reference -> new Result(Kind.BIBLE, reference.getLocation(), reference.verses().getFirst().getText(),
                        previewVerses(reference.verses()), 0, null,
                        () -> addToSchedule(new BiblePassage(bible.getName(), reference.getLocation(),
                                reference.verses().toArray(new BibleVerse[0]), reference.multi()))))
                .toList();
    }

    private static List<String> previewVerses(List<BibleVerse> verses) {
        return verses.stream()
                .map(verse -> verse.getNum() + " " + verse.getText())
                .toList();
    }

    private List<Result> searchSongs(String query, int max) {
        // By ID, because a song can match on both its title and its author
        Map<Long, Result> found = new LinkedHashMap<>();
        Arrays.stream(SongManager.get().getIndex().filter(query, FilterType.TITLE))
                .sorted(Comparator.comparingInt(song -> rankTitle(song.getTitle(), query)))
                .forEach(song -> found.putIfAbsent(song.getID(), songResult(song, song.getAuthor(), query)));
        for (SongDisplayable song : SongManager.get().getIndex().filter(query, FilterType.AUTHOR)) {
            found.putIfAbsent(song.getID(), songResult(song, song.getAuthor(), query));
        }
        for (SongDisplayable song : SongManager.get().getIndex().filter(query, FilterType.BODY)) {
            found.putIfAbsent(song.getID(), songResult(song, findLine(song.getLyrics(false, false, false), query), query));
        }
        return found.values().stream().limit(max).toList();
    }

    private Result songResult(SongDisplayable song, String subtitle, String query) {
        List<String> sections = Arrays.stream(song.getSectionsWithoutSequence())
                .map(section -> String.join("\n", section.getText(false, false)))
                .toList();
        return new Result(Kind.SONG, song.getTitle(), subtitle == null ? "" : subtitle, sections, findSection(sections, query), null, () -> {
            new AddSongActionHandler(QueleaProperties.get().getDefaultSongDBUpdate()).add(song);
        });
    }

    /**
     * Find the section of a song to scroll the preview to, so that a song
     * found by its lyrics shows why.
     *
     * @param sections the text of each section of the song.
     * @param query what was searched for.
     * @return the index of the first section with the query, otherwise 0.
     */
    static int findSection(List<String> sections, String query) {
        for (int i = 0; i < sections.size(); i++) {
            if (containsWords(sections.get(i), query)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Rank a song title against a query, so that typing a whole title gets you
     * that song and not one that happens to contain it. Lower is better.
     *
     * @param title the title of the song.
     * @param query what was searched for.
     * @return 0 for the same title, 1 if it starts with the query, otherwise 2.
     */
    static int rankTitle(String title, String query) {
        String foldedTitle = SearchIndexUtils.fold(title);
        String foldedQuery = SearchIndexUtils.fold(query);
        if (foldedTitle.equals(foldedQuery)) {
            return 0;
        }
        return foldedTitle.startsWith(foldedQuery) ? 1 : 2;
    }

    /**
     * Find the line of some lyrics that contains a query, to show why a song
     * was found.
     *
     * @param lyrics the lyrics of the song.
     * @param query what was searched for.
     * @return the first line that contains the query, or an empty string.
     */
    static String findLine(String lyrics, String query) {
        return lyrics.lines()
                .filter(line -> containsWords(line, query))
                .map(String::trim)
                .findFirst()
                .orElse("");
    }

    private List<Result> searchBibleText(String query, Bible bible, int max) {
        if (bible == null || query.length() < MIN_BIBLE_TEXT_LENGTH || !BibleManager.get().isIndexInit()) {
            return List.of();
        }
        List<Result> ret = new ArrayList<>();
        // The index covers every bible and finds chapters, so narrow it down
        for (BibleChapter chapter : BibleManager.get().getIndex().filter(query, null)) {
            if (!chapter.getBook().getBible().getName().equals(bible.getName())) {
                continue;
            }
            for (BibleVerse verse : chapter.getVerses()) {
                if (containsWords(verse.getText(), query)) {
                    String location = chapter.getBook() + " " + chapter.getNum() + ":" + verse.getNum();
                    ret.add(new Result(Kind.BIBLE, location, verse.getText(), previewVerses(List.of(verse)), 0, null, () -> addToSchedule(
                            new BiblePassage(bible.getName(), location, new BibleVerse[]{verse}, false))));
                    if (ret.size() == max) {
                        return ret;
                    }
                }
            }
        }
        return ret;
    }

    private List<Result> searchFiles(String query, Kind kind, File dir, Predicate<File> isWanted, int max) {
        File[] files = dir.listFiles();
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files)
                .filter(file -> !file.isDirectory() && !file.isHidden() && isWanted.test(file))
                .filter(file -> matchesName(file.getName(), query))
                .sorted()
                .limit(max)
                .map(file -> new Result(kind, file.getName(), "", List.of(), 0, file, () -> addToSchedule(
                        kind == Kind.IMAGE ? new ImageDisplayable(file) : new VideoDisplayable(file.getAbsolutePath()))))
                .toList();
    }

    private List<Result> searchTimers(String query, List<TimerDisplayable> timers, int max) {
        return timers.stream()
                .filter(timer -> matchesName(timer.getName(), query))
                .limit(max)
                .map(timer -> new Result(Kind.TIMER, timer.getName(), "", List.of(), 0, null, () -> addToSchedule(timer)))
                .toList();
    }

    private List<Result> searchNotices(String query, List<Notice> notices, int max) {
        return notices.stream()
                .filter(notice -> matchesName(notice.getText(), query))
                .limit(max)
                .map(notice -> new Result(Kind.NOTICE, notice.getText(), "", List.of(notice.getText()), 0, null,
                        () -> QueleaApp.get().getMainWindow().getNoticeDialog().useTemplate(notice)))
                .toList();
    }

    /**
     * Check if a text contains a query the way the search indexes see it: the
     * words in a row, whatever the case, punctuation or line breaks between
     * them. So "dangers toils" is in "Through many dangers, toils and snares".
     *
     * @param text the text to look in.
     * @param query what was searched for.
     * @return true if the text contains the query.
     */
    static boolean containsWords(String text, String query) {
        return wordsOf(text).contains(wordsOf(query));
    }

    private static String wordsOf(String text) {
        return NOT_A_WORD.matcher(SearchIndexUtils.fold(text)).replaceAll(" ").trim();
    }

    /**
     * Check a name against a query, word by word, so that "easter sunrise"
     * finds "Sunrise at Easter.jpg".
     *
     * @param name the name to check.
     * @param query what was searched for.
     * @return true if every word of the query is somewhere in the name.
     */
    static boolean matchesName(String name, String query) {
        String foldedName = SearchIndexUtils.fold(name);
        return Arrays.stream(SearchIndexUtils.fold(query).split("\\s+")).allMatch(foldedName::contains);
    }

    private static void addToSchedule(Displayable displayable) {
        QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().add(displayable);
    }
}
