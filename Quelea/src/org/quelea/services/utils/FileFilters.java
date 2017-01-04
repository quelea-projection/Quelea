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

import javafx.stage.FileChooser;
import org.quelea.services.languages.LabelGrabber;

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
    public static final FileChooser.ExtensionFilter SURVIVOR_SONGBOOK = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.survivor.songbook") + " (acetates.pdf)", "acetates.pdf");
    /**
     * Accept XML bbibles.
     */
    public static final FileChooser.ExtensionFilter XML_BIBLE = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.xml.bibles") + " (*.xml, *.xmm)", "*.xml", "*.xmm");
    /**
     * Accept XML easyslide files.
     */
    public static final FileChooser.ExtensionFilter XML_EASYSLIDES = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.xml.easyslide") + " (*.xml)", "*.xml");
    /**
     * Accept generic XML files.
     */
    public static final FileChooser.ExtensionFilter XML_GENERIC = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.xml.files") + " (*.xml)", "*.xml");
    /**
     * Accept generic PDF files.
     */
    public static final FileChooser.ExtensionFilter PDF_GENERIC = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.pdf.files") + " (*.pdf)", "*.pdf");
    /**
     * Accept USR (songselect) files.
     */
    public static final FileChooser.ExtensionFilter USR_SS = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.usr.files") + " (*.usr)", "*.usr");
    /**
     * Accept video files.
     */
    public static final FileChooser.ExtensionFilter VIDEOS = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.video.files"), Utils.getFileExtensions(Utils.getVideoExtensions()));
    /**
     * Accept video files.
     */
    public static final FileChooser.ExtensionFilter IMAGE_VIDEOS = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.image.video.files"), Utils.getFileExtensions(Utils.getImageAndVideoExtensions()));
    /**
     * Accept audio files.
     */
    public static final FileChooser.ExtensionFilter AUDIOS = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.audio.files"), Utils.getFileExtensions(Utils.getAudioExtensions()));
    /**
     * Accept powerpoint files.
     */
    public static final FileChooser.ExtensionFilter POWERPOINT = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.powerpoint.presentations") + " (*.ppt, *.pptx)", "*.ppt", "*.pptx");
    /**
     * Only accept images.
     */
    public static final FileChooser.ExtensionFilter IMAGES = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("image.files.description"), Utils.getFileExtensions(Utils.getImageExtensions()));
    /**
     * Only accept timers.
     */
    public static final FileChooser.ExtensionFilter TIMERS = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("timer.files.description") +  "(*.cdt)", "*.cdt");
    /**
     * Only accept png files.
     */
    public static final FileChooser.ExtensionFilter PNG = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("png.files.description"), "*.png");
    /**
     * Only accept png files.
     */
    public static final FileChooser.ExtensionFilter RTF = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("rtf.files.description"), "*.rtf");
    /**
     * Only accept SDB (songpro database) files.
     */
    public static final FileChooser.ExtensionFilter SDB = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("sdb.files.description"), "*.sdb");
    /**
     * The file filter used for Quelea song packs.
     */
    public static final FileChooser.ExtensionFilter SONG_PACK = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("qsp.button") + " (*.qsp)", "*.qsp");
    /**
     * The file filter used for SongBeamer songs.
     */
    public static final FileChooser.ExtensionFilter SNG = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("sng.files.description") + " (*.sng)", "*.sng");
    /**
     * The file filter used for Quelea schedules.
     */
    public static final FileChooser.ExtensionFilter SCHEDULE = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.quelea.schedules"), "*." + QueleaProperties.get().getScheduleExtension());
    /**
     * The file filter used for plain text files.
     */
    public static final FileChooser.ExtensionFilter PLAIN_TEXT = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.plain.text.song"), "*.txt");
    /**
     * The file filter used for zip files.
     */
    public static final FileChooser.ExtensionFilter ZIP = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.zip.files") + " (*.zip)", "*.zip");
    /**
     * The file filter used for txt files.
     */
    public static final FileChooser.ExtensionFilter TXT = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.txt.files") + " (*.txt)", "*.txt");
    /**
     * The file filter used for SQLite files.
     */
    public static final FileChooser.ExtensionFilter SQLITE = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.sqlite.files") + " (*.sqlite)", "*.sqlite");
    /**
     * The file filter used for Presentation Manager songs.
     */
    public static final FileChooser.ExtensionFilter PM_SONG = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.pmsong.files") + " (*.sng)", "*.sng");
    /**
     * The file filter used for VideoPsalm databases.
     */
    public static final FileChooser.ExtensionFilter VS_DB = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.vs.files") + " (*.json)", "*.json");
    /**
     * The file filter used for MainTable.dat.
     */
    public static final FileChooser.ExtensionFilter MAINTABLE_DAT = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.maintable.dat") + " (MainTable.dat)", "MainTable.dat");
    /**
     * The file filter used for Songs.MB (the easyworship database.)
     */
    public static final FileChooser.ExtensionFilter SONGS_MB = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.songs.mb") + " (Songs.MB)", "Songs.MB");
    /**
     * The file filter used for Songs.MB (the easyworship database.)
     */
    public static final FileChooser.ExtensionFilter MISSION_PRAISE_RTF = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.songs.missionpraise") + " (*.rtf)", "*.rtf", "*.rtf'");
    /**
     * The file filter used for *.epc (the epicworship songpack.)
     */
    public static final FileChooser.ExtensionFilter EPC = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.epc") + " (*.epc)", "*.epc");
    /**
     * Accept multimedia files.
     */
    public static final FileChooser.ExtensionFilter MULTIMEDIA = new FileChooser.ExtensionFilter(LabelGrabber.INSTANCE.getLabel("filefilters.description.multimedia.files"), Utils.getFileExtensions(Utils.getMultimediaExtensions()));
    

    /**
     * No instantiation for me thanks.
     */
    private FileFilters() {
        throw new AssertionError();
    }
}
