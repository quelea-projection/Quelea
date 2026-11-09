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
        return new LiveState("it_1", "Amazing Grace", "song", section, 3, logo, black, clear, recording, 4);
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

    private void checkTimeout() {
        if (timeOut) {
            throw new FxCall.TimeoutException(Duration.ofSeconds(2));
        }
    }
}
