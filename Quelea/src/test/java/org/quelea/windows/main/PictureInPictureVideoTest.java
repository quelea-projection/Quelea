package org.quelea.windows.main;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.VideoTestSupport;
import org.quelea.windows.video.VidDisplay;
import org.quelea.windows.video.VideoOnlyTestPlayer;

import static org.junit.jupiter.api.Assertions.*;
import static org.quelea.testutil.VideoTestSupport.*;

class PictureInPictureVideoTest {
    private static long fingerprint(Image image) {
        if (image == null || image.getPixelReader() == null) return 0;
        long hash = 1;
        for (int y = 1; y < 10; y++) {
            for (int x = 1; x < 10; x++) {
                hash = hash * 31 + image.getPixelReader().getArgb(
                        x * (int) image.getWidth() / 10, y * (int) image.getHeight() / 10);
            }
        }
        return hash;
    }

    private static boolean hasVisibleColor(Image image) {
        if (image == null || image.getPixelReader() == null || image.getWidth() < 2 || image.getHeight() < 2) return false;
        for (int y = 1; y < 10; y++) {
            for (int x = 1; x < 10; x++) {
                if ((image.getPixelReader().getArgb(x * (int) image.getWidth() / 10,
                        y * (int) image.getHeight() / 10) & 0x00ffffff) != 0) return true;
            }
        }
        return false;
    }

    @Test
    void mirrorsRealPlayingVideoWithoutCreatingAnotherPlayer() throws Exception {
        exerciseVideo(true);
    }

    @Test
    void mirrorsRealVideoWhenTheProjectionRemainsVisible() throws Exception {
        exerciseVideo(false);
    }

    private void exerciseVideo(boolean hideProjection) throws Exception {
        VideoTestSupport.assumeVideoAvailable();
        int baseline = activePipelines();
        VidDisplay[] video = new VidDisplay[1];
        Stage[] stages = new Stage[2];
        PictureInPictureWindow[] pip = new PictureInPictureWindow[1];
        Stage[] floating = new Stage[1];
        ImageView[] output = new ImageView[1];
        try {
            onFx(() -> {
                video[0] = VideoOnlyTestPlayer.create();
                ImageView sourceImage = new ImageView();
                sourceImage.imageProperty().bind(video[0].imageProperty());
                sourceImage.setFitWidth(640);
                sourceImage.setFitHeight(360);
                StackPane source = new StackPane(sourceImage);
                stages[0] = new Stage();
                stages[0].setScene(new Scene(source, 640, 360));
                stages[0].show();
                stages[1] = new Stage();
                stages[1].setScene(new Scene(new StackPane(), 320, 180));
                stages[1].show();
                pip[0] = new PictureInPictureWindow(stages[1], () -> source, () -> null,
                        () -> { }, () -> stages[0], () -> hideProjection);
                video[0].setURI(VideoTestSupport.TEST_VIDEO.toURI());
                video[0].play();
            });
            assertTrue(waitFor(() -> hasVisibleColor(video[0].imageProperty().get()), 20_000), "video produces a frame");
            onFx(() -> {
                pip[0].toggle();
                assertEquals(!hideProjection, stages[0].isShowing(), "projection visibility should follow the preference");
                floating[0] = Window.getWindows().stream().filter(w -> w instanceof Stage && w != stages[0] && w != stages[1])
                        .map(w -> (Stage) w).filter(w -> org.quelea.services.languages.LabelGrabber.INSTANCE
                                .getLabel("picture.in.picture.title").equals(w.getTitle())).findFirst().orElseThrow();
                floating[0].getScene().getRoot().applyCss();
                assertEquals(1, floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity(), 0.001,
                        "video should keep playing while the logo is visible");
                output[0] = (ImageView) floating[0].getScene().getRoot().lookup(".pip-frame");
                assertNotNull(output[0]);
            });
            assertTrue(waitFor(() -> hasVisibleColor(output[0].getImage()), 10_000), "the preview paints a video frame");
            long first = onFx(() -> fingerprint(output[0].getImage()));
            assertTrue(waitFor(() -> fingerprint(output[0].getImage()) != first, 15_000),
                    "the floating image must keep updating during PiP");
            onFx(() -> PictureInPictureLogoTest.hover(floating[0], true));
            assertTrue(waitFor(() -> floating[0].getScene().getRoot().lookup(".pip-logo-cover").getOpacity() < 0.01, 5_000),
                    "hover should reveal the playing video");
            long revealed = onFx(() -> fingerprint(output[0].getImage()));
            assertTrue(waitFor(() -> fingerprint(output[0].getImage()) != revealed, 15_000),
                    "video frames must continue updating after the transition");
            assertEquals(baseline + 1, activePipelines(), "mirroring must not start another video pipeline");
            onFx(() -> {
                pip[0].toggle();
                assertTrue(stages[0].isShowing(), "returning should restore the projection");
            });
        } finally {
            onFx(() -> {
                if (stages[1] != null) stages[1].hide();
                if (stages[0] != null) stages[0].hide();
                if (video[0] != null) video[0].dispose();
            });
            assertTrue(waitFor(() -> activePipelines() == baseline, 5_000));
        }
    }
}
