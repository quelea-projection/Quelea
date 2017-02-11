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
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.DisplayableDrawer;

/**
 *
 * @author Arvid
 */
public class PresentationDrawer extends DisplayableDrawer {


    public PresentationDrawer(PresentationControls controlPanel) {
    }

    @Override
    public void draw(Displayable displayable) {
        if(getCanvas().isStageView()) {
            ImageView imageView = getCanvas().getNewImageView();
            imageView.setImage(Utils.getImageFromColour(QueleaProperties.get().getStageBackgroundColor()));
            getCanvas().getChildren().add(0, imageView);
        }
        else {
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
