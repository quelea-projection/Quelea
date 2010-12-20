package org.quelea.windows.main;

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
        catch (IOException ex) {
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
                songEntryWindow.resetEditSong((Song) mainpanel.getLibraryPanel().getLibrarySongPanel().getSongList().getSelectedValue());
                songEntryWindow.setVisible(true);
            }
        });
        toolbar.getNewButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (confirmClear()) {
                    mainpanel.getSchedulePanel().getScheduleList().clearSchedule();
                }
            }
        });
        toolbar.getOpenButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (confirmClear()) {
                    JFileChooser chooser = getFileChooser();
                    if (chooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                        Schedule schedule = Schedule.fromFile(chooser.getSelectedFile());
                        mainpanel.getSchedulePanel().getScheduleList().setSchedule(schedule);
                    }
                }
            }
        });
        toolbar.getSaveButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Schedule schedule = mainpanel.getSchedulePanel().getScheduleList().getSchedule();
                if(schedule.getFile()==null) {
                    JFileChooser chooser = getFileChooser();
                    if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                        schedule.setFile(chooser.getSelectedFile());
                    }
                }
                boolean success = schedule.writeToFile();
                if (!success) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Couldn't save schedule", "Error", JOptionPane.ERROR_MESSAGE, null);
                }
            }
        });

        setJMenuBar(menubar);
        add(toolbar, BorderLayout.NORTH);
        add(mainpanel);
        pack();
    }

    /**
     * Get the JFileChooser used for opening and saving schedules.
     * @return the JFileChooser.
     */
    private JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().toLowerCase().endsWith(".qsch");
            }

            @Override
            public String getDescription() {
                return "Quelea schedules (.qsch)";
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
     * Get the new song window used for this main panel.
     */
    public SongEntryWindow getNewSongWindow() {
        return songEntryWindow;
    }
}
