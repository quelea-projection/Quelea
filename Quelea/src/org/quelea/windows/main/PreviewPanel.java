package org.quelea.windows.main;

import org.quelea.utils.Utils;

import javax.swing.*;
import java.awt.*;
import org.quelea.displayable.Displayable;

/**
 * The panel displaying the preview lyrics selection - this is viewed before displaying the actual lyrics on the
 * projector.
 */
public class PreviewPanel extends LivePreviewPanel {

    private final JButton liveButton;

    /**
     * Create a new preview lyrics panel.
     */
    public PreviewPanel() {
        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Preview</b></html>"));
        header.add(new JToolBar.Separator());
        liveButton = new JButton("Go live", Utils.getImageIcon("icons/2rightarrow.png"));
        liveButton.setToolTipText("Go live (space)");
        liveButton.setRequestFocusEnabled(false);
        header.add(liveButton);
        liveButton.setEnabled(false);
        add(header, BorderLayout.NORTH);
    }

    /**
     * Set the given displayable to be shown on the panel.
     * @param d the displayable to show.
     * @param index an index that may be used or ignored depending on the
     * displayable.
     */
    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        liveButton.setEnabled(true);
    }

    /**
     * Clear the preview panel.
     */
    @Override
    public void clear() {
        super.clear();
        liveButton.setEnabled(false);
    }

    /**
     * Get the "go live" button.
     * @return the "go live" button.
     */
    public JButton getLiveButton() {
        return liveButton;
    }

}
