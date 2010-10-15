package org.quelea.mainwindow.components;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import org.quelea.Utils;

/**
 * The panel displaying the live lyrics selection - changes made on this panel
 * are reflected on the live projection.
 * @author Michael
 */
public class SelectLiveLyricsPanel extends SelectLyricsPanel {

    private JToolBar header;
    private JToggleButton black;
    private JToggleButton clear;

    /**
     * Create a new live lyrics panel.
     * @param fullScreenCanvas the full screen canvas that this live window
     * controls.
     */
    public SelectLiveLyricsPanel() {
        header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Live</b></html>"));
        header.add(new JToolBar.Separator());
        black = new JToggleButton(Utils.getImageIcon("icons/black.png"));
        black.setToolTipText("Black screen");
        black.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleBlack();
                    getLyricsList().requestFocus();
                }
            }
        });
        clear = new JToggleButton(Utils.getImageIcon("icons/filenew.png"));
        clear.setToolTipText("Clear text");
        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleClear();
                    getLyricsList().requestFocus();
                }
            }
        });
        header.add(black);
        header.add(clear);
        add(header, BorderLayout.NORTH);
    }
    
}
