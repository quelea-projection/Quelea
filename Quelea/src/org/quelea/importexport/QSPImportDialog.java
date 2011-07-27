package org.quelea.importexport;

import org.quelea.utils.FileFilters;
import org.quelea.utils.LoggerUtils;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * An import dialog for Quelea song packs.
 * @author Michael
 */
public class QSPImportDialog extends ImportDialog {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new SCHEDULE import dialog.
     * @param owner the owner of the dialog.
     */
    public QSPImportDialog(JFrame owner) {
        super(owner, new String[]{
                "Select the location of the Quelea songpack below."
        }, FileFilters.SCHEDULE, new QSPParser(), false);
    }
}
