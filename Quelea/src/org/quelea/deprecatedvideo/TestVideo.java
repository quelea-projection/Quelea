/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.deprecatedvideo;

/**
 *
 * @author Michael
 */
public class TestVideo {

    public static void main(String[] args) throws Exception {
        TestFrame frame = new TestFrame();
        Video vid = new Video("F:\\Videos\\Fast and Furious\\Fast and Furious 4.mkv");
        vid.addFrameChangeListener(frame);
        vid.open();
        vid.play();
    }
}
