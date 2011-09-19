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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import org.quelea.Background;
import org.quelea.Theme;
import org.quelea.displayable.ImageDisplayable;

/**
 * A panel used in the live / preview panels for displaying images.
 * @author Michael
 */
public class ImagePanel extends ContainedPanel {

    private JPanel containerPanel = new JPanel();
    private LyricCanvas canvas = new LyricCanvas(false);
    private LivePreviewPanel container;

    /**
     * Create a new image panel.
     * @param container the container this panel is contained within.
     */
    public ImagePanel(LivePreviewPanel container) {
        this.container = container;
        setLayout(new BorderLayout());
        containerPanel.setLayout(new GridBagLayout());
        containerPanel.add(canvas, new GridBagConstraints());
        add(containerPanel, BorderLayout.CENTER);
        canvas.setPreferredSize(new Dimension(200, 200));
    }

    @Override
    public void focus() {
        //TODO: Something probably
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void clear() {
        updateCanvases(null);
    }

    /**
     * Show a given image displayable on the panel.
     * @param displayable the image displayable.
     */
    public void showDisplayable(ImageDisplayable displayable) {
//        canvas.setPreferredSize(new Dimension(container.getWidth(), container.getWidth()));
        Theme theme = new Theme(null, null, new Background(displayable.getFile().getAbsolutePath(), displayable.getOriginalImage()));
        updateCanvases(theme);
    }

    /**
     * Update the canvases with the given theme.
     * @param theme the given theme.
     */
    private void updateCanvases(Theme theme) {
        canvas.setTheme(theme);
        canvas.eraseText();
        for (LyricCanvas lCanvas : container.getCanvases()) {
            lCanvas.setTheme(theme);
            lCanvas.eraseText();
        }
    }
}
