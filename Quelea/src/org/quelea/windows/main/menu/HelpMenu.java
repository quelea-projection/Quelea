package org.quelea.windows.main.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;

/**
 * The help menu.
 * @author Michael
 */
public class HelpMenu extends JMenu {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final JMenuItem queleaSite;
    private final JMenuItem queleaDiscuss;
    private final JMenuItem queleaDownload;
    private final JMenuItem updateCheck;
    private final JMenuItem about;

    /**
     * Create a new help menu
     */
    public HelpMenu() {
        super("Help");
        if(Desktop.isDesktopSupported()) {
            queleaSite = new JMenuItem("Website");
            queleaSite.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getWebsiteLocation()));
                    }
                    catch(Exception ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea website", ex);
                        showError("the Quelea website");
                    }
                }
            });
            add(queleaSite);
            queleaDiscuss = new JMenuItem("Discussion (ask any questions here)");
            queleaDiscuss.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDiscussLocation()));
                    }
                    catch(Exception ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea discuss", ex);
                        showError("Quelea discuss");
                    }
                }
            });
            add(queleaDiscuss);
            queleaDownload = new JMenuItem("Download");
            queleaDownload.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDownloadLocation()));
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea download page", ex);
                        showError("Quelea download page");
                    }
                }
            });
            add(queleaDownload);
        }
        else {
            queleaSite = null;
            queleaDiscuss = null;
            queleaDownload = null;
        }
        updateCheck = new JMenuItem("Check for updates");
        add(updateCheck);
        about = new JMenuItem("About...");
        add(about);
    }

    /**
     * Show a dialog saying we couldn't open the given location.
     * @param location the location that failed to open.
     */
    private void showError(String location) {
        JOptionPane.showMessageDialog(this, "Sorry, we couldn't open " + location + ".", "Error", JOptionPane.ERROR_MESSAGE, null);
    }

    /**
     * Get the quelea discuss menu item.
     * @return the quelea discuss menu item.
     */
    public JMenuItem getQueleaDiscuss() {
        return queleaDiscuss;
    }

    /**
     * Get the quelea download menu item.
     * @return the quelea download menu item.
     */
    public JMenuItem getQueleaDownload() {
        return queleaDownload;
    }

    /**
     * Get the quelea website menu item.
     * @return the quelea website menu item.
     */
    public JMenuItem getQueleaSite() {
        return queleaSite;
    }

    /**
     * Get the about menu item.
     * @return the about menu item.
     */
    public JMenuItem getAbout() {
        return about;
    }

    /**
     * Get the "check update" menu item.
     * @return the "check update" menu item.
     */
    public JMenuItem getUpdateCheck() {
        return updateCheck;
    }
}
