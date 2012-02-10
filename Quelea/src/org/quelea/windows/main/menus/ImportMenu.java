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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.quelea.Application;
import org.quelea.importexport.*;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

/**
 * Quelea's import menu.
 * @author Michael
 */
public class ImportMenu extends JMenu {
    
    private final ImportDialog sImportDialog;
    private final ImportDialog qspImportDialog;
    private final ImportDialog sourceImportDialog;
    private final ImportDialog kingswayImportDialog;
//    private final ImportDialog sofImportDialog;
    
    private final JMenuItem qspItem;
    private final JMenuItem ssItem;
    private final JMenuItem sourceItem;
    private final JMenuItem kingswayItem;
//    private final JMenuItem sofItem;
    
    /**
     * Create the import menu.
     */
    public ImportMenu() {
        super(LabelGrabber.INSTANCE.getLabel("import.heading"));
        setMnemonic('i');
        
        qspImportDialog = new QSPImportDialog(Application.get().getMainWindow());
        sImportDialog = new SurvivorImportDialog(Application.get().getMainWindow());
        sourceImportDialog = new SourceImportDialog(Application.get().getMainWindow());
        kingswayImportDialog = new KingswayImportDialog(Application.get().getMainWindow());
//        sofImportDialog = new SofImportDialog(Application.get().getMainWindow());
        
        qspItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("qsp.button"), Utils.getImageIcon("img/logo.png", 16, 16));
        qspItem.setMnemonic('q');
        qspItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                qspImportDialog.setLocationRelativeTo(qspImportDialog.getOwner());
                qspImportDialog.setVisible(true);
            }
        });
        add(qspItem);
        
        ssItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("ss.button"), Utils.getImageIcon("icons/survivor.jpg", 16, 16));
        ssItem.setMnemonic('s');
        ssItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sImportDialog.setLocationRelativeTo(sImportDialog.getOwner());
                sImportDialog.setVisible(true);
            }
        });
        add(ssItem);
        
        sourceItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("source.button"), Utils.getImageIcon("icons/source.jpg", 16, 16));
        sourceItem.setMnemonic('o');
        sourceItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sourceImportDialog.setLocationRelativeTo(sourceImportDialog.getOwner());
                sourceImportDialog.setVisible(true);
            }
        });
        add(sourceItem);
        
        kingswayItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("kingsway.button"), Utils.getImageIcon("icons/kingsway.png", 16, 16));
        kingswayItem.setMnemonic('k');
        kingswayItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                kingswayImportDialog.setLocationRelativeTo(kingswayImportDialog.getOwner());
                kingswayImportDialog.setVisible(true);
            }
        });
        add(kingswayItem);
        
        //TODO: Implement
//        sofItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("sof.button"), Utils.getImageIcon("icons/sof.jpg", 16, 16));
//        sofItem.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                sofImportDialog.setLocationRelativeTo(sofImportDialog.getOwner());
//                sofImportDialog.setVisible(true);
//            }
//        });
//        add(sofItem);
        
    }
    
}
