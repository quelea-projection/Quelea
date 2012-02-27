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
package org.quelea.windows.main.quickedit;

import com.inet.jortho.SpellChecker;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import org.quelea.Application;
import org.quelea.displayable.Song;
import org.quelea.displayable.TextSection;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;

/**
 * A frame used for quickly editing a song.
 *
 * @author Michael
 */
public class QuickEditDialog extends JDialog {

    private JTextArea sectionArea;
    private JLabel statusLabel;
    private JButton okButton;
    private JButton cancelButton;
    private Song currentSong;
    private int currentIndex;

    /**
     * Construct a quick edit dialog.
     */
    public QuickEditDialog() {
        super(Application.get().getMainWindow(), "Quick Edit", ModalityType.APPLICATION_MODAL);
        currentIndex = -1;
        setLayout(new BorderLayout());
        statusLabel = new JLabel();
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);
        sectionArea = new JTextArea(8, 40);
        SpellChecker.register(sectionArea);
        sectionArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent ke) {
                if(ke.isShiftDown() && ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    okButton.doClick();
                }
            }
        });
        add(new JScrollPane(sectionArea), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        okButton = new JButton(LabelGrabber.INSTANCE.getLabel("ok.button"));
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                TextSection oldSection = currentSong.getSections()[currentIndex];
                String[] sectionLyrics = sectionArea.getText().split("\n\n");
                currentSong.replaceSection(new TextSection(oldSection.getTitle(), sectionLyrics[0].split("\n"), oldSection.getSmallText(), oldSection.shouldCapitaliseFirst(), oldSection.getTheme()), currentIndex);
                for(int i = 1; i < sectionLyrics.length; i++) {
                    String[] lyrics = sectionLyrics[i].split("\n");
                    String newTitle = "";
                    if(oldSection.getTitle() != null && !oldSection.getTitle().trim().isEmpty()) {
                        newTitle = oldSection.getTitle() + " (" + LabelGrabber.INSTANCE.getLabel("part") + " " + (i + 1) + ")";
                    }
                    currentSong.addSection(currentIndex + i, new TextSection(newTitle, lyrics, oldSection.getSmallText(), oldSection.shouldCapitaliseFirst(), oldSection.getTheme()));
                }
                setVisible(false);
                Utils.updateSongInBackground(currentSong, false);
            }
        });
        cancelButton = new JButton(LabelGrabber.INSTANCE.getLabel("cancel.button"));
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                setSongSection(null, 0);
                setVisible(false);
            }
        });
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                cancelButton.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Get the new text as an array of strings from the entered text. One array
     * item per line.
     *
     * @return the new text for the section.
     */
    private String[] getNewText() {
        return sectionArea.getText().split("\n");
    }

    /**
     * Set the song and section of this dialog.
     *
     * @param song the song to set the dialog to.
     * @param sectionIndex the section to set the dialog to.
     */
    public void setSongSection(Song song, int sectionIndex) {
        currentSong = song;
        setStatusLabel(song);
        if(song == null) {
            currentIndex = -1;
            sectionArea.setText("");
            return;
        }
        currentIndex = sectionIndex;
        StringBuilder text = new StringBuilder();
        String[] lines = song.getSections()[sectionIndex].getText(true, true);
        for(int i = 0; i < lines.length; i++) {
            String str = lines[i];
            text.append(str);
            if(i < lines.length - 1) {
                text.append('\n');
            }
        }
        sectionArea.setText(text.toString());
        sectionArea.setCaretPosition(0);
        pack();
    }

    /**
     * Set the status label to a particular song.
     *
     * @param song the song to use.
     */
    private void setStatusLabel(Song song) {
        if(song == null) {
            statusLabel.setText("");
        }
        else {
            statusLabel.setText("<html><b>" + song.getTitle() + "</b><br/><i>(" + LabelGrabber.INSTANCE.getLabel("quick.shortcut.description") + ")</i></html>");
        }
    }
}
