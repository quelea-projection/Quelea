package org.quelea.print;

import java.awt.print.PrinterJob;
import org.quelea.Schedule;

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

    public void print(Schedule schedule) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(schedule);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
