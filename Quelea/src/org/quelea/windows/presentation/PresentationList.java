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

import java.util.Arrays;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.quelea.data.powerpoint.PresentationSlide;

/**
 * A JList for specifically displaying presentation slides.
 * <p/>
 * @author Michael
 */
public class PresentationList extends ListView<PresentationSlide> {

    /**
     * Create a new presentation list.
     */
    public PresentationList() {
        setCellFactory(new Callback<ListView<PresentationSlide>, ListCell<PresentationSlide>>() {
            @Override
            public ListCell<PresentationSlide> call(ListView<PresentationSlide> p) {
                return new ListCell<PresentationSlide>() {
                    @Override
                    public void updateItem(PresentationSlide item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null) {
                            ImageView iv = new ImageView(item.getImage());
                            iv.setPreserveRatio(true);
                            iv.fitWidthProperty().bind(PresentationList.this.widthProperty());
                            setGraphic(iv);
                        }
                    }
                };
            }
        });
    }

    /**
     * Clear all current slides and set the slides in the list.
     * <p/>
     * @param slides the slides to put in the list.
     */
    public void setSlides(PresentationSlide[] slides) {
        itemsProperty().get().clear();
        itemsProperty().get().addAll(Arrays.asList(slides));
    }
}
