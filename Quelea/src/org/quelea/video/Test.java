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
package org.quelea.video;

import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.quelea.windows.video.VideoControlPanel;

/**
 * An internal test class to play around with the video stuff without launching
 * a full Quelea instance.
 * @author Michael
 */
public class Test {

    /**
     * Start the test.
     * @param args command line arguments.
     * @throws Exception if something goes wrong.
     */
    public static void main(String[] args) throws Exception {
//        JFrame frame = new JFrame();
//        frame.setLayout(new GridLayout(1, 2));
//        Canvas panel = new Canvas();
//        panel.setPreferredSize(new Dimension(300,300));
//        Canvas panel2 = new Canvas();
//        panel2.setPreferredSize(new Dimension(300,300));
//        frame.add(panel);
//        frame.add(panel2);
//        frame.pack();
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        RemotePlayer player = RemotePlayerFactory.getRemotePlayer(panel);
//        player.load("F:\\Videos\\Inception\\Inception.mkv");
//        player.play();
//        RemotePlayer player2 = RemotePlayerFactory.getRemotePlayer(panel2);
//        player2.load("F:\\Videos\\Gone in 60 Seconds\\Gone in 60 Seconds.avi");
//        player2.play();
        go();
    }

    /**
     * Fire off the test.
     */
    public static void go() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = new JFrame();
                frame.setLayout(new GridLayout(1, 2));
                VideoControlPanel panel1 = new VideoControlPanel();
                panel1.loadVideo("C:\\vid.avi");
                panel1.playVideo();
                frame.add(panel1);
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }
}
