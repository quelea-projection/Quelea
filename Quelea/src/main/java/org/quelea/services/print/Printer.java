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

import java.util.ArrayList;
import java.util.List;
import org.quelea.data.Schedule;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.utils.DesktopApi;
import org.quelea.windows.print.PrintPreviewDialog;
import org.quelea.windows.print.PrintPreviewDialog.Choice;

/**
 * Used for printing. Nothing is sent to the printer until the user has seen
 * a preview of the pages and approved them.
 *
 * @author Michael
 */
public class Printer {

    private static volatile Printer instance;

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
     * Show a preview of a song and print it if the user approves. Must be
     * called on the FX application thread.
     *
     * @param song the song to print.
     */
    public void print(SongDisplayable song) {
        List<Choice<Boolean>> choices = new ArrayList<>();
        String withoutChords = song.getPrintXML(false, false);
        choices.add(new Choice<>(false, LabelGrabber.INSTANCE.getLabel("print.song.lyrics"),
                pdf -> SongPDFPrinter.INSTANCE.printXML(withoutChords, pdf)));
        if (song.hasChords()) {
            String withChords = song.getPrintXML(false, true);
            choices.add(new Choice<>(true, LabelGrabber.INSTANCE.getLabel("print.song.lyrics.chords"),
                    pdf -> SongPDFPrinter.INSTANCE.printXML(withChords, pdf)));
        }
        PrintPreviewDialog<Boolean> dialog = new PrintPreviewDialog<>(
                LabelGrabber.INSTANCE.getLabel("library.print.song.text"), song.getTitle(),
                choices, QueleaProperties.get().getPrintSongChords());
        dialog.showAndGetApproved().ifPresent(approved -> {
            if (choices.size() > 1) {
                QueleaProperties.get().setPrintSongChords(approved.value());
            }
            DesktopApi.print(approved.pdf());
        });
    }

    /**
     * Show a preview of a schedule and print it if the user approves. The user
     * chooses whether to print only the order of service or the song lyrics
     * as well (with or without chords.) Must be called on the FX application
     * thread.
     *
     * @param schedule the schedule to print.
     */
    public void print(Schedule schedule) {
        List<Choice<SchedulePrintContent>> choices = new ArrayList<>();
        SchedulePDFPrinter pdfPrinter = new SchedulePDFPrinter();
        for (SchedulePrintContent content : SchedulePrintContent.values()) {
            // Gather the content now, the PDF is written on a background thread
            String xml = schedule.getPrintXML(content);
            choices.add(new Choice<>(content, LabelGrabber.INSTANCE.getLabel(content.getLabelKey()),
                    pdf -> pdfPrinter.printXML(xml, pdf)));
        }
        PrintPreviewDialog<SchedulePrintContent> dialog = new PrintPreviewDialog<>(
                LabelGrabber.INSTANCE.getLabel("print.schedule.text"),
                LabelGrabber.INSTANCE.getLabel("order.service.heading"),
                choices, QueleaProperties.get().getSchedulePrintContent());
        dialog.showAndGetApproved().ifPresent(approved -> {
            QueleaProperties.get().setSchedulePrintContent(approved.value());
            DesktopApi.print(approved.pdf());
        });
    }

}
