package org.quelea.windows.newsong;

import javax.swing.*;
import java.awt.*;

/**
 * A chooser that sets the location to the absolute location of the parent.
 * @author Michael
 */
public class JLocationFileChooser extends JFileChooser {

    /**
     * Create a file chooser pointing to the default location.
     */
    public JLocationFileChooser() {
        super();
    }

    /**
     * Create the file chooser pointed to a specified folder.
     * @param folder the folder to point to.
     */
    public JLocationFileChooser(String folder) {
        super(folder);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setLocation((int) parent.getLocationOnScreen().getX(), (int) parent.getLocationOnScreen().getY());
        return dialog;
    }

}
