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
package org.quelea.services.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

/**
 * Compresses a file using the GZip format and maximum compression.
 *
 * @author mjrb5
 */
public class GZMaxFileCompressor {

    /**
     * Compresses the given file with maximum compression.
     *
     * @param input the input file to compress.
     * @param output the compressed file.
     * @return true if the operation completed successfully, false otherwise.
     */
    public boolean compress(File input, File output) {
        try(FileInputStream inputStream = new FileInputStream(input);
                GZipOutputStreamEx outputStream = new GZipOutputStreamEx(new FileOutputStream(output), 1024, Deflater.BEST_COMPRESSION)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0 ,length);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        System.out.println("Compressed " + input.getAbsolutePath() + " to " + output.getAbsolutePath() + " successfully.");
        return true;
    }
    
    /**
     * Compress a file with maximum compression based on arguments.
     * @param args first argument is source file, second is destination file.
     */
    public static void main(String[] args) {
        boolean success = new GZMaxFileCompressor().compress(new File(args[0]), new File(args[1]));
        if(!success) {
            System.exit(1);
        }
    }
}
