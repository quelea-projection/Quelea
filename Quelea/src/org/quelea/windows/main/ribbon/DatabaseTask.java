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
package org.quelea.windows.main.ribbon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.quelea.Application;
import org.quelea.SongDatabase;
import org.quelea.importexport.ImportDialog;
import org.quelea.importexport.KingswayImportDialog;
import org.quelea.importexport.QSPImportDialog;
import org.quelea.importexport.SelectExportedSongsDialog;
import org.quelea.importexport.SourceImportDialog;
import org.quelea.importexport.SurvivorImportDialog;
import org.quelea.languages.LabelGrabber;
import org.quelea.tags.TagDialog;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.EditSongDBActionListener;
import org.quelea.windows.main.NewSongActionListener;
import org.quelea.windows.main.RemoveSongDBActionListener;

/**
 * The database task (i.e. group of buttons) displayed on the ribbon. Manages
 * all the database related actions.
 * @author Michael
 */
public class DatabaseTask extends RibbonTask {

    private static final ImportDialog sImportDialog;
    private static final ImportDialog qspImportDialog;
    private static final ImportDialog sourceImportDialog;
    private static final ImportDialog kingswayImportDialog;
    private static final TagDialog tagDialog;

    /**
     * Sort out all the dialogs.
     */
    static {
        qspImportDialog = new QSPImportDialog(Application.get().getMainWindow());
        sImportDialog = new SurvivorImportDialog(Application.get().getMainWindow());
        sourceImportDialog = new SourceImportDialog(Application.get().getMainWindow());
        kingswayImportDialog = new KingswayImportDialog(Application.get().getMainWindow());
        tagDialog = new TagDialog();
    }

    /**
     * Create the database task.
     */
    public DatabaseTask() {
        super(LabelGrabber.INSTANCE.getLabel("database.heading"), getSongBand(), getImportBand(), getExportBand());
    }

    /**
     * Get the import band of this task.
     * @return the import band.
     */
    private static JRibbonBand getImportBand() {
        JRibbonBand importBand = new JRibbonBand(LabelGrabber.INSTANCE.getLabel("import.heading"), RibbonUtils.getRibbonIcon("icons/import.png", 100, 100));
        RibbonUtils.applyStandardResizePolicies(importBand);
        JCommandButton qsp = new JCommandButton(LabelGrabber.INSTANCE.getLabel("qsp.button"), RibbonUtils.getRibbonIcon("img/logo.png", 100, 100));
        qsp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                qspImportDialog.setLocationRelativeTo(qspImportDialog.getOwner());
                qspImportDialog.setVisible(true);
            }
        });
        importBand.addCommandButton(qsp, RibbonElementPriority.TOP);
        JCommandButton survivor = new JCommandButton(LabelGrabber.INSTANCE.getLabel("ss.button"), RibbonUtils.getRibbonIcon("icons/survivor.jpg", 100, 100));
        survivor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sImportDialog.setLocationRelativeTo(sImportDialog.getOwner());
                sImportDialog.setVisible(true);
            }
        });
        importBand.addCommandButton(survivor, RibbonElementPriority.TOP);
        JCommandButton source = new JCommandButton(LabelGrabber.INSTANCE.getLabel("source.button"), RibbonUtils.getRibbonIcon("icons/source.jpg", 100, 100));
        source.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sourceImportDialog.setLocationRelativeTo(sourceImportDialog.getOwner());
                sourceImportDialog.setVisible(true);
            }
        });
        importBand.addCommandButton(source, RibbonElementPriority.TOP);
        JCommandButton kingsway = new JCommandButton(LabelGrabber.INSTANCE.getLabel("kingsway.button"), RibbonUtils.getRibbonIcon("icons/kingsway.png", 100, 100));
        kingsway.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                kingswayImportDialog.setLocationRelativeTo(kingswayImportDialog.getOwner());
                kingswayImportDialog.setVisible(true);
            }
        });
        importBand.addCommandButton(kingsway, RibbonElementPriority.TOP);
