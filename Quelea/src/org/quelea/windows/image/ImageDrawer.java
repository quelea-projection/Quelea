package org.quelea.windows.image;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.windows.lyrics.SelectLyricsList;
import org.quelea.windows.main.DisplayableDrawer;

/**
 *
 * @author tomaszpio@gmail.com
 */
public class ImageDrawer extends DisplayableDrawer {

    private ImageView imageView = null;
    private final Image image;

    public ImageDrawer(ImageDisplayable displayable) {
        image = displayable.getImage();

    }

    @Override
    public void draw() {
        imageView = canvas.getNewImageView();
        imageView.setFitHeight(canvas.getHeight());
        imageView.setFitWidth(canvas.getWidth());
        imageView.setImage(image);
        canvas.getChildren().add(imageView);
    }

    @Override
    public void clear() {
        canvas.getChildren().clear();
    }

    public void requestFocus() {
        imageView.requestFocus();;
    }
}
