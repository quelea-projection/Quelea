package org.quelea.windows.newsong;

import org.apache.commons.io.FileUtils;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.utils.LoggerUtils;
import org.quelea.windows.main.LyricCanvas;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.Utils;

/**
 * The colour button where the user selects a colour.
 * @author Michael
 */
public class ImageButton extends JButton {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private String imageLocation;
    private final JFileChooser fileChooser;

    /**
     * Create and initialise the colour button.
     * @param defaultColor the default colour of the button.
     */
    public ImageButton(final JTextField imageLocationField, final LyricCanvas canvas) {
        super("Select...");
        fileChooser = new JLocationFileChooser("img");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return Utils.fileIsImage(f);
            }

            @Override
            public String getDescription() {
                return "Image files";
            }
        });
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int ret = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(ImageButton.this));
                if(ret == JFileChooser.APPROVE_OPTION) {
                    File imageDir = new File("img");
                    File selectedFile = fileChooser.getSelectedFile();
                    File newFile = new File(imageDir, selectedFile.getName());
                    try {
                        if(!selectedFile.getCanonicalPath().startsWith(imageDir.getCanonicalPath())) {
                            FileUtils.copyFile(selectedFile, newFile);
                        }
                    }
                    catch(IOException ex) {
                        LOGGER.log(Level.WARNING, "", ex);
                    }

                    imageLocation = imageDir.toURI().relativize(newFile.toURI()).getPath();
                    imageLocationField.setText(imageLocation);
                    canvas.setTheme(new Theme(canvas.getTheme().getFont(), canvas.getTheme().getFontColor(), new Background(imageLocation, null)));
                }
            }
        });
    }

    /**
     * Get the location of the selected image.
     * @return the selected image location.
     */
    public String getImageLocation() {
        return imageLocation;
    }
}
