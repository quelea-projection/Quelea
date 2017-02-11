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
package org.quelea.data.imagegroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;

/**
 * Responsible for generating an image group
 *
 * @author Arvid
 */
public class ImageGroupFactory {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Generates a image group object from a group of files.
     *
     * @param files the files to generate the image group from.
     * @return the presentation object, or null if a problem occurs.
     */
    public ImageGroup getPresentation(File[] files) throws IOException {
        ArrayList<File> al = new ArrayList<>();
        for (File f : files) {
            if (Utils.fileIsImage(f)) {
                al.add(f);
            } else {
                LOGGER.log(Level.WARNING, "Illegal file type: {0}", f.getName());
            }
        }
        return new ImageGroupPresentation(al.toArray(new File[al.size()]));
    }
}
