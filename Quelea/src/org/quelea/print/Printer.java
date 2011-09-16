/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.print;

import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.utils.LoggerUtils;

/**
 * Used for printing schedules.
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

    /**
     * Print the given printable object.
     * @param printable the object to print.
     */
    public void print(Printable printable) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
//        PageFormat pf = printJob.defaultPage();
//        Paper paper = new Paper();
//        double margin = 36; // half inch
//        paper.setImageableArea(margin, margin, paper.getWidth() - margin * 2,
//                paper.getHeight() - margin * 2);
//        pf.setPaper(paper);
        
        printJob.setPrintable(printable);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Couldn't print", ex);
            }
        }
    }
}
