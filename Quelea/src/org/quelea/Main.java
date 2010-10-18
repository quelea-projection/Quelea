package org.quelea;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.quelea.display.Song;
import org.quelea.mainwindow.components.LyricWindow;
import org.quelea.mainwindow.components.MainWindow;

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
    public static void main(String[] args) {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();

        final LyricWindow fullScreenWindow;
        if(gds.length > 1) {
            fullScreenWindow = new LyricWindow(gds[1].getDefaultConfiguration().getBounds());
        }
        else {
            fullScreenWindow = null;
        }

        SongDatabase database = null;
        try {
            database = new SongDatabase("queleadatabase.zip");
        }
        catch(IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't load the database.");
        }
        final Song[] songs = database.getSongs();;

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
                }
                catch(UnsupportedLookAndFeelException ex) {
                    //Oh well...
                }
                
                JFrame.setDefaultLookAndFeelDecorated(true);
                final MainWindow mainWindow = new MainWindow();
                DefaultListModel model = (DefaultListModel)mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getModel();
                for(Song song : songs) {
                    model.addElement(song);
                }
                mainWindow.setLocation((int) gds[0].getDefaultConfiguration().getBounds().getMinX() + 100, (int) gds[0].getDefaultConfiguration().getBounds().getMinY() + 100);
                mainWindow.setVisible(true);

                if(fullScreenWindow == null) {
                    JOptionPane.showMessageDialog(mainWindow, "Looks like you've only got one monitor installed. I can't display the full screen canvas in this setup.");
                }
                else {
                    mainWindow.getMainPanel().getLiveLyricsPanel().registerLyricCanvas(fullScreenWindow.getCanvas());
                    fullScreenWindow.setVisible(true);
                }
            }
        });
    }
}
