package org.quelea.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.services.utils.QueleaProperties;

@DisplayName("Schedule persistence")
public class SchedulePersistenceTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-schedule-persistence").toString());
        }
    }

    @Test
    public void roundTripsSongsInOrderAndTracksModifiedState() throws Exception {
        Schedule schedule = new Schedule();
        Assertions.assertTrue(schedule.isEmpty());
        Assertions.assertFalse(schedule.isModified());
        Assertions.assertFalse(schedule.add(null));
        schedule.add(song("First & foremost", "First verse"));
        schedule.add(song("Second", "Another verse"));
        Assertions.assertTrue(schedule.isModified());
        Path path = directory.resolve("service.quelea");
        schedule.setFile(path.toFile());
        Assertions.assertTrue(schedule.writeToFile());
        Assertions.assertFalse(schedule.isModified());
        try (ZipFile zip = new ZipFile(path.toFile())) {
            Assertions.assertNotNull(zip.getEntry("schedule.xml"));
        }
        Schedule restored = Schedule.fromFile(path.toFile());
        Assertions.assertNotNull(restored);
        Assertions.assertEquals(2, restored.getSize());
        Assertions.assertEquals("First & foremost", ((SongDisplayable) restored.getDisplayable(0)).getTitle());
        Assertions.assertEquals("Second", ((SongDisplayable) restored.getDisplayable(1)).getTitle());
        Assertions.assertEquals("First verse", ((SongDisplayable) restored.getDisplayable(0)).getLyrics(false, false, false));
        Assertions.assertFalse(restored.isModified());
        restored.clear();
        Assertions.assertTrue(restored.isEmpty());
        Assertions.assertTrue(restored.isModified());
    }

    @Test
    public void unsavedScheduleCannotBeWritten() {
        Assertions.assertFalse(new Schedule().writeToFile());
    }

    @Test
    public void archiveWithoutScheduleXmlIsRejectedGracefully() throws Exception {
        Path broken = directory.resolve("empty.quelea");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(broken))) {
            // Empty but structurally valid ZIP, as a truncated export might be.
        }
        Assertions.assertNull(Schedule.fromFile(broken.toFile()));
    }

    @Test
    public void roundTripsMixedScheduleItemsInOrder() throws Exception {
        boolean original = QueleaProperties.get().getEmbedMediaInScheduleFile();
        try {
            QueleaProperties.get().setEmbedMediaInScheduleFile(false);
            Path video = Files.write(directory.resolve("video & café.mp4"), new byte[]{1});
            Path audio = Files.write(directory.resolve("audio.mp3"), new byte[]{2});
            Schedule schedule = new Schedule();
            schedule.add(song("Opening", "Opening words"));
            schedule.add(new VideoDisplayable(video.toString()));
            schedule.add(new AudioDisplayable(audio.toFile()));
            schedule.add(new TimerDisplayable("Countdown", 90, "Starts in", "Go", ThemeDTO.DEFAULT_THEME));
            schedule.setFile(directory.resolve("mixed.quelea").toFile());
            Assertions.assertTrue(schedule.writeToFile());
            Schedule restored = Schedule.fromFile(schedule.getFile());
            Assertions.assertNotNull(restored);
            Assertions.assertEquals(4, restored.getSize());
            Assertions.assertEquals("Opening", ((SongDisplayable) restored.getDisplayable(0)).getTitle());
            Assertions.assertEquals(video.toString(), ((VideoDisplayable) restored.getDisplayable(1)).getLocation());
            Assertions.assertEquals(audio.toAbsolutePath().toString(), ((AudioDisplayable) restored.getDisplayable(2)).getLocation());
            Assertions.assertEquals(90, ((TimerDisplayable) restored.getDisplayable(3)).getSeconds());
        } finally {
            QueleaProperties.get().setEmbedMediaInScheduleFile(original);
        }
    }

    @Test
    public void embeddedMediaIsIncludedOnlyOnceForRepeatedItems() throws Exception {
        boolean original = QueleaProperties.get().getEmbedMediaInScheduleFile();
        try {
            QueleaProperties.get().setEmbedMediaInScheduleFile(true);
            byte[] contents = {1, 2, 3, 4};
            Path media = Files.write(directory.resolve("clip.mp4"), contents);
            Schedule schedule = new Schedule();
            schedule.add(new VideoDisplayable(media.toString()));
            schedule.add(new VideoDisplayable(media.toString()));
            Path path = directory.resolve("embedded.quelea");
            schedule.setFile(path.toFile());
            Assertions.assertTrue(schedule.writeToFile());
            try (ZipFile zip = new ZipFile(path.toFile())) {
                var resources = zip.stream().filter(entry -> entry.getName().startsWith("resources/")).toList();
                Assertions.assertEquals(1, resources.size());
                try (var stream = zip.getInputStream(resources.get(0))) {
                    Assertions.assertArrayEquals(contents, stream.readAllBytes());
                }
            }
        } finally {
            QueleaProperties.get().setEmbedMediaInScheduleFile(original);
        }
    }

    private SongDisplayable song(String title, String lyrics) {
        SongDisplayable song = new SongDisplayable(title, "Author");
        song.setLyrics(lyrics);
        return song;
    }
}
