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
                    "This will take a long time (potentially hours!), so please be patient and leave Quelea running!"
                }, null, new KingswayWorshipParser(), false);
    }
    
    
}
