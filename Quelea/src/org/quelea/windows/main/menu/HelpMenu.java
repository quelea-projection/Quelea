/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.UpdateChecker;

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
                    catch(URISyntaxException | IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea website", ex);
                        showError("the Quelea website");
                    }
                }
            });
            queleaSite.setMnemonic(KeyEvent.VK_W);
            add(queleaSite);
            queleaDiscuss = new JMenuItem("Discussion (ask any questions here)");
            queleaDiscuss.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDiscussLocation()));
                    }
                    catch(URISyntaxException | IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea discuss", ex);
                        showError("Quelea discuss");
                    }
                }
            });
            queleaDiscuss.setMnemonic(KeyEvent.VK_D);
            add(queleaDiscuss);
            queleaDownload = new JMenuItem("Download");
            queleaDownload.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDownloadLocation()));
                    }
                    catch(URISyntaxException | IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea download page", ex);
                        showError("Quelea download page");
                    }
                }
            });
            queleaDownload.setMnemonic(KeyEvent.VK_O);
            add(queleaDownload);
        }
        else {
            queleaSite = null;
            queleaDiscuss = null;
            queleaDownload = null;
        }
        updateCheck = new JMenuItem("Check for updates");
        updateCheck.setMnemonic(KeyEvent.VK_C);
        updateCheck.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new UpdateChecker(((JPopupMenu) updateCheck.getParent()).getInvoker()).checkUpdate(true, true, true);
            }
        });
        add(updateCheck);
        about = new JMenuItem("About...");
        about.setMnemonic(KeyEvent.VK_A);
        add(about);
    }

    /**
     * Show a dialog saying we couldn't open the given location.
     * @param location the location that failed to open.
     */
    private void showError(String location) {
        JOptionPane.showMessageDialog(this, "Sorry, couldn't open " + location + ".", "Error", JOptionPane.ERROR_MESSAGE, null);
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
