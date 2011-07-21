package org.quelea.video;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JFrame;
import org.quelea.windows.main.VideoControlPanel;

/**
 *
 * @author Michael
 */
public class Test {

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

    public static void go() {
        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(1, 2));
        VideoControlPanel panel1 = new VideoControlPanel();
        panel1.loadVideo("F:\\Videos\\Inception\\Inception.mkv");
        frame.add(panel1);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



    }

}
