package org.quelea.windows.newsong;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.JPanel;

public class ColorWheel extends JPanel implements Observer {

    private Rectangle wheel;
    private ColorModel model;
    private Image wheelImage = null;
    protected Dimension offDimension;
    protected Image offImage;
    protected Graphics offGraphics = null;
    private Color background;

    public ColorWheel(ColorModel model, Color background) {
        this.model = model;
        model.addObserver(this);
        this.background = background;
        setBackground(background);
        initEvents();
    }

    public ColorModel getModel() {
        return model;
    }

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

    public void update(Observable o, Object arg) {
        repaint();
    }

    public synchronized void paint(Graphics g) {
        update(g);
    }

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

    public Dimension getPreferredSize() {
        return new Dimension(200, 120);
    }

    public Dimension getMinimumSize() {
        return new Dimension(20, 20);
    }

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