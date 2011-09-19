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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.quelea.utils.FadeWindow;
import org.quelea.utils.LoggerUtils;
import org.quelea.windows.library.LibrarySongList;

/**
 * A popup window that displays the tags suggested based on what the user
 * has typed in.
 * @author Michael
 */
public class TagPopupWindow extends FadeWindow {

    /**
     * Represents a tag displayed in the popup window. This is not an external
     * class since it relies on a bit of a bodged method to sort the tags into
     * the order we want them to be in.
     */
    private class Tag implements Comparable<Tag> {

        private String str;
        private int count;

        /**
         * Create a new tag.
         * @param str the tag name.
         * @param count the number of times the tag has been used.
         */
        public Tag(String str, int count) {
            this.str = str;
            this.count = count;
        }

        /**
         * Compare this tag to another tag.
         * @param o the other tag.
         * @return -1 if this tag is "less than" another and 1 if it's greater 
         * than the other. We never return 0 here because that seems to break
         * things and we don't care about equality anyway.
         */
        @Override
        public int compareTo(Tag o) { //Bodged method but does what we need!
            if (count == 0) {
                return -1; //If there's a new one should always appear on top
            }
            if (o.count == 0) {
                return 1;
            }
            if (count > o.count) {
                return -1;
            }
            return 1;
            //Don't care about equal ones (in fact this breaks things)
        }

        /**
         * Determine whether this tag equals another object.
         * @param obj the other object.
         * @return true if they're equal, false otherwise.
         */
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Tag other = (Tag) obj;
            if (!this.str.trim().equalsIgnoreCase(other.str.trim())) {
                return false;
            }
            return true;
        }

        /**
         * Get a hashcode for this tag.
         * @return the tag's hashcode.
         */
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.str);
            return hash;
        }
    }
    
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static final int MAX_RESULTS = 12;
    private Map<String, Integer> tagMap;
    private boolean includeUserText;
    private JButton firstButton;

    /**
     * Create a new tag popup window.
     * @param includeUserText true if user text should be included in the tag
     * suggestions, false otherwise.
     */
    public TagPopupWindow(final boolean includeUserText) {
        this.includeUserText = includeUserText;
        setSpeed(0.07f);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        setAlwaysOnTop(true);
    }

    /**
     * Refresh the tags displayed in this popup window.
     * @param search the text field where the user enters their search criteria.
     * @param panel the panel where the tags are displayed.
     * @param list the song list currently in use.
     */
    public void setString(final JTextField search, final TagPanel panel, final LibrarySongList list) {

        boolean visible = false;
        getContentPane().removeAll();

        Set<Tag> chosenTags = new TreeSet<>();
        for (final String tag : tagMap.keySet()) {
            if (tag.startsWith(search.getText()) && !panel.getTags().contains(tag.trim()) && (!includeUserText || !search.getText().trim().equalsIgnoreCase(tag.trim()))) {
                chosenTags.add(new Tag(tag.trim(), tagMap.get(tag)));
            }
        }
        if (includeUserText && search.getText() != null && !search.getText().trim().isEmpty() && !chosenTags.contains(new Tag(search.getText().toLowerCase().trim(), 0))) {
            chosenTags.add(new Tag(search.getText().toLowerCase().trim(), 0));
        }

        firstButton = null;
        Iterator<Tag> iter = chosenTags.iterator();
        for (int i = 0; i < MAX_RESULTS; i++) {
            if (!iter.hasNext()) {
                break;
            }
            final String tag = iter.next().str;
            Integer num = tagMap.get(tag);
            if (num == null) {
                num = 0;
            }
            final JButton button = new JButton(tag + " (x" + num + ")");
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    search.setText("");
                    panel.addTag(tag, list);
                    if (list != null) {
                        list.filterByTag(panel.getTags());
                    }
                    setVisible(false);
                }
            });
            if(firstButton==null) {
                firstButton = button;
            }
            add(button);
            add(Box.createRigidArea(new Dimension(0, 5)));
            visible = true;
        }

        pack();
        validate();
        repaint();
        setVisible(visible);
        toFront();
    }

    /**
     * Click the first button in the window if there is one.
     */
    public void clickFirst() {
        if(firstButton != null) {
            firstButton.doClick();
        }
    }

    /**
     * Set the tag map to the given tag map.
     * @param tagMap the tag map to use.
     */
    public void setTags(Map<String, Integer> tagMap) {
        this.tagMap = tagMap;
    }
}
