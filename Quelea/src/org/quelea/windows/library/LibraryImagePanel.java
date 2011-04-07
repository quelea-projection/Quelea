package org.quelea.windows.library;

import java.awt.BorderLayout;
import javax.swing.*;

/**
 * The image panel in the library.
 * @author Michael
 */
public class LibraryImagePanel extends JPanel {

    private final ImageListPanel imagePanel;

    public LibraryImagePanel() {
        setLayout(new BorderLayout());
        imagePanel = new ImageListPanel("img");
        add(imagePanel, BorderLayout.CENTER);
    }

    /**
     * Get the image list panel.
     * @return the image list panel.
     */
    public ImageListPanel getImagePanel() {
        return imagePanel;
    }

}
