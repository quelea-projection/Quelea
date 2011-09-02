package org.quelea.utils;

import javax.swing.JWindow;

/**
 * A window that fades in or out gradually rather than suddenly.
 * @author Michael
 */
public class FadeWindow extends JWindow {

    private int sleepTime = 15;
    private float speed = 0.03f;
    private boolean nextStateVisible;

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Fade in or out, replaces setVisible().
     * @param in true if the window should fade in, false to fade out.
     */
    private void fade(final boolean in) {
        if (in) {
            FadeWindow.super.setVisible(true);
        }
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
                    while (opacity < 1) {
                        setOpacity(opacity);
                        Utils.sleep(sleepTime);
                        opacity += speed;
                    }
                }
                else {
                    while (opacity > 0) {
                        setOpacity(opacity);
                        Utils.sleep(sleepTime);
                        opacity -= speed;
                    }
                    FadeWindow.super.setVisible(false);
                }
            }
        }.start();
    }

    /**
     * Fade the window in and out gradually.
     * @param visible whether the window should be visible or not.
     */
    @Override
    public void setVisible(boolean visible) {
        if (nextStateVisible == visible) {
            return;
        }
        nextStateVisible = visible;
        fade(visible);
    }
}
