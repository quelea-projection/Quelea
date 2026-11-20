package org.quelea.windows.main.schedule;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.QueleaProperties;

/** Exercises the actual schedule list without opening the application window. */
@DisplayName("Schedule list reordering")
public class ScheduleListReorderTest {

    private static QueleaProperties previousProperties;
    private ScheduleList list;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        previousProperties = QueleaProperties.get();
        QueleaProperties.init(Files.createTempDirectory("quelea-schedule-test").toString());
        CompletableFuture<Void> started = new CompletableFuture<>();
        try {
            Platform.startup(() -> started.complete(null));
            started.get(15, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            // Another test started the toolkit.
        } catch (UnsupportedOperationException unavailable) {
            Assumptions.abort("JavaFX is unavailable: " + unavailable);
        }
        Platform.setImplicitExit(false);
    }

    @AfterAll
    public static void restoreProperties() throws Exception {
        Field instance = QueleaProperties.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, previousProperties);
    }

    @BeforeEach
    public void setUp() throws Exception {
        onFx(() -> list = new ScheduleList());
    }

    @Test
    @DisplayName("Adding items preserves their order and assigns consecutive indexes")
    public void addItems() throws Exception {
        onFx(() -> {
            Item first = new Item("first");
            Item second = new Item("second");
            Item third = new Item("third");
            list.add(first);
            list.add(second);
            list.add(third);
            assertOrder(first, second, third);
            assertSelected(2, third);
            assertScheduleOrder(first, second, third);
        });
    }

    @Test
    @DisplayName("Moving up keeps the moved item selected and updates the schedule")
    public void moveUp() throws Exception {
        onFx(() -> {
            Item a = new Item("A");
            Item b = new Item("B");
            Item c = new Item("C");
            add(a, b, c);
            list.getSelectionModel().select(1);
            list.moveCurrentItem(ScheduleList.Direction.UP);
            assertOrder(b, a, c);
            assertSelected(0, b);
            assertScheduleOrder(b, a, c);
        });
    }

    @Test
    @DisplayName("Moving down keeps the moved item selected and updates the schedule")
    public void moveDown() throws Exception {
        onFx(() -> {
            Item a = new Item("A");
            Item b = new Item("B");
            Item c = new Item("C");
            add(a, b, c);
            list.getSelectionModel().select(1);
            list.moveCurrentItem(ScheduleList.Direction.DOWN);
            assertOrder(a, c, b);
            assertSelected(2, b);
            assertScheduleOrder(a, c, b);
        });
    }

    @Test
    @DisplayName("Moving past either end leaves the list and selection unchanged")
    public void moveAtBoundaries() throws Exception {
        onFx(() -> {
            Item a = new Item("A");
            Item b = new Item("B");
            add(a, b);
            list.getSelectionModel().select(0);
            list.moveCurrentItem(ScheduleList.Direction.UP);
            assertOrder(a, b);
            assertSelected(0, a);
            list.getSelectionModel().select(1);
            list.moveCurrentItem(ScheduleList.Direction.DOWN);
            assertOrder(a, b);
            assertSelected(1, b);
        });
    }

    @Test
    @DisplayName("Moving with no selection does nothing")
    public void moveWithoutSelection() throws Exception {
        onFx(() -> {
            Item a = new Item("A");
            Item b = new Item("B");
            add(a, b);
            list.getSelectionModel().clearSelection();
            list.moveCurrentItem(ScheduleList.Direction.UP);
            list.moveCurrentItem(ScheduleList.Direction.DOWN);
            assertOrder(a, b);
            Assertions.assertEquals(-1, list.getSelectionModel().getSelectedIndex());
        });
    }

    @Test
    @DisplayName("Repeated moves keep indexes and selection aligned")
    public void repeatedMoves() throws Exception {
        onFx(() -> {
            Item a = new Item("A");
            Item b = new Item("B");
            Item c = new Item("C");
            Item d = new Item("D");
            add(a, b, c, d);
            list.getSelectionModel().select(3);
            list.moveCurrentItem(ScheduleList.Direction.UP);
            assertOrder(a, b, d, c);
            assertSelected(2, d);
            list.moveCurrentItem(ScheduleList.Direction.UP);
            assertOrder(a, d, b, c);
            assertSelected(1, d);
            list.moveCurrentItem(ScheduleList.Direction.DOWN);
            assertOrder(a, b, d, c);
            assertSelected(2, d);
            assertScheduleOrder(a, b, d, c);
        });
    }

    @Test
    @DisplayName("Moving a duplicate occurrence selects the occurrence that moved")
    public void moveDuplicateOccurrence() throws Exception {
        onFx(() -> {
            Item repeated = new Item("Repeated");
            Item middle = new Item("Middle");
            add(repeated, middle, repeated);
            IndexedDisplayable firstOccurrence = list.getItems().get(0);
            IndexedDisplayable secondOccurrence = list.getItems().get(2);
            list.getSelectionModel().select(2);
            list.moveCurrentItem(ScheduleList.Direction.UP);
            assertOrder(repeated, repeated, middle);
            Assertions.assertSame(firstOccurrence, list.getItems().get(0));
            Assertions.assertSame(secondOccurrence, list.getItems().get(1));
            Assertions.assertSame(secondOccurrence, list.getSelectionModel().getSelectedItem());
        });
    }

    @Test
    @DisplayName("A reordered schedule preserves distinct songs that compare equal")
    public void schedulePreservesEqualSongsWithDifferentMetadata() throws Exception {
        // SongDisplayable.equals ignores copyright, but the schedule must retain
        // the order of the distinct occurrences and their persisted metadata.
        onFx(() -> {
            SongDisplayable first = song("First copyright");
            SongDisplayable second = song("Second copyright");
            Assertions.assertEquals(first, second, "The songs differ in metadata excluded by equals");
            list.add(first);
            list.add(second);
            assertScheduleOrder(first, second);
            list.getSelectionModel().select(1);
            list.moveCurrentItem(ScheduleList.Direction.UP);
            assertOrder(second, first);
            assertScheduleOrder(second, first);
        });
    }

    @Test
    @DisplayName("Replacement targets the chosen occurrence even when the same item appears twice")
    public void replaceDuplicateOccurrence() throws Exception {
        onFx(() -> {
            Item repeated = new Item("Repeated");
            Item middle = new Item("Middle");
            Item replacement = new Item("Replacement");
            add(repeated, middle, repeated);
            IndexedDisplayable first = list.getItems().get(0);
            IndexedDisplayable second = list.getItems().get(2);
            list.getSelectionModel().select(2);
            list.moveCurrentItem(ScheduleList.Direction.UP);
            Assertions.assertEquals(1, list.replaceItem(second, replacement));
            assertOrder(repeated, replacement, middle);
            Assertions.assertSame(first, list.getItems().get(0));
            Assertions.assertEquals(-1, list.replaceItem(second, new Item("Stale")));
            assertScheduleOrder(repeated, replacement, middle);
        });
    }

    @Test
    @DisplayName("Looking up a live item uses identity instead of equal song metadata")
    public void lookupMatchesTheActualDisplayable() throws Exception {
        onFx(() -> {
            SongDisplayable first = song("First copyright");
            SongDisplayable second = song("Second copyright");
            Assertions.assertEquals(first, second);
            add(first, second);
            Assertions.assertEquals(1, list.indexOfDisplayableIdentity(second));
            Assertions.assertEquals(-1, list.indexOfDisplayableIdentity(song("First copyright")));
            Assertions.assertEquals(-1, list.indexOfDisplayableIdentity(null));
        });
    }

    @Test
    @DisplayName("Clearing a loaded schedule removes its items and file association")
    public void clearLoadedSchedule() throws Exception {
        onFx(() -> {
            Schedule schedule = new Schedule();
            schedule.add(new Item("A"));
            schedule.add(new Item("B"));
            schedule.setFile(new File("example.qsch"));
            list.setSchedule(schedule);
            list.clearSchedule();
            Assertions.assertTrue(list.isEmpty());
            Assertions.assertNull(list.getSchedule().getFile());
            Assertions.assertEquals(0, list.getSchedule().getSize());
            Assertions.assertEquals(-1, list.getSelectionModel().getSelectedIndex());
        });
    }

    private static SongDisplayable song(String copyright) {
        return new SongDisplayable.Builder("Same song", "Author")
                .copyright(copyright).lyrics("Verse 1\nSame words").get();
    }

    private void add(Displayable... items) {
        for (Displayable item : items) {
            list.add(item);
        }
    }

    private void assertOrder(Displayable... expected) {
        Assertions.assertEquals(expected.length, list.getItems().size());
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertSame(expected[i], list.getItems().get(i).displayable(), "item at " + i);
            Assertions.assertEquals(i, list.getItems().get(i).index(), "stored index at " + i);
        }
    }

    private void assertSelected(int index, Displayable item) {
        Assertions.assertEquals(index, list.getSelectionModel().getSelectedIndex());
        Assertions.assertSame(item, list.getSelectionModel().getSelectedItem().displayable());
    }

    private void assertScheduleOrder(Displayable... expected) {
        Schedule schedule = list.getSchedule();
        Assertions.assertEquals(expected.length, schedule.getSize());
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertSame(expected[i], schedule.getDisplayable(i), "schedule item at " + i);
        }
    }

    private static void onFx(Runnable action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                result.complete(null);
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
        });
        result.get(15, TimeUnit.SECONDS);
    }

    private record Item(String name) implements Displayable {
        @Override public boolean supportClear() { return false; }
        @Override public String getXML() { return "<item/>"; }
        @Override public Node getPreviewIcon() { return new Group(); }
        @Override public String getPreviewText() { return name; }
        @Override public Collection<File> getResources() { return List.of(); }
        @Override public void dispose() { }
    }
}
