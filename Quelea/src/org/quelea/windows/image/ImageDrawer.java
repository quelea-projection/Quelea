package org.quelea.windows.image;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * Responsible for drawing an image onto the DisplayCanvas.
 * <p/>
 * @author tomaszpio@gmail.com
 * @author berry120@gmail.com
 */
public class ImageDrawer extends DisplayableDrawer {

    private ImageView imageView;
    private Image image;

    @Override
    public void draw(Displayable displayable) {
        clear();
        if(getCanvas().getPlayVideo()) {
            VLCWindow.INSTANCE.stop();
        }
        if(displayable == null) {
            return;
        }
        image = ((ImageDisplayable) displayable).getImage();
        imageView = getCanvas().getNewImageView();
        imageView.setFitWidth(getCanvas().getWidth());
        imageView.setPreserveRatio(true);
        imageView.setImage(image);
        StackPane imageBox = new StackPane();
        imageBox.getChildren().add(imageView);
        if(getCanvas() != QueleaApp.get().getProjectionWindow().getCanvas()
                && getCanvas() != QueleaApp.get().getStageWindow().getCanvas()) {
            imageBox.setStyle("-fx-background-color:#dddddd;");
        }
        getCanvas().getChildren().add(imageBox);
        getCanvas().setOpacity(1);
    }

    @Override
    public void clear() {
        if(getCanvas().getChildren() != null) {
            getCanvas().clearApartFromNotice();
        }
    }

    @Override
    public void requestFocus() {
        imageView.requestFocus();;
    }
}
