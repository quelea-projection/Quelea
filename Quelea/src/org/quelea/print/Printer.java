package org.quelea.print;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
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

    private Printer() {
        //Internal only
    }

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
