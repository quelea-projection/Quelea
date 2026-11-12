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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.LongSupplier;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javax.imageio.ImageIO;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleReferenceParser;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageGroupDisplayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.displayable.WebDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.lucene.SearchIndex.FilterType;
import org.quelea.services.notice.Notice;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableFont;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.ThemeUtils;
import org.quelea.windows.library.LibraryVideoPanel;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.ThemeThumbnails;
import org.quelea.windows.main.actionhandlers.AddSongActionHandler;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.main.schedule.ScheduleList.Direction;
import org.quelea.windows.main.schedule.ScheduleThemeNode;
import org.quelea.windows.main.toolbars.MainToolbar;
import org.quelea.windows.multimedia.MultimediaControls;

/**
 * {@link QueleaControl} backed by Quelea's real user interface. Everything
 * that touches the user interface runs on the FX thread through
 * {@link FxCall}, so callers on network threads never touch JavaFX directly
 * and never wait for it longer than the timeout. Song and bible lookups do
 * not touch the user interface and run on the caller's thread.
 */
final class FxQueleaControl implements QueleaControl {

    private final MainPanel mainPanel;
    private final MainToolbar toolbar;
    private final ItemIds ids;
    private final LiveReader reader;
    private final LongSupplier scheduleRevision;
    private final FxCall fx;
    private final NoticeReader notices;

    FxQueleaControl(MainPanel mainPanel, MainToolbar toolbar, ItemIds ids, LiveReader reader,
            LongSupplier scheduleRevision, FxCall fx, NoticeReader notices) {
        this.notices = notices;
        this.mainPanel = mainPanel;
        this.toolbar = toolbar;
        this.ids = ids;
        this.reader = reader;
        this.scheduleRevision = scheduleRevision;
        this.fx = fx;
    }

    // ---- State ----
    @Override
    public LiveState getState() {
        return fx.call(reader::state);
    }

    @Override
    public ScheduleSnapshot getSchedule() {
        return fx.call(reader::schedule);
    }

    @Override
    public LiveSections getLiveSections() {
        return fx.call(reader::sections);
    }

    @Override
    public Map<String, List<String>> getLiveTranslations() {
        return fx.call(reader::translations);
    }

    @Override
    public PublicLive.BibleSlide getLiveBibleSlide() {
        return fx.call(reader::bibleSlide);
    }

    @Override
    public Catalog.Passage bibleVerses(String translation, int book, List<PublicLive.VerseRef> verses) {
        Bible bible = requireBible(translation);
        org.quelea.data.bible.BibleBook found = null;
        for (org.quelea.data.bible.BibleBook b : bible.getBooks()) {
            if (b.getBookNumber() == book) {
                found = b;
                break;
            }
        }
        List<Catalog.Verse> result = new ArrayList<>();
        if (found != null) {
            for (PublicLive.VerseRef ref : verses) {
                // By number, not position: some Bibles leave verses out.
                for (org.quelea.data.bible.BibleChapter chapter : found.getChapters()) {
                    if (chapter.getNum() != ref.chapter()) {
                        continue;
                    }
                    for (org.quelea.data.bible.BibleVerse verse : chapter.getVerses()) {
                        if (verse.getNum() == ref.verse()) {
                            result.add(new Catalog.Verse(ref.chapter(), ref.verse(), verse.getVerseText()));
                        }
                    }
                }
            }
        }
        return new Catalog.Passage(bible.getBibleName(), found == null ? "" : found.getBookName(), result);
    }

    // ---- Live commands ----
    @Override
    public LiveState next() {
        return command(() -> mainPanel.getLivePanel().advance());
    }

    @Override
    public LiveState previous() {
        return command(() -> mainPanel.getLivePanel().previous());
    }

    @Override
    public LiveState gotoSection(int index) {
        return command(() -> {
            LivePanel lp = mainPanel.getLivePanel();
            Displayable d = lp.getDisplayable();
            int count = reader.sectionCount();
            if (d == null || index < 0 || index >= count) {
                throw new IllegalArgumentException("Section " + index + " is out of range (0 to " + (count - 1) + ")");
            }
            // The slide previews number their slides from 1.
            if (d instanceof PresentationDisplayable) {
                lp.getPresentationPanel().getPresentationPreview().select(index + 1);
            } else if (d instanceof PdfDisplayable) {
                lp.getPdfPanel().getPresentationPreview().select(index + 1);
            } else if (d instanceof ImageGroupDisplayable) {
                lp.getImageGroupPanel().getPresentationPreview().select(index + 1);
            } else {
                lp.getLyricsPanel().select(index);
            }
        });
    }

