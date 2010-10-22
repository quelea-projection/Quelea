package org.quelea.windows.newsong;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.windows.main.LyricCanvas;

/**
 * The colour button where the user selects a colour.
 * @author Michael
 */
public class ImageButton extends JButton {

    private String imageLocation;
    private JFileChooser fileChooser;
    private ColourSelectionWindow selectionWindow;

    /**
     * Create and initialise the colour button.
     * @param defaultColor the default colour of the button.
     */
    public ImageButton(final JTextField imageLocationField, final LyricCanvas canvas) {
        super("Select...");
        selectionWindow = new ColourSelectionWindow(SwingUtilities.getWindowAncestor(this));
        fileChooser = new JLocationFileChooser("img");
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                String suffix = f.getName().split("\\.")[f.getName().split("\\.").length - 1].toLowerCase().trim();
                if(suffix.equals("png")
                        || suffix.equals("bmp")
                        || suffix.equals("tif")
                        || suffix.equals("jpg")
                        || suffix.equals("jpeg")
                        || suffix.equals("gif")) {
                    return true;
                }
                return false;
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
                        ex.printStackTrace();
                    }

                    imageLocation = imageDir.toURI().relativize(newFile.toURI()).getPath();
                    imageLocationField.setText(imageLocation);
                    canvas.setTheme(new Theme(canvas.getTheme().getFont(), canvas.getTheme().getFontColor(), new Background(imageLocation)));
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
