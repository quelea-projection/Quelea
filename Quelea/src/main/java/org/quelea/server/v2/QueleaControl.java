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
package org.quelea.server.v2;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * What the v2 remote API can see and do in Quelea. The server only talks to
 * this interface, so it can be tested without JavaFX.
 * <p>
 * All methods may be called from any thread. Implementations that touch the
 * user interface throw {@link FxCall.TimeoutException} if the FX thread does
 * not respond in time. Commands return the live state right after the
 * command ran.
 */
public interface QueleaControl {

    /**
     * @return the current live state.
     */
    LiveState getState();

    /**
     * @return the current schedule.
     */
    ScheduleSnapshot getSchedule();

    /**
     * @return the sections of the live item, with their text.
     */
    LiveSections getLiveSections();

    /**
     * Go to the next section, or the next item if the setting for that is on.
     *
     * @return the new state.
     */
    LiveState next();

    /**
     * Go to the previous section.
     *
     * @return the new state.
     */
    LiveState previous();

    /**
     * Select a section of the live item.
     *
     * @param index the zero-based section index.
     * @return the new state.
     * @throws IllegalArgumentException if the index is out of range or
     * nothing is live.
     */
    LiveState gotoSection(int index);

    /**
     * Put the next schedule item live.
     *
     * @return the new state.
     */
    LiveState nextItem();

    /**
     * Put the previous schedule item live.
     *
     * @return the new state.
     */
    LiveState previousItem();

    /**
     * Put a schedule item live.
     *
     * @param itemId the item's id.
     * @return the new state.
     * @throws NoSuchItemException if no schedule item has that id.
     */
    LiveState goLive(String itemId);

    /**
     * Set the logo, black and clear states. A null argument leaves that state
     * as it is.
     *
     * @param logo the new logo state, or null.
     * @param black the new black state, or null.
     * @param clear the new clear state, or null.
     * @return the new state.
     */
    LiveState setScreen(Boolean logo, Boolean black, Boolean clear);

    /**
     * Start or stop audio recording.
     *
     * @param recording true to record.
     * @return the new state.
     */
    LiveState setRecording(boolean recording);

    // ---- The live video, audio file or timer. Each fails with
    // IllegalArgumentException if the live item is none of those.
    LiveState playMedia();

    LiveState pauseMedia();

    LiveState stopMedia();

    /**
     * Jump and/or change looping; null leaves a setting as it is.
     *
     * @param position where to jump to, from 0 to 1.
     * @param loop true to play again at the end.
     * @return the new state.
     */
    LiveState setMedia(Double position, Boolean loop);

    // ---- Schedule changes. A non-null ifRevision makes the change fail
    // with ScheduleConflictException if the schedule has another revision.
    /**
     * Add a song from the database to the end of the schedule.
     *
     * @param songId the song's database id.
     * @param ifRevision the expected schedule revision, or null.
     * @return the new item and schedule.
     * @throws java.util.NoSuchElementException if there is no such song.
     */
    ScheduleChange addSong(long songId, Long ifRevision);

    /**
     * Add a bible passage to the end of the schedule.
     *
     * @param translation the bible's name.
     * @param reference the passage, for example "John 3:16-18".
     * @param ifRevision the expected schedule revision, or null.
     * @return the new item and schedule.
     * @throws java.util.NoSuchElementException if there is no such bible.
     * @throws IllegalArgumentException if the reference is not understood.
     */
    ScheduleChange addBible(String translation, String reference, Long ifRevision);

    /**
     * Remove an item from the schedule.
     *
     * @param itemId the item's id.
     * @param ifRevision the expected schedule revision, or null.
     * @return the removed item's id and the new schedule.
     * @throws NoSuchItemException if there is no such item.
     */
    ScheduleChange removeItem(String itemId, Long ifRevision);

