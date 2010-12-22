package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * The panel displaying the live lyrics selection - changes made on this panel
 * are reflected on the live projection.
 * @author Michael
 */
public class SelectLiveLyricsPanel extends SelectLyricsPanel {

    private final JToggleButton black;
    private final JToggleButton clear;
    private final JToggleButton hide;

    /**
     * Create a new live lyrics panel.
     * @param fullScreenCanvas the full screen canvas that this live window
     * controls.
     */
    public SelectLiveLyricsPanel() {
        JToolBar header = new JToolBar();
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
        header.add(black);
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
        header.add(clear);
        hide = new JToggleButton(Utils.getImageIcon("icons/cross.png"));
        hide.setToolTipText("Hide display output");
        hide.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(QueleaProperties.get().getProjectorScreen()==-1) {
                    return;
                }
                for(LyricWindow window : getWindows()) {
                    window.setVisible(!window.isVisible());
                }
            }
        });
        header.add(hide);
        add(header, BorderLayout.NORTH);
    }

    /**
     * Get the "black" toggle button.
     * @return the "black" toggle button.
     */
    public JToggleButton getBlack() {
        return black;
    }

    /**
     * Get the "clear" toggle button.
     * @return the "clear" toggle button.
     */
    public JToggleButton getClear() {
        return clear;
    }

    /**
     * Get the "hide" toggle button.
     * @return the "hide" toggle button.
     */
    public JToggleButton getHide() {
        return hide;
    }

}
