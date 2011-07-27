package org.quelea.importexport;

import org.quelea.utils.FileFilters;
import org.quelea.utils.LoggerUtils;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * An import dialog for the survivor song books in PDF format.
 * @author Michael
 */
public class SourceImportDialog extends ImportDialog {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new survivor import dialog.
     * @param owner the owner of the dialog.
     */
    public SourceImportDialog(JFrame owner) {
        super(owner, new String[]{
                "<html>Select the location of the <b>hymns</b> directory on the source CD.</html>"
        }, FileFilters.DIR_ONLY, new SourceParser(), true);
    }
}
