/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.tags;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.quelea.SongDatabase;
import org.quelea.displayable.Song;
import org.quelea.windows.library.LibrarySongList;

/**
 * The panel used for entering tags and displaying those that have been entered.
 * @author Michael
 */
public class TagEntryPanel extends JPanel {

    private JTextField tagField;
    private Map<String, Integer> tags;
    private TagPanel tagPanel;
    private TagPopupWindow popup;

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
        setLayout(new BorderLayout());
        tagPanel = new TagPanel();
        tagField = new JTextField(20);
        tagField.setText("<Type a tag name here>");
        tagField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                tagField.setText("");
                removeFocusListener(this);
            }
        });
        tagField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                    popup.clickFirst();
                }
            }
        });
        tags = new HashMap<>();
        popup = new TagPopupWindow(includeUserText);
        addAncestorListener(new AncestorListener() {

            private ComponentAdapter adapter = new ComponentAdapter() {

                @Override
                public void componentMoved(ComponentEvent e) {
                    if (popup.isVisible() && tagField.isVisible()) {
                        try {
                            popup.setLocation((int) tagField.getLocationOnScreen().getX(), (int) tagField.getLocationOnScreen().getY() + tagField.getHeight());
                        }
                        catch (IllegalComponentStateException ex) {
                            //Never mind...
                        }
                    }
                }
            };

            @Override
            public void ancestorAdded(AncestorEvent event) {
                event.getAncestor().addComponentListener(adapter);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                event.getAncestor().removeComponentListener(adapter);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        popup.setTags(tags);
        tagField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }

            private void check() {
                popup.setLocation((int) tagField.getLocationOnScreen().getX(), (int) tagField.getLocationOnScreen().getY() + tagField.getHeight());
                popup.setString(tagField, tagPanel, list);
            }
        });
        tagField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                popup.setString(tagField, tagPanel, list);
                if (popup.isVisible() && tagField.isVisible()) {
                    try {
                        popup.setLocation((int) tagField.getLocationOnScreen().getX(), (int) tagField.getLocationOnScreen().getY() + tagField.getHeight());
                    }
                    catch (IllegalComponentStateException ex) {
                        //Never mind...
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                popup.setVisible(false);
            }
        });
        tagField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                popup.setString(tagField, tagPanel, list);
            }
        });
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        if (includeLabel) {
            textPanel.add(new JLabel("Tags:"));
            textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        }
        textPanel.add(tagField);
        northPanel.add(textPanel);
        northPanel.add(tagPanel);
        add(northPanel, BorderLayout.NORTH);
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
        for (Song song : SongDatabase.get().getSongs()) {
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
