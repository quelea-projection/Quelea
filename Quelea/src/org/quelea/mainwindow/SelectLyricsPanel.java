package org.quelea.mainwindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.display.LyricCanvas;
import org.quelea.display.components.SongSection;

/**
 * The panel where the lyrics for different songs can be selected.
 * @author Michael
 */
public abstract class SelectLyricsPanel extends JPanel {

    private SelectLyricsList lyricsList;
    private LyricCanvas previewCanvas;

    /**
     * Create a new lyrics panel.
     */
    public SelectLyricsPanel() {
        setPreferredSize(new Dimension(300, 600));
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        lyricsList = new SelectLyricsList(new DefaultListModel());
        previewCanvas = new LyricCanvas(4,3);
        splitPane.add(new JScrollPane(lyricsList) {
            {
                setBorder(new EmptyBorder(0, 0, 0, 0));
                setPreferredSize(lyricsList.getPreferredSize());
            }
        });
        splitPane.setOneTouchExpandable(true);
        splitPane.add(previewCanvas);
        add(splitPane, BorderLayout.CENTER);
        lyricsList.registerLyricCanvas(previewCanvas);

    }

    /**
     * Get the underlying lyrics list in this panel.
     * @return the lyrics list.
     */
    public SelectLyricsList getLyricsList() {
        return lyricsList;
    }

    /**
     * Get the underlying preview canvas in this panel.
     * @return the preview canvas.
     */
    public LyricCanvas getLyricCanvas() {
        return previewCanvas;
    }
}
