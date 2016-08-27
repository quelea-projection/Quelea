/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.presentation;

import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.QueleaApp;

/**
 * The presentation controls containing a next and a previous button.
 * <p/>
 * @author Arvid
 */
public class PresentationControls extends StackPane {

    private static final Image PREVIOUS_IMAGE = new Image("file:icons/previous.png");
    private static final Image NEXT_IMAGE = new Image("file:icons/next.png");
    private final Button previousButton;
    private final Button nextButton;

    public PresentationControls() {
        previousButton = new Button("", setImageView(PREVIOUS_IMAGE));
        setButtonParams(previousButton);
        previousButton.setTranslateX(-120);
        getChildren().add(previousButton);
        previousButton.setOnMouseClicked((MouseEvent t) -> {
            PowerPointHandler.gotoPrevious();
            String result = PowerPointHandler.getCurrentSlide();
            if (!result.contains("not running") && !result.equals("")) {
                int i = Integer.parseInt(result);
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getPresentationPanel().getPresentationPreview().select(i, true);
            }
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
                    if (lp.getBlacked() && !PowerPointHandler.screenStatus().equals("3")) {
                        lp.setBlacked(false);
                    }
        });

        nextButton = new Button("", setImageView(NEXT_IMAGE));
        nextButton.setOnMouseClicked((MouseEvent t) -> {
            PowerPointHandler.gotoNext();
            String result = PowerPointHandler.getCurrentSlide();
            if (!result.contains("not running") && !result.equals("")) {
                int i = Integer.parseInt(result);
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getPresentationPanel().getPresentationPreview().select(i, true);
            }
            LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
                    if (lp.getBlacked() && !PowerPointHandler.screenStatus().equals("3")) {
                        lp.setBlacked(false);
                    }
        });

        setButtonParams(nextButton);
        nextButton.setTranslateX(120);
        getChildren().add(nextButton);
    }

    private void setButtonParams(final Button button) {
        button.setOnMouseEntered((MouseEvent t) -> {
            button.setEffect(new Glow(0.5));
        });
        button.setOnMouseExited((MouseEvent t) -> {
            button.setEffect(null);
        });
        button.setPrefWidth(200);
    }

    private ImageView setImageView(Image image) {
        ImageView iv2 = new ImageView();
        iv2.setImage(image);
        iv2.setPreserveRatio(true);
        iv2.setSmooth(true);
        iv2.setCache(true);
        return iv2;
    }
}
