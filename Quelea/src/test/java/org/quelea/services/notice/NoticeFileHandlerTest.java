package org.quelea.services.notice;

import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableFont;

@DisplayName("Saved notices")
public class NoticeFileHandlerTest {
    @TempDir Path directory;
    private QueleaProperties previous;

    @BeforeEach
    public void isolateProperties() throws Exception {
        previous = QueleaProperties.get();
        QueleaProperties.init(directory.toString());
        Files.createDirectories(QueleaProperties.get().getNoticeDir().toPath());
    }

    @AfterEach
    public void restoreProperties() throws Exception {
        var instance = QueleaProperties.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, previous);
    }

    @Test
    public void saveLoadAndDeletePreservesUnicodeAndCreationTime() {
        Notice original = new Notice("Café & tea", 3, new SerializableColor(Color.WHITE),
                new SerializableFont(Font.font("System", 24)));
        original.setCreationTime(123456789L);
        NoticeFileHandler.saveNotice(original);
        Path file = QueleaProperties.get().getNoticeDir().toPath().resolve("123456789.qnf");
        Assertions.assertTrue(Files.exists(file));
        Notice loaded = NoticeFileHandler.noticeFromFile(file.toFile());
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(original.getText(), loaded.getText());
        Assertions.assertEquals(original.getTimes(), loaded.getTimes());
        Assertions.assertEquals(original.getCreationTime(), loaded.getCreationTime());
        Assertions.assertTrue(NoticeFileHandler.deleteNotice(loaded));
        Assertions.assertFalse(Files.exists(file));
    }

    @Test
    public void malformedOrMissingNoticeFileReturnsNull() throws Exception {
        Path bad = Files.writeString(directory.resolve("broken.qnf"), "<notice>");
        Assertions.assertNull(NoticeFileHandler.noticeFromFile(bad.toFile()));
        Assertions.assertNull(NoticeFileHandler.noticeFromFile(directory.resolve("missing.qnf").toFile()));
    }
}
