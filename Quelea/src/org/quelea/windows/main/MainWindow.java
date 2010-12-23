package org.quelea.windows.main;

import org.quelea.windows.main.menu.MainMenuBar;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.quelea.Schedule;
import org.quelea.display.Song;
import org.quelea.utils.QueleaProperties;
import org.quelea.windows.help.AboutDialog;
import org.quelea.windows.newsong.SongEntryWindow;
import org.quelea.windows.options.OptionsDialog;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends JFrame {

    private final MainToolbar toolbar;
    private final MainMenuBar menubar;
    private final MainPanel mainpanel;
    private final SongEntryWindow songEntryWindow;
    private final OptionsDialog optionsDialog;
    private final AboutDialog aboutDialog;

    /**
     * Create a new main window.
     */
    public MainWindow() {
        super("Quelea (Version " + QueleaProperties.get().getVersion().getVersionString() + " Pre-release)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setIconImage(ImageIO.read(new File("img/logo.png")));
        }
        catch (IOException ex) {
        }
        setLayout(new BorderLayout());
        menubar = new MainMenuBar();
        toolbar = new MainToolbar();
        mainpanel = new MainPanel();
        songEntryWindow = new SongEntryWindow(this);
        optionsDialog = new OptionsDialog(this);
        aboutDialog = new AboutDialog(this);
        mainpanel.getLibraryPanel().getLibrarySongPanel().getAddButton().addActionListener(new NewSongActionListener());
        mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getPopupMenu().getEditDBButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                songEntryWindow.centreOnOwner();
                songEntryWindow.resetEditSong((Song) mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValue());
                songEntryWindow.setVisible(true);
            }
        });
        addToolbarListeners();
        addMenuBarListeners();
        setJMenuBar(menubar);
        add(toolbar, BorderLayout.NORTH);
        add(mainpanel);
        pack();
    }

    private class NewSongActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            songEntryWindow.centreOnOwner();
            songEntryWindow.resetNewSong();
            songEntryWindow.setVisible(true);
        }

    }

    /**
     * Add the required action listeners to the menu bar.
     */
    private void addMenuBarListeners() {
        menubar.getFileMenu().getNewSchedule().addActionListener(new NewScheduleActionListener());
        menubar.getFileMenu().getOpenSchedule().addActionListener(new OpenScheduleActionListener());
        menubar.getFileMenu().getSaveSchedule().addActionListener(new SaveScheduleActionListener());
        menubar.getFileMenu().getSaveScheduleAs().addActionListener(new SaveScheduleAsActionListener());
        menubar.getDatabaseMenu().getNewSong().addActionListener(new NewSongActionListener());
        menubar.getToolsMenu().getOptions().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                optionsDialog.setVisible(true);
            }
        });
        menubar.getHelpMenu().getAbout().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                aboutDialog.setVisible(true);
            }
        });

    }

    /**
     * Add the required action listeners to the toolbar.
     */
    private void addToolbarListeners() {
        toolbar.getNewButton().addActionListener(new NewScheduleActionListener());
        toolbar.getOpenButton().addActionListener(new OpenScheduleActionListener());
        toolbar.getSaveButton().addActionListener(new SaveScheduleActionListener());
    }

    /**
     * The action listener fired when a new schedule is created.
     */
    private class NewScheduleActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (confirmClear()) {
                mainpanel.getLiveLyricsPanel().getLyricsList().getModel().clear();
                mainpanel.getSchedulePanel().getScheduleList().clearSchedule();
            }
        }

    }

    /**
     * The action listener fired when a schedule is opened.
     */
    private class OpenScheduleActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (confirmClear()) {
                JFileChooser chooser = getFileChooser();
                if (chooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                    Schedule schedule = Schedule.fromFile(chooser.getSelectedFile());
                    if(schedule==null) {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "There was a problem opening the schedule. Perhaps it's corrupt, or is not a schedule saved by Quelea.",
                                "Error opening schedule", JOptionPane.ERROR_MESSAGE, null);
                    }
                    else {
                        mainpanel.getLiveLyricsPanel().getLyricsList().getModel().clear();
                        mainpanel.getSchedulePanel().getScheduleList().setSchedule(schedule);
                    }
                }
            }
        }

    }

    /**
     * The action listener fired when a schedule is saved.
     */
    private class SaveScheduleActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            saveSchedule(false);
        }

    }

    /**
     * The action listener fired when a schedule is saved "as" a file.
     */
    private class SaveScheduleAsActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            saveSchedule(true);
        }

    }

    /**
     * Save the current schedule.
     * @param saveAs true if the file location should be specified, false if
     * the current one should be used.
     */
    private void saveSchedule(boolean saveAs) {
        Schedule schedule = mainpanel.getSchedulePanel().getScheduleList().getSchedule();
        File file = null;
        if (saveAs || schedule.getFile() == null) {
            JFileChooser chooser = getFileChooser();
            if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                String extension = QueleaProperties.get().getScheduleExtension();
                file = chooser.getSelectedFile();
                if(!file.getName().endsWith("." + extension)) {
                    file = new File(file.getAbsoluteFile() + "." + extension);
                }
                if(file.exists()) {
                    int result = JOptionPane.showConfirmDialog(MainWindow.this, file.getName() + " already exists. Overwrite?",
                            "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
                    if(result!=JOptionPane.YES_OPTION) {
                        file = null;
                    }
                }
                schedule.setFile(file);
            }
        }
        if(file != null) {
            boolean success = schedule.writeToFile();
            if (!success) {
                JOptionPane.showMessageDialog(MainWindow.this, "Couldn't save schedule", "Error", JOptionPane.ERROR_MESSAGE, null);
            }
        }
    }

    /**
     * Get the JFileChooser used for opening and saving schedules.
     * @return the JFileChooser.
     */
    private JFileChooser getFileChooser() {
        final String extension = QueleaProperties.get().getScheduleExtension();
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().toLowerCase().endsWith("." + extension);
            }

            @Override
            public String getDescription() {
                return "Quelea schedules (." + extension + ")";
            }
        });
        return chooser;
    }

    /**
     * Confirm whether it's ok to clear the current schedule.
     * @return true if this is ok, false otherwise.
     */
    private boolean confirmClear() {
        if(mainpanel.getSchedulePanel().getScheduleList().isEmpty()) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(this, "This will clear the current schedule. Is this OK?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
        if(result==JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
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
    public SongEntryWindow getNewSongWindow() {
        return songEntryWindow;
    }

    /**
     * Get the main menu bar used on this window.
     * @return the main menu bar.
     */
    public MainMenuBar getMainMenuBar() {
        return menubar;
    }

    /**
     * Get the options window.
     * @return the options window.
     */
    public OptionsDialog getOptionsWindow() {
        return optionsDialog;
    }
}
