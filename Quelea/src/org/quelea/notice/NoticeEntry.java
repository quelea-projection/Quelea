package org.quelea.notice;

import javax.swing.JDialog;

/**
 *
 * @author Michael
 */
public class NoticeEntry extends JDialog {
    
    public static Notice getNotice() {
        return new Notice("Test notice 1 going 2 times", 2);
    }
    
}
