package org.quelea.windows.main;

import java.awt.Dimension;
import org.quelea.utils.QueleaProperties;
import org.quelea.windows.newsong.SongEntryWindow;

import javax.swing.JFrame;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.quelea.Application;
import org.quelea.windows.main.ribbon.RibbonPopulator;
import org.quelea.windows.main.ribbon.RibbonUtils;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends JRibbonFrame {

    private final MainPanel mainpanel;
    private final SongEntryWindow songEntryWindow;

    /**
     * Create a new main window.
     */
    public MainWindow(boolean setApplicationWindow) {
        super("Quelea " + QueleaProperties.get().getVersion().getVersionString());
        if(setApplicationWindow) {
            Application.get().setMainWindow(this);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setApplicationIcon(RibbonUtils.getRibbonIcon("img/logo.png", 100, 100));

        mainpanel = new MainPanel();
        songEntryWindow = new SongEntryWindow(this);
        mainpanel.getLibraryPanel().getLibrarySongPanel().getAddButton().addActionListener(new NewSongActionListener());
        mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getPopupMenu().getEditDBButton().addActionListener(new EditSongDBActionListener());
        mainpanel.getSchedulePanel().getScheduleList().getPopupMenu().getEditSongButton().addActionListener(new EditSongScheduleActionListener());
        
        new RibbonPopulator(getRibbon()).populate();
        add(mainpanel);
        mainpanel.getLibraryPanel().getImagePanel().setPreferredSize(new Dimension(100, 200));
        setSize(800,600);
    }

    /**
     * Get the main panel on this window.
     * @return the main panel part of this window.
     */
    public MainPanel getMainPanel() {
        return mainpanel;
    }

    /**
     * Get the new song window used for this window.
     * @return the song entry window.
     */
    public SongEntryWindow getSongEntryWindow() {
        return songEntryWindow;
    }

}
