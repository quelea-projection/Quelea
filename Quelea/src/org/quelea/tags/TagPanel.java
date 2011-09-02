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
import org.apache.xmlbeans.impl.xb.xsdschema.LengthDocument;
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
