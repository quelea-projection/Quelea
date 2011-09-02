package org.quelea.tags;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import org.quelea.Application;
import org.quelea.windows.library.LibrarySongList;

/**
 * A dialog used for finding songs with certain tags.
 * @author Michael
 */
public class TagDialog extends JDialog {

    private TagEntryPanel tagEntryPanel;
    private LibrarySongList list;

    public TagDialog() {
        super(Application.get().getMainWindow(), "Filter by tag", ModalityType.MODELESS);
        list = new LibrarySongList(false);
        
        setLayout(new BorderLayout());
        tagEntryPanel = new TagEntryPanel(list, false, true);
        add(tagEntryPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(500, 500));
        add(scroll, BorderLayout.CENTER);
        pack();
    }
    
    public void reloadTags() {
        tagEntryPanel.reloadTags();
    }

    @Override
    public void setVisible(boolean visible) {
        reloadTags();
        setLocationRelativeTo(getParent());
        super.setVisible(visible);
    }

    public static void main(String[] args) {
        TagDialog tagDialog = new TagDialog();
        tagDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        tagDialog.setVisible(true);
    }
}
