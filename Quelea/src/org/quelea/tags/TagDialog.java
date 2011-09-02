package org.quelea.tags;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
    
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private Map<String, Integer> tags;
    private JTextField tagField;
    private TagPanel tagPanel;
    private LibrarySongList list;
    private TagPopupWindow popup;
    
    public TagDialog() {
        super(Application.get().getMainWindow(), "Filter by tag", ModalityType.DOCUMENT_MODAL);
        tags = new HashMap<>();
        reloadTags();
        popup = new TagPopupWindow();
        popup.setTags(tags);
        list = new LibrarySongList();
        tagPanel = new TagPanel();
        tagField = new JTextField(20);
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
        addComponentListener(new ComponentAdapter() {
            
            @Override
            public void componentMoved(ComponentEvent e) {
                if (popup.isVisible()) {
                    popup.setLocation((int) tagField.getLocationOnScreen().getX(), (int) tagField.getLocationOnScreen().getY() + tagField.getHeight());
                }
            }
        });
        tagField.addFocusListener(new FocusListener() {
            
            @Override
            public void focusGained(FocusEvent e) {
                popup.setString(tagField, tagPanel, list);
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
        setLayout(new BorderLayout());
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(new JLabel("Tags:"));
        textPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        textPanel.add(tagField);
        northPanel.add(textPanel);
        northPanel.add(tagPanel);
        add(northPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(500, 500));
        add(scroll, BorderLayout.CENTER);
        pack();
    }
    
    public final void reloadTags() {
        LOGGER.log(Level.INFO, "Reloading tags");
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
