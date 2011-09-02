package org.quelea.tags;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;
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
        list = new LibrarySongList();
        
        setLayout(new BorderLayout());
        tagEntryPanel = new TagEntryPanel(list);
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
