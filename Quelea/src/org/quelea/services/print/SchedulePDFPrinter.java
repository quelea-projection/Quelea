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

import java.io.File;
import java.io.IOException;
import org.quelea.data.Schedule;

/**
 * Responsible for exporting a schedule to a PDF file.
 *
 * @author Michael
 */
public class SchedulePDFPrinter extends PDFPrinter {
    
    public void print(Schedule schedule, File pdfFile) throws IOException {
        print(schedule.getPrintXML(), new File("scheduleformat.xsl"), pdfFile);
    }
    
}