//        JCommandButton sof = new JCommandButton("Songs of fellowship", RibbonUtils.getRibbonIcon("icons/sof.jpg", 100, 100));
//        sof.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                //TODO: Implement
//            }
//        });
//        sof.setEnabled(false);
//        importBand.addCommandButton(sof, RibbonElementPriority.TOP);
        return importBand;
    }

    /**
     * Get the export band of this task.
     * @return the export band.
     */
    private static JRibbonBand getExportBand() {
        JRibbonBand exportBand = new JRibbonBand(LabelGrabber.INSTANCE.getLabel("export.heading"), RibbonUtils.getRibbonIcon("icons/export.png", 100, 100));
        RibbonUtils.applyStandardResizePolicies(exportBand);
        JCommandButton qsp = new JCommandButton(LabelGrabber.INSTANCE.getLabel("qsp.button"), RibbonUtils.getRibbonIcon("img/logo.png", 100, 100));
        qsp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SelectExportedSongsDialog dialog = new SelectExportedSongsDialog(Application.get().getMainWindow());
                dialog.setLocationRelativeTo(dialog.getOwner());
                dialog.setSongs(Arrays.asList(SongDatabase.get().getSongs()), null, false);
                dialog.setVisible(true);
            }
        });
        exportBand.addCommandButton(qsp, RibbonElementPriority.TOP);
        return exportBand;
    }

    /**
     * Get the song band of this task.
     * @return the song band.
     */
    private static JRibbonBand getSongBand() {
        final JRibbonBand songBand = new JRibbonBand(LabelGrabber.INSTANCE.getLabel("songs.heading"), RibbonUtils.getRibbonIcon("icons/database.png", 100, 100));
        RibbonUtils.applyStandardResizePolicies(songBand);
        final JCommandButton newSongButton = new JCommandButton(LabelGrabber.INSTANCE.getLabel("new.song.button"), RibbonUtils.getRibbonIcon("icons/newsong.png", 100, 100));
        newSongButton.addActionListener(new NewSongActionListener());
        songBand.addCommandButton(newSongButton, RibbonElementPriority.TOP);
        final JCommandButton editSongButton = new JCommandButton(LabelGrabber.INSTANCE.getLabel("edit.song.button"), RibbonUtils.getRibbonIcon("icons/edit.png", 100, 100));
        editSongButton.addActionListener(new EditSongDBActionListener());
        editSongButton.setEnabled(false);
        songBand.addCommandButton(editSongButton, RibbonElementPriority.MEDIUM);
        final JCommandButton deleteSongButton = new JCommandButton(LabelGrabber.INSTANCE.getLabel("delete.song.button"), RibbonUtils.getRibbonIcon("icons/remove 2.png", 100, 100));
        deleteSongButton.addActionListener(new RemoveSongDBActionListener());
        deleteSongButton.setEnabled(false);
        songBand.addCommandButton(deleteSongButton, RibbonElementPriority.MEDIUM);
        final JCommandButton tagsButton = new JCommandButton(LabelGrabber.INSTANCE.getLabel("tags.button"), RibbonUtils.getRibbonIcon("icons/tag.png", 100, 100));
        tagsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tagDialog.reloadTags();
                tagDialog.setVisible(true);
            }
        });
        songBand.addCommandButton(tagsButton, RibbonElementPriority.MEDIUM);
        final LibrarySongList libraryList = Application.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();

        libraryList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkEditDeleteButtons(editSongButton, deleteSongButton);
            }
        });
        libraryList.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                checkEditDeleteButtons(editSongButton, deleteSongButton);
            }

            @Override
            public void focusLost(FocusEvent e) {
                checkEditDeleteButtons(editSongButton, deleteSongButton);
            }
        });
        return songBand;
    }
    
    /**
     * Check whether the edit / delete buttons should be set to enabled or 
     * not.
     */
    private static void checkEditDeleteButtons(JCommandButton editSongButton, JCommandButton deleteSongButton) {
        final LibrarySongList libraryList = Application.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();
        if(!libraryList.isFocusOwner()) {
            deleteSongButton.setEnabled(false);
            editSongButton.setEnabled(false);
            return;
        }
        if (libraryList.getSelectedIndex() == -1) {
            deleteSongButton.setEnabled(false);
            editSongButton.setEnabled(false);
        } else {
            deleteSongButton.setEnabled(true);
            editSongButton.setEnabled(true);
        }
    }
}
