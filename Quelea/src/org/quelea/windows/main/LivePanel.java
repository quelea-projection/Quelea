package org.quelea.windows.main;

import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The panel displaying the live lyrics selection - changes made on this panel are reflected on the live projection.
 * @author Michael
 */
public class LivePanel extends LivePreviewPanel {

    private final JToggleButton black;
    private final JToggleButton clear;
    private final JToggleButton hide;

    /**
     * Create a new live lyrics panel.
     * @param fullScreenCanvas the full screen canvas that this live window controls.
     */
    public LivePanel() {
        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Live</b></html>"));
        header.add(new JToolBar.Separator());
        black = new JToggleButton(Utils.getImageIcon("icons/black.png"));
        black.setToolTipText("Black screen (F1)");
        black.setRequestFocusEnabled(false);
        black.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleBlack();
                }
            }
        });
        header.add(black);
        clear = new JToggleButton(Utils.getImageIcon("icons/filenew.png"));
        clear.setToolTipText("Clear text (F2)");
        clear.setRequestFocusEnabled(false);
        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for(LyricCanvas canvas : getCanvases()) {
                    canvas.toggleClear();
                }
            }
        });
        header.add(clear);
        hide = new JToggleButton(Utils.getImageIcon("icons/cross.png"));
        hide.setToolTipText("Hide display output (F3)");
        hide.setRequestFocusEnabled(false);
        hide.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(QueleaProperties.get().getProjectorScreen() == -1) {
                    return;
                }
                for(LyricWindow window : getWindows()) {
                    window.setVisible(!window.isVisible());
                }
            }
        });
        header.add(hide);
        add(header, BorderLayout.NORTH);

        addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                //Nothing needed here
            }

            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_F1) {
                    black.doClick();
                }
                else if(e.getKeyCode() == KeyEvent.VK_F2) {
                    clear.doClick();
                }
                else if(e.getKeyCode() == KeyEvent.VK_F3) {
                    hide.doClick();
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });
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
