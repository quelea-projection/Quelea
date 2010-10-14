package org.quelea.mainwindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.quelea.Utils;
import org.quelea.display.LyricCanvas;
import org.quelea.display.components.SongSection;

/**
 * The panel displaying the live lyrics selection - changes made on this panel
 * are reflected on the live projection.
 * @author Michael
 */
public class LiveLyricsPanel extends SelectLyricsPanel {

    private JToolBar header;
    private JToggleButton black;
    private JToggleButton clear;

    /**
     * Create a new live lyrics panel.
     * @param fullScreenCanvas the full screen canvas that this live window
     * controls.
     */
    public LiveLyricsPanel(LyricCanvas fullScreenCanvas) {
        header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Live</b></html>"));
        header.add(new JToolBar.Separator());
        black = new JToggleButton("Black", Utils.getImageIcon("icons/black.png"));
        black.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleBlack();
                }
            }
        });
        clear = new JToggleButton("Clear text", Utils.getImageIcon("icons/filenew.png"));
        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleClear();
                }
            }
        });
        header.add(black);
        header.add(clear);
        add(header, BorderLayout.NORTH);
        registerLyricCanvas(fullScreenCanvas);
    }

    private void toggleClear() {
        String[] text;
        if(clear.getModel().isSelected()) {
            text = new String[]{};
        }
        else {
            text = ((SongSection) getLyricsList().getSelectedValue()).getLyrics();
        }
        for(LyricCanvas canvas : getCanvases()) {
            canvas.setText(text);
        }
    }
}
