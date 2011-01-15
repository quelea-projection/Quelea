package org.quelea.windows.main;

import org.quelea.bible.Bible;
import org.quelea.displayable.BiblePassage;
import org.quelea.displayable.Song;
import org.quelea.displayable.TextDisplayable;
import org.quelea.displayable.TextSection;
import org.quelea.windows.library.LibraryPanel;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;

/**
 * The main body of the main window, containing the schedule, the media bank, the preview and the live panels.
 * @author Michael
 */
public class MainPanel extends JPanel {

    private final SchedulePanel schedulePanel;
    private final LibraryPanel libraryPanel;
    private final SelectPreviewLyricsPanel previewPanel;
    private final SelectLiveLyricsPanel livePanel;

    /**
     * Create the new main panel.
     */
    public MainPanel() {
        setLayout(new BorderLayout());
        schedulePanel = new SchedulePanel();
        libraryPanel = new LibraryPanel();
        previewPanel = new SelectPreviewLyricsPanel();
        livePanel = new SelectLiveLyricsPanel();

        addKeyListeners();
        addScheduleListeners();
        addScheduleAddListeners();
        addBibleListeners();

        previewPanel.getLiveButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DefaultListModel liveModel = livePanel.getLyricsList().getModel();
                DefaultListModel previewModel = previewPanel.getLyricsList().getModel();
                liveModel.clear();
                for(int i = 0; i < previewModel.getSize(); i++) {
                    liveModel.addElement(previewModel.get(i));
                }
                livePanel.getLyricsList().setSelectedIndex(previewPanel.getLyricsList().getSelectedIndex());
                if(schedulePanel.getScheduleList().getSelectedIndex() < schedulePanel.getScheduleList().getModel().getSize()) {
                    schedulePanel.getScheduleList().setSelectedIndex(schedulePanel.getScheduleList().getSelectedIndex() + 1);
                }
                livePanel.getLyricsList().requestFocus();
            }
        });

        JSplitPane scheduleAndLibrary = new JSplitPane(JSplitPane.VERTICAL_SPLIT, schedulePanel, libraryPanel);
        scheduleAndLibrary.setResizeWeight(0.5);
        scheduleAndLibrary.setOneTouchExpandable(true);
        JSplitPane previewAndLive = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, livePanel);
        previewAndLive.setResizeWeight(0.5);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scheduleAndLibrary, previewAndLive);
        mainSplit.setResizeWeight(0.2);
        mainSplit.setSize(300, 300);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void addBibleListeners() {
        libraryPanel.getBiblePanel().getAddToSchedule().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                BiblePassage passage = new BiblePassage(((Bible) libraryPanel.getBiblePanel().getBibleSelector().getSelectedItem()).getName(), libraryPanel.getBiblePanel().getBibleLocation(), libraryPanel.getBiblePanel().getVerses());
                ((DefaultListModel) schedulePanel.getScheduleList().getModel()).addElement(passage);
            }
        });
    }

    /**
     * Add the listeners that add songs to the schedule.
     */
    private void addScheduleAddListeners() {
        libraryPanel.getLibrarySongPanel().getSongList().getPopupMenu().getAddToScheduleButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Song song = (Song) libraryPanel.getLibrarySongPanel().getSongList().getSelectedValue();
                ((DefaultListModel) schedulePanel.getScheduleList().getModel()).addElement(song);
            }
        });

        libraryPanel.getLibrarySongPanel().getSongList().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                JList songList = (JList) e.getSource();
                if(e.getClickCount() == 2) {
                    Song song = (Song) songList.getSelectedValue();
                    ((DefaultListModel) schedulePanel.getScheduleList().getModel()).addElement(song);
                }
            }
        });
    }

    /**
     * Add the key listeners to the lists used for switching focus between them.
     */
    private void addKeyListeners() {

        schedulePanel.getScheduleList().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == ' ') {
                    previewPanel.getLiveButton().doClick();
                    livePanel.getLyricsList().ensureIndexIsVisible(livePanel.getLyricsList().getSelectedIndex());
                }
            }

            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_RIGHT && !previewPanel.getLyricsList().getModel().isEmpty()) {
                    previewPanel.getLyricsList().requestFocus();
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });

        previewPanel.getLyricsList().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == ' ') {
                    previewPanel.getLiveButton().doClick();
                    livePanel.getLyricsList().ensureIndexIsVisible(livePanel.getLyricsList().getSelectedIndex());
                }
            }

            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_RIGHT && !livePanel.getLyricsList().getModel().isEmpty()) {
                    livePanel.getLyricsList().requestFocus();
                }
                else if(e.getKeyCode() == KeyEvent.VK_LEFT && schedulePanel.getScheduleList().getModel().getSize() > 0) {
                    schedulePanel.getScheduleList().requestFocus();
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });

        livePanel.getLyricsList().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == ' ') {
                    previewPanel.getLiveButton().doClick();
                    livePanel.getLyricsList().ensureIndexIsVisible(livePanel.getLyricsList().getSelectedIndex());
                }
            }

            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_LEFT && !previewPanel.getLyricsList().getModel().isEmpty()) {
                    previewPanel.getLyricsList().requestFocus();
                }
            }

            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });
    }

    /**
     * Add the listeners to check for changes in the schedule panel.
     */
    private void addScheduleListeners() {
        schedulePanel.getScheduleList().getModel().addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                //Nothing needs to be done here.
            }

            public void intervalRemoved(ListDataEvent e) {
                //Nothing needs to be done here.
            }

            /**
             * listChanged() must be called in case we're removing the last
             * element in the list, in which case the preview panel must be
             * cleared.
             */
            public void contentsChanged(ListDataEvent e) {
                scheduleListChanged();
            }
        });

        schedulePanel.getScheduleList().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                scheduleListChanged();
            }
        });
    }

    /**
     * This method should be called every time the list values are updated or changed.
     */
    private void scheduleListChanged() {
        if(schedulePanel.getScheduleList().getSelectedIndex() == -1) {
            previewPanel.getLyricsList().getModel().clear();
            return;
        }
        TextDisplayable newDisplayable = (TextDisplayable) schedulePanel.getScheduleList().getModel().getElementAt(schedulePanel.getScheduleList().getSelectedIndex());
        DefaultListModel model = previewPanel.getLyricsList().getModel();
        model.clear();
        for(TextSection section : newDisplayable.getSections()) {
            model.addElement(section);
        }
        previewPanel.getLyricsList().setSelectedIndex(0);
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

    /**
     * Get the panel displaying the order of service.
     * @return the panel displaying the order of service.
     */
    public SchedulePanel getSchedulePanel() {
        return schedulePanel;
    }

    /**
     * Get the panel displaying the library of media.
     * @return the library panel.
     */
    public LibraryPanel getLibraryPanel() {
        return libraryPanel;
    }
}
