/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.data.displayable;

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
     * @return the transfer data if the flavor is DISPLAYABLE_FLAVOR, else null.
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
     * @return the displayable backing this transfer displayable.
     */
    public Displayable getDisplayable() {
        return displayable;
    }

}
