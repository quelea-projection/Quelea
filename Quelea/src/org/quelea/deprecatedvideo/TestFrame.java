package org.quelea.deprecatedvideo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This class just displays a 2d graphic on a Swing window.  It's
 * only here so the video playback demos look simpler.  Please don't
 * reuse this component; why?  Because I know next to nothing
 * about Swing, and this is probably busted.
 * <p>
 * Of note though, is this class has NO XUGGLER dependencies.
 * </p>
 * @author aclarke
 *
 */
public class TestFrame extends JFrame implements FrameChangeListener {

    private final ImageComponent mOnscreenPicture;

    /**
     * Create the frame
     */
    public TestFrame() {
        mOnscreenPicture = new ImageComponent();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(mOnscreenPicture);
        this.setVisible(true);
        this.pack();
    }

    @Override
    public void frameChanged(BufferedImage newFrame) {
        mOnscreenPicture.setImage(newFrame);
    }

    public class ImageComponent extends JComponent {

        private Image mImage;
        private Dimension mSize;

        public void setImage(Image image) {
            SwingUtilities.invokeLater(new ImageRunnable(image));
        }

        public void setImageSize(Dimension newSize) {
        }

        private class ImageRunnable implements Runnable {

            private final Image newImage;

            public ImageRunnable(Image newImage) {
                super();
                this.newImage = newImage;
            }

            public void run() {
                ImageComponent.this.mImage = newImage;
                final Dimension newSize = new Dimension(mImage.getWidth(null),
                        mImage.getHeight(null));
                if (!newSize.equals(mSize)) {
                    ImageComponent.this.mSize = newSize;
                    TestFrame.this.setSize(mImage.getWidth(null), mImage.getHeight(null));
                    TestFrame.this.setVisible(true);
                }
                repaint();
            }
        }

        public ImageComponent() {
            mSize = new Dimension(0, 0);
            setSize(mSize);
        }

        public synchronized void paint(Graphics g) {
            if (mImage != null) {
                Graphics2D g2 = (Graphics2D) g;
                long val = System.currentTimeMillis();
                int newW = (int) (mImage.getWidth(null) * 3);
                int newH = (int) (mImage.getHeight(null) * 3);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(mImage, 0, 0, newW, newH, null);
                System.out.println(System.currentTimeMillis()-val);
//                g.drawImage(mImage, 0, 0, this);
            }
        }
    }
}
