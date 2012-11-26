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

import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.quelea.data.db.SongManager;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.library.LibrarySongList;

/**
 * The panel used for entering tags and displaying those that have been entered.
 * @author Michael
 */
public class TagEntryPanel extends BorderPane {

    private TextField tagField;
    private Map<String, Integer> tags;
    private TagPanel tagPanel;
    private TagPopupWindow popup; //TODO: popup window

    /**
     * Create a new tag entry panel.
     * @param list the song list currently in use.
     * @param includeUserText true if the user's current text should be available
     * as a tag option even if no tags currently exist with that name, false
     * otherwise.
     * @param includeLabel true if we should include the "tags: " label, false
     * otherwise.
     */
    public TagEntryPanel(final LibrarySongList list, boolean includeUserText, boolean includeLabel) {
        tagPanel = new TagPanel();
        tagField = new TextField();
        tags = new HashMap<>();
//        popup = new TagPopupWindow(includeUserText);
//        popup.setTags(tags);
//        tagField.getDocument().addDocumentListener(new DocumentListener() {
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                check();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                check();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                check();
//            }
//
//            private void check() {
//                popup.setLocation((int) tagField.getLocationOnScreen().getX(), (int) tagField.getLocationOnScreen().getY() + tagField.getHeight());
//                popup.setString(tagField, tagPanel, list);
//            }
//        });
//        tagField.addFocusListener(new FocusListener() {
//
//            @Override
//            public void focusGained(FocusEvent e) {
//                popup.setString(tagField, tagPanel, list);
//                if (popup.isVisible() && tagField.isVisible()) {
//                    try {
//                        popup.setLocation((int) tagField.getLocationOnScreen().getX(), (int) tagField.getLocationOnScreen().getY() + tagField.getHeight());
//                    }
//                    catch (IllegalComponentStateException ex) {
//                        //Never mind...
//                    }
//                }
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                popup.setVisible(false);
//            }
//        });
//        tagField.addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                popup.setString(tagField, tagPanel, list);
//            }
//        });
        VBox northPanel = new VBox();
        HBox textPanel = new HBox();
        if (includeLabel) {
            textPanel.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("tags.colon.label")));
        }
        textPanel.getChildren().add(tagField);
        northPanel.getChildren().add(textPanel);
        northPanel.getChildren().add(tagPanel);
        setTop(northPanel);
    }

    /**
     * Remove all the current tags.
     */
    public void removeTags() {
        tagPanel.removeTags();
    }

    /**
     * Reload the tags and then set the tags to the given string of tags.
     * @param tags semi-colon delimited list of tags.
     */
    public void setTags(String tags) {
        reloadTags();
        tagPanel.setTags(tags);
    }

    /**
     * Get the list of currently used tags as a semi-colon delimited string.
     * @return a string containing all the tags.
     */
    public String getTagsAsString() {
        return tagPanel.getTagsAsString();
    }

    /**
     * Clear the tags then reload them all from the database.
     */
    public final void reloadTags() {
        tags.clear();
        for (SongDisplayable song : SongManager.get().getSongs()) {
            for (String tag : song.getTags()) {
                tag = tag.trim();
                if (tag.isEmpty()) {
                    continue;
                }
                if (tags.get(tag.toLowerCase()) == null) {
                    tags.put(tag.toLowerCase(), 1);
                }
                else {
                    tags.put(tag.toLowerCase(), tags.get(tag.toLowerCase()) + 1);
                }
            }
        }
    }
}
