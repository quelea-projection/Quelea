package org.quelea.windows.main.ribbon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import javax.swing.JDialog;
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
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.EditSongDBActionListener;
import org.quelea.windows.main.NewSongActionListener;
import org.quelea.windows.main.RemoveSongDBActionListener;

/**
 *
 * @author Michael
 */
public class DatabaseTask extends RibbonTask {

    private static final ImportDialog sImportDialog;
    private static final ImportDialog qspImportDialog;
    private static final ImportDialog sourceImportDialog;
    private static final ImportDialog kingswayImportDialog;

    static {
        qspImportDialog = new QSPImportDialog(Application.get().getMainWindow());
        sImportDialog = new SurvivorImportDialog(Application.get().getMainWindow());
        sourceImportDialog = new SourceImportDialog(Application.get().getMainWindow());
        kingswayImportDialog = new KingswayImportDialog(Application.get().getMainWindow());
    }

    public DatabaseTask() {
        super("Database", getSongBand(), getImportBand(), getExportBand());
    }

    private static JRibbonBand getImportBand() {
        JRibbonBand importBand = new JRibbonBand("Import", RibbonUtils.getRibbonIcon("icons/import.png", 100, 100));
        RibbonUtils.applyStandardResizePolicies(importBand);
        JCommandButton qsp = new JCommandButton("Quelea song pack", RibbonUtils.getRibbonIcon("img/logo.png", 100, 100));
        qsp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                qspImportDialog.setLocationRelativeTo(qspImportDialog.getOwner());
                qspImportDialog.setVisible(true);
            }
        });
        importBand.addCommandButton(qsp, RibbonElementPriority.TOP);
        JCommandButton survivor = new JCommandButton("Survivor songbook", RibbonUtils.getRibbonIcon("icons/survivor.jpg", 100, 100));
        survivor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sImportDialog.setLocationRelativeTo(sImportDialog.getOwner());
                sImportDialog.setVisible(true);
            }
        });
        importBand.addCommandButton(survivor, RibbonElementPriority.TOP);
        JCommandButton source = new JCommandButton("The source", RibbonUtils.getRibbonIcon("icons/source.jpg", 100, 100));
        source.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sourceImportDialog.setLocationRelativeTo(sourceImportDialog.getOwner());
                sourceImportDialog.setVisible(true);
            }
        });
        importBand.addCommandButton(source, RibbonElementPriority.TOP);
        JCommandButton kingsway = new JCommandButton("Kingsway", RibbonUtils.getRibbonIcon("icons/kingsway.png", 100, 100));
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

    private static JRibbonBand getExportBand() {
        JRibbonBand exportBand = new JRibbonBand("Export", RibbonUtils.getRibbonIcon("icons/export.png", 100, 100));
        RibbonUtils.applyStandardResizePolicies(exportBand);
        JCommandButton qsp = new JCommandButton("Quelea song pack", RibbonUtils.getRibbonIcon("img/logo.png", 100, 100));
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

    private static JRibbonBand getSongBand() {
        final JRibbonBand songBand = new JRibbonBand("Songs", RibbonUtils.getRibbonIcon("icons/database.png", 100, 100));
        RibbonUtils.applyStandardResizePolicies(songBand);
        final JCommandButton newSongButton = new JCommandButton("New song", RibbonUtils.getRibbonIcon("icons/newsong.png", 100, 100));
        newSongButton.addActionListener(new NewSongActionListener());
        songBand.addCommandButton(newSongButton, RibbonElementPriority.TOP);
        final JCommandButton editSongButton = new JCommandButton("Edit song", RibbonUtils.getRibbonIcon("icons/edit.png", 100, 100));
        editSongButton.addActionListener(new EditSongDBActionListener());
        editSongButton.setEnabled(false);
        songBand.addCommandButton(editSongButton, RibbonElementPriority.MEDIUM);
        final JCommandButton deleteSongButton = new JCommandButton("Delete song", RibbonUtils.getRibbonIcon("icons/remove 2.png", 100, 100));
        deleteSongButton.addActionListener(new RemoveSongDBActionListener());
        deleteSongButton.setEnabled(false);
        songBand.addCommandButton(deleteSongButton, RibbonElementPriority.MEDIUM);
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
