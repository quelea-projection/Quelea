package org.quelea.data.displayable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.services.utils.QueleaProperties;
import org.w3c.dom.Element;

@DisplayName("Audio and video schedule persistence")
public class MediaDisplayablePersistenceTest {
    @TempDir Path directory;
    private boolean originalEmbedSetting;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-media-xml-test").toString());
        }
    }

    @BeforeEach
    public void useExternalMediaPaths() {
        originalEmbedSetting = QueleaProperties.get().getEmbedMediaInScheduleFile();
        QueleaProperties.get().setEmbedMediaInScheduleFile(false);
    }

    @AfterEach
    public void restoreEmbedSetting() {
        QueleaProperties.get().setEmbedMediaInScheduleFile(originalEmbedSetting);
    }

    @Test
    public void videoXmlRoundTripPreservesUnicodeAndXmlCharacters() throws Exception {
        Path file = Files.write(directory.resolve("café & welcome.mp4"), new byte[]{1, 2, 3});
        VideoDisplayable original = new VideoDisplayable(file.toString());
        VideoDisplayable loaded = VideoDisplayable.parseXML(element(original.getXML()), Map.of());
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(file.toAbsolutePath().toString(), loaded.getLocation());
        Assertions.assertEquals("café & welcome.mp4", loaded.getName());
        Assertions.assertEquals(file.toFile(), loaded.getResources().iterator().next());
        Assertions.assertFalse(loaded.supportClear());
    }

    @Test
    public void videoUsesRelocatedFileAndRejectsMissingOrLegacyYoutubeEntries() throws Exception {
        Path missing = directory.resolve("old.mp4").toAbsolutePath();
        Path replacement = Files.write(directory.resolve("new.mp4"), new byte[]{1});
        Element entry = element("<filevideo>" + missing + "</filevideo>");
        VideoDisplayable loaded = VideoDisplayable.parseXML(entry, Map.of(missing.toString(), replacement.toString()));
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(replacement.toAbsolutePath().toString(), loaded.getLocation());
        Assertions.assertNull(VideoDisplayable.parseXML(entry, Map.of()));
        Assertions.assertNull(VideoDisplayable.parseXML(element("<filevideo youtubetitle=\"Old YouTube\">ignored</filevideo>"), Map.of()));
    }

    @Test
    public void audioXmlRoundTripPreservesTheFileAndCanRelocateIt() throws Exception {
        Path originalPath = Files.write(directory.resolve("song & audio.mp3"), new byte[]{1});
        AudioDisplayable original = new AudioDisplayable(originalPath.toFile());
        AudioDisplayable loaded = AudioDisplayable.parseXML(element(original.getXML()), Map.of());
        Assertions.assertEquals(originalPath.toAbsolutePath().toString(), loaded.getLocation());
        Assertions.assertEquals(originalPath.toFile(), loaded.getResources().iterator().next());
        Assertions.assertTrue(loaded.supportClear());

        Path old = directory.resolve("missing.mp3").toAbsolutePath();
        AudioDisplayable relocated = AudioDisplayable.parseXML(element("<fileaudio>" + old + "</fileaudio>"),
                Map.of(old.toString(), originalPath.toString()));
        Assertions.assertEquals(originalPath.toAbsolutePath().toString(), relocated.getLocation());
    }

    private Element element(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
    }
}
