package org.quelea.windows.video;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.VideoTestSupport;

/** The frame grab test uses GStreamer and the bundled 1280x720 video. */
@DisplayName("VidPreviewDisplay")
public class VidPreviewDisplayTest {

    @Test
    @DisplayName("A frame is scaled down to fit, keeping its aspect ratio")
    public void fitSizeScalesDown() {
        Assertions.assertArrayEquals(new int[]{160, 90}, VidPreviewDisplay.fitSize(7680, 4320, 1, 160, 90));
        Assertions.assertArrayEquals(new int[]{552, 311}, VidPreviewDisplay.fitSize(7680, 4320, 1, 552, 552));
        // Portrait: the height limits it.
        Assertions.assertArrayEquals(new int[]{51, 90}, VidPreviewDisplay.fitSize(1080, 1920, 1, 160, 90));
    }

    @Test
    @DisplayName("A frame smaller than the box isn't scaled up")
    public void fitSizeNeverScalesUp() {
        Assertions.assertArrayEquals(new int[]{100, 50}, VidPreviewDisplay.fitSize(100, 50, 1, 552, 552));
    }

    @Test
    @DisplayName("Non-square pixels are made square")
    public void fitSizeUsesPixelAspectRatio() {
        // Anamorphic PAL widescreen: 720x576 with 64:45 pixels shows as 1024x576.
        Assertions.assertArrayEquals(new int[]{1024, 576}, VidPreviewDisplay.fitSize(720, 576, 64.0 / 45, 2000, 2000));
        Assertions.assertArrayEquals(new int[]{160, 90}, VidPreviewDisplay.fitSize(720, 576, 64.0 / 45, 160, 90));
    }

    @Test
    @DisplayName("The grabbed frame is already scaled down by GStreamer")
    public void grabbedFrameIsScaled() {
        VideoTestSupport.assumeVideoAvailable();
        Image frame = new VidPreviewDisplay().getPreviewImg(VideoTestSupport.TEST_VIDEO.toURI(), 160, 90);
        Assertions.assertEquals(160, (int) frame.getWidth());
        Assertions.assertEquals(90, (int) frame.getHeight());
        Assertions.assertNull(frame.getException());
    }
}
