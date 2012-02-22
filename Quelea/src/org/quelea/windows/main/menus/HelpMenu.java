/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.main.menus;

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
import org.quelea.Application;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.UpdateChecker;
import org.quelea.utils.Utils;
import org.quelea.windows.help.AboutDialog;

/**
 * Quelea's help menu.
 *
 * @author Michael
 */
public class HelpMenu extends JMenu {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final JMenuItem queleaSite;
    private final JMenuItem queleaDiscuss;
    private final JMenuItem queleaDownload;
    private final JMenuItem updateCheck;
    private final JMenuItem about;
    private final AboutDialog aboutDialog;

    /**
     * Create a new help menu
     */
    public HelpMenu() {
        super(LabelGrabber.INSTANCE.getLabel("help.menu"));
        setMnemonic('h');
        
        aboutDialog = new AboutDialog(Application.get().getMainWindow());
        
        if(Desktop.isDesktopSupported()) {
            queleaSite = new JMenuItem(LabelGrabber.INSTANCE.getLabel("help.menu.website"), Utils.getImageIcon("icons/website.png", 16, 16));
            queleaSite.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getWebsiteLocation()));
                    }
                    catch (URISyntaxException | IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea website", ex);
                        showError();
                    }
                }
            });
            queleaSite.setMnemonic(KeyEvent.VK_W);
            add(queleaSite);
            queleaDiscuss = new JMenuItem(LabelGrabber.INSTANCE.getLabel("help.menu.discussion"), Utils.getImageIcon("icons/discuss.png", 16, 16));
            queleaDiscuss.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDiscussLocation()));
                    }
                    catch (URISyntaxException | IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea discuss", ex);
                        showError();
                    }
                }
            });
            queleaDiscuss.setMnemonic(KeyEvent.VK_D);
            add(queleaDiscuss);
            queleaDownload = new JMenuItem(LabelGrabber.INSTANCE.getLabel("help.menu.download"), Utils.getImageIcon("icons/download.png", 16, 16));
            queleaDownload.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(QueleaProperties.get().getDownloadLocation()));
                    }
                    catch (URISyntaxException | IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't launch Quelea download page", ex);
                        showError();
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
        updateCheck = new JMenuItem(LabelGrabber.INSTANCE.getLabel("help.menu.update"), Utils.getImageIcon("icons/update.png", 16, 16));
        updateCheck.setMnemonic(KeyEvent.VK_C);
        updateCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new UpdateChecker(Application.get().getMainWindow()).checkUpdate(true, true, true);
            }
        });
        add(updateCheck);
        about = new JMenuItem(LabelGrabber.INSTANCE.getLabel("help.menu.about"), Utils.getImageIcon("icons/about.png", 16, 16));
        about.setMnemonic(KeyEvent.VK_A);
        about.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                aboutDialog.setVisible(true);
            }
        });
        add(about);
    }

    /**
     * Show a dialog saying we couldn't open the given location.
     *
     * @param location the location that failed to open.
     */
    private void showError() {
        JOptionPane.showMessageDialog(this, LabelGrabber.INSTANCE.getLabel("help.menu.error.text"), LabelGrabber.INSTANCE.getLabel("help.menu.error.title"), JOptionPane.ERROR_MESSAGE, null);
    }

    /**
     * Get the quelea discuss menu item.
     *
     * @return the quelea discuss menu item.
     */
    public JMenuItem getQueleaDiscuss() {
        return queleaDiscuss;
    }

    /**
     * Get the quelea download menu item.
     *
     * @return the quelea download menu item.
     */
    public JMenuItem getQueleaDownload() {
        return queleaDownload;
    }

    /**
     * Get the quelea website menu item.
     *
     * @return the quelea website menu item.
     */
    public JMenuItem getQueleaSite() {
        return queleaSite;
    }

    /**
     * Get the about menu item.
     *
     * @return the about menu item.
     */
    public JMenuItem getAbout() {
        return about;
    }

    /**
     * Get the "check update" menu item.
     *
     * @return the "check update" menu item.
     */
    public JMenuItem getUpdateCheck() {
        return updateCheck;
    }
}
