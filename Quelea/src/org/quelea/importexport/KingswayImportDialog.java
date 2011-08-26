package org.quelea.importexport;

import javax.swing.JFrame;

/**
 *
 * @author Michael
 */
public class KingswayImportDialog extends ImportDialog {
    
    /**
     * Create a new survivor import dialog.
     * @param owner the owner of the dialog.
     */
    public KingswayImportDialog(JFrame owner) {
        super(owner, new String[]{
                    "This will import the kingsway song library from online.",
                }, null, new KingswayWorshipParser(), false);
    }
    
    
}
