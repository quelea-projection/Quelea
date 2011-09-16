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
package org.quelea.graphics.keystone;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Michael
 */
public class KeystoneCorrector {

    private BufferedImage originalImage;

    public KeystoneCorrector(Image originalImage) {
        this.originalImage = (BufferedImage) originalImage;
    }

    public BufferedImage getCorrectedImage() {
        double width = originalImage.getWidth(null) * 0.5;
        double increment = (originalImage.getWidth(null) - width)/originalImage.getHeight();

        BufferedImage ret = new BufferedImage(originalImage.getWidth(null), originalImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        for (int h = 0; h < originalImage.getHeight(); h++) {
            int[] arr = new int[originalImage.getWidth()];
            for (int w = 0; w < originalImage.getWidth(); w++) {
                arr[w] = originalImage.getRGB(w, h);
            }
            int[] newPixels = getShortLine(arr, (int) (width + 0.5));
            
            for (int w = 0; w < originalImage.getWidth(); w++) {
                ret.setRGB(w, h, newPixels[w]);
            }
            width += increment;
        }

        return ret;
    }

    private int[] getShortLine(int[] original, int newSize) {
        int[] newArr = new int[original.length];
        double scale = original.length / newSize;
        int start = (original.length - newSize) / 2;
        int end = original.length - ((original.length - newSize) / 2);
        for (int i = start; i < end-1; i++) {
            newArr[i] = original[(int) ((i - start) * scale)];
        }
        return newArr;
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        final BufferedImage image = ImageIO.read(new File("D:\\My Pictures\\lightning.jpg"));
        JPanel panel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                long time = System.currentTimeMillis();
                g.drawImage(new KeystoneCorrector(image).getCorrectedImage(), 0, 0, null);
//                System.out.println(System.currentTimeMillis()-time);
            }
        };
        frame.add(panel);
        frame.setSize(image.getWidth(), image.getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
