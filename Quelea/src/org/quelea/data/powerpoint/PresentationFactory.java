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
package org.quelea.data.powerpoint;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;

/**
 * Responsible for generating presentations.
 *
 * @author mjrb5
 */
public class PresentationFactory {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Generates a presentation object from a file.
     *
     * @param file the file to generate the presentation from.
     * @return the presentation object, or null if a problem occurs.
     */
    public Presentation getPresentation(File file) throws IOException {
        Presentation ret = null;
        if(Utils.hasExtension(file, "ppt") || Utils.hasExtension(file, "pptx")) {
            try {
                ret = new PPTPresentation(file.getAbsolutePath());
            }
            catch(OfficeXmlFileException ex) {
                ret = new PPTXPresentation(file.getAbsolutePath());
            }
        }
        else {
            LOGGER.log(Level.WARNING, "Illegal file type: {0}", file.getName());
        }
        return ret;
    }
}
