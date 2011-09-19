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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;

/**
 * A colour wheel. Separate from the in-built one to avoid the Java 7 / 
 * subtance bugs (plus the built in one is a bit clunky anyway.)
 * @author Michael
 */
public class ColorWheel extends JPanel implements Observer {

    private Rectangle wheel;
    private ColorModel model;
    private Image wheelImage = null;
    protected Dimension offDimension;
    protected Image offImage;
    protected Graphics offGraphics = null;
    private Color background;

    /**
     * Create a new colour wheel.
     * @param model the model to use.
     * @param background the background colour.
     */
    public ColorWheel(ColorModel model, Color background) {
        this.model = model;
        model.addObserver(this);
        this.background = background;
        setBackground(background);
        initEvents();
    }

    /**
     * Get the model in use.
     * @return the model in use.
     */
    public ColorModel getModel() {
        return model;
    }

    /**
     * Notify the wheel has resize, redraw it.
     */
    private void resized() {
        int w = getWidth();
        int h = getHeight();

        int r = (h > w ? w : h);

        wheel = new Rectangle((w - r) / 2, (h - r) / 2, r, r);

        if ((wheel.width <= 0) || (wheel.height <= 0)) {
            return;
        }

        int saturationStep = 1;
        int hueStep = 1;

        if (r <= 150) {
            saturationStep = 5;
            hueStep = 3;
        }
        else if (r <= 280) {
            saturationStep = 3;
            hueStep = 2;
        }
        else if (r < 350) {
            hueStep = 2;
        }

        // Redraw wheel in separate image buffer
        if (wheelImage != null) {
            wheelImage.flush();
        }
        wheelImage = createImage(wheel.width, wheel.height);
        Graphics g = wheelImage.getGraphics();
        g.setColor(background);

        g.fillRect(0, 0, wheel.width, wheel.height);

        int midx = wheel.width / 2;
        int midy = wheel.height / 2;
        int arcw, arch;
        float sat, hue, lum;

        lum = model.getBrightness();

        for (int s = 100; s > 0; s -= saturationStep) {
            arcw = wheel.width * s / 100;
            arch = wheel.height * s / 100;
            sat = s / 100f;
            for (h = 0; h <= 360; h += hueStep) {
                hue = h / 360f;
                if (hue >= 1.0f) {
                    hue = 0.0f;
                }
                Color c = Color.getHSBColor(hue, sat, lum);
                g.setColor(c);
                g.fillArc(midx - arcw / 2, midy - arch / 2, arcw, arch, h, hueStep);
            }
        }
    }

    /**
     * Update the wheel (repaint it.)
     * @param o observer.
     * @param arg arg obj.
     */
    public void update(Observable o, Object arg) {
        repaint();
    }

    /**
     * Paint the wheel.
     * @param g graphics to paint with.
     */
    public synchronized void paint(Graphics g) {
        update(g);
    }

    /**
     * Update the wheel.
     * @param g graphics to paint with.
     */
    public synchronized void update(Graphics g) {
        Dimension d = getSize();

        offDimension = d;
        offImage = createImage(d.width, d.height);
        offGraphics = offImage.getGraphics();
        resized();

        // Erases the previous image
        offGraphics.setColor(background);
        offGraphics.fillRect(0, 0, d.width, d.height);

        offGraphics.drawImage(wheelImage, wheel.x, wheel.y, this);

        // Draw circles around the current colour
        int midx = wheel.x + wheel.width / 2;
        int midy = wheel.y + wheel.height / 2;
        Color inverse = new Color((int)(model.getBrightness()*255), (int)(model.getBrightness()*255), (int)(model.getBrightness()*255));
        inverse = new Color(255-inverse.getRed(), 255-inverse.getGreen(), 255-inverse.getBlue());
        offGraphics.setColor(inverse);
        int arcw = (int) (wheel.width * model.getSaturation() / 2);
        int arch = (int) (wheel.height * model.getSaturation() / 2);
        double th = model.getHue() * 2 * Math.PI;
        offGraphics.drawOval((int) (midx + arcw * Math.cos(th) - 3),
                (int) (midy - arch * Math.sin(th) - 3), 6, 6);

        g.drawImage(offImage, 0, 0, this);
    }

    /**
     * Add the events to this wheel.
     */
    private void initEvents() {
        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent e) {
                update(e);
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                update(e);
            }
        });
    }

    /**
     * Preferred size should always be 200x120.
     * @return 200x120.
     */
    public Dimension getPreferredSize() {
        return new Dimension(200, 120);
    }

    /**
     * Minimum size should always be 20x20.
     * @return 20x20.
     */
    public Dimension getMinimumSize() {
        return new Dimension(20, 20);
    }

    /**
     * Update the colour wheel.
     * @param e the mouse event.
     */
    private void update(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int midx = wheel.x + wheel.width / 2;
        int midy = wheel.y + wheel.height / 2;
        double s, h;

        s = Math.sqrt((double) ((x - midx) * (x - midx) + (y - midy) * (y - midy))) / (wheel.height / 2);
        h = -Math.atan2((double) (y - midy), (double) (x - midx)) / (2 * Math.PI);

        if (h < 0.0) {
            h += 1.0;
        }
        model.setHue((float) h);
        if (s > 1.0) {
            s = 1.0;
        }
        model.setSaturation((float) s);

        e.consume();
    }
}
