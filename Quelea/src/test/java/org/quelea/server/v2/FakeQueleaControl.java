package org.quelea.server.v2;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A QueleaControl without JavaFX for server tests. Records the commands it
 * gets and keeps a small live state.
 */
final class FakeQueleaControl implements QueleaControl {

    final List<String> calls = new CopyOnWriteArrayList<>();
    volatile boolean timeOut;
    private volatile int section;
    private volatile boolean logo;
    private volatile boolean black;
    private volatile boolean clear;
    private volatile boolean recording;

    @Override
    public LiveState getState() {
        checkTimeout();
        return new LiveState("it_1", "Amazing Grace", "song", section, 3, logo, black, clear, recording, 4, media);
    }

    /**
     * The live item's playback; null means it is not a video, audio file or
     * timer.
     */
    volatile LiveState.Media media;

    private LiveState.Media requireMedia() {
        if (media == null) {
            throw new IllegalArgumentException("The live item is not a video, audio file or timer");
        }
        return media;
    }

    @Override
    public LiveState playMedia() {
        LiveState.Media m = requireMedia();
        media = new LiveState.Media(m.kind(), true, m.loop(), m.position());
        return getState();
    }

    @Override
    public LiveState pauseMedia() {
        LiveState.Media m = requireMedia();
        media = new LiveState.Media(m.kind(), false, m.loop(), m.position());
        return getState();
    }

    @Override
    public LiveState stopMedia() {
        LiveState.Media m = requireMedia();
        media = new LiveState.Media(m.kind(), false, m.loop(), m.position() == null ? null : 0.0);
        return getState();
    }

    @Override
    public LiveState setMedia(Double position, Boolean loop) {
        if (position != null && (position < 0 || position > 1)) {
            throw new IllegalArgumentException("\"position\" must be from 0 to 1");
        }
        LiveState.Media m = requireMedia();
        if ("timer".equals(m.kind())) {
            throw new IllegalArgumentException("A timer cannot jump or loop");
        }
        media = new LiveState.Media(m.kind(), m.playing(), loop == null ? m.loop() : loop,
                position == null ? m.position() : position);
        return getState();
    }

    @Override
    public ScheduleSnapshot getSchedule() {
        checkTimeout();
        return new ScheduleSnapshot(4, List.of(
                new ScheduleSnapshot.Entry("it_1", "song", "Amazing Grace", true, false),
                new ScheduleSnapshot.Entry("it_2", "bible", "John 3:16", false, true)));
    }

    @Override
    public LiveSections getLiveSections() {
        checkTimeout();
        return new LiveSections("it_1", section, List.of(
                new LiveSections.Section("Verse 1", List.of("Amazing grace", "how sweet the sound"), null),
                new LiveSections.Section("Verse 2", List.of("'Twas grace"), List.of("G  C", "'Twas grace")),
                new LiveSections.Section("Verse 3", List.of("Through many dangers"), null)));
    }

    @Override
    public LiveState next() {
        calls.add("next");
        return getState();
    }

    @Override
    public LiveState previous() {
        calls.add("previous");
        return getState();
    }

    @Override
    public LiveState gotoSection(int index) {
        calls.add("gotoSection " + index);
        if (index < 0 || index >= 3) {
            throw new IllegalArgumentException("Section " + index + " is out of range (0 to 2)");
        }
        section = index;
        return getState();
    }

    @Override
    public LiveState nextItem() {
        calls.add("nextItem");
        return getState();
    }

    @Override
    public LiveState previousItem() {
        calls.add("previousItem");
        return getState();
    }

    @Override
    public LiveState goLive(String itemId) {
        calls.add("goLive " + itemId);
        if (!itemId.equals("it_1") && !itemId.equals("it_2")) {
            throw new NoSuchItemException(itemId);
        }
        return getState();
    }

    @Override
    public LiveState setScreen(Boolean logo, Boolean black, Boolean clear) {
        calls.add("setScreen " + logo + " " + black + " " + clear);
        if (logo != null) {
            this.logo = logo;
        }
        if (black != null) {
            this.black = black;
        }
        if (clear != null) {
            this.clear = clear;
        }
        return getState();
    }

    @Override
    public LiveState setRecording(boolean recording) {
        calls.add("setRecording " + recording);
        this.recording = recording;
        return getState();
    }

    // ---- Schedule changes: the fake schedule is a list of ids ----
    final List<String> schedule = new CopyOnWriteArrayList<>(List.of("it_1", "it_2"));
    volatile long revision = 4;
    private int nextId = 3;

    private ScheduleSnapshot snapshot() {
        return new ScheduleSnapshot(revision, schedule.stream()
                .map(id -> new ScheduleSnapshot.Entry(id, "song", "Item " + id, false, false)).toList());
    }

    private void checkRevision(Long ifRevision) {
        if (ifRevision != null && ifRevision != revision) {
            throw new ScheduleConflictException(ifRevision, revision);
        }
    }

    private ScheduleChange added() {
        String id = "it_" + nextId++;
        schedule.add(id);
        revision++;
        return new ScheduleChange(id, snapshot());
    }

    @Override
    public synchronized ScheduleChange addSong(long songId, Long ifRevision) {
        calls.add("addSong " + songId);
        if (songId != 42) {
            throw new java.util.NoSuchElementException("No song with id " + songId);
        }
        checkRevision(ifRevision);
        return added();
    }

