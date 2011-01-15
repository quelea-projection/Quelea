package org.quelea.windows.main;

import org.quelea.displayable.TextSection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The panel where the lyrics for different songs can be selected.
 * @author Michael
 */
public abstract class SelectLyricsPanel extends JPanel {

    private final SelectLyricsList lyricsList;
    private final Set<LyricCanvas> canvases = new HashSet<LyricCanvas>();
    private final Set<LyricWindow> windows = new HashSet<LyricWindow>();

    /**
     * Create a new lyrics panel.
     */
    public SelectLyricsPanel() {
        setPreferredSize(new Dimension(300, 600));
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        lyricsList = new SelectLyricsList(new DefaultListModel());
        LyricCanvas previewCanvas = new LyricCanvas(4, 3);
        splitPane.add(new JScrollPane(lyricsList) {

            {
                setBorder(new EmptyBorder(0, 0, 0, 0));
                setPreferredSize(lyricsList.getPreferredSize());
            }
        });
        splitPane.setOneTouchExpandable(true);
        splitPane.add(previewCanvas);
        add(splitPane, BorderLayout.CENTER);
        registerLyricCanvas(previewCanvas);

    }

    /**
     * Get the underlying lyrics list in this panel.
     * @return the lyrics list.
     */
    public SelectLyricsList getLyricsList() {
        return lyricsList;
    }

    /**
     * Register a lyric canvas with this lyrics panel.
     * @param canvas the canvas to register.
     */
    public final void registerLyricCanvas(final LyricCanvas canvas) {
        if(canvas == null) {
            return;
        }
        getLyricsList().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                updateCanvas(canvas);
            }
        });
        getLyricsList().getModel().addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                updateCanvas(canvas);
            }

            public void intervalRemoved(ListDataEvent e) {
                updateCanvas(canvas);
            }

            public void contentsChanged(ListDataEvent e) {
                updateCanvas(canvas);
            }
        });
        canvases.add(canvas);
    }

    /**
     * Called to update the contents of the canvases when the list selection changes.
     */
    private void updateCanvas(final LyricCanvas canvas) {
        int selectedIndex = getLyricsList().getSelectedIndex();
        if(selectedIndex == -1 || selectedIndex >= getLyricsList().getModel().getSize()) {
            canvas.setTheme(null);
            canvas.setText(null);
            return;
        }
        TextSection currentSection = (TextSection) getLyricsList().getModel().getElementAt(selectedIndex);
        canvas.setTheme(currentSection.getTheme());
        canvas.setCapitaliseFirst(currentSection.shouldCapitaliseFirst());
        canvas.setText(currentSection.getText());
    }

    /**
     * Register a lyric window with this lyrics panel.
     * @param window the window to register.
     */
    public final void registerLyricWindow(final LyricWindow window) {
        if(window == null) {
            return;
        }
        windows.add(window);
    }

    /**
     * Get the canvases registered to this panel.
     * @return the canvases.
     */
    public Set<LyricCanvas> getCanvases() {
        return canvases;
    }

    /**
     * Get the windows registered to this panel.
     * @return the windows.
     */
    public Set<LyricWindow> getWindows() {
        return windows;
    }
}
