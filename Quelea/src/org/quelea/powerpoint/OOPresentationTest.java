package org.quelea.powerpoint;

public class OOPresentationTest {

    public static void main(String args[]) throws Exception {
        OOPresentation.init("C:\\Program Files (x86)\\OpenOffice.org 3\\program"); //Change path to OO
        OOPresentation p = new OOPresentation("C:\\Users\\Michael\\Documents\\test.ppt"); //Change path to test presentation
        p.start(0);
    }
}
