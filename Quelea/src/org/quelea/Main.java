package org.quelea;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;
import org.quelea.display.Song;
import org.quelea.display.SongSection;
import org.quelea.windows.main.LibrarySongPanel;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * The main class, sets everything in motion...
 * @author Michael
 */
public final class Main {

    private static MainWindow mainWindow;
    private static LyricWindow fullScreenWindow;
    private static SongDatabase database;

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

        if(gds.length > 1) {
            fullScreenWindow = new LyricWindow(gds[1].getDefaultConfiguration().getBounds());
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setLaf();
                try {
                    database = new SongDatabase();
                }
                catch(SQLException ex) {
                    JOptionPane.showMessageDialog(null, "It looks like you already have an instance of Quelea running, make sure you close all instances before running the program.", "Already running", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                mainWindow = new MainWindow();
                addDBSongs();

                addNewSongWindowListeners();

                addSongPanelListeners();

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

    /**
     * Add the required action listeners to the song panel.
     */
    private static void addSongPanelListeners() {
        final LibrarySongPanel songPanel = mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel();
        songPanel.getRemoveButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Song song = (Song) songPanel.getSongList().getSelectedValue();
                if(song == null) {
                    return;
                }
                int confirmResult = JOptionPane.showConfirmDialog(mainWindow, "Really remove " + song.getTitle() + " from the database? This action cannnot be undone.", "Confirm remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if(confirmResult == JOptionPane.NO_OPTION) {
                    return;
                }
                if(!database.removeSong(song)) {
                    //Error
                }
                SortedListModel model = (SortedListModel) songPanel.getSongList().getModel();
                model.removeElement(song);
            }
        });
    }

    /**
     * Add the required action listeners to the buttons on the new song window.
     */
    private static void addNewSongWindowListeners() {
        final SongEntryWindow songEntryWindow = mainWindow.getNewSongWindow();
        songEntryWindow.getConfirmButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                Song song = songEntryWindow.getSong();
                song.setLyrics(songEntryWindow.getBasicSongPanel().getLyricsField().getText());
                for(SongSection section : song.getSections()) {
                    section.setTheme(songEntryWindow.getTheme());
                }
                SortedListModel model = (SortedListModel) mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getModel();
                model.removeElement(song);
                if(!database.updateSong(song)) {
                    //Error
                }
                songEntryWindow.setVisible(false);
                model.add(song);
            }
        });
    }

    /**
     * Add the songs to the GUI from the database.
     */
    private static void addDBSongs() {
        SortedListModel model = (SortedListModel) mainWindow.getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList().getModel();
        for(Song song : database.getSongs()) {
            model.add(song);
        }
    }

    /**
     * Attempt to set the look and feel of the components.
     */
    private static void setLaf() {
        try {
            UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
        }
        catch(UnsupportedLookAndFeelException ex) {
            //Oh well...
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }
}
