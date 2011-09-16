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
package org.quelea.splash;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.quelea.utils.FadeWindow;
import org.quelea.utils.QueleaProperties;

/**
 * The splash screen to display when the program starts.
 * @author Michael
 */
public class SplashWindow extends FadeWindow {

    /**
     * Create a new splash window.
     */
    public SplashWindow() {
        try {
            BufferedImage image = ImageIO.read(new File("img/splash.png"));
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setFont(new Font("Verdana", 0, 45));
            graphics.drawString(QueleaProperties.VERSION.getVersionString(), 220, 140);

//            setAlwaysOnTop(true);
            JLabel splash = new JLabel(new ImageIcon(image));
            setLayout(new BorderLayout());
            add(splash, BorderLayout.CENTER);
            pack();
        }
        catch (IOException ex) {
            //Don't really care, just splash.
        }

        int controlScreenProp = QueleaProperties.get().getControlScreen();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        if(controlScreenProp >= gds.length) {
            controlScreenProp = gds.length-1;
        }
        Rectangle bounds = gds[controlScreenProp].getDefaultConfiguration().getBounds();
        setLocation((int) (bounds.getLocation().x + bounds.getWidth() / 2) - getWidth() / 2, (int) (bounds.getLocation().y + bounds.getHeight() / 2) - getHeight() / 2);

    }

    /**
     * Just for testing...
     * @param args not used.
     */
    public static void main(String[] args) {
        new SplashWindow().setVisible(true);
    }
}
