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
package org.quelea.services.print;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;

/**
 * Used for printing.
 * @author Michael
 */
public class Printer {

    private static volatile Printer instance;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create the printer.
     */
    private Printer() {
        //Internal only
    }

    /**
     * Get the singleton instance of this class.
     * @return the singleton instance.
     */
    public static Printer getInstance() {
        if (instance == null) {
            synchronized (Printer.class) {
                if (instance == null) {
                    instance = new Printer();
                }
            }
        }
        return instance;
    }
    
    public void print(SongDisplayable song) {
        try {
            File temp = File.createTempFile(song.getTitle(), ".pdf");
            temp.deleteOnExit();
            SongPDFPrinter.INSTANCE.print(song, temp, false);
            Desktop.getDesktop().print(temp);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't print song", ex);
        }
    }
    
    public void print(Schedule schedule) {
        try {
            File temp = File.createTempFile("schedule", ".pdf");
            temp.deleteOnExit();
            new SchedulePDFPrinter().print(schedule, temp);
            Desktop.getDesktop().print(temp);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't print song", ex);
        }
    }

}
