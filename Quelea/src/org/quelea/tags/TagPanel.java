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

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.quelea.utils.Utils;
import org.quelea.utils.WrapLayout;
import org.quelea.windows.library.LibrarySongList;

/**
 *
 * @author Michael
 */
public class TagPanel extends JPanel {

    private Set<String> tags;

    public TagPanel() {
        setLayout(new WrapLayout(FlowLayout.LEFT));
        tags = new HashSet<>();
    }

    public void addTag(final String tag, final LibrarySongList list) {
        tags.add(tag);
        final JPanel tagPanel = new JPanel();
        tagPanel.setBorder(new LineBorder(Color.BLACK, 2));
        tagPanel.add(new JLabel(tag));
        final JButton button = new JButton(Utils.getImageIcon("icons/delete.png", 10, 10));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Container ancestor = button.getTopLevelAncestor();
                tags.remove(tag);
                remove(tagPanel);
                redo(ancestor);
                if (list != null) {
                    list.filterByTag(getTags(), false);
                }
            }
        });
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        tagPanel.add(button);
        add(tagPanel);
        redo(button.getTopLevelAncestor());
    }

    private void redo(Container ancestor) {
        validate();
        repaint();
        ((JDialog) ancestor).validate();
        ((JDialog) ancestor).repaint();
    }

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

    public List<String> getTags() {
        List<String> ret = new ArrayList<>();
        ret.addAll(tags);
        return ret;
    }

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

    public void removeTags() {
        tags.clear();
        removeAll();
    }
}