    @Override
    public synchronized ScheduleChange addBible(String translation, String reference, Long ifRevision) {
        calls.add("addBible " + translation + " / " + reference);
        if (!"KJV".equals(translation)) {
            throw new java.util.NoSuchElementException("No bible named " + translation);
        }
        if (!"John 3:16".equals(reference)) {
            throw new IllegalArgumentException("Could not understand the reference \"" + reference + "\"");
        }
        checkRevision(ifRevision);
        return added();
    }

    @Override
    public synchronized ScheduleChange removeItem(String itemId, Long ifRevision) {
        calls.add("removeItem " + itemId);
        checkRevision(ifRevision);
        if (!schedule.remove(itemId)) {
            throw new NoSuchItemException(itemId);
        }
        revision++;
        return new ScheduleChange(itemId, snapshot());
    }

    @Override
    public synchronized ScheduleChange moveItem(String itemId, int to, Long ifRevision) {
        calls.add("moveItem " + itemId + " " + to);
        checkRevision(ifRevision);
        if (!schedule.contains(itemId)) {
            throw new NoSuchItemException(itemId);
        }
        if (to < 0 || to >= schedule.size()) {
            throw new IllegalArgumentException("Position " + to + " is out of range");
        }
        schedule.remove(itemId);
        schedule.add(to, itemId);
        revision++;
        return new ScheduleChange(itemId, snapshot());
    }

    @Override
    public synchronized ScheduleChange transpose(String itemId, int semitones, Long ifRevision) {
        calls.add("transpose " + itemId + " " + semitones);
        checkRevision(ifRevision);
        if (!schedule.contains(itemId)) {
            throw new NoSuchItemException(itemId);
        }
        if (itemId.equals("it_2")) {
            throw new IllegalArgumentException("Only songs can be transposed");
        }
        revision++;
        return new ScheduleChange(itemId, snapshot());
    }

    // ---- Songs, bibles and themes ----
    @Override
    public List<Catalog.Song> searchSongs(String query, int limit) {
        calls.add("searchSongs " + query + " " + limit);
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("A search text is required");
        }
        return List.of(new Catalog.Song(42, "Amazing Grace", "John Newton"));
    }

    @Override
    public Catalog.SongDetails getSong(long songId) {
        if (songId != 42) {
            throw new java.util.NoSuchElementException("No song with id " + songId);
        }
        return new Catalog.SongDetails(42, "Amazing Grace", "John Newton",
                List.of(new LiveSections.Section("Verse 1", List.of("Amazing grace"), null)), List.of("Svenska"));
    }

    final List<Catalog.NewSong> createdSongs = new CopyOnWriteArrayList<>();

    @Override
    public Catalog.SongDetails createSong(Catalog.NewSong song) {
        createdSongs.add(song);
        List<LiveSections.Section> sections = new java.util.ArrayList<>();
        for (String section : song.lyrics().split("\n\\s*\n")) {
            sections.add(new LiveSections.Section(null, List.of(section.split("\n")), null));
        }
        return new Catalog.SongDetails(100 + createdSongs.size(), song.title(),
                song.author() == null ? "" : song.author(), sections, List.of());
    }

    @Override
    public List<Catalog.Translation> bibleTranslations() {
        return List.of(new Catalog.Translation("KJV", true), new Catalog.Translation("Svenska 1917", false));
    }

    @Override
    public List<Catalog.Book> bibleBooks(String translation) {
        if (!"Svenska 1917".equals(translation)) {
            throw new java.util.NoSuchElementException("No bible named " + translation);
        }
        return List.of(new Catalog.Book("1 Mosebok", 50));
    }

    @Override
    public Catalog.Passage biblePassage(String translation, String reference) {
        calls.add("biblePassage " + translation + " / " + reference);
        return new Catalog.Passage(translation, reference, List.of(new Catalog.Verse(3, 16, "For God so loved")));
    }

    @Override
    public List<Catalog.Theme> themes() {
        return List.of(new Catalog.Theme("Blue", false), new Catalog.Theme("Default theme", true));
    }

    @Override
    public LiveState setTheme(String name) {
        calls.add("setTheme " + name);
        if (!"Blue".equals(name)) {
            throw new java.util.NoSuchElementException("No theme named " + name);
        }
        return getState();
    }

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};

    @Override
    public byte[] themeThumbnail(String name) {
        calls.add("themeThumbnail " + name);
        return PNG;
    }

    @Override
    public byte[] slideImage(String itemId, int slide) {
        calls.add("slideImage " + itemId + " " + slide);
        if (slide > 5) {
            throw new java.util.NoSuchElementException("Slide " + slide + " does not exist");
        }
        return PNG;
    }

    final List<Notices.Active> notices = new CopyOnWriteArrayList<>();
    private int nextNotice = 1;

    @Override
    public Notices notices() {
        return new Notices(notices, List.of(new Notices.Saved("Coffee after the service", 2, "#ffffff")));
    }

    @Override
    public synchronized Notices addNotice(Notices.Request request) {
        calls.add("addNotice " + request.text() + " " + request.times() + " " + request.color());
        notices.add(new Notices.Active("nt_" + nextNotice++, request.text(),
                request.times() == 0 ? null : request.times(), request.color() == null ? "#ffffff" : request.color()));
        return notices();
    }

    @Override
    public Notices removeNotice(String noticeId) {
        calls.add("removeNotice " + noticeId);
        if (!notices.removeIf(n -> n.id().equals(noticeId))) {
            throw new java.util.NoSuchElementException("No notice " + noticeId + " is being shown");
        }
        return notices();
    }

    private void checkTimeout() {
        if (timeOut) {
            throw new FxCall.TimeoutException(Duration.ofSeconds(2));
        }
    }
}
