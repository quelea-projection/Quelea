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
import javax.swing.JWindow;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class SplashWindow extends JWindow {

    public SplashWindow() {
        try {
            BufferedImage image = ImageIO.read(new File("img/splash.png"));
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setFont(new Font("Verdana", 0, 45));
            graphics.drawString(QueleaProperties.get().getVersion().getVersionString(), 220, 140);

            setAlwaysOnTop(true);
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
        Rectangle bounds = gds[controlScreenProp].getDefaultConfiguration().getBounds();
        setLocation((int) (bounds.getLocation().x + bounds.getWidth() / 2) - getWidth() / 2, (int) (bounds.getLocation().y + bounds.getHeight() / 2) - getHeight() / 2);

    }

    private void fade(final boolean in) {
        new Thread() {

            private float opacity;

            {
                if (in) {
                    opacity = 0;
                }
                else {
                    opacity = 1;
                }
            }

            @Override
            public void run() {
                if (in) {
                    SplashWindow.super.setVisible(true);
                    while (opacity < 1) {
                        com.sun.awt.AWTUtilities.setWindowOpacity(SplashWindow.this, opacity);
                        Utils.sleep(15);
                        opacity += 0.03f;
                    }
                }
                else {
                    while (opacity > 0) {
                        com.sun.awt.AWTUtilities.setWindowOpacity(SplashWindow.this, opacity);
                        Utils.sleep(15);
                        opacity -= 0.03f;
                    }
                    SplashWindow.super.setVisible(false);
                }
            }
        }.start();
    }

    @Override
    public void setVisible(boolean visible) {
        fade(visible);
    }

    public static void main(String[] args) {
        new SplashWindow().setVisible(true);
    }
}