    @Override
    public LiveState nextItem() {
        return command(() -> {
            ScheduleList schedule = scheduleList();
            int size = schedule.getItems().size();
            if (size == 0) {
                return;
            }
            int current = liveIndex(schedule);
            goLiveAt(schedule, Math.min(current + 1, size - 1));
        });
    }

    @Override
    public LiveState previousItem() {
        return command(() -> {
            ScheduleList schedule = scheduleList();
            if (schedule.getItems().isEmpty()) {
                return;
            }
            int current = liveIndex(schedule);
            goLiveAt(schedule, Math.max(current - 1, 0));
        });
    }

    @Override
    public LiveState goLive(String itemId) {
        return command(() -> goLiveAt(scheduleList(), requireIndex(itemId)));
    }

    @Override
    public LiveState setScreen(Boolean logo, Boolean black, Boolean clear) {
        return command(() -> {
            LivePanel lp = mainPanel.getLivePanel();
            // The toggles do nothing when their button is disabled, for
            // example clear on an item that cannot be cleared.
            if (logo != null && logo != lp.getLogoed()) {
                lp.toggleLogo();
            }
            if (black != null && black != lp.getBlacked()) {
                lp.toggleBlack();
            }
            if (clear != null && clear != lp.getCleared()) {
                lp.toggleClear();
            }
        });
    }

    @Override
    public LiveState setRecording(boolean recording) {
        return command(() -> {
            boolean current = toolbar.recordingProperty().get();
            if (recording && !current) {
                toolbar.startRecording();
            } else if (!recording && current) {
                toolbar.stopRecording();
            }
        });
    }

    // ---- Schedule changes ----
    @Override
    public ScheduleChange addSong(long songId, Long ifRevision) {
        SongDisplayable song = SongManager.get().getIndex().getByID(songId);
        if (song == null) {
            throw new NoSuchElementException("No song with id " + songId);
        }
        return fx.call(() -> {
            checkRevision(ifRevision);
            // The same handler as the library, so the default translation
            // and the database settings apply.
            new AddSongActionHandler(QueleaProperties.get().getDefaultSongDBUpdate()).add(song);
            return addedLast();
        });
    }

    @Override
    public ScheduleChange addBible(String translation, String reference, Long ifRevision) {
        Bible bible = requireBible(translation);
        BibleReferenceParser.Reference ref = parseReference(bible, reference);
        return fx.call(() -> {
            checkRevision(ifRevision);
            scheduleList().add(new BiblePassage(bible.getName(), ref.getLocation(),
                    ref.verses().toArray(new BibleVerse[0]), ref.multi()));
            return addedLast();
        });
    }

    @Override
    public ScheduleChange removeItem(String itemId, Long ifRevision) {
        return fx.call(() -> {
            checkRevision(ifRevision);
            ScheduleList schedule = scheduleList();
            int index = requireIndex(itemId);
            // removeCurrentItem also clears live and preview when needed.
            schedule.getSelectionModel().clearSelection();
            schedule.getSelectionModel().select(index);
            schedule.removeCurrentItem();
            return new ScheduleChange(itemId, reader.schedule());
        });
    }

    @Override
    public ScheduleChange moveItem(String itemId, int to, Long ifRevision) {
        return fx.call(() -> {
            checkRevision(ifRevision);
            ScheduleList schedule = scheduleList();
            int from = requireIndex(itemId);
            int size = schedule.getItems().size();
            if (to < 0 || to >= size) {
                throw new IllegalArgumentException("Position " + to + " is out of range (0 to " + (size - 1) + ")");
            }
            // Moved one step at a time, the way the schedule's own buttons
            // do, so the selection and the item indexes stay consistent.
            schedule.getSelectionModel().clearSelection();
            schedule.getSelectionModel().select(from);
            Direction direction = to < from ? Direction.UP : Direction.DOWN;
            for (int i = 0; i < Math.abs(to - from); i++) {
                schedule.moveCurrentItem(direction);
            }
            return new ScheduleChange(reader.scheduleIds().get(to), reader.schedule());
        });
    }

    @Override
    public ScheduleChange transpose(String itemId, int semitones, Long ifRevision) {
        if (semitones < -11 || semitones > 11) {
            throw new IllegalArgumentException("semitones must be between -11 and 11");
        }
        return fx.call(() -> {
            checkRevision(ifRevision);
            ScheduleList schedule = scheduleList();
            int index = requireIndex(itemId);
            if (!(schedule.getItems().get(index).displayable() instanceof SongDisplayable song)) {
                throw new IllegalArgumentException("Only songs can be transposed");
            }
            SongDisplayable copy = new SongDisplayable(song);
            // The copy lives in this schedule only and is never saved to
            // the song database, so the song keeps its key elsewhere.
            copy.setNoDBUpdate();
            String newKey = SongTransposer.transposeKey(song.getKey(), semitones);
            copy.setKey(newKey);
            copy.setLyrics(SongTransposer.transposeLyrics(song.getLyrics(true, true, false), newKey, semitones));
            ids.transfer(song, copy);
            schedule.getItems().set(index, new IndexedDisplayable(copy, index));

            LivePanel live = mainPanel.getLivePanel();
            if (live.getDisplayable() == song) {
                live.setDisplayable(copy, Math.max(0, reader.sectionIndex()));
            }
            if (mainPanel.getPreviewPanel().getDisplayable() == song) {
                mainPanel.getPreviewPanel().setDisplayable(copy, 0);
            }
            return new ScheduleChange(itemId, reader.schedule());
        });
    }

