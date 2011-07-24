package org.quelea.windows.main;

import java.awt.Dimension;
import org.quelea.utils.QueleaProperties;
import org.quelea.windows.newsong.SongEntryWindow;

import javax.swing.JFrame;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.quelea.Application;
import org.quelea.windows.main.ribbon.DatabaseTask;
import org.quelea.windows.main.ribbon.ProjectorTask;
import org.quelea.windows.main.ribbon.RibbonMenu;
import org.quelea.windows.main.ribbon.RibbonUtils;
import org.quelea.windows.main.ribbon.ScheduleTask;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends JRibbonFrame {

    private final MainPanel mainpanel;
    private final SongEntryWindow songEntryWindow;
    //Ribbon stuff
    private final ScheduleTask scheduleTask;
    private final DatabaseTask databaseTask;
    private final ProjectorTask projectorTask;
    private final RibbonMenu ribbonMenu;

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
        
        scheduleTask = new ScheduleTask();
        databaseTask = new DatabaseTask();
        projectorTask = new ProjectorTask();
        ribbonMenu = new RibbonMenu();
        populateRibbon();
        add(mainpanel);
        mainpanel.getLibraryPanel().getImagePanel().setPreferredSize(new Dimension(100, 200));
        setSize(800,600);
    }

    private void populateRibbon() {
        JRibbon ribbon = getRibbon();
        ribbon.addTask(scheduleTask);
        ribbon.addTask(databaseTask);
        ribbon.addTask(projectorTask);
        ribbon.setApplicationMenu(ribbonMenu);
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
