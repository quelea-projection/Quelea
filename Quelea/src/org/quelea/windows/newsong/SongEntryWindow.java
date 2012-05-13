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
package org.quelea.windows.newsong;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.quelea.Application;
import org.quelea.Theme;
import org.quelea.displayable.Song;
import org.quelea.displayable.TextSection;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

/**
 * A new song window that users use for inserting the text content of a new
 * song.
 *
 * @author Michael
 */
public class SongEntryWindow extends JDialog {

    private BasicSongPanel basicSongPanel;
    private DetailedSongPanel detailedSongPanel;
    private ThemePanel themePanel;
    private final JTabbedPane tabbedPane;
    private final JButton confirmButton;
    private final JButton cancelButton;
    private final JCheckBox addToSchedCBox;
    private Song song;

    /**
     * Create and initialise the new song window.
     *
     * @param owner the owner of this window.
     */
    public SongEntryWindow(JFrame owner) {
        super(owner, LabelGrabber.INSTANCE.getLabel("song.entry.heading"), true);
        setResizable(false);
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setupBasicSongPanel();
        tabbedPane.add(basicSongPanel);
        setupDetailedSongPanel();
        tabbedPane.add(detailedSongPanel);
        setupThemePanel();
        tabbedPane.add(themePanel);
        add(tabbedPane, BorderLayout.CENTER);

        confirmButton = new JButton(LabelGrabber.INSTANCE.getLabel("add.song.button"), Utils.getImageIcon("icons/tick.png"));
        confirmButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                setVisible(false);
                Utils.updateSongInBackground(getSong(), true, false);
                if(addToSchedCBox.isSelected()) {
                    Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().addElement(getSong());
                }
                Application.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
            }
        });
        cancelButton = new JButton(LabelGrabber.INSTANCE.getLabel("cancel.button"), Utils.getImageIcon("icons/cross.png"));
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        addToSchedCBox = new JCheckBox(LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"), false);
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        checkBoxPanel.add(addToSchedCBox);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        bottomPanel.add(checkBoxPanel);
        bottomPanel.add(buttonPanel);
        add(bottomPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Called by the constructor to initialise the theme panel.
     */
    private void setupThemePanel() {
        themePanel = new ThemePanel();
    }

    /**
     * Called by the constructor to initialise the detailed song panel.
     */
    private void setupDetailedSongPanel() {
        detailedSongPanel = new DetailedSongPanel();
    }

    /**
     * Called by the constructor to initialise the basic song panel.
     */
    private void setupBasicSongPanel() {
        basicSongPanel = new BasicSongPanel();
        basicSongPanel.getLyricsField().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                checkHighlight();
                checkConfirmButton();
            }

            public void keyPressed(KeyEvent e) {
                checkHighlight();
                checkConfirmButton();
            }

            public void keyReleased(KeyEvent e) {
                checkHighlight();
                checkConfirmButton();
            }

            private void checkHighlight() {
                //TODO: Highlighting
            }
        });
        basicSongPanel.getTitleField().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                checkConfirmButton();
            }

            public void keyPressed(KeyEvent e) {
                checkConfirmButton();
            }

            public void keyReleased(KeyEvent e) {
                checkConfirmButton();
            }
        });
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        toFront();
    }

    /**
     * Get the confirm button on the new song window.
     *
     * @return the confirm button.
     */
    public JButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * Get the cancel button on the new song window.
     *
     * @return the cancel button.
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Get the panel where the user enters the basic song information.
     *
     * @return the basic song panel.
     */
    public BasicSongPanel getBasicSongPanel() {
        return basicSongPanel;
    }

    /**
     * Get the panel where the user enters the more detailed song information.
     *
     * @return the detailed song panel.
     */
    public DetailedSongPanel getDetailedSongPanel() {
        return detailedSongPanel;
    }

    /**
     * Get the theme currently displayed on this window.
     *
     * @return the current theme.
     */
    public Theme getTheme() {
        return themePanel.getTheme();
    }

    /**
     * Set this window up ready to enter a new song.
     */
    public void resetNewSong() {
        setTitle(LabelGrabber.INSTANCE.getLabel("new.song.title"));
        song = null;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("new.song.button"));
        confirmButton.setEnabled(false);
        basicSongPanel.resetNewSong();
        detailedSongPanel.resetNewSong();
        themePanel.setTheme(Theme.DEFAULT_THEME);
        tabbedPane.setSelectedIndex(0);
        addToSchedCBox.setSelected(false);
        addToSchedCBox.setEnabled(true);
    }

    /**
     * Set this window up ready to edit an existing song.
     *
     * @param song the song to edit.
     */
    public void resetEditSong(Song song) {
        setTitle(LabelGrabber.INSTANCE.getLabel("edit.song.title"));
        this.song = song;
        confirmButton.setText(LabelGrabber.INSTANCE.getLabel("edit.song.button"));
        confirmButton.setEnabled(true);
        basicSongPanel.resetEditSong(song);
        detailedSongPanel.resetEditSong(song);
        if(song.getSections().length > 0) {
            themePanel.setTheme(song.getSections()[0].getTheme());
        }
        tabbedPane.setSelectedIndex(0);
        addToSchedCBox.setSelected(false);
        if(Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().contains(song)) {
            addToSchedCBox.setEnabled(false);
        }
        else {
            addToSchedCBox.setEnabled(true);
        }
    }

    /**
     * Get the song that's been edited or created by the window.
     *
     * @return the song.
     */
    public Song getSong() {
        if(song == null) {
            song = new Song(getBasicSongPanel().getTitleField().getText(), getBasicSongPanel().getAuthorField().getText());
        }
        song.setLyrics(getBasicSongPanel().getLyricsField().getText());
        song.setTitle(getBasicSongPanel().getTitleField().getText());
        song.setAuthor(getBasicSongPanel().getAuthorField().getText());
        song.setTags(getDetailedSongPanel().getTagsPanel().getTagsAsString());
        song.setCcli(getDetailedSongPanel().getCcliField().getText());
        song.setCopyright(getDetailedSongPanel().getCopyrightField().getText());
        song.setPublisher(getDetailedSongPanel().getPublisherField().getText());
        song.setYear(getDetailedSongPanel().getYearField().getText());
        song.setKey(getDetailedSongPanel().getKeyField().getText());
        song.setCapo(getDetailedSongPanel().getCapoField().getText());
        song.setInfo(getDetailedSongPanel().getInfoField().getText());
        Theme tempTheme = song.getSections()[0].getTempTheme();
        for(TextSection section : song.getSections()) {
            section.setTheme(themePanel.getTheme());
            if(tempTheme != null) {
                section.setTempTheme(tempTheme);
            }
        }
        return song;
    }

    /**
     * Check whether the confirm button should be enabled or disabled and set it
     * accordingly.
     */
    private void checkConfirmButton() {
        if(getBasicSongPanel().getLyricsField().getText().trim().equals("")
                || getBasicSongPanel().getTitleField().getText().trim().equals("")) {
            confirmButton.setEnabled(false);
        }
        else {
            confirmButton.setEnabled(true);
        }
    }
}
