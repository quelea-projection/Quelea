/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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

import javafx.scene.image.ImageView;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageDisplayable;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.image.ImageDrawer;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.QueleaApp;

/**
 * Draws presentations while PowerPoint shows them. The projection shows the
 * slide itself (Quelea's own rendering) until the PowerPoint show is up on
 * the projector screen; then it is left empty, and the projection window is
 * transparent, so PowerPoint shows through while Quelea's logo, black screen
 * and notices can still be drawn over it.
 *
 * @author Arvid
 */
public class PresentationDrawer extends DisplayableDrawer {

    private final ImageDrawer slideDrawer = new ImageDrawer();
    private boolean showSlides = true;

    public PresentationDrawer(PresentationControls controlPanel) {
    }

    /**
     * @param showSlides true to draw the slides on the projection, false to
     * leave it empty for the PowerPoint show underneath.
     */
    public void setShowSlides(boolean showSlides) {
        this.showSlides = showSlides;
    }

    @Override
    public void draw(Displayable displayable) {
        if(getCanvas().isStageView()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);
        }
        else if(!showSlides && QueleaApp.get().getProjectionWindow() != null
                && getCanvas() == QueleaApp.get().getProjectionWindow().getCanvas()) {
            getCanvas().clearNonPermanentChildren();
        }
        else if(displayable instanceof ImageDisplayable) {
            slideDrawer.setCanvas(getCanvas());
            slideDrawer.draw(displayable);
        }
    }

    public void setPlayVideo(boolean playVideo) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void requestFocus() {
    }
}
