package org.quelea.windows.main;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.utils.Utils;

/**
 * The panel displaying the preview lyrics selection - this is viewed before
 * displaying the actual lyrics on the projector.
 */
public class SelectPreviewLyricsPanel extends SelectLyricsPanel {

    private final JButton liveButton;

    /**
     * Create a new preview lyrics panel.
     */
    public SelectPreviewLyricsPanel() {
        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Preview</b></html>"));
        header.add(new JToolBar.Separator());
        liveButton = new JButton("Go live", Utils.getImageIcon("icons/2rightarrow.png"));
        header.add(liveButton);
        liveButton.setEnabled(false);
        getLyricsList().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if(getLyricsList().getModel().isEmpty()) {
                    liveButton.setEnabled(false);
                }
                else {
                    liveButton.setEnabled(true);
                }
            }
        });
        add(header, BorderLayout.NORTH);
    }

    /**
     * Get the "go live" button.
     * @return the "go live" button.
     */
    public JButton getLiveButton() {
        return liveButton;
    }

}