    /**
     * Move an item to another position in the schedule.
     *
     * @param itemId the item's id.
     * @param to the new zero-based position.
     * @param ifRevision the expected schedule revision, or null.
     * @return the moved item's id and the new schedule.
     * @throws NoSuchItemException if there is no such item.
     * @throws IllegalArgumentException if the position is out of range.
     */
    ScheduleChange moveItem(String itemId, int to, Long ifRevision);

    /**
     * Transpose a song in the schedule for this session only: the schedule
     * entry is replaced by a transposed copy that is never written to the
     * song database. The item keeps its id; if it is live, the transposed
     * version is shown at once.
     *
     * @param itemId the item's id.
     * @param semitones how far to move the chords, for example -2.
     * @param ifRevision the expected schedule revision, or null.
     * @return the item's id and the new schedule.
     * @throws NoSuchItemException if there is no such item.
     * @throws IllegalArgumentException if the item is not a song.
     */
    ScheduleChange transpose(String itemId, int semitones, Long ifRevision);

    // ---- Songs, bibles and themes.
    /**
     * Search the song database by title, author and lyrics.
     *
     * @param query the search text.
     * @param limit the maximum number of results.
     * @return the matching songs, best matches first.
     */
    List<Catalog.Song> searchSongs(String query, int limit);

    /**
     * @param songId the song's database id.
     * @return the song with its text.
     * @throws java.util.NoSuchElementException if there is no such song.
     */
    Catalog.SongDetails getSong(long songId);

    /**
     * Save a new song in the song database, as Quelea's new song window
     * does. It is not added to the schedule; use addSong for that.
     *
     * @param song the title, lyrics and other details.
     * @return the saved song with its new id.
     * @throws IllegalArgumentException if the lyrics have no text.
     */
    Catalog.SongDetails createSong(Catalog.NewSong song);

    /**
     * @return the installed bibles.
     */
    List<Catalog.Translation> bibleTranslations();

    /**
     * @param translation the bible's name.
     * @return its books.
     * @throws java.util.NoSuchElementException if there is no such bible.
     */
    List<Catalog.Book> bibleBooks(String translation);

    /**
     * @param translation the bible's name.
     * @param reference the passage, for example "John 3:16-18".
     * @return the verses.
     * @throws java.util.NoSuchElementException if there is no such bible.
     * @throws IllegalArgumentException if the reference is not understood.
     */
    Catalog.Passage biblePassage(String translation, String reference);

    /**
     * @return the themes, Quelea's default theme last.
     */
    List<Catalog.Theme> themes();

    /**
     * Use a theme for songs and bible passages, like the theme menu above
     * the schedule.
     *
     * @param name the theme's name.
     * @return the new state.
     * @throws java.util.NoSuchElementException if there is no such theme.
     */
    LiveState setTheme(String name);

    /**
     * @param name the theme's name.
     * @return a PNG preview of the theme.
     * @throws java.util.NoSuchElementException if there is no such theme.
     */
    byte[] themeThumbnail(String name);

    /**
     * @param itemId a presentation, PDF or image group in the schedule.
     * @param slide the zero-based slide number.
     * @return the slide as PNG.
     * @throws NoSuchItemException if there is no such item.
     * @throws java.util.NoSuchElementException if there is no such slide.
     * @throws IllegalArgumentException if the item has no slides.
     */
    byte[] slideImage(String itemId, int slide);

    // ---- Notices.
    /**
     * @return the notices being shown and the saved ones.
     */
    Notices notices();

    /**
     * Start showing a notice, as the notice dialog in Quelea does.
     *
     * @param request the text, times and colour.
     * @return the notices afterwards.
     */
    Notices addNotice(Notices.Request request);

    /**
     * Stop showing a notice. One scrolling past right now finishes its pass.
     *
     * @param noticeId the notice's id.
     * @return the notices afterwards.
     * @throws NoSuchElementException if no notice with that id is shown.
     */
    Notices removeNotice(String noticeId);
}
