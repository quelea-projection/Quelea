package org.quelea.data.imagegroup;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.testutil.FxTestSupport;

@DisplayName("Image group presentation")
public class ImageGroupPresentationTest {
    @TempDir Path directory;

    @Test
    public void preservesImageOrderAndSkipsOtherFiles() throws Exception {
        FxTestSupport.assumeAvailable();
        Path first = image("first.png", 5, 10);
        Path second = image("second.png", 10, 5);
        Path text = Files.writeString(directory.resolve("readme.txt"), "not an image");
        ImageGroup group = new ImageGroupFactory().getPresentation(new java.io.File[]{first.toFile(), text.toFile(), second.toFile()});
        Assertions.assertEquals(2, group.getSlides().length);
        Assertions.assertEquals(5, group.getSlide(0).getImage().getWidth());
        Assertions.assertEquals(10, group.getSlide(1).getImage().getWidth());
        Assertions.assertFalse(group.getSlide(0).getImage().isError());
    }

    private Path image(String name, int width, int height) throws Exception {
        Path path = directory.resolve(name);
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", path.toFile());
        return path;
    }
}
