package org.quelea.tags;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.quelea.utils.FadeWindow;
import org.quelea.windows.library.LibrarySongList;

/**
 *
 * @author Michael
 */
public class TagPopupWindow extends FadeWindow {
    
    private class Tag implements Comparable<Tag> {

        private String str;
        private int count;

        public Tag(String str, int count) {
            this.str = str;
            this.count = count;
        }

        @Override
        public int compareTo(Tag o) {
            if (count > o.count) {
                return -1;
            }
            return 1;
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Tag other = (Tag) obj;
            if (!Objects.equals(this.str, other.str)) {
                return false;
            }
            if (this.count != other.count) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.str);
            hash = 59 * hash + this.count;
            return hash;
        }
    }
    
    private static final int MAX_RESULTS = 12;
    private Map<String, Integer> tagMap;

    public TagPopupWindow() {
        setSpeed(0.07f);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        setAlwaysOnTop(true);
    }

    public void setString(final JTextField search, final TagPanel panel, final LibrarySongList list) {
        
        boolean visible = false;
        getContentPane().removeAll();

        Set<Tag> chosenTags = new TreeSet<>();
        for (final String tag : tagMap.keySet()) {
            if (tag.startsWith(search.getText()) && !panel.getTags().contains(tag)) {
                chosenTags.add(new Tag(tag, tagMap.get(tag)));
            }
        }

        Iterator<Tag> iter = chosenTags.iterator();
        for (int i = 0; i < MAX_RESULTS; i++) {
            if (!iter.hasNext()) {
                break;
            }
            final String tag = iter.next().str;
            final JButton button = new JButton(tag + " (x" + tagMap.get(tag) + ")");
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    search.setText("");
                    panel.addTag(tag, list);
                    list.filterByTag(panel.getTags(), false);
                    setVisible(false);
                }
            });
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

    public void setTags(Map<String, Integer> tagMap) {
        this.tagMap = tagMap;
    }
}
