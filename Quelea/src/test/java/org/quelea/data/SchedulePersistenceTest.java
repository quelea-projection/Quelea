package org.quelea.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.data.displayable.AudioDisplayable;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.TimerDisplayable;
import org.quelea.data.displayable.VideoDisplayable;
import org.quelea.data.bible.BibleVerse;
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
    public void malformedScheduleXmlIsRejectedGracefully() throws Exception {
        Path broken = directory.resolve("bad-xml.quelea");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(broken))) {
            out.putNextEntry(new ZipEntry("schedule.xml"));
            out.write("<schedule><song>".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        Assertions.assertNull(Schedule.fromFile(broken.toFile()));
    }

    @Test
    public void commentsBeforeScheduleRootDoNotHideItems() throws Exception {
        Path file = directory.resolve("commented.quelea");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file))) {
            out.putNextEntry(new ZipEntry("schedule.xml"));
            String xml = "<?xml version=\"1.0\"?><!-- comment --><schedule>"
                    + song("Included", "Words").getXML() + "</schedule>";
            out.write(xml.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        Schedule loaded = Schedule.fromFile(file.toFile());
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(1, loaded.getSize());
        Assertions.assertEquals("Included", ((SongDisplayable) loaded.getDisplayable(0)).getTitle());
    }

    @Test
    public void scheduleXmlDoesNotExpandExternalEntities() throws Exception {
        Path secret = Files.writeString(directory.resolve("private.txt"), "PRIVATE_TEST_CONTENT");
        String songXml = song("Placeholder", "Words").getXML().replace("<title>Placeholder</title>", "<title>&data;</title>");
        String xml = "<!DOCTYPE schedule [<!ENTITY data SYSTEM \"" + secret.toUri() + "\">]>"
                + "<schedule>" + songXml + "</schedule>";
        Path file = directory.resolve("external.quelea");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file))) {
            out.putNextEntry(new ZipEntry("schedule.xml"));
            out.write(xml.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        Schedule loaded = Schedule.fromFile(file.toFile());
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(1, loaded.getSize());
        Assertions.assertFalse(((SongDisplayable) loaded.getDisplayable(0)).getTitle().contains("PRIVATE_TEST_CONTENT"));
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

    @Test
    public void opensScheduleWithLegacyVideoHueTheme() throws Exception {
        ThemeDTO defaults = ThemeDTO.DEFAULT_THEME;
        ThemeDTO videoTheme = new ThemeDTO(defaults.getSerializableFont(), defaults.getFontPaint(),
                defaults.getTranslateSerializableFont(), defaults.getTranslateFontPaint(),
                new VideoBackground("background.mp4", true), defaults.getShadow(),
                false, false, false, false, -1, 0);
        SongDisplayable song = song("Legacy", "Words");
        song.getSectionsWithoutSequence()[0].setTheme(videoTheme);
        Schedule schedule = new Schedule();
        schedule.add(song);
        String legacyXml = schedule.getXML().replace("$vidstretch:", "$vidhue:0.35$vidstretch:");
        Assertions.assertTrue(legacyXml.contains("$vidhue:0.35"));
        Path file = directory.resolve("legacy-theme.quelea");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            zip.putNextEntry(new ZipEntry("schedule.xml"));
            zip.write(legacyXml.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        Schedule restored = Schedule.fromFile(file.toFile());
        Assertions.assertNotNull(restored);
        SongDisplayable restoredSong = (SongDisplayable) restored.getDisplayable(0);
        VideoBackground background = Assertions.assertInstanceOf(VideoBackground.class,
                restoredSong.getSectionsWithoutSequence()[0].getTheme().getBackground());
        Assertions.assertEquals("background.mp4", background.getString());
        Assertions.assertTrue(background.getStretch());
        Assertions.assertFalse(restored.getXML().contains("$vidhue:"));
    }

    @Test
    public void dollarNamesSurviveScheduleRoundTripForSongsTimersAndBiblePassages() throws Exception {
        ThemeDTO defaults = ThemeDTO.DEFAULT_THEME;
        ThemeDTO custom = new ThemeDTO(defaults.getSerializableFont(), defaults.getFontPaint(),
                defaults.getTranslateSerializableFont(), defaults.getTranslateFontPaint(),
                defaults.getBackground(), defaults.getShadow(), false, false, false, false, -1, 0);
        String name = "Café & praise $5 <Sunday>";
        custom.setThemeName(name);
        SongDisplayable song = song("Opening", "Words");
        song.getSectionsWithoutSequence()[0].setTheme(custom);
        Schedule schedule = new Schedule();
        schedule.add(song);
        schedule.add(new TimerDisplayable("Countdown", 90, "Starts", "Go", custom));
        schedule.add(new BiblePassage("John 3:16\nTest Bible", new BibleVerse[0], custom, false));
        ThemeDTO ordinary = new ThemeDTO(defaults.getSerializableFont(), defaults.getFontPaint(),
                defaults.getTranslateSerializableFont(), defaults.getTranslateFontPaint(),
                defaults.getBackground(), defaults.getShadow(), false, false, false, false, -1, 0);
        ordinary.setThemeName("Grace & <praise>");
        schedule.add(new BiblePassage("Psalm 1:1\nTest Bible", new BibleVerse[0], ordinary, false));
        schedule.setFile(directory.resolve("named-themes.quelea").toFile());
        Assertions.assertTrue(schedule.writeToFile());

        Schedule restored = Schedule.fromFile(schedule.getFile());
        Assertions.assertNotNull(restored);
        SongDisplayable restoredSong = (SongDisplayable) restored.getDisplayable(0);
        Assertions.assertEquals(name, restoredSong.getSectionsWithoutSequence()[0].getTheme().getThemeName());
        Assertions.assertEquals(name, ((TimerDisplayable) restored.getDisplayable(1)).getTheme().getThemeName());
        Assertions.assertEquals(name, ((BiblePassage) restored.getDisplayable(2)).getTheme().getThemeName());
        Assertions.assertEquals("Grace & <praise>", ((BiblePassage) restored.getDisplayable(3)).getTheme().getThemeName());
    }

    private SongDisplayable song(String title, String lyrics) {
        SongDisplayable song = new SongDisplayable(title, "Author");
        song.setLyrics(lyrics);
        return song;
    }
}