    // ---- Songs, bibles and themes ----
    @Override
    public List<Catalog.Song> searchSongs(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("A search text is required");
        }
        // Title matches first, then author, then lyrics; each song once.
        Map<Long, Catalog.Song> found = new LinkedHashMap<>();
        for (FilterType type : new FilterType[]{FilterType.TITLE, FilterType.AUTHOR, FilterType.BODY}) {
            for (SongDisplayable song : SongManager.get().getIndex().filter(query, type)) {
                found.putIfAbsent(song.getID(), new Catalog.Song(song.getID(), song.getTitle(), song.getAuthor()));
            }
        }
        return found.values().stream().limit(limit).toList();
    }

    @Override
    public Catalog.SongDetails getSong(long songId) {
        SongDisplayable song = SongManager.get().getIndex().getByID(songId);
        if (song == null) {
            throw new NoSuchElementException("No song with id " + songId);
        }
        return details(song);
    }

    @Override
    public Catalog.SongDetails createSong(Catalog.NewSong newSong) {
        // Off the FX thread, like Quelea's song import: saving waits for the
        // search index, and the library refreshes itself through its
        // database listener. The CCLI number and copyright go in before the
        // lyrics, since the small lines under each section are made from
        // them.
        SongDisplayable song = new SongDisplayable.Builder(newSong.title(),
                newSong.author() == null ? "" : newSong.author())
                .ccli(newSong.ccli())
                .copyright(newSong.copyright())
                .key(newSong.key())
                .lyrics(newSong.lyrics())
                .get();
        if (song.getSectionsWithoutSequence().length == 0) {
            throw new IllegalArgumentException("The lyrics have no text");
        }
        if (!SongManager.get().addSong(song, true)) {
            throw new IllegalStateException("Quelea could not save the song");
        }
        return details(song);
    }

    private static Catalog.SongDetails details(SongDisplayable song) {
        List<LiveSections.Section> sections = new ArrayList<>();
        for (TextSection section : song.getSectionsWithoutSequence()) {
            List<String> lines = Arrays.asList(section.getText(false, false));
            List<String> chordLines = Arrays.asList(section.getText(true, false));
            sections.add(new LiveSections.Section(section.getTitle(), lines,
                    chordLines.equals(lines) ? null : chordLines));
        }
        return new Catalog.SongDetails(song.getID(), song.getTitle(), song.getAuthor(), sections,
                new ArrayList<>(song.getTranslations().keySet()));
    }

    @Override
    public List<Catalog.Translation> bibleTranslations() {
        String defaultBible = QueleaProperties.get().getDefaultBible();
        return Arrays.stream(BibleManager.get().getBibles())
                .map(bible -> new Catalog.Translation(bible.getBibleName(), bible.getBibleName().equals(defaultBible)))
                .toList();
    }

    @Override
    public List<Catalog.Book> bibleBooks(String translation) {
        return Arrays.stream(requireBible(translation).getBooks())
                .map(book -> new Catalog.Book(book.getBookName(), book.getChapters().length))
                .toList();
    }

    @Override
    public Catalog.Passage biblePassage(String translation, String reference) {
        Bible bible = requireBible(translation);
        BibleReferenceParser.Reference ref = parseReference(bible, reference);
        List<Catalog.Verse> verses = ref.verses().stream()
                .map(verse -> new Catalog.Verse(verse.getChapter().getNum(), verse.getNum(), verse.getVerseText()))
                .toList();
        return new Catalog.Passage(bible.getBibleName(), ref.getLocation(), verses);
    }

    @Override
    public List<Catalog.Theme> themes() {
        List<Catalog.Theme> themes = new ArrayList<>();
        for (ThemeDTO theme : ThemeUtils.getThemes()) {
            themes.add(new Catalog.Theme(theme.getThemeName(), false));
        }
        themes.add(new Catalog.Theme(defaultThemeName(), true));
        return themes;
    }

    @Override
    public LiveState setTheme(String name) {
        ThemeDTO theme = requireTheme(name);
        return command(() -> {
            ScheduleThemeNode themeNode = mainPanel.getSchedulePanel().getThemeNode();
            themeNode.selectSongTheme(theme);
            themeNode.selectBibleTheme(theme);
        });
    }

    @Override
    public byte[] themeThumbnail(String name) {
        // Rendered off the FX thread: for a video theme it waits for a still.
        return png(ThemeThumbnails.render(requireTheme(name)));
    }

    // ---- Libraries ----
    private static java.nio.file.Path libraryDir(MediaLibrary.Kind kind) {
        java.io.File dir = switch (kind) {
            case IMAGE -> QueleaProperties.get().getImageDir();
            case VIDEO -> QueleaProperties.get().getVidDir();
            case DOCUMENT -> new java.io.File(QueleaProperties.get().getQueleaUserHome(), "documents");
        };
        return dir.toPath().toAbsolutePath();
    }

    @Override
    public List<Catalog.MediaFile> libraryFiles(MediaLibrary.Kind kind) {
        return MediaLibrary.list(libraryDir(kind), kind);
    }

    /** Recent thumbnails by file and change time; small, so a few hundred are fine. */
    private final Map<String, byte[]> thumbnails = java.util.Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > 300;
        }
    });

    @Override
    public byte[] libraryThumbnail(MediaLibrary.Kind kind, String name) {
        java.nio.file.Path file = MediaLibrary.resolve(libraryDir(kind), kind, name);
        String key = file + "@" + file.toFile().lastModified();
        byte[] cached = thumbnails.get(key);
        if (cached != null) {
            return cached;
        }
        if (kind == MediaLibrary.Kind.DOCUMENT) {
            // Reading a whole presentation for a thumbnail is too slow.
            throw new NoSuchElementException("Documents have no thumbnails");
        }
        Image image;
        if (kind == MediaLibrary.Kind.IMAGE) {
            // Loaded and scaled off the FX thread, which Image allows.
            image = new Image(file.toUri().toString(), 320, 180, true, true, false);
            if (image.isError()) {
                throw new NoSuchElementException("Quelea cannot read " + name);
            }
        } else {
            // The thumbnail Quelea's video list already made: asking
            // GStreamer again could hang Quelea (see VidDisplay).
            // Without the video tab (an option in Quelea) there are none.
            LibraryVideoPanel videos = mainPanel.getLibraryPanel().getVideoPanel();
            image = videos == null ? null : videos.getVideoListPanel().getPreview(file.toFile());
            if (image == null) {
                throw new NoSuchElementException("No thumbnail of " + name + " yet");
            }
        }
        byte[] png = png(image);
        thumbnails.put(key, png);
        return png;
    }

    @Override
    public ScheduleChange addLibraryFile(MediaLibrary.Kind kind, String name, Long ifRevision) {
        java.io.File file = MediaLibrary.resolve(libraryDir(kind), kind, name).toFile();
        Displayable document = null;
        if (kind == MediaLibrary.Kind.DOCUMENT) {
            // Opened here, off the FX thread, as Quelea's add handlers do:
            // a large presentation takes a while to read.
            try {
                document = file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                        ? new PdfDisplayable(file) : new PresentationDisplayable(file);
            } catch (IOException | RuntimeException ex) {
                throw new IllegalArgumentException("Quelea could not open " + name, ex);
            }
        }
        Displayable opened = document;
        return fx.call(() -> {
            checkRevision(ifRevision);
            scheduleList().add(switch (kind) {
                case IMAGE -> new ImageDisplayable(file);
                case VIDEO -> new VideoDisplayable(file.getAbsolutePath());
                case DOCUMENT -> opened;
            });
            return addedLast();
        });
    }

    @Override
    public ScheduleChange addWebPage(String url, Long ifRevision) {
        String checked = WebAddress.check(url);
        return fx.call(() -> {
            checkRevision(ifRevision);
            // A WebView, so on the FX thread, as Quelea's own handler does.
            scheduleList().add(new WebDisplayable(checked));
            return addedLast();
        });
    }

    @Override
    public Catalog.MediaFile uploadLibraryFile(MediaLibrary.Kind kind, String name, long size,
            java.io.InputStream data) {
        Catalog.MediaFile saved;
        try {
            saved = MediaLibrary.save(libraryDir(kind), kind, name, size, data);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        // Show it in Quelea's library tab, as a file dropped there would be.
        fx.run(() -> {
            if (kind == MediaLibrary.Kind.IMAGE) {
                mainPanel.getLibraryPanel().getImagePanel().getImagePanel().refresh();
            } else if (kind == MediaLibrary.Kind.VIDEO && mainPanel.getLibraryPanel().getVideoPanel() != null) {
                mainPanel.getLibraryPanel().getVideoPanel().getVideoListPanel().refresh();
            }
            // Documents have no library tab in Quelea.
        });
        return saved;
    }

    // ---- Media ----
    @Override
    public LiveState playMedia() {
        return command(() -> {
            LivePanel lp = mainPanel.getLivePanel();
            if (lp.getDisplayable() instanceof TimerDisplayable) {
                if (!lp.getTimerPanel().runningProperty().get()) {
                    lp.getTimerPanel().play();
                }
                return;
            }
            MultimediaControls controls = liveControls();
            if (!controls.playingProperty().get()) {
                controls.play();
            }
        });
    }

    @Override
    public LiveState pauseMedia() {
        return command(() -> {
            LivePanel lp = mainPanel.getLivePanel();
            if (lp.getDisplayable() instanceof TimerDisplayable) {
                if (lp.getTimerPanel().runningProperty().get()) {
                    lp.getTimerPanel().togglePause();
                }
                return;
            }
            MultimediaControls controls = liveControls();
            if (controls.playingProperty().get()) {
                // The play button toggles between playing and paused.
                controls.play();
            }
        });
    }

    @Override
    public LiveState stopMedia() {
        return command(() -> {
            LivePanel lp = mainPanel.getLivePanel();
            if (lp.getDisplayable() instanceof TimerDisplayable) {
                lp.getTimerPanel().stop();
                return;
            }
            liveControls().reset();
        });
    }

    @Override
    public LiveState setMedia(Double position, Boolean loop) {
        if (position != null && (position.isNaN() || position < 0 || position > 1)) {
            throw new IllegalArgumentException("\"position\" must be from 0 to 1");
        }
        return command(() -> {
            if (mainPanel.getLivePanel().getDisplayable() instanceof TimerDisplayable) {
                throw new IllegalArgumentException("A timer cannot jump or loop");
            }
            MultimediaControls controls = liveControls();
            if (loop != null && loop != controls.loopProperty().get()) {
                controls.setLoop(loop);
            }
            if (position != null) {
                controls.seek(position);
            }
        });
    }

    /**
     * @return the controls of the live video or audio file.
     */
    private MultimediaControls liveControls() {
        LivePanel lp = mainPanel.getLivePanel();
        Displayable d = lp.getDisplayable();
        if (d instanceof VideoDisplayable) {
            return lp.getVideoPanel().getControls();
        }
        if (d instanceof AudioDisplayable) {
            return lp.getAudioPanel().getControls();
        }
        throw new IllegalArgumentException("The live item is not a video, audio file or timer");
    }

    // ---- Notices ----
    @Override
    public Notices notices() {
        return fx.call(notices::notices);
    }

    @Override
    public Notices addNotice(Notices.Request request) {
        Color color = request.color() == null ? Color.WHITE : Color.web(request.color());
        return fx.call(() -> {
            // The same font as a new notice in Quelea's notice dialog.
            Font font = Font.font("Arial", FontWeight.NORMAL, FontPosture.REGULAR,
                    QueleaProperties.get().getNoticeFontSize());
            notices.dialog().addNotice(new Notice(request.text(),
                    request.times() == 0 ? Integer.MAX_VALUE : request.times(),
                    new SerializableColor(color), new SerializableFont(font)));
            return notices.notices();
        });
    }

    @Override
    public Notices removeNotice(String noticeId) {
        return fx.call(() -> {
            Notice notice = notices.find(noticeId);
            if (notice == null) {
                throw new NoSuchElementException("No notice " + noticeId + " is being shown");
            }
            notices.dialog().removeNotice(notice);
            return notices.notices();
        });
    }

    @Override
    public byte[] slideImage(String itemId, int slide) {
        Image image = fx.call(() -> {
            Displayable d = scheduleList().getItems().get(requireIndex(itemId)).displayable();
            int count;
            if (d instanceof PresentationDisplayable presentation) {
                count = presentation.getPresentation().getSlides().length;
                checkSlide(slide, count);
                return presentation.getPresentation().getSlide(slide).getImage();
            } else if (d instanceof PdfDisplayable pdf) {
                count = pdf.getPresentation().getSlides().length;
                checkSlide(slide, count);
                return pdf.getPresentation().getSlide(slide).getImage();
            } else if (d instanceof ImageGroupDisplayable images) {
                count = images.getPresentation().getSlides().length;
                checkSlide(slide, count);
                return images.getPresentation().getSlide(slide).getImage();
            }
            throw new IllegalArgumentException("Item " + itemId + " has no slides");
        });
        // Encoded on the caller's thread, so the FX thread is not held up.
        return png(image);
    }

    // ---- Helpers ----
    private LiveState command(Runnable action) {
        return fx.call(() -> {
            action.run();
            return reader.state();
        });
    }

    private ScheduleList scheduleList() {
        return mainPanel.getSchedulePanel().getScheduleList();
    }

    private void goLiveAt(ScheduleList schedule, int index) {
        schedule.getSelectionModel().clearSelection();
        schedule.getSelectionModel().select(index);
        mainPanel.getPreviewPanel().goLive();
    }

    private int liveIndex(ScheduleList schedule) {
        Displayable live = mainPanel.getLivePanel().getDisplayable();
        for (int i = 0; i < schedule.getItems().size(); i++) {
            if (schedule.getItems().get(i).displayable() == live) {
                return i;
            }
        }
        return -1;
    }

    private int requireIndex(String itemId) {
        int index = reader.indexOfId(itemId);
        if (index < 0) {
            throw new NoSuchItemException(itemId);
        }
        return index;
    }

    private void checkRevision(Long ifRevision) {
        long current = scheduleRevision.getAsLong();
        if (ifRevision != null && ifRevision != current) {
            throw new ScheduleConflictException(ifRevision, current);
        }
    }

    private ScheduleChange addedLast() {
        List<String> itemIds = reader.scheduleIds();
        return new ScheduleChange(itemIds.isEmpty() ? null : itemIds.getLast(), reader.schedule());
    }

    private static void checkSlide(int slide, int count) {
        if (slide < 0 || slide >= count) {
            throw new NoSuchElementException("Slide " + slide + " does not exist (0 to " + (count - 1) + ")");
        }
    }

    private static Bible requireBible(String translation) {
        if (translation != null) {
            for (Bible bible : BibleManager.get().getBibles()) {
                if (bible.getBibleName().equalsIgnoreCase(translation) || bible.getName().equalsIgnoreCase(translation)) {
                    return bible;
                }
            }
        }
        throw new NoSuchElementException("No bible named " + translation);
    }

    private static BibleReferenceParser.Reference parseReference(Bible bible, String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("A bible reference is required");
        }
        List<BibleReferenceParser.Reference> refs = BibleReferenceParser.parse(reference, bible);
        if (refs.isEmpty() || refs.getFirst().verses().isEmpty()) {
            throw new IllegalArgumentException("Could not understand the reference \"" + reference + "\"");
        }
        return refs.getFirst();
    }

    private static String defaultThemeName() {
        return LabelGrabber.INSTANCE.getLabel("default.theme.text");
    }

    private static ThemeDTO requireTheme(String name) {
        if (name != null) {
            for (ThemeDTO theme : ThemeUtils.getThemes()) {
                if (name.equals(theme.getThemeName())) {
                    return theme;
                }
            }
            if (name.equals(defaultThemeName())) {
                return ThemeDTO.DEFAULT_THEME;
            }
        }
        throw new NoSuchElementException("No theme named " + name);
    }

    private static byte[] png(Image image) {
        BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(buffered, "png", out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return out.toByteArray();
    }
}
