package org.quelea.services.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.data.displayable.ImageDisplayable;

/**
 * Images in formats that JavaFX can't read itself, such as TIFF, are shown
 * by converting them with ImageIO.
 */
@DisplayName("ImageFormatsTest")
public class ImageFormatsTest {

    private static File dir;

    @BeforeAll
    public static void setUp() throws Exception {
        QueleaProperties.init(Files.createTempDirectory("quelea-image-formats-test").toString());
        dir = Files.createTempDirectory("quelea image formats").toFile();
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            started.await();
        } catch (IllegalStateException ex) {
            // Already started
        } catch (UnsupportedOperationException ex) {
            Assumptions.abort("No JavaFX toolkit available");
        }
    }

    @Test
    public void tiffIsShown() throws Exception {
        for (String extension : new String[]{"tiff", "tif"}) {
            File file = write("picture." + extension, "tiff");
            Assertions.assertTrue(new Image(file.toURI().toString()).isError(),
                    "JavaFX can't read this TIFF itself, or this test isn't needed any more");
            Image image = new Image(ImageManager.loadableUri(file));
            assertPicture(image);
            assertPicture(new ImageDisplayable(file).getImage());
            assertPicture(ImageManager.INSTANCE.getImage(file.toURI().toString()));
            Assertions.assertTrue(Utils.fileIsImage(file), extension + " should be offered as an image");
        }
    }

    @Test
    public void tiffIsConvertedOnce() throws Exception {
        File file = write("once.tiff", "tiff");
        String first = ImageManager.loadableUri(file);
        File converted = new File(java.net.URI.create(first));
        long modified = converted.lastModified();
        Thread.sleep(20);
        Assertions.assertEquals(first, ImageManager.loadableUri(file));
        Assertions.assertEquals(modified, converted.lastModified(), "the converted copy should be reused");
    }

    @Test
    public void unencodedFileUriWorks() throws Exception {
        File file = write("with space.tiff", "tiff");
        assertPicture(new Image(ImageManager.loadableUri("file:" + file.getAbsolutePath())));
    }

    @Test
    public void formatsJavaFxReadsAreLeftAlone() throws Exception {
        File file = write("picture.png", "png");
        Assertions.assertEquals(file.toURI().toString(), ImageManager.loadableUri(file));
    }

    private static File write(String name, String format) throws Exception {
        BufferedImage picture = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 30; y++) {
                picture.setRGB(x, y, x < 20 ? 0xFF0000 : 0x0000FF);
            }
        }
        File file = new File(dir, name);
        Assertions.assertTrue(ImageIO.write(picture, format, file));
        return file;
    }

    private static void assertPicture(Image image) {
        Assertions.assertFalse(image.isError(), "the image should load: " + image.getException());
        Assertions.assertEquals(40, image.getWidth());
        Assertions.assertEquals(30, image.getHeight());
        Assertions.assertEquals(0xFFFF0000, image.getPixelReader().getArgb(5, 5));
        Assertions.assertEquals(0xFF0000FF, image.getPixelReader().getArgb(35, 5));
    }
}
