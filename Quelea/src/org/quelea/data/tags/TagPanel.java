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
package org.quelea.data.tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.quelea.windows.library.LibrarySongList;

/**
 * The panel where the tags are displayed.
 * @author Michael
 */
public class TagPanel extends HBox {

    private Set<String> tags;

    /**
     * Create a new tag panel.
     */
    public TagPanel() {
        tags = new HashSet<>();
    }

    /**
     * Add a tag to the panel.
     * @param tag the tag text to add.
     * @param list the library song list in use.
     */
    public void addTag(final String tag, final LibrarySongList list) {
        tags.add(tag);
        final HBox tagPanel = new HBox();
        tagPanel.getChildren().add(new Label(tag));
        final Button button = new Button("",new ImageView(new Image("file:icons/delete.png", 10, 10, false, true)));
        button.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                tags.remove(tag);
                getChildren().remove(tagPanel);
                if(list != null) {
                    list.filterByTag(getTags());
                }
            }
        });
        tagPanel.getChildren().add(button);
        getChildren().add(tagPanel);
    }

    /**
     * Set the tags on this panel to a certain list of tags.
     * @param tags a semi-colon delimited list of tags.
     */
    public void setTags(String tags) {
        removeTags();
        if(tags.trim().isEmpty()) {
            return;
        }
        for (String tag : tags.split(";")) {
            addTag(tag.trim(), null);
            this.tags.add(tag.trim());
        }
    }

    /**
     * Get all the tags in use on this panel as a list.
     * @return all the tags in use as a list.
     */
    public List<String> getTags() {
        List<String> ret = new ArrayList<>();
        ret.addAll(tags);
        return ret;
    }

    /**
     * Get all the tags in use on this panel as a semi-colon delimited string.
     * @return all the tags in use as a semi-colon delimited string.
     */
    public String getTagsAsString() {
        StringBuilder ret = new StringBuilder();
        for (String str : getTags()) {
            ret.append(str).append(";");
        }
        if(ret.length()==0) {
            return "";
        }
        return ret.subSequence(0, ret.length() - 1).toString();
    }

    /**
     * Remove all the tags on this panel.
     */
    public void removeTags() {
        tags.clear();
        getChildren().clear();
    }
}
