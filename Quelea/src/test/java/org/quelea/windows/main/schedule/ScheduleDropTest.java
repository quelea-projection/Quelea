package org.quelea.windows.main.schedule;

import com.sun.javafx.scene.input.DragboardHelper;
import com.sun.javafx.tk.TKClipboard;
import com.sun.javafx.tk.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.data.ImageBackground;
import org.quelea.data.ThemeDTO;
import org.quelea.data.VideoBackground;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.SongDisplayableList;
import org.quelea.utils.SongDragboard;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.main.PreviewPanel;
import org.quelea.windows.main.QueleaApp;

/** Exercises the real drop handler with an in-memory dragboard and an isolated app shell. */
@DisplayName("Schedule drop handling")
public class ScheduleDropTest {

    private static MainWindow previousWindow;
    private static boolean appShellInstalled;
    private static QueleaProperties previousProperties;
    private static Path testHome;
    private static TestPreviewPanel preview;
    private ScheduleList list;

    @BeforeAll
    public static void setUpApplication() throws Exception {
        previousProperties = QueleaProperties.get();
        testHome = Files.createTempDirectory("quelea-drop-test");
        QueleaProperties.init(testHome.toString());
        Files.createDirectories(QueleaProperties.get().getImageDir().toPath());
        Files.createDirectories(QueleaProperties.get().getVidDir().toPath());
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
        onFx(() -> {
            previousWindow = QueleaApp.get().getMainWindow();
            TestMainWindow window = withoutConstructor(TestMainWindow.class);
            TestMainPanel panel = withoutConstructor(TestMainPanel.class);
            preview = withoutConstructor(TestPreviewPanel.class);
            panel.preview = preview;
            panel.live = withoutConstructor(TestLivePanel.class);
            window.panel = panel;
            QueleaApp.get().setMainWindow(window);
            appShellInstalled = true;
        });
    }

    @AfterAll
    public static void restoreApplication() throws Exception {
        try {
            if (appShellInstalled) {
                onFx(() -> QueleaApp.get().setMainWindow(previousWindow));
            }
        } finally {
            Field instance = QueleaProperties.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(null, previousProperties);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        onFx(() -> {
            preview.displayable = null;
            preview.refreshCount = 0;
            list = new ScheduleList();
            new Scene(list, 320, 400);
            list.resize(320, 400);
            list.applyCss();
            list.layout();
        });
    }

    @Test
    @DisplayName("Song drags use a local token and release their snapshot when finished")
    public void songDragUsesTokenAndReleasesPayload() throws Exception {
        onFx(() -> {
            SongDisplayable source = song("Library song");
            source.setID(123);
            SongDisplayableList snapshot = SongDragboard.snapshot(new SongDisplayableList(new IndexedDisplayable(source, -1)));
            SongDisplayable copy = (SongDisplayable) snapshot.getSongDisplayables().getFirst().displayable();
            Assertions.assertNotSame(source, copy);
            Assertions.assertEquals(source.getID(), copy.getID());
            copy.setID(-1);
            Assertions.assertEquals(123, source.getID(), "Changing the dropped song must not change the library copy");
            ClipboardContent content = new ClipboardContent();
            SongDragboard.put(content, snapshot);
            Dragboard board = DragboardHelper.createDragboard(new TestClipboard());
            Assertions.assertTrue(board.setContent(content));
            Assertions.assertInstanceOf(String.class, board.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT));
            Assertions.assertSame(snapshot, SongDragboard.get(board));
            SongDragboard.clear(board);
            Assertions.assertNull(SongDragboard.get(board));
        });
    }

