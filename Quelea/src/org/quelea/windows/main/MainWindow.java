package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.quelea.windows.newsong.NewSongWindow;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends JFrame {

    private MainToolbar toolbar;
    private MainMenuBar menubar;
    private MainPanel mainpanel;
    private NewSongWindow newSongWindow;

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
        newSongWindow = new NewSongWindow(this);
        mainpanel.getLibraryPanel().getLibrarySongPanel().getAddButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                newSongWindow.centreOnOwner();
                newSongWindow.setVisible(true);
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
    public NewSongWindow getNewSongWindow() {
        return newSongWindow;
    }

}
