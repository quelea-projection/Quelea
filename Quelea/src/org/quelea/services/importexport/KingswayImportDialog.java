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
package org.quelea.services.importexport;

import org.quelea.services.languages.LabelGrabber;

/**
 * The kingsway import dialog, used for importing songs from the online kingsway
 * library.
 * @author Michael
 */
public class KingswayImportDialog extends ImportDialog {
    
    private final KingswayWorshipParser kwp;
    
    /**
     * Create a new kingsway import dialog.
     * @param kwp the parser used for this dialog.
     */
    public KingswayImportDialog(KingswayWorshipParser kwp) {
        super(new String[]{
                    LabelGrabber.INSTANCE.getLabel("kingsway.import.line1"),
                    LabelGrabber.INSTANCE.getLabel("kingsway.import.line2")
                }, null, (kwp = new KingswayWorshipParser()), false, false);
        this.kwp = kwp;
        //TODO Messy fix...
    }
    
    @Override
    public void setAll(boolean all) {
        kwp.setAll(all);
    }
    
    @Override
    public void setRange(boolean range) {
        kwp.setRange(range);
    }
    
}
