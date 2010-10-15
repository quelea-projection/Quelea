package org.quelea.mainwindow.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.display.SongSection;

/**
 * The panel where the lyrics for different songs can be selected.
 * @author Michael
 */
public abstract class SelectLyricsPanel extends JPanel {

    private SelectLyricsList lyricsList;
    private Set<LyricCanvas> canvases = new HashSet<LyricCanvas>();

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
     * Register a lyric canvas with this lyrics list.
     * @param canvas the canvas to register.
     */
    public final void registerLyricCanvas(final LyricCanvas canvas) {
        if(canvas == null) {
            return;
        }
        getLyricsList().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int selectedIndex = getLyricsList().getSelectedIndex();
                if(selectedIndex == -1) {
                    return;
                }
                SongSection currentSection = (SongSection) getLyricsList().getModel().getElementAt(selectedIndex);
                canvas.setBackground(currentSection.getBackground());
                canvas.setText((currentSection).getLyrics());
            }
        });
        canvases.add(canvas);
    }

    /**
     * Get the canvases registered to this panel.
     * @return the preview canvas.
     */
    public Set<LyricCanvas> getCanvases() {
        return canvases;
    }
}
