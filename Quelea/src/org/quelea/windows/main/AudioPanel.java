/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2012 Ben Goodwin and Michael Berry
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

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import org.quelea.ColourBackground;
import org.quelea.Theme;
import org.quelea.displayable.AudioDisplayable;

/**
 * This is the panel used in the live/ preview panes for displaying audio files
 * Currently displays a black square. May become deprecated with audio panel
 * separated from schedule.
 * 
 * @author Ben Goodwin
 * @version 19-May-2012
 */
public class AudioPanel extends ContainedPanel {

    private JPanel containerPanel = new JPanel();
    private LyricCanvas canvas = new LyricCanvas(false, false);
    private LivePreviewPanel container;
    
    /**
     * Create a new audio panel.
     * @param container the container this panel is contained within.
     */
    public AudioPanel(LivePreviewPanel container) {
        this.container = container;
        setLayout(new BorderLayout());
        containerPanel.setLayout(new GridBagLayout());
        containerPanel.add(canvas, new GridBagConstraints());
        add(containerPanel, BorderLayout.CENTER);
        canvas.setPreferredSize(new Dimension(200, 200));
    }
    
    
    @Override
    public void focus() {
        // TODO: Maybe do something in future with a play/pause/ volume control
    }

    /* 
     * Stolen from ImagePanel :D
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        updateCanvases(null);
    }

    void showDisplayable(AudioDisplayable audioDisplayable) {
        Theme theme = new Theme(null, null, new ColourBackground(Color.BLACK));
        // TODO: Make clear.
        updateCanvases(theme);
    }

    /**
     * Update the canvases with the given theme.
     * @param theme the given theme.
     */
    private void updateCanvases(Theme theme) {
        canvas.setTheme(theme);
        canvas.eraseText();
        Set<LyricCanvas> canvases = new HashSet<>();
        canvases.addAll(container.getCanvases());
        for (LyricCanvas lCanvas : canvases) {
            lCanvas.setTheme(theme);
            lCanvas.eraseText();
        }
    }
    
}