    @Test
    @DisplayName("A stale or external drag payload cannot be used as a song")
    public void staleAndForeignPayloadsAreRejected() throws Exception {
        onFx(() -> {
            ClipboardContent oldContent = songContent(song("Old"));
            Dragboard oldBoard = DragboardHelper.createDragboard(new TestClipboard());
            Assertions.assertTrue(oldBoard.setContent(oldContent));
            ClipboardContent currentContent = songContent(song("Current"));
            Dragboard currentBoard = DragboardHelper.createDragboard(new TestClipboard());
            Assertions.assertTrue(currentBoard.setContent(currentContent));
            Assertions.assertNull(SongDragboard.get(oldBoard));
            // The dragboard can be invalid by DRAG_DONE; the source keeps its
            // token independently and must not clear a newer gesture.
            SongDragboard.clear(oldContent);
            Assertions.assertNotNull(SongDragboard.get(currentBoard), "An old drag must not clear the current drag");
            ClipboardContent foreign = new ClipboardContent();
            foreign.put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, ByteBuffer.wrap(new byte[]{(byte) 0xac, (byte) 0xed, 0, 5}));
            Assertions.assertFalse(dragOver(foreign).isAccepted());
            Assertions.assertFalse(drop(foreign, 10, 10).isDropCompleted());
            Assertions.assertTrue(list.isEmpty());
            SongDragboard.clear(currentContent);
            Assertions.assertNull(SongDragboard.get(currentBoard));
        });
    }

    @Test
    @DisplayName("A library song can be dropped into an empty schedule")
    public void songIntoEmptySchedule() throws Exception {
        AtomicReference<SongDisplayable> added = new AtomicReference<>();
        onFx(() -> {
            SongDisplayable song = song("Song");
            added.set(song);
            ClipboardContent content = new ClipboardContent();
            SongDragboard.put(content,
                    new SongDisplayableList(new IndexedDisplayable(song, -1)));
            drop(content, 10, 10);
            Assertions.assertEquals(1, list.getItems().size());
            Assertions.assertSame(song, list.getItems().getFirst().displayable());
            Assertions.assertEquals(0, list.getItems().getFirst().index());
            Assertions.assertSame(song, list.getSelectionModel().getSelectedItem().displayable());
        });
        onFx(() -> {
            Assertions.assertSame(added.get(), preview.displayable);
            Assertions.assertTrue(preview.refreshCount > 0);
        }); // Run and check the preview update queued by the drop handler.
    }

    @Test
    @DisplayName("A library song can be dropped between existing items")
    public void songBetweenExistingItems() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable inserted = song("Inserted");
            list.add(a);
            list.add(b);
            Point2D point = pointOnCell(1, 0.25);
            drop(songContent(inserted), point.getX(), point.getY());
            assertOrder(a, inserted, b);
            Assertions.assertSame(inserted, list.getSelectionModel().getSelectedItem().displayable());
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("A library song can be inserted before the first item")
    public void songBeforeFirstItem() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable inserted = song("Inserted");
            add(a, b);
            dropAt(songContent(inserted), 0, 0.25);
            assertOrder(inserted, a, b);
            assertSelected(0, inserted);
            assertScheduleOrder(inserted, a, b);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("A library song can be inserted after the last item")
    public void songAfterLastItem() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable inserted = song("Inserted");
            add(a, b);
            dropAt(songContent(inserted), 1, 0.75);
            assertOrder(a, b, inserted);
            assertSelected(2, inserted);
            assertScheduleOrder(a, b, inserted);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Dragging a schedule song upward moves it before the target")
    public void moveSongUp() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable c = song("C");
            add(a, b, c);
            dropAt(scheduleSongContent(2), 0, 0.25);
            assertOrder(c, a, b);
            assertSelected(0, c);
            assertScheduleOrder(c, a, b);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Dragging a schedule song downward moves it after the target")
    public void moveSongDown() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable c = song("C");
            add(a, b, c);
            dropAt(scheduleSongContent(0), 2, 0.75);
            assertOrder(b, c, a);
            assertSelected(2, a);
            assertScheduleOrder(b, c, a);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Moving several schedule items preserves their order and the remaining items")
    public void moveSeveralScheduleItems() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable c = song("C");
            SongDisplayable d = song("D");
            SongDisplayable e = song("E");
            add(a, b, c, d, e);
            ClipboardContent content = new ClipboardContent();
            SongDragboard.put(content,
                    new SongDisplayableList(List.of(list.getItems().get(1), list.getItems().get(3))));
            dropAt(content, 4, 0.75);
            assertOrder(a, c, e, b, d);
            assertSelected(4, d);
            assertScheduleOrder(a, c, e, b, d);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Dropping a song back on itself neither removes nor duplicates it")
    public void dropSongOnItself() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable c = song("C");
            add(a, b, c);
            dropAt(scheduleSongContent(1), 1, 0.25);
            assertOrder(a, b, c);
            assertSelected(1, b);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Dropping a song just after itself keeps it in place")
    public void dropSongJustAfterItself() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable c = song("C");
            add(a, b, c);
            dropAt(scheduleSongContent(1), 1, 0.75);
            assertOrder(a, b, c);
            assertSelected(1, b);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Adding a library song already in the schedule keeps both occurrences")
    public void addSongAlreadyInSchedule() throws Exception {
        onFx(() -> {
            SongDisplayable original = song("Repeated");
            SongDisplayable incoming = song("Repeated");
            Assertions.assertEquals(original, incoming);
            list.add(original);
            dropAt(songContent(incoming), 0, 0.75);
            assertOrder(original, incoming);
            assertSelected(1, incoming);
            assertScheduleOrder(original, incoming);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Dragging one of two equal songs moves the selected occurrence")
    public void moveOneDuplicateSong() throws Exception {
        onFx(() -> {
            SongDisplayable first = song("Repeated");
            SongDisplayable middle = song("Middle");
            SongDisplayable last = song("Repeated");
            Assertions.assertEquals(first, last);
            add(first, middle, last);
            dropAt(scheduleSongContent(2), 0, 0.25);
            assertOrder(last, first, middle);
            assertSelected(0, last);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("A serialized library payload still adds the correct song")
    public void serializedSongPayload() throws Exception {
        onFx(() -> {
            SongDisplayable source = new SongDisplayable.Builder("Café", "Author")
                    .copyright("Copyright holder").lyrics("Verse 1\nWords").get();
            SongDisplayableList payload = roundTrip(new SongDisplayableList(new IndexedDisplayable(source, -1)));
            SongDisplayable transferred = (SongDisplayable) payload.getSongDisplayables().getFirst().displayable();
            Assertions.assertNotSame(source, transferred);
            ClipboardContent content = new ClipboardContent();
            SongDragboard.put(content, payload);
            drop(content, 10, 10);
            assertOrder(transferred);
            Assertions.assertEquals("Café", transferred.getTitle());
            Assertions.assertEquals("Copyright holder", transferred.getCopyright());
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("A serialized schedule song still moves its original occurrence")
    public void serializedScheduleSongMovesOriginal() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable c = song("C");
            add(a, b, c);
            SongDisplayableList payload = roundTrip(new SongDisplayableList(list.getItems().get(2)));
            SongDisplayable transferred = (SongDisplayable) payload.getSongDisplayables().getFirst().displayable();
            ClipboardContent content = new ClipboardContent();
            SongDragboard.put(content, payload);
            dropAt(content, 0, 0.25);
            assertOrder(transferred, a, b);
            assertSelected(0, transferred);
            assertScheduleOrder(transferred, a, b);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Moving a copied drag snapshot moves the original occurrence")
    public void moveSongWithFreshDragboardCopies() throws Exception {
        onFx(() -> {
            SongDisplayable a = song("A");
            SongDisplayable b = song("B");
            SongDisplayable c = song("C");
            add(a, b, c);
            Point2D target = pointOnCell(0, 0.25);
            ClipboardContent content = new ClipboardContent();
            SongDragboard.put(content, SongDragboard.snapshot(new SongDisplayableList(list.getItems().get(2))));
            drop(content, target.getX(), target.getY());
            Assertions.assertEquals(3, list.getItems().size());
            SongDisplayable moved = Assertions.assertInstanceOf(SongDisplayable.class,
                    list.getItems().get(0).displayable());
            Assertions.assertNotSame(c, moved);
            Assertions.assertEquals("C", moved.getTitle());
            assertOrder(moved, a, b);
            assertSelected(0, moved);
            assertScheduleOrder(moved, a, b);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Multiple library songs keep their order in an empty schedule")
    public void multipleLibrarySongsIntoEmptySchedule() throws Exception {
        assertMultipleSongsAt(0, true);
    }

    @Test
    @DisplayName("Multiple library songs retain their order at every insertion position")
    public void multipleLibrarySongsKeepOrder() {
        // A multi-song selection must keep its order at every insertion position.
        Assertions.assertAll(
                () -> assertMultipleSongsAt(0, false),
                () -> assertMultipleSongsAt(1, false),
                () -> assertMultipleSongsAt(2, false));
    }

    @Test
    @DisplayName("A moved non-song item stays in the schedule at the new position")
    public void moveNonSongItem(@TempDir Path directory) throws Exception {
        Path video = Files.createFile(directory.resolve("clip.mp4"));
        onFx(() -> {
            SongDisplayable a = song("A");
            VideoDisplayable clip = new VideoDisplayable(video.toString());
            SongDisplayable b = song("B");
            add(a, clip, b);
            Field field = ScheduleList.class.getDeclaredField("tempDisp");
            field.setAccessible(true);
            field.set(list, list.getItems().get(1));
            ClipboardContent content = new ClipboardContent();
            content.putString("tempdisp");
            dropAt(content, 2, 0.75);
            assertOrder(a, b, clip);
            assertSelected(2, clip);
            assertScheduleOrder(a, b, clip);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("Moving a non-song item does not depend on the image-library directory")
    public void moveNonSongWithoutImageDirectory(@TempDir Path directory) throws Exception {
        // An internal move does not need an image-library directory.
        Path video = Files.createFile(directory.resolve("clip.mp4"));
        Path homeWithoutImages = Files.createDirectory(directory.resolve("empty-home"));
        onFx(() -> {
            try {
                QueleaProperties.init(homeWithoutImages.toString());
                SongDisplayable a = song("A");
                VideoDisplayable clip = new VideoDisplayable(video.toString());
                SongDisplayable b = song("B");
                add(a, clip, b);
                Field field = ScheduleList.class.getDeclaredField("tempDisp");
                field.setAccessible(true);
                field.set(list, list.getItems().get(1));
                ClipboardContent content = new ClipboardContent();
                content.putString("tempdisp");
                dropAt(content, 2, 0.75);
                assertOrder(a, b, clip);
            } finally {
                QueleaProperties.init(testHome.toString());
            }
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("External image and video files are added in their original order")
    public void externalMediaFiles(@TempDir Path directory) throws Exception {
        Path image = directory.resolve("picture.PNG");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile());
        Path video = Files.createFile(directory.resolve("clip.mp4"));
        Path unsupported = Files.createFile(directory.resolve("notes.txt"));
        onFx(() -> {
            SongDisplayable existing = song("Existing");
            list.add(existing);
            ClipboardContent content = new ClipboardContent();
            content.putFiles(List.of(image.toFile(), unsupported.toFile(), video.toFile()));
            DragEvent event = drop(content, 10, 10);
            Assertions.assertTrue(event.isDropCompleted());
            Assertions.assertEquals(3, list.getItems().size());
            Assertions.assertSame(existing, list.getItems().get(0).displayable());
            ImageDisplayable picture = Assertions.assertInstanceOf(ImageDisplayable.class,
                    list.getItems().get(1).displayable());
            VideoDisplayable clip = Assertions.assertInstanceOf(VideoDisplayable.class,
                    list.getItems().get(2).displayable());
            Assertions.assertEquals(image.toFile(), picture.getFile());
            Assertions.assertEquals(video.toFile(), clip.getLocationAsFile());
            for (int i = 0; i < list.getItems().size(); i++) {
                Assertions.assertEquals(i, list.getItems().get(i).index());
            }
        });
    }

    @Test
    @DisplayName("Drag-over accepts supported songs and files but rejects unsupported files")
    public void dragOverAcceptance(@TempDir Path directory) throws Exception {
        Path image = Files.createFile(directory.resolve("photo.png"));
        Path unsupported = Files.createFile(directory.resolve("notes.txt"));
        onFx(() -> {
            Assertions.assertTrue(dragOver(songContent(song("Song"))).isAccepted());
            ClipboardContent imageContent = new ClipboardContent();
            imageContent.putFiles(List.of(image.toFile()));
            Assertions.assertTrue(dragOver(imageContent).isAccepted());
            ClipboardContent unsupportedContent = new ClipboardContent();
            unsupportedContent.putFiles(List.of(unsupported.toFile()));
            Assertions.assertFalse(dragOver(unsupportedContent).isAccepted());
            Assertions.assertFalse(drop(unsupportedContent, 10, 10).isDropCompleted());
            ClipboardContent unsupportedString = new ClipboardContent();
            unsupportedString.putString(unsupported.toString());
            Assertions.assertFalse(dragOver(unsupportedString).isAccepted());
            Assertions.assertFalse(drop(unsupportedString, 10, 10).isDropCompleted());
            Assertions.assertTrue(list.isEmpty());
        });
    }

    @Test
    @DisplayName("An image dropped onto text becomes its background")
    public void imageDroppedOntoText(@TempDir Path directory) throws Exception {
        Path image = directory.resolve("text-background.png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile());
        onFx(() -> {
            TestText text = new TestText();
            list.add(text);
            ClipboardContent content = new ClipboardContent();
            content.putString(image.toString());
            dropAt(content, 0, 0.5);
            assertOrder(text);
            ImageBackground background = Assertions.assertInstanceOf(ImageBackground.class, text.getTheme().getBackground());
            Assertions.assertTrue(background.getImageFile().isFile());
            Assertions.assertSame(text.getTheme(), text.getSections()[0].getTheme());
        });
    }

    @Test
    @DisplayName("An image dropped below the list is added as an item, not a background")
    public void imageDroppedAfterText(@TempDir Path directory) throws Exception {
        Path image = directory.resolve("standalone.png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile());
        onFx(() -> {
            TestText text = new TestText();
            ThemeDTO originalTheme = text.getTheme();
            list.add(text);
            ClipboardContent content = new ClipboardContent();
            content.putString(image.toString());
            dropAt(content, 0, 1.25);
            Assertions.assertEquals(2, list.getItems().size());
            Assertions.assertSame(text, list.getItems().get(0).displayable());
            ImageDisplayable picture = Assertions.assertInstanceOf(ImageDisplayable.class,
                    list.getItems().get(1).displayable());
            Assertions.assertEquals(image.toFile(), picture.getFile());
            Assertions.assertSame(originalTheme, text.getTheme());
            assertSelected(1, picture);
            assertScheduleOrder(text, picture);
        });
        onFx(() -> { });
    }

    @Test
    @DisplayName("A video from the video library becomes a text background")
    public void videoLibraryItemDroppedOntoText() throws Exception {
        Path video = QueleaProperties.get().getVidDir().toPath().resolve("library-background.mp4");
        Files.write(video, new byte[]{0});
        onFx(() -> {
            TestText text = new TestText();
            list.add(text);
            ClipboardContent content = new ClipboardContent();
            content.putString(video.toString());
            dropAt(content, 0, 0.5);
            assertOrder(text);
            VideoBackground background = Assertions.assertInstanceOf(VideoBackground.class, text.getTheme().getBackground());
            Assertions.assertEquals(video.toFile(), background.getVideoFile());
            Assertions.assertSame(text.getTheme(), text.getSections()[0].getTheme());
        });
    }

    @Test
    @DisplayName("A video dragged from another folder remains available as a text background")
    public void videoFromAnotherFolderRemainsAvailable(@TempDir Path directory) throws Exception {
        // The video library can browse folders outside the video-library root.
        byte[] contents = {1, 2, 3};
        Path video = Files.write(directory.resolve("outside-background.mp4"), contents);
        AtomicReference<Path> stored = new AtomicReference<>();
        AtomicReference<String> savedTheme = new AtomicReference<>();
        onFx(() -> {
            TestText text = new TestText();
            list.add(text);
            ClipboardContent content = new ClipboardContent();
            content.putString(video.toString());
            dropAt(content, 0, 0.5);
            assertOrder(text);
            VideoBackground background = Assertions.assertInstanceOf(VideoBackground.class, text.getTheme().getBackground());
            Assertions.assertTrue(background.getVideoFile().isFile(), "The background must resolve to a real video file");
            stored.set(background.getVideoFile().toPath());
            savedTheme.set(text.getTheme().asString());
            Assertions.assertSame(text.getTheme(), text.getSections()[0].getTheme());
        });
        Files.delete(video);
        Assertions.assertArrayEquals(contents, Files.readAllBytes(stored.get()));
        onFx(() -> {
            ThemeDTO restored = ThemeDTO.fromString(savedTheme.get(), Map.of());
            VideoBackground background = Assertions.assertInstanceOf(VideoBackground.class, restored.getBackground());
            Assertions.assertEquals(stored.get().toFile(), background.getVideoFile());
            Assertions.assertTrue(background.getVideoFile().isFile());
        });
    }

    @Test
    @DisplayName("An imported video does not overwrite a different video with the same name")
    public void videoBackgroundNameCollision(@TempDir Path directory) throws Exception {
        Path existing = QueleaProperties.get().getVidDir().toPath().resolve("collision-video.mp4");
        byte[] existingContents = {1, 2};
        byte[] incomingContents = {3, 4, 5};
        Files.write(existing, existingContents);
        Path incoming = Files.write(directory.resolve("collision-video.mp4"), incomingContents);
        onFx(() -> {
            TestText text = new TestText();
            list.add(text);
            ClipboardContent content = new ClipboardContent();
            content.putString(incoming.toString());
            Assertions.assertTrue(dropAt(content, 0, 0.5).isDropCompleted());
            VideoBackground first = Assertions.assertInstanceOf(VideoBackground.class, text.getTheme().getBackground());
            Path stored = first.getVideoFile().toPath();
            Assertions.assertNotEquals(existing, stored);
            Assertions.assertArrayEquals(existingContents, Files.readAllBytes(existing));
            Assertions.assertArrayEquals(incomingContents, Files.readAllBytes(stored));
            Assertions.assertTrue(dropAt(content, 0, 0.5).isDropCompleted());
            VideoBackground second = Assertions.assertInstanceOf(VideoBackground.class, text.getTheme().getBackground());
            Assertions.assertEquals(stored, second.getVideoFile().toPath(), "Repeated drops should reuse identical media");
        });
    }

    @Test
    @DisplayName("An imported image does not overwrite a different image with the same name")
    public void imageBackgroundNameCollision(@TempDir Path directory) throws Exception {
        Path existing = QueleaProperties.get().getImageDir().toPath().resolve("collision-image.png");
        ImageIO.write(new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB), "png", existing.toFile());
        byte[] existingContents = Files.readAllBytes(existing);
        Path incoming = directory.resolve("collision-image.png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", incoming.toFile());
        byte[] incomingContents = Files.readAllBytes(incoming);
        onFx(() -> {
            TestText text = new TestText();
            list.add(text);
            ClipboardContent content = new ClipboardContent();
            content.putString(incoming.toString());
            Assertions.assertTrue(dropAt(content, 0, 0.5).isDropCompleted());
            ImageBackground background = Assertions.assertInstanceOf(ImageBackground.class, text.getTheme().getBackground());
            Path stored = background.getImageFile().toPath();
            Assertions.assertNotEquals(existing, stored);
            Assertions.assertArrayEquals(existingContents, Files.readAllBytes(existing));
            Assertions.assertArrayEquals(incomingContents, Files.readAllBytes(stored));
        });
    }

    @Test
    @DisplayName("A failed background copy leaves the theme unchanged and reports failure")
    public void failedBackgroundCopy(@TempDir Path directory) throws Exception {
        Path home = Files.createDirectory(directory.resolve("home"));
        Path quelea = Files.createDirectory(home.resolve(".quelea"));
        Files.write(quelea.resolve("vid"), new byte[]{1}); // A file cannot be a video-library directory.
        Path video = Files.createFile(directory.resolve("cannot-copy.mp4"));
        onFx(() -> {
            try {
                QueleaProperties.init(home.toString());
                TestText text = new TestText();
                ThemeDTO original = text.getTheme();
                list.add(text);
                ClipboardContent content = new ClipboardContent();
                content.putString(video.toString());
                Assertions.assertFalse(dropAt(content, 0, 0.5).isDropCompleted());
                Assertions.assertSame(original, text.getTheme());
                assertOrder(text);
            } finally {
                QueleaProperties.init(testHome.toString());
            }
        });
    }

    @Test
    @DisplayName("Dropping a directory does not add it as an image")
    public void externalDirectoryIsIgnored(@TempDir Path directory) throws Exception {
        // File chooser filters allow directories for navigation; a schedule drop
        // must not mistake one for a renderable image.
        onFx(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putFiles(List.of(directory.toFile()));
            Assertions.assertFalse(dragOver(content).isAccepted());
            Assertions.assertFalse(drop(content, 10, 10).isDropCompleted());
            Assertions.assertTrue(list.isEmpty(), "A directory is not a displayable image");
        });
    }

    @Test
    @DisplayName("An internal move without a source is rejected")
    public void staleInternalMoveIsRejected() throws Exception {
        onFx(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putString("tempdisp");
            Assertions.assertFalse(dragOver(content).isAccepted());
            Assertions.assertFalse(drop(content, 10, 10).isDropCompleted());
            Assertions.assertTrue(list.isEmpty());
        });
    }

    @Test
    @DisplayName("A successful drop is reported to the drag source")
    public void successfulDropIsReported() throws Exception {
        // The source needs to distinguish a successful drop from a cancellation.
        onFx(() -> {
            DragEvent event = drop(songContent(song("Song")), 10, 10);
            Assertions.assertTrue(event.isDropCompleted(), "The source should be told that the drop succeeded");
        });
        onFx(() -> { });
    }

    private void assertMultipleSongsAt(int position, boolean initiallyEmpty) throws Exception {
        try {
            onFx(() -> {
                list.getItems().clear();
                SongDisplayable a = song("A");
                SongDisplayable b = song("B");
                SongDisplayable x = song("X");
                SongDisplayable y = song("Y");
                SongDisplayable z = song("Z");
                if (!initiallyEmpty) {
                    add(a, b);
                }
                ClipboardContent content = new ClipboardContent();
                SongDragboard.put(content, new SongDisplayableList(List.of(
                        new IndexedDisplayable(x, -1), new IndexedDisplayable(y, -1), new IndexedDisplayable(z, -1))));
                if (initiallyEmpty) {
                    drop(content, 10, 10);
                    assertOrder(x, y, z);
                } else if (position == 0) {
                    dropAt(content, 0, 0.25);
                    assertOrder(x, y, z, a, b);
                } else if (position == 1) {
                    dropAt(content, 1, 0.25);
                    assertOrder(a, x, y, z, b);
                } else {
                    dropAt(content, 1, 0.75);
                    assertOrder(a, b, x, y, z);
                }
            });
        } finally {
            onFx(() -> { });
        }
    }

    private static SongDisplayableList roundTrip(SongDisplayableList payload) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(payload);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (SongDisplayableList) input.readObject();
        }
    }

    private static ClipboardContent songContent(SongDisplayable song) {
        ClipboardContent content = new ClipboardContent();
        SongDragboard.put(content,
                new SongDisplayableList(new IndexedDisplayable(song, -1)));
        return content;
    }

    private ClipboardContent scheduleSongContent(int index) {
        ClipboardContent content = new ClipboardContent();
        SongDragboard.put(content, new SongDisplayableList(list.getItems().get(index)));
        return content;
    }

    private void add(Displayable... items) {
        for (Displayable item : items) {
            list.add(item);
        }
    }

    private DragEvent dropAt(ClipboardContent content, int index, double verticalFraction) {
        Point2D point = pointOnCell(index, verticalFraction);
        return drop(content, point.getX(), point.getY());
    }

    private Point2D pointOnCell(int index, double verticalFraction) {
        list.applyCss();
        list.layout();
        list.getListView().applyCss();
        list.getListView().layout();
        for (var node : list.getListView().lookupAll(".list-cell")) {
            if (node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getIndex() == index) {
                return cell.localToScene(cell.getWidth() / 2, cell.getHeight() * verticalFraction);
            }
        }
        throw new AssertionError("No visible cell for index " + index);
    }

    private void assertOrder(Displayable... expected) {
        Assertions.assertEquals(expected.length, list.getItems().size());
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertSame(expected[i], list.getItems().get(i).displayable(), "item at " + i);
            Assertions.assertEquals(i, list.getItems().get(i).index(), "stored index at " + i);
        }
    }

    private void assertSelected(int index, Displayable expected) {
        Assertions.assertEquals(index, list.getSelectionModel().getSelectedIndex());
        Assertions.assertSame(expected, list.getSelectionModel().getSelectedItem().displayable());
    }

    private void assertScheduleOrder(Displayable... expected) {
        var schedule = list.getSchedule();
        Assertions.assertEquals(expected.length, schedule.getSize());
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertSame(expected[i], schedule.getDisplayable(i), "schedule item at " + i);
        }
    }

    private DragEvent drop(ClipboardContent content, double x, double y) {
        Dragboard dragboard = DragboardHelper.createDragboard(new TestClipboard());
        Assertions.assertTrue(dragboard.setContent(content));
        DragEvent event = new DragEvent(list.getListView(), list.getListView(), DragEvent.DRAG_DROPPED,
                dragboard, x, y, x, y, TransferMode.COPY, new Object(), list.getListView(),
                new PickResult(list.getListView(), x, y));
        list.getListView().getOnDragDropped().handle(event);
        return event;
    }

    private DragEvent dragOver(ClipboardContent content) {
        Dragboard dragboard = DragboardHelper.createDragboard(new TestClipboard());
        Assertions.assertTrue(dragboard.setContent(content));
        DragEvent event = new DragEvent(list.getListView(), list.getListView(), DragEvent.DRAG_OVER,
                dragboard, 10, 10, 10, 10, TransferMode.COPY, new Object(), list.getListView(),
                new PickResult(list.getListView(), 10, 10));
        list.getListView().getOnDragOver().handle(event);
        return event;
    }

    private static SongDisplayable song(String title) {
        return new SongDisplayable.Builder(title, "Author").lyrics("Verse 1\nWords").get();
    }

    private static void onFx(ThrowingRunnable action) throws Exception {
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

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Local clipboard data plus the transfer modes a real drag source offers. */
    private static final class TestClipboard implements TKClipboard {
        private final TKClipboard delegate = Toolkit.getToolkit().createLocalClipboard();
        @Override public Set<DataFormat> getContentTypes() { return delegate.getContentTypes(); }
        @SafeVarargs
        @Override public final boolean putContent(Pair<DataFormat, Object>... content) { return delegate.putContent(content); }
        @Override public Object getContent(DataFormat format) {
            Object value = delegate.getContent(format);
            // Native clipboard reads may decode a new String on each access.
            return value instanceof String string ? new String(string) : value;
        }
        @Override public boolean hasContent(DataFormat format) { return delegate.hasContent(format); }
        @Override public Set<TransferMode> getTransferModes() { return Set.of(TransferMode.COPY, TransferMode.MOVE); }
        @Override public void setDragView(Image image) { }
        @Override public void setDragViewOffsetX(double offset) { }
        @Override public void setDragViewOffsetY(double offset) { }
        @Override public Image getDragView() { return null; }
        @Override public double getDragViewOffsetX() { return 0; }
        @Override public double getDragViewOffsetY() { return 0; }
    }

    private static final class TestText implements TextDisplayable {
        private ThemeDTO theme = ThemeDTO.DEFAULT_THEME;
        private final TextSection[] sections = {new TextSection("Verse", new String[]{"Words"}, null, false)};
        @Override public TextSection[] getSections() { return sections; }
        @Override public Double getCachedUniformFontSize(Dimension dimension) { return null; }
        @Override public void setCachedUniformFontSize(Dimension dimension, double size) { }
        @Override public ThemeDTO getTheme() { return theme; }
        @Override public void setTheme(ThemeDTO theme) { this.theme = theme; }
        @Override public boolean supportClear() { return false; }
        @Override public String getXML() { return "<text/>"; }
        @Override public Node getPreviewIcon() { return new Group(); }
        @Override public String getPreviewText() { return "Text"; }
        @Override public Collection<File> getResources() { return List.of(); }
        @Override public void dispose() { }
    }

    // The drop handler updates the global preview asynchronously. These tiny
    // test doubles replace only that app shell; constructing a real MainWindow
    // would start the database, preferences, and other unrelated services.
    // Serialization constructors avoid running the heavy JavaFX constructors;
    // the uninitialised nodes are never displayed or used beyond these overrides.
    private static <T> T withoutConstructor(Class<T> type) throws Exception {
        Class<?> factoryType = Class.forName("sun.reflect.ReflectionFactory");
        Object factory = factoryType.getMethod("getReflectionFactory").invoke(null);
        Constructor<?> constructor = (Constructor<?>) factoryType
                .getMethod("newConstructorForSerialization", Class.class, Constructor.class)
                .invoke(factory, type, Object.class.getDeclaredConstructor());
        return type.cast(constructor.newInstance());
    }

    private static final class TestMainWindow extends MainWindow {
        private TestMainPanel panel;
        private TestMainWindow() { super(false, false); }
        @Override public MainPanel getMainPanel() { return panel; }
    }

    private static final class TestMainPanel extends MainPanel {
        private TestPreviewPanel preview;
        private TestLivePanel live;
        @Override public PreviewPanel getPreviewPanel() { return preview; }
        @Override public LivePanel getLivePanel() { return live; }
    }

    private static final class TestPreviewPanel extends PreviewPanel {
        private Displayable displayable;
        private int refreshCount;
        @Override public void setDisplayable(Displayable displayable, int index) { this.displayable = displayable; }
        @Override public void refresh() { refreshCount++; }
    }

    private static final class TestLivePanel extends LivePanel {
        @Override public Displayable getDisplayable() { return null; }
        @Override public void refresh() { }
    }
}
