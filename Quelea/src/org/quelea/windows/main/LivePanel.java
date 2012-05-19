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
package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.quelea.Application;
import org.quelea.displayable.Displayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.sound.AudioPlayer;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 * The panel displaying the live lyrics selection - changes made on this panel
 * are reflected on the live projection.
 *
 * @author Michael
 */
public class LivePanel extends LivePreviewPanel {

    private final JToggleButton black;
    private final JToggleButton clear;
    private final JToggleButton hide;
    private final JToggleButton pause;

    /**
     * Create a new live lyrics panel.
     */
    public LivePanel() {
        getPresentationPanel().setLive();
        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>" + LabelGrabber.INSTANCE.getLabel("live.heading") + "</b></html>"));
        header.add(new JToolBar.Separator());
        black = new JToggleButton(Utils.getImageIcon("icons/black.png"));
        black.setToolTipText(LabelGrabber.INSTANCE.getLabel("black.screen.tooltip") + " (F1)");
        black.setRequestFocusEnabled(false);
        black.addActionListener(new ActionListener() {

            /**
             * Toggle all the canvases to black.
             *
             * @param e the action event.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<LyricCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for(LyricCanvas canvas : canvases) {
                    canvas.toggleBlack();
                }
            }
        });
        header.add(black);
        clear = new JToggleButton(Utils.getImageIcon("icons/clear.png", 16, 16));
        clear.setToolTipText(LabelGrabber.INSTANCE.getLabel("clear.text.tooltip") + " (F2)");
        clear.setRequestFocusEnabled(false);
        clear.addActionListener(new ActionListener() {

            /**
             * Toggle all the canvases to clear.
             *
             * @param e the action event.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<LyricCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for(LyricCanvas canvas : canvases) {
                    canvas.toggleClear();
                }
            }
        });
        header.add(clear);
        hide = new JToggleButton(Utils.getImageIcon("icons/cross.png"));
        hide.setToolTipText(LabelGrabber.INSTANCE.getLabel("hide.display.output.tooltip") + " (F3)");
        hide.setRequestFocusEnabled(false);
        hide.addActionListener(new ActionListener() {

            /**
             * Hide the lyric windows.
             *
             * @param e the action event.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int projectorScreen = QueleaProperties.get().getProjectorScreen();
                int stageScreen = QueleaProperties.get().getStageScreen();
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                final GraphicsDevice[] gds = ge.getScreenDevices();
                LyricWindow lyricWindow = Application.get().getLyricWindow();
                LyricWindow stageWindow = Application.get().getStageWindow();

                final boolean lyricsHidden;
                if(!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= gds.length || projectorScreen < 0)) {
                    lyricsHidden = true;
                }
                else {
                    lyricsHidden = false;
                }

                final boolean stageHidden;
                if(!QueleaProperties.get().isStageModeCoords() && (stageScreen >= gds.length || stageScreen < 0)) {
                    stageHidden = true;
                }
                else {
                    stageHidden = false;
                }

                if(!lyricsHidden) {
                    lyricWindow.setVisible(!lyricWindow.isVisible());
                }
                if(!stageHidden) {
                    stageWindow.setVisible(!stageWindow.isVisible());
                }

                Application.get().getMainWindow().repaint();
            }
        });
        header.add(hide);
        
        pause = new JToggleButton(Utils.getImageIcon("icons/pauseaudio.png"));
        pause.setToolTipText(LabelGrabber.INSTANCE.getLabel("pause.display.output.tooltip") + " (F4)");
        pause.setRequestFocusEnabled(false);
        pause.addActionListener(new ActionListener() {

            /**
             * Pause/unpause the background audio.
             *
             * @param e the action event.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                Application.get().getAudioPlayer().togglePause();
            }
        });
        header.add(pause);
        
        add(header, BorderLayout.NORTH);

        addKeyListener(new KeyListener() {

            /**
             * Nothing when typed...
             *
             * @param e the key event.
             */
            @Override
            public void keyTyped(KeyEvent e) {
                //Nothing needed here
            }

            /**
             * Detect F1 to go to black, F2 to clear and F3 to hide the window.
             *
             * @param e the key event.
             */
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_F1) {
                    black.doClick();
                }
                else if(e.getKeyCode() == KeyEvent.VK_F2) {
                    clear.doClick();
                }
                else if(e.getKeyCode() == KeyEvent.VK_F3) {
                    hide.doClick();
                }
                else if(e.getKeyCode() == KeyEvent.VK_F4) {
                    pause.doClick();
                }
            }

            /**
             * Nothing when released...
             *
             * @param e the key event.
             */
            @Override
            public void keyReleased(KeyEvent e) {
                //Nothing needed here
            }
        });
    }

    /**
     * Set the displayable to be shown on this live panel.
     *
     * @param d the displayable to show.
     * @param index the index to use for the displayable, if relevant.
     */
    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        clear.setEnabled(d.supportClear());
        AudioPlayer player = Application.get().getAudioPlayer();
        player.stop();
        if(d != null) {
            String audioPath = d.getAudio();
            if(audioPath != null) {
                Application.get().getAudioPlayer().play(audioPath);

            }
        }
    }

    /**
     * Get the "black" toggle button.
     *
     * @return the "black" toggle button.
     */
    public JToggleButton getBlack() {
        return black;
    }

    /**
     * Get the "clear" toggle button.
     *
     * @return the "clear" toggle button.
     */
    public JToggleButton getClear() {
        return clear;
    }

    /**
     * Get the "hide" toggle button.
     *
     * @return the "hide" toggle button.
     */
    public JToggleButton getHide() {
        return hide;
    }
}
