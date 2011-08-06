package org.quelea.utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A class that contains all the file filters as a number of static final fields.
 * @author Michael
 */
public final class FileFilters {

    /**
     * The file filter used for the survivor songbooks.
     */
    public static final FileFilter SURVIVOR_SONGBOOK = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if(f.isDirectory()
                    || f.getName().trim().equalsIgnoreCase("acetates.pdf")) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "acetates.pdf";
        }
    };

    /**
     * Accept only folders.
     */
    public static final FileFilter DIR_ONLY = new FileFilter() {

        @Override
        public boolean accept(File f) {
            return f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Folders";
        }
    };

    /**
     * Accept only folders.
     */
    public static final FileFilter XML_BIBLE = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return f.getName().toLowerCase().endsWith(".xml");
        }

        @Override
        public String getDescription() {
            return "XML bibles";
        }
    };

    /**
     * The file filter used for Quelea song packs.
     */
    public static final FileFilter SONG_PACK = new FileFilter() {

        private final String extension = QueleaProperties.get().getSongPackExtension();

        @Override
        public boolean accept(File f) {
            if(f.isDirectory()) {
                return true;
            }
            return f.getName().toLowerCase().endsWith("." + extension);
        }

        @Override
        public String getDescription() {
            return "Quelea song pack (." + extension + ")";
        }
    };

    /**
     * The file filter used for Quelea song packs.
     */
    public static final FileFilter SCHEDULE = new FileFilter() {

        private final String extension = QueleaProperties.get().getScheduleExtension();

        @Override
        public boolean accept(File f) {
            if(f.isDirectory()) {
                return true;
            }
            return f.getName().toLowerCase().endsWith("." + extension);
        }

        @Override
        public String getDescription() {
            return "Quelea schedules (." + extension + ")";
        }
    };

    /**
     * No instantiation for me thanks.
     */
    private FileFilters() {
        throw new AssertionError();
    }
}
