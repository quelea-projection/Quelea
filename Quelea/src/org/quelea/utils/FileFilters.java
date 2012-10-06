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
package org.quelea.utils;

import java.io.File;
import javafx.stage.FileChooser;
import javax.swing.filechooser.FileFilter;
import org.quelea.languages.LabelGrabber;

/**
 * A class that contains all the file filters as a number of static final
 * fields.
 * <p/>
 * @author Michael
 */
public final class FileFilters {

    /**
     * The file filter used for the survivor songbooks.
     */
    public static final FileChooser.ExtensionFilter SURVIVOR_SONGBOOK = new FileChooser.ExtensionFilter("Survivor acetates file (acetates.pdf)", "acetates.pdf");
    /**
     * Accept XML bbibles.
     */
    public static final FileChooser.ExtensionFilter XML_BIBLE = new FileChooser.ExtensionFilter("XML bibles (*.xml)", "*.xml");
    /**
     * Only accept images.
     */
    public static final FileChooser.ExtensionFilter IMAGES = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("image.files.description"), Utils.getImageExtensions());
    /**
     * The file filter used for Quelea song packs.
     */
    public static final FileChooser.ExtensionFilter SONG_PACK = new FileChooser.ExtensionFilter("Quelea song pack (*.qsp)", "*.qsp");
    /**
     * The file filter used for Quelea schedules.
     */
    public static final FileChooser.ExtensionFilter SCHEDULE = new FileChooser.ExtensionFilter("Quelea schedules", "." + QueleaProperties.get().getScheduleExtension());

    /**
     * No instantiation for me thanks.
     */
    private FileFilters() {
        throw new AssertionError();
    }
}
