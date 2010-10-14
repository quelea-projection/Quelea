package org.quelea;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.quelea.display.LyricWindow;
import org.quelea.display.components.SongSection;
import org.quelea.mainwindow.MainWindow;

/**
 * The main class, sets everything in motion...
 * @author Michael
 */
public class Main {

    /**
     * Don't instantiate me. I bite.
     */
    private Main() {
        throw new AssertionError();
    }

    /**
     * Go go go!
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();

        final LyricWindow fullScreenWindow;
        if(gds.length > 1) {
            fullScreenWindow = new LyricWindow(gds[1].getDefaultConfiguration().getBounds());
        }
        else {
            fullScreenWindow = null;
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
//                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch(UnsupportedLookAndFeelException ex) {
                    //Oh well...
                }
//                catch(ClassNotFoundException ex) {}
//                catch(InstantiationException ex) {}
//                catch(IllegalAccessException ex) {}
                
                JFrame.setDefaultLookAndFeelDecorated(true);
                final MainWindow mainWindow;
                if(fullScreenWindow == null) {
                    mainWindow = new MainWindow();
                    JOptionPane.showMessageDialog(null, "Looks like you've only got one monitor installed. I can't display the full screen canvas in this setup.");
                }
                else {
                    mainWindow = new MainWindow(fullScreenWindow.getCanvas());
                    mainWindow.getLiveLyricsList().addListSelectionListener(new ListSelectionListener() {

                        public void valueChanged(ListSelectionEvent e) {
                            if(mainWindow.getLiveLyricsList().getSelectedIndex() != -1) {
                                fullScreenWindow.getCanvas().setText(((SongSection) mainWindow.getLiveLyricsList().getSelectedValue()).getLyrics());
                            }
                        }
                    });
                    fullScreenWindow.setVisible(true);
                }
                mainWindow.setLocation((int) gds[0].getDefaultConfiguration().getBounds().getMinX() + 100, (int) gds[0].getDefaultConfiguration().getBounds().getMinY() + 100);
                mainWindow.setVisible(true);

            }
        });
    }
}
