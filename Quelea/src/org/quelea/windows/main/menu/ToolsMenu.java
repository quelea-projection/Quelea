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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.quelea.Application;
import org.quelea.importexport.ImportDialog;
import org.quelea.importexport.QSPImportDialog;
import org.quelea.importexport.SurvivorImportDialog;

/**
 * The tools menu on the menu bar.
 * @author Michael
 */
public class ToolsMenu extends JMenu {

    private final JMenuItem options;
    private final JMenuItem qspImport;
    private final JMenuItem ssImport;
    private final ImportDialog sImportDialog;
    private final ImportDialog qspImportDialog;

    /**
     * Create a new tools menu.
     */
    public ToolsMenu() {
        super("Tools");
        JMenu importMenu = new JMenu("Import songs");
        importMenu.setMnemonic(KeyEvent.VK_I);
        qspImport = new JMenuItem("Quelea song pack...");
        qspImport.setMnemonic(KeyEvent.VK_Q);
        qspImportDialog = new QSPImportDialog(Application.get().getMainWindow());
        qspImport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                qspImportDialog.setLocationRelativeTo(qspImportDialog.getOwner());
                qspImportDialog.setVisible(true);
            }
        });
        importMenu.add(qspImport);
        ssImport = new JMenuItem("Survivor songbook...");
        ssImport.setMnemonic(KeyEvent.VK_S);
        sImportDialog = new SurvivorImportDialog(Application.get().getMainWindow());
        ssImport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                sImportDialog.setLocationRelativeTo(sImportDialog.getOwner());
                sImportDialog.setVisible(true);
            }
        });
        importMenu.add(ssImport);
        add(importMenu);
        options = new JMenuItem("Options");
        options.setMnemonic(KeyEvent.VK_O);
        add(options);
    }

    /**
     * Get the options menu item.
     * @return the options menu item.
     */
    public JMenuItem getOptions() {
        return options;
    }

    /**
     * Get the import quelea song pack menu item.
     * @return the import quelea song pack menu item.
     */
    public JMenuItem getQSPImport() {
        return qspImport;
    }

    /**
     * Get the import survivor songbook menu item.
     * @return the import survivor songbook menu item.
     */
    public JMenuItem getSSImport() {
        return ssImport;
    }

}
