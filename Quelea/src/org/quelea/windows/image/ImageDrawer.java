package org.quelea.windows.image;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.windows.main.DisplayableDrawer;

/**
 *
 * @author tomaszpio@gmail.com
 */
public class ImageDrawer extends DisplayableDrawer {

    private ImageView imageView = null;
    private Image image = null;

    @Override
    public void draw(Displayable displayable) {
        image = ((ImageDisplayable)displayable).getImage();
        imageView = getCanvas().getNewImageView();
        imageView.setFitHeight(getCanvas().getHeight());
        imageView.setFitWidth(getCanvas().getWidth());
        imageView.setImage(image);
        getCanvas().getChildren().add(imageView);
    }

    @Override
    public void clear() {
        if (getCanvas().getChildren() != null) {
            getCanvas().getChildren().clear();
        }
    }

    @Override
    public void requestFocus() {
        imageView.requestFocus();;
    }
}
