package org.quelea.windows.multimedia;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.data.displayable.MultimediaDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.image.ImageDrawer;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;

/**
 *
 * @author tomaszpio@gmail.com, Michael
 */
public class MultimediaDrawer extends DisplayableDrawer {

    private final MultimediaControls controlPanel;
    private boolean playVideo;

    public MultimediaDrawer(MultimediaControls controlPanel) {
        this.controlPanel = controlPanel;
    }

    @Override
    public void draw(Displayable displayable) {
        if (getCanvas().isStageView()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);

            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    
                        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getVideoPanel().updatePreview(getCanvas().getPreviewCanvas());
                  

                }
            });

            if (QueleaProperties.get().getStageDrawImages()) {
                Slider updateSlider = new Slider(0, 1, 0);
                final Label updateLabel = new Label("");
                controlPanel.setPreviewSlider(updateSlider);
                controlPanel.setPreviewLabel(updateLabel);
                controlPanel.setVideoName(displayable.getPreviewText());
                updateLabel.setTextFill(QueleaProperties.get().getStageLyricsColor());

                double size = getCanvas().getWidth();
                if (getCanvas().getHeight() < size) {
                    size = getCanvas().getHeight();
                }
                updateLabel.setFont(Font.font("Noto Sans", FontWeight.BOLD, FontPosture.REGULAR, size / 14));

                updateSlider.setStyle("-fx-background-color: " + Utils.getHexFromColor(QueleaProperties.get().getStageBackgroundColor()) + ";");
                VBox children = new VBox();
                VBox.setVgrow(updateLabel, Priority.ALWAYS);
                updateLabel.setAlignment(Pos.CENTER);
                children.setSpacing(20);
                children.getChildren().add(updateLabel);
                children.getChildren().add(updateSlider);

                getCanvas().getChildren().add(1, children);
            }

        } else if (getCanvas().isTextOnlyView()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getTextOnlyUseThemeBackground() ? Color.BLACK : QueleaProperties.get().getTextOnlyBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);
        } else {
            if (playVideo) {
                controlPanel.reset();
                controlPanel.loadMultimedia(((MultimediaDisplayable) displayable).getLocation(), true);
                VLCWindow.INSTANCE.refreshPosition();
                VLCWindow.INSTANCE.show();
            }
        }
    }

    public void setPlayVideo(boolean playVideo) {
        this.playVideo = playVideo;
        if (playVideo) {
            getCanvas().clearNonPermanentChildren();
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public void requestFocus() {
    }
}
