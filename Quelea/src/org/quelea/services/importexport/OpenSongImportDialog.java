/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.services.importexport;

import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;

/**
 * An import dialog for Opensong databases.
 * <p>
 * @author Michael
 */
public class OpenSongImportDialog extends ImportDialog {

    /**
     * Create a new opensong import dialog.
     * <p>
     */
    public OpenSongImportDialog() {
        super(new String[]{LabelGrabber.INSTANCE.getLabel("os.import.line1")
        }, FileFilters.ZIP, new OpensongParser(), false);
    }

}
