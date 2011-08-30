package org.quelea.print;

import java.awt.print.Printable;
import java.awt.print.PrinterJob;

/**
 * Used for printing schedules.
 * @author Michael
 */
public class Printer {

    private static volatile Printer instance;

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
        printJob.setPrintable(printable);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
