package org.quelea.displayable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Decorates a displayable by providing transfer options.
 * @author Michael
 */
public class TransferDisplayable implements Transferable {

    public static final DataFlavor DISPLAYABLE_FLAVOR = new DataFlavor(Displayable.class, "Displayable");
    private final Displayable displayable;

    /**
     * Create a new transfer displayable from the given displayable.
     * @param displayable the displayable to use.
     */
    public TransferDisplayable(Displayable displayable) {
        this.displayable = displayable;
    }

    /**
     * @inheritDoc
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DISPLAYABLE_FLAVOR};
    }

    /**
     * @inheritDoc
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DISPLAYABLE_FLAVOR);
    }

    /**
     * @inheritDoc
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(flavor.equals(DISPLAYABLE_FLAVOR)) {
            return displayable;
        }
        else {
            return null;
        }
    }

    /**
     * Get the displayable backing this transfer displayable.
     */
    public Displayable getDisplayable() {
        return displayable;
    }

}
