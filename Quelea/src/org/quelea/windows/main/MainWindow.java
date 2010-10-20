package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.quelea.display.Song;
import org.quelea.windows.newsong.SongEntryWindow;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends JFrame {

    private MainToolbar toolbar;
    private MainMenuBar menubar;
    private MainPanel mainpanel;
    private SongEntryWindow songEntryWindow;

    /**
     * Create a new main window.
     */
    public MainWindow() {
        super("Quelea V0.0 alpha");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setIconImage(ImageIO.read(new File("img/logo.png")));
        }
        catch(IOException ex) {
        }
        setLayout(new BorderLayout());
        menubar = new MainMenuBar();
        toolbar = new MainToolbar();
        mainpanel = new MainPanel();
        songEntryWindow = new SongEntryWindow(this);
        mainpanel.getLibraryPanel().getLibrarySongPanel().getAddButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                songEntryWindow.centreOnOwner();
                songEntryWindow.resetNewSong();
                songEntryWindow.setVisible(true);
            }
        });
        mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getPopupMenu().getEditDBButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                songEntryWindow.centreOnOwner();
                songEntryWindow.resetEditSong((Song)mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValue());
                songEntryWindow.setVisible(true);
            }
        });

        setJMenuBar(menubar);
        add(toolbar, BorderLayout.NORTH);
        add(mainpanel);
        pack();
    }

    /**
     * Get the main panel on this window.
     * @return the main panel part of this window.
     */
    public MainPanel getMainPanel() {
        return mainpanel;
    }

    /**
     * Get the new song window used for this main panel.
     */
    public SongEntryWindow getNewSongWindow() {
        return songEntryWindow;
    }

}
