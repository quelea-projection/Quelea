package org.quelea.mainwindow.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.quelea.display.Song;
import org.quelea.display.SongSection;

/**
 * The main body of the main window, containing the schedule, the media bank,
 * the preview and the live panels.
 * @author Michael
 */
public class MainPanel extends JPanel {

    private SchedulePanel schedulePanel;
    private LibraryPanel libraryPanel;
    private SelectPreviewLyricsPanel previewPanel;
    private SelectLiveLyricsPanel livePanel;

    /**
     * Create the new main panel.
     */
    public MainPanel() {
        setLayout(new BorderLayout());
        schedulePanel = new SchedulePanel();
        libraryPanel = new LibraryPanel();
        previewPanel = new SelectPreviewLyricsPanel();
        livePanel = new SelectLiveLyricsPanel();

        schedulePanel.getScheduleList().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (schedulePanel.getScheduleList().getSelectedIndex() == -1) {
                    return;
                }
                Song newSong = (Song) schedulePanel.getScheduleList().getModel().getElementAt(schedulePanel.getScheduleList().getSelectedIndex());
                DefaultListModel model = previewPanel.getLyricsList().getModel();
                model.clear();
                for (SongSection section : newSong.getSections()) {
                    model.addElement(section);
                }
                previewPanel.getLyricsList().setSelectedIndex(0);
            }
        });

        previewPanel.addLiveButtonListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DefaultListModel liveModel = livePanel.getLyricsList().getModel();
                DefaultListModel previewModel = previewPanel.getLyricsList().getModel();
                liveModel.clear();
                for (int i = 0; i < previewModel.getSize(); i++) {
                    liveModel.addElement(previewModel.get(i));
                }
                livePanel.getLyricsList().setSelectedIndex(previewPanel.getLyricsList().getSelectedIndex());
                if (schedulePanel.getScheduleList().getSelectedIndex() < schedulePanel.getScheduleList().getModel().getSize()) {
                    schedulePanel.getScheduleList().setSelectedIndex(schedulePanel.getScheduleList().getSelectedIndex() + 1);
                }
                livePanel.getLyricsList().requestFocus();
            }
        });

        JSplitPane scheduleAndLibrary = new JSplitPane(JSplitPane.VERTICAL_SPLIT, schedulePanel, libraryPanel);
        scheduleAndLibrary.setResizeWeight(0.5);
        JSplitPane previewAndLive = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, livePanel);
        previewAndLive.setResizeWeight(0.5);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scheduleAndLibrary, previewAndLive);
        mainSplit.setResizeWeight(0.2);
        mainSplit.setSize(300, 300);
        add(mainSplit, BorderLayout.CENTER);
    }

    /**
     * Get the panel displaying the selection of the preview lyrics.
     * @return the panel displaying the selection of the preview lyrics.
     */
    public SelectPreviewLyricsPanel getPreviewLyricsPanel() {
        return previewPanel;
    }

    /**
     * Get the panel displaying the selection of the live lyrics.
     * @return the panel displaying the selection of the live lyrics.
     */
    public SelectLiveLyricsPanel getLiveLyricsPanel() {
        return livePanel;
    }
}
