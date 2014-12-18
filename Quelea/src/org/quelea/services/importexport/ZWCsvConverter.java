/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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

import java.io.File;
import java.io.IOException;
import org.quelea.services.utils.QueleaProperties;

/**
 * Responsible for converting the MainTable.dat ZionWorx database into a CSV
 * file.
 * <p>
 * @author Michael
 */
public class ZWCsvConverter {

    private File tdbExe;
    private File mainTable;

    /**
     * Create a new ZionWorx CSV converter for a particular table.
     * @param mainTable the file pointing to "MainTable.dat"
     */
    public ZWCsvConverter(File mainTable) {
        this.mainTable = mainTable;
        tdbExe = QueleaProperties.getTurboDBExe();
    }

    /**
     * Get the CSV file converted using TurboDB.
     * @return the CSV file from the ZionWorx database.
     * @throws IOException if something went wrong.
     */
    public File getCSV() throws IOException {
        File csvTemp = File.createTempFile("queleazionworximport", ".csv");
        csvTemp.deleteOnExit();
        Process process = new ProcessBuilder(tdbExe.getAbsolutePath(), mainTable.getAbsolutePath(), csvTemp.getAbsolutePath(), "-fsdf", "-s,", "-q\\").start();
        try {
            process.waitFor();
        }
        catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        return csvTemp;
    }

}