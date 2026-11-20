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
import org.quelea.windows.presentation.powerpoint.PowerPointService;
import org.quelea.windows.style.MediaButton;
import org.quelea.windows.style.QueleaIcon;

/**
 * The presentation controls containing a next and a previous button.
 * <p/>
 *
 * @author Arvid
 */
public class PresentationControls extends StackPane {

    public PresentationControls() {
        Button previousButton = new MediaButton(QueleaIcon.PREVIOUS, 46, false);
        setButtonParams(previousButton);
        previousButton.setTranslateX(-120);
        getChildren().add(previousButton);
        previousButton.setOnMouseClicked((MouseEvent t) -> PowerPointService.get().previous());

        Button nextButton = new MediaButton(QueleaIcon.NEXT, 46, false);
        nextButton.setOnMouseClicked((MouseEvent t) -> PowerPointService.get().next());

        setButtonParams(nextButton);
        nextButton.setTranslateX(120);
        getChildren().add(nextButton);
    }

    private void setButtonParams(final Button button) {
        // Look and hover state: see .media-button in base.css
    }
}
