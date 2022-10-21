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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import org.quelea.data.bible.Bible;
import org.quelea.data.displayable.TextAlignment;
import org.quelea.services.languages.spelling.Dictionary;
import org.quelea.services.languages.spelling.DictionaryManager;
import org.quelea.services.notice.NoticeDrawer.NoticePosition;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

/**
 * Manages the properties specific to Quelea.
 * <p>
 *
 * @author Michael
 */
public final class QueleaProperties extends SortedProperties {

    public static final Version VERSION = new Version("2022.0", VersionType.CI);
    private static QueleaProperties INSTANCE;
    private String userHome;

    public static void init(String userHome) {
        INSTANCE = new QueleaProperties(userHome);
        try {
            if (!get().getPropFile().exists()) {
                get().getPropFile().createNewFile();
            }
            try (StringReader reader = new StringReader(Utils.getTextFromFile(get().getPropFile().getAbsolutePath(), ""))) {
                get().load(reader);
            }
        } catch (IOException ex) { //Never mind.
        }
    }

    /**
     * Load the properties from the properties file.
     */
    private QueleaProperties(String userHome) {
        if (userHome != null && !userHome.isEmpty()) {
            this.userHome = userHome;
        } else {
            this.userHome = System.getProperty("user.home");
        }
    }

    /**
     * Get the properties file.
     * <p>
     *
     * @return the properties file.
     */
    private File getPropFile() {
        return new File(getQueleaUserHome(), "quelea.properties");
    }

    /**
     * Save these properties to the file.
     */
    private void write() {
        try (FileWriter writer = new FileWriter(getPropFile())) {
            store(writer, "Auto save");
        } catch (IOException ex) {
//            LOGGER.log(Level.WARNING, "Couldn't store properties", ex);
        }
    }

    /**
     * Get the singleton instance of this class.
     * <p>
     *
     * @return the instance.
     */
    public static QueleaProperties get() {
        return INSTANCE;
    }

    /**
     * Get the languages file that should be used as specified in the properties
     * file.
     * <p>
     *
     * @return the languages file for the GUI.
     */
    public File getLanguageFile() {
        return new File("languages", getProperty(languageFileKey, "gb.lang"));
    }

    public boolean isDictionaryEnabled() {
        return Boolean.parseBoolean(getProperty(enableDictKey, "false"));
    }

    /**
     * Get the languages file that should be used as specified in the properties
     * file.
     * <p>
     *
     * @return the languages file for the GUI.
     */
    public Dictionary getDictionary() {
        String dict = getProperty(languageFileKey, "gb.lang");
        String[] parts = dict.split("\\.");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            builder.append(parts[i]);
            builder.append(".");
        }
        builder.append("words");
        return DictionaryManager.INSTANCE.getFromFilename(builder.toString());
    }

    /**
     * Set the name of the language file to use.
     * <p>
     *
     * @param file the name of the language file to use.
     */
    public void setLanguageFile(String file) {
        setProperty(languageFileKey, file);
        write();
    }

    /**
     * Get the english languages file that should be present on all
     * installations. We can default to this if labels are missing in other
     * languages.
     * <p>
     *
     * @return the english languages file for the GUI.
     */
    public File getEnglishLanguageFile() {
        return new File("languages", "gb.lang");
    }

    /**
     * Determine whether or not to display the video tab.
     *
     * @return true if the video tab should be displayed, false otherwise.
     */
    public boolean getDisplayVideoTab() {
        try {
            return Boolean.parseBoolean(getProperty(videoTabKey, "false"));
        } catch (Exception ex) {
            return true;
        }
    }

    public void setDisplayVideoTab(boolean videoTab) {
        setProperty(videoTabKey, Boolean.toString(videoTab));
        write();
    }

    /**
     * Get the scene info as stored from the last exit of Quelea (or some
     * default values if it doesn't exist in the properties file.)
     * <p>
     *
     * @return the scene info.
     */
    public SceneInfo getSceneInfo() {
        try {
            String[] parts = getProperty(sceneInfoKey, "461,15,997,995,false").split(",");
            if (parts.length == 4) {
                return new SceneInfo(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), false);
            } else if (parts.length == 5) {
                return new SceneInfo(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Boolean.parseBoolean(parts[4]));
            } else {
                return null;
            }
        } catch (Exception ex) {
            LoggerUtils.getLogger().log(Level.WARNING, "Invalid scene info: " + getProperty(sceneInfoKey), ex);
            return null;
        }
    }

    /**
     * Set the scene info for Quelea's main window - generally called just
     * before exit so the next invocation of the program can display the window
     * in the same position.
     * <p>
     *
     * @param info the scene info.
     */
    public void setSceneInfo(SceneInfo info) {
        setProperty(sceneInfoKey, info.toString());
        write();
    }

    /**
     * Get the main splitpane divider position property.
     *
     * @return the main splitpane divider position property, or -1 if none is
     * set.
     */
    public double getMainDivPos() {
        return Double.parseDouble(getProperty(mainDivposKey, "-1"));
    }

    public String getElevantoClientId() {
        return getProperty(elevantoClientIdKey, "91955");
    }

    /**
     * Get the library / schedule splitpane divider position property.
     *
     * @return the library / schedule splitpane divider position property, or -1
     * if none is set.
     */
    public double getLibraryDivPos() {
        return Double.parseDouble(getProperty(libraryDivposKey, "-1"));
    }

    /**
     * Get the preview / live splitpane divider position property.
     *
     * @return the preview / live splitpane divider position property, or -1 if
     * none is set.
     */
    public double getPrevLiveDivPos() {
        return Double.parseDouble(getProperty(preliveDivposKey, "-1"));
    }

    /**
     * Get the canvas divider position property.
     *
     * @return the canvas divider position property, or -1 if none is set.
     */
    public double getCanvasDivPos() {
        return Double.parseDouble(getProperty(canvasDivposKey, "-1"));
    }

    /**
     * Get the preview panel divider position property.
     *
     * @return the preview panel divider position property, or -1 if none is set.
     */
    public double getPreviewDivposKey() {
        return Double.parseDouble(getProperty(previewDivposKey, "-1"));
    }

    /**
     * Set the main divider position property.
     *
     * @param val the position of the divider 0-1.
     */
    public void setMainDivPos(double val) {
        setProperty(mainDivposKey, Double.toString(val));
        write();
    }

    /**
     * Set the preview / live divider position property.
     *
     * @param val the position of the divider 0-1.
     */
    public void setPrevLiveDivPos(double val) {
        setProperty(preliveDivposKey, Double.toString(val));
        write();
    }

    /**
     * Set the canvas divider position property.
     *
     * @param val the position of the divider 0-1.
     */
    public void setCanvasDivPos(double val) {
        setProperty(canvasDivposKey, Double.toString(val));
        write();
    }

    /**
     * Set the preview panel divider position property.
     *
     * @param val the position of the preview panel divider 0-1.
     */
    public void setPreviewDivPos(double val) {
        setProperty(previewDivposKey, Double.toString(val));
        write();
    }

    /**
     * Set the library divider position property.
     *
     * @param val the position of the divider 0-1.
     */
    public void setLibraryDivPos(double val) {
        setProperty(libraryDivposKey, Double.toString(val));
        write();
    }

    /**
     * Get a list of user chosen fonts to appear in the theme dialog.
     * <p>
     *
     * @return a list of user chosen fonts to appear in the theme dialog.
     */
    public List<String> getChosenFonts() {
        String fontStr = getProperty(chosenFontsKey, "Arial|Liberation Sans|Noto Sans|Oxygen|Roboto|Vegur|Roboto Mono|Ubuntu Mono");
        List<String> ret = new ArrayList<>();
        for (String str : fontStr.split("\\|")) {
            if (!str.trim().isEmpty()) {
                ret.add(str);
            }
        }
        return ret;
    }

    /**
     * Set a list of user chosen fonts to appear in the theme dialog.
     * <p>
     *
     * @param fonts the list of user chosen fonts to appear in the theme dialog.
     */
    public void setChosenFonts(List<String> fonts) {
        StringBuilder fontBuilder = new StringBuilder();
        for (int i = 0; i < fonts.size(); i++) {
            fontBuilder.append(fonts.get(i));
            if (i < fonts.size() - 1) {
                fontBuilder.append("|");
            }
        }
        setProperty(chosenFontsKey, fontBuilder.toString());
        write();
    }

    /**
     * Determine if the same font size should be used for each section in a
     * displayable - this can stop the sizes jumping all over the place
     * depending on how much text there is per slide.
     * <p>
     *
     * @return true if the uniform font size should be used, false otherwise.
     */
    public boolean getUseUniformFontSize() {
        return Boolean.parseBoolean(getProperty(uniformFontSizeKey, "true"));
    }

    /**
     * Set if the same font size should be used for each section in a
     * displayable - this can stop the sizes jumping all over the place
     * depending on how much text there is per slide.
     * <p>
     *
     * @param val true if the uniform font size should be used, false otherwise.
     */
    public void setUseUniformFontSize(boolean val) {
        setProperty(uniformFontSizeKey, Boolean.toString(val));
    }

    /**
     * Determine if we should show verse numbers for bible passages.
     * <p>
     *
     * @return true if we should show verse numbers, false otherwise.
     */
    public boolean getShowVerseNumbers() {
        return Boolean.parseBoolean(getProperty(showVerseNumbersKey, "true"));
    }

    /**
     * Set if we should show verse numbers for bible passages.
     * <p>
     *
     * @param val true if we should show verse numbers, false otherwise.
     */
    public void setShowVerseNumbers(boolean val) {
        setProperty(showVerseNumbersKey, Boolean.toString(val));
    }

    /**
     * Get the colour to use for notice backgrounds.
     *
     * @return the colour to use for notice backgrounds.
     */
    public Color getNoticeBackgroundColour() {
        return getColor(getProperty(noticeBackgroundColourKey, getStr(Color.BROWN)));
    }

    /**
     * Set the colour to use for notice backgrounds.
     *
     * @param colour the colour to use for notice backgrounds.
     */
    public void setNoticeBackgroundColour(Color colour) {
        setProperty(noticeBackgroundColourKey, getStr(colour));
    }

    /**
     * Get the position at which to display the notices.
     *
     * @return the position at which to display the notices.
     */
    public NoticePosition getNoticePosition() {
        if (getProperty(noticePositionKey, "Bottom").equalsIgnoreCase("top")) {
            return NoticePosition.TOP;
        } else {
            return NoticePosition.BOTTOM;
        }
    }

    /**
     * Set the position at which to display the notices.
     *
     * @param position the position at which to display the notices.
     */
    public void setNoticePosition(NoticePosition position) {
        setProperty(noticePositionKey, position.getText());
    }

    /**
     * Get the speed at which to display the notices.
     *
     * @return the speed at which to display the notices.
     */
    public double getNoticeSpeed() {
        return Double.parseDouble(getProperty(noticeSpeedKey, "10"));
    }

    /**
     * Set the speed at which to display the notices.
     *
     * @param speed the speed at which to display the notices.
     */
    public void setNoticeSpeed(double speed) {
        setProperty(noticeSpeedKey, Double.toString(speed));
    }

    /**
     * Get the last directory used in the general file chooser.
     *
     * @return the last directory used in the general file chooser.
     */
    public File getLastDirectory() {
        String path = getProperty(lastDirectoryKey);
        if (path == null) {
            return null;
        }
        File f = new File(path);
        if (f.isDirectory()) {
            return f;
        } else {
            LoggerUtils.getLogger().log(Level.INFO, "Cannot find last directory, reverting to default location");
            return null;
        }
    }

    /**
     * Set the last directory used in the general file chooser.
     *
     * @param directory the last directory used in the general file chooser.
     */
    public void setLastDirectory(File directory) {
        setProperty(lastDirectoryKey, directory.getAbsolutePath());
    }

    /**
     * Get the last directory used in the schedule file chooser.
     *
     * @return the last directory used in the schedule file chooser.
     */
    public File getLastScheduleFileDirectory() {
        String path = getProperty(lastSchedulefileDirectoryKey);
        if (path == null) {
            return null;
        }
        File f = new File(path);
        if (f.isDirectory()) {
            return f;
        } else {
            LoggerUtils.getLogger().log(Level.INFO, "Cannot find last schedule directory, reverting to default location");
            return null;
        }
    }

    /**
     * Sets whether the schedule should embed videos when saving
     *
     * @param embed true if should embed, false otherwise
     */
    public void setEmbedMediaInScheduleFile(boolean embed) {
        setProperty(scheduleEmbedMediaKey, embed + "");
    }

    /**
     * Gets whether the schedule should embed videos when saving
     *
     * @return true if should embed, false otherwise
     */
    public boolean getEmbedMediaInScheduleFile() {
        boolean ret = Boolean.parseBoolean(getProperty(scheduleEmbedMediaKey, "true"));
        return ret;
    }

    /**
     * Sets whether item themes can override the global theme.
     *
     * @param val true if should override, false otherwise
     */
    public void setItemThemeOverride(boolean val) {
        setProperty(itemThemeOverrideKey, val + "");
    }

    /**
     * Gets whether item themes can override the global theme.
     *
     * @return true if should override, false otherwise
     */
    public boolean getItemThemeOverride() {
        boolean ret = Boolean.parseBoolean(getProperty(itemThemeOverrideKey, "false"));
        return ret;
    }

    /**
     * Set the currently selected global theme file.
     */
    public void setGlobalSongThemeFile(File file) {
        if (file == null) {
            setProperty(globalSongThemeFileKey, "");
        } else {
            setProperty(globalSongThemeFileKey, file.getAbsolutePath());
        }
    }

    /**
     * Get the currently selected global theme file.
     */
    public File getGlobalSongThemeFile() {
        String path = getProperty(globalSongThemeFileKey);
        if (path == null || path.isEmpty()) {
            return null;
        }
        return new File(path);
    }

    /**
     * Set the currently selected global theme file.
     */
    public void setGlobalBibleThemeFile(File file) {
        if (file == null) {
            setProperty(globalBibleThemeFileKey, "");
        } else {
            setProperty(globalBibleThemeFileKey, file.getAbsolutePath());
        }
    }

    /**
     * Get the currently selected global theme file.
     */
    public File getGlobalBibleThemeFile() {
        String path = getProperty(globalBibleThemeFileKey);
        if (path == null || path.isEmpty()) {
            return null;
        }
        return new File(path);
    }

    /**
     * Set the last directory used in the schedule file chooser.
     *
     * @param directory the last directory used in the schedule file chooser.
     */
    public void setLastScheduleFileDirectory(File directory) {
        setProperty(lastSchedulefileDirectoryKey, directory.getAbsolutePath());
    }

    /**
     * Get the last directory used in the video file chooser.
     *
     * @return the last directory used in the video file chooser.
     */
    public File getLastVideoDirectory() {
        String path = getProperty(lastVideoDirectoryKey);
        if (path == null) {
            return null;
        }
        File f = new File(path);
        if (f.isDirectory()) {
            return f;
        } else {
            LoggerUtils.getLogger().log(Level.INFO, "Cannot find last video directory, reverting to default location");
            return null;
        }
    }

    /**
     * Set the last directory used in the video file chooser.
     *
     * @param directory the last directory used in the video file chooser.
     */
    public void setLastVideoDirectory(File directory) {
        setProperty(lastVideoDirectoryKey, directory.getAbsolutePath());
    }

    /**
     * Determine whether to auto-play videos after they have been set in live
     * view.
     *
     * @return true if auto play is enabled, false otherwise.
     */
    public boolean getAutoPlayVideo() {
        return Boolean.parseBoolean(getProperty(autoplayVidKey, "false"));
    }

    /**
     * Set whether to auto-play videos after they have been set in live view.
     *
     * @param val true to enable auto play, false otherwise.
     */
    public void setAutoPlayVideo(boolean val) {
        setProperty(autoplayVidKey, Boolean.toString(val));
    }

    /**
     * Determine whether to use Java FX rendering for video playback with VLC.
     * This approach is totally cross-platform capable.
     *
     * @return true if should use java fx for VLC Rendering, false otherwise
     */
    public boolean getUseJavaFXforVLCRendering() {
        return Boolean.parseBoolean(getProperty(useVlcJavafxRenderingKey, "false"));
    }

    /**
     * Set whether to use Java FX rendering for video playback with VLC. This
     * approach is totally cross-platform capable.
     *
     * @param val true if should use java fx for VLC Rendering, false otherwise.
     */
    public void setUseJavaFXforVLCRendering(boolean val) {
        setProperty(useVlcJavafxRenderingKey, Boolean.toString(val));
    }

    /**
     * Get the font size at which to display the notices.
     *
     * @return the font size at which to display the notices.
     */
    public double getNoticeFontSize() {
        return Double.parseDouble(getProperty(noticeFontSizeKey, "50"));
    }

    /**
     * Set the font size at which to display the notices.
     *
     * @param fontSize the font size at which to display the notices.
     */
    public void setNoticeFontSize(double fontSize) {
        setProperty(noticeFontSizeKey, Double.toString(fontSize));
    }

    /**
     * Determine if we should attempt to fetch translations automatically.
     * <p>
     *
     * @return true if we should translate automatically, false otherwise.
     */
    public boolean getAutoTranslate() {
        return Boolean.parseBoolean(getProperty(autoTranslateKey, "true"));
    }

    /**
     * Set if we should attempt to fetch translations automatically.
     * <p>
     *
     * @param val true if we should translate automatically, false otherwise.
     */
    public void setAutoTranslate(boolean val) {
        setProperty(autoTranslateKey, Boolean.toString(val));
    }

    /**
     * Get the maximum font size used by text displayables.
     * <p>
     *
     * @return the maximum font size used by text displayables.
     */
    public double getMaxFontSize() {
        return Double.parseDouble(getProperty(maxFontSizeKey, "1000"));
    }

    /**
     * Set the maximum font size used by text displayables.
     * <p>
     *
     * @param fontSize the maximum font size used by text displayables.
     */
    public void setMaxFontSize(double fontSize) {
        setProperty(maxFontSizeKey, Double.toString(fontSize));
    }

    /**
     * Get the additional line spacing (in pixels) to be used between each line.
     * <p>
     *
     * @return the additional line spacing.
     */
    public double getAdditionalLineSpacing() {
        return Double.parseDouble(getProperty(additionalLineSpacingKey, "10"));
    }

    /**
     * Set the additional line spacing (in pixels) to be used between each line.
     * <p>
     *
     * @param spacing the additional line spacing.
     */
    public void setAdditionalLineSpacing(double spacing) {
        setProperty(additionalLineSpacingKey, Double.toString(spacing));
    }

    /**
     * Get the thumbnail size.
     * <p>
     *
     * @return the thumbnail size.
     */
    public int getThumbnailSize() {
        return Integer.parseInt(getProperty(thumbnailSizeKey, "200"));
    }

    /**
     * Set the thumbnail size.
     * <p>
     *
     * @param thumbnailSize the thumbnail size.
     */
    public void setThumbnailSize(int thumbnailSize) {
        setProperty(thumbnailSizeKey, Integer.toString(thumbnailSize));
    }

    public int getPlanningCentrePrevDays() {
        return Integer.parseInt(getProperty(planningCentrePrevDaysKey, "31"));
    }

    public void setPlanningCentrePrevDays(int days) {
        setProperty(planningCentrePrevDaysKey, Integer.toString(days));
    }

    public boolean getUseDefaultTranslation() {
        return Boolean.parseBoolean(getProperty(useDefaultTranslation, "false"));
    }

    public void setUseDefaultTranslation(boolean val) {
        setProperty(useDefaultTranslation, Boolean.toString(val));
    }

    public String getDefaultTranslationName() {
        return getProperty(defaultTranslationName, "");
    }

    public void setDefaultTranslationName(String val) {
        setProperty(defaultTranslationName, val);
    }

    /**
     * Get the show extra live panel toolbar options setting.
     * <p>
     *
     * @return the true to show extra toolbar options.
     */
    public boolean getShowExtraLivePanelToolbarOptions() {
        return Boolean.parseBoolean(getProperty(showExtraLivePanelToolbarOptionsKey, "false"));
    }

    /**
     * Set the show extra live panel toolbar options setting.
     * <p>
     *
     * @param show the extra options or leave them hidden.
     */
    public void setShowExtraLivePanelToolbarOptions(boolean show) {
        setProperty(showExtraLivePanelToolbarOptionsKey, Boolean.toString(show));
    }

    /**
     * Get the setting for whether the preview and live dividers should be linked. eg move together
     * <p>
     *
     * @return true if the preview and live dividers should be linked, else false
     */
    public boolean getLinkPreviewAndLiveDividers() {
        return Boolean.parseBoolean(getProperty(linkPreviewAndLiveDividers, "true"));
    }

    /**
     * Determine if, when an item is removed from the schedule and displayed on
     * the live view, whether it should be removed from the live view or kept
     * until something replaces it.
     * <p>
     *
     * @return true if it should be cleared, false otherwise.
     */
    public boolean getClearLiveOnRemove() {
        return Boolean.parseBoolean(getProperty(clearLiveOnRemoveKey, "true"));
    }

    /**
     * Set if, when an item is removed from the schedule and displayed on the
     * live view, whether it should be removed from the live view or kept until
     * something replaces it.
     * <p>
     *
     * @param val true if it should be cleared, false otherwise.
     */
    public void setClearLiveOnRemove(boolean val) {
        setProperty(clearLiveOnRemoveKey, Boolean.toString(val));
    }

    /**
     * Get the location of Quelea's Facebook page.
     * <p>
     *
     * @return the location of the facebook page.
     */
    public String getFacebookPageLocation() {
        return getProperty(facebookPageKey, "http://www.facebook.com/quelea.projection");
    }

    /**
     * Get the location of Quelea's Facebook page.
     * <p>
     *
     * @return the location of the facebook page.
     */
    public String getWikiPageLocation() {
        return getProperty(wikiPageKey, "http://quelea.org/wiki/index.php/Main_Page");
    }

    /**
     * Get the Quelea home directory in the user's directory.
     * <p>
     *
     * @return the Quelea home directory.
     */
    public File getQueleaUserHome() {
        File ret = new File(new File(userHome), ".quelea");
        if (!ret.exists()) {
            ret.mkdir();
        }
        return ret;
    }

    /**
     * Get the user's turbo db exe converter file.
     * <p>
     *
     * @return the user's turbo db exe converter.
     */
    public File getTurboDBExe() {
        return new File(getQueleaUserHome(), "TdbDataX.exe");
    }

    public int getTranslationFontSizeOffset() {
        return Integer.parseInt(getProperty(translationFontSizeOffsetKey, "3"));
    }

    /**
     * Get the font to use for stage text.
     * <p>
     *
     * @return the font to use for stage text.
     */
    public String getStageTextFont() {
        return getProperty(stageFontKey, "SansSerif");
    }

    /**
     * Set the font to use for stage text.
     * <p>
     *
     * @param font the font to use for stage text.
     */
    public void setStageTextFont(String font) {
        setProperty(stageFontKey, font);
        write();
    }

    /**
     * Get the alignment of the text on stage view.
     * <p>
     *
     * @return the alignment of the text on stage view.
     */
    public String getStageTextAlignment() {
        return TextAlignment.parse(getProperty(stageTextAlignmentKey, "LEFT")).toFriendlyString();
    }

    /**
     * Set the alignment of the text on stage view.
     * <p>
     *
     * @param alignment the alignment of the text on stage view.
     */
    public void setStageTextAlignment(TextAlignment alignment) {
        setProperty(stageTextAlignmentKey, alignment.toString());
        write();
    }

    /**
     * Get whether we should display the chords in stage view.
     * <p>
     *
     * @return true if they should be displayed, false otherwise.
     */
    public boolean getShowChords() {
        return Boolean.parseBoolean(getProperty(stageShowChordsKey, "true"));
    }

    /**
     * Set whether we should display the chords in stage view.
     * <p>
     *
     * @param showChords true if they should be displayed, false otherwise.
     */
    public void setShowChords(boolean showChords) {
        setProperty(stageShowChordsKey, Boolean.toString(showChords));
        write();
    }

    /**
     * Determine whether we should phone home at startup with anonymous
     * information. Simply put phonehome=false in the properties file to disable
     * phonehome.
     * <p>
     *
     * @return true if we should phone home, false otherwise.
     */
    public boolean getPhoneHome() {
        return Boolean.parseBoolean(getProperty(phonehomeKey, "true"));
    }

    /**
     * Get the directory used for storing the bibles.
     * <p>
     *
     * @return the bibles directory.
     */
    public File getBibleDir() {
        return new File(getQueleaUserHome(), "bibles");
    }

    /**
     * Get the directory used for storing images.
     * <p>
     *
     * @return the img directory
     */
    public File getImageDir() {
        return new File(getQueleaUserHome(), "img");
    }

    /**
     * Get the directory used for storing dictionaries.
     * <p>
     *
     * @return the dictionaries directory
     */
    public File getDictionaryDir() {
        return new File(getQueleaUserHome(), "dictionaries");
    }

    /**
     * Get the directory used for storing videos.
     * <p>
     *
     * @return the vid directory
     */
    public File getVidDir() {
        return new File(getQueleaUserHome(), "vid");
    }

    /**
     * Get the directory used for storing temporary recordings.
     * <p>
     *
     * @return the temp directory
     */
    public File getTempDir() {
        return new File(getQueleaUserHome(), "temp");
    }

    /**
     * Get the extension used for quelea schedules.
     * <p>
     *
     * @return the extension used for quelea schedules.
     */
    public String getScheduleExtension() {
        return getProperty(queleaScheduleExtensionKey, "qsch");
    }

    /**
     * Get the extension used for quelea song packs.
     * <p>
     *
     * @return the extension used for quelea song packs.
     */
    public String getSongPackExtension() {
        return getProperty(queleaSongpackExtensionKey, "qsp");
    }

    /**
     * Get the number of the screen used for the control screen. This is the
     * screen that the main Quelea operator window will be displayed on.
     * <p>
     *
     * @return the control screen number.
     */
    public int getControlScreen() {
        return Integer.parseInt(getProperty(controlScreenKey, "0"));
    }

    /**
     * Set the control screen output.
     * <p>
     *
     * @param screen the number of the screen to use for the output.
     */
    public void setControlScreen(int screen) {
        setProperty(controlScreenKey, Integer.toString(screen));
        write();
    }

    /**
     * Get the one line mode.
     * <p>
     *
     * @return true if one line mode should be enabled, false otherwise.
     */
    public boolean getOneLineMode() {
        return Boolean.parseBoolean(getProperty(oneLineModeKey, "false"));
    }

    /**
     * Set the one line mode property.
     * <p>
     *
     * @param val the value of the one linde mode.
     */
    public void setOneLineMode(boolean val) {
        setProperty(oneLineModeKey, Boolean.toString(val));
        write();
    }

    /**
     * Get the text shadow property.
     * <p>
     *
     * @return true if text shadows are enabled, false otherwise.
     */
    public boolean getTextShadow() {
        return Boolean.parseBoolean(getProperty(textShadowKey, "false"));
    }

    /**
     * Set the text shadow property.
     * <p>
     *
     * @param val true if text shadows are enabled, false otherwise.
     */
    public void setTextShadow(boolean val) {
        setProperty(textShadowKey, Boolean.toString(val));
        write();
    }

    /**
     * Get the number of the projector screen. This is the screen that the
     * projected output will be displayed on.
     * <p>
     *
     * @return the projector screen number.
     */
    public int getProjectorScreen() {
        return Integer.parseInt(getProperty(projectorScreenKey, "1"));
    }

    /**
     * Set the control screen output.
     * <p>
     *
     * @param screen the number of the screen to use for the output.
     */
    public void setProjectorScreen(int screen) {
        setProperty(projectorScreenKey, Integer.toString(screen));
        write();
    }

    /**
     * Determine whether the projection screen automatically should be moved to
     * a recently inserted monitor.
     * * <p/>
     *
     * @return true if the projector screen should be moved, false otherwise.
     */
    public boolean getUseAutoExtend() {
        return Boolean.parseBoolean(getProperty(useAutoExtendKey, "false"));
    }

    /**
     * Set whether the projection screen automatically should be moved to a
     * recently inserted monitor.
     * * <p/>
     *
     * @param extend true if it should automatically move projection screen,
     *               false otherwise.
     */
    public void setUseAutoExtend(boolean extend) {
        setProperty(useAutoExtendKey, Boolean.toString(extend));
    }

    /**
     * Get the maximum number of characters allowed on any one line of projected
     * text. If the line is longer than this, it will be split up intelligently.
     * <p>
     *
     * @return the maximum number of characters allowed on any one line of
     * projected text.
     */
    public int getMaxChars() {
        return Integer.parseInt(getProperty(maxCharsKey, "30"));
    }

    /**
     * Set the max chars value.
     * <p>
     *
     * @param maxChars the maximum number of characters allowed on any one line
     *                 of projected text.
     */
    public void setMaxChars(int maxChars) {
        setProperty(maxCharsKey, Integer.toString(maxChars));
        write();
    }

    /**
     * Get the custom projector co-ordinates.
     * <p>
     *
     * @return the co-ordinates.
     */
    public Bounds getProjectorCoords() {
        String[] prop = getProperty(projectorCoordsKey, "0,0,0,0").trim().split(",");
        return new BoundingBox(Integer.parseInt(prop[0]),
                Integer.parseInt(prop[1]),
                Integer.parseInt(prop[2]),
                Integer.parseInt(prop[3]));
    }

    /**
     * Set the custom projector co-ordinates.
     * <p>
     *
     * @param coords the co-ordinates to set.
     */
    public void setProjectorCoords(Bounds coords) {
        String rectStr = Integer.toString((int) coords.getMinX())
                + "," + Integer.toString((int) coords.getMinY())
                + "," + Integer.toString((int) coords.getWidth())
                + "," + Integer.toString((int) coords.getHeight());

        setProperty(projectorCoordsKey, rectStr);
        write();
    }

    public void setXProjectorCoord(String x) {
        String[] prop = getProperty(projectorCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = x
                + "," + prop[1]
                + "," + prop[2]
                + "," + prop[3];
        setProperty(projectorCoordsKey, rectStr);
        write();
    }

    public void setYProjectorCoord(String y) {
        String[] prop = getProperty(projectorCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = prop[0]
                + "," + y
                + "," + prop[2]
                + "," + prop[3];
        setProperty(projectorCoordsKey, rectStr);
        write();
    }

    public void setWidthProjectorCoord(String width) {
        String[] prop = getProperty(projectorCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = prop[0]
                + "," + prop[1]
                + "," + width
                + "," + prop[3];
        setProperty(projectorCoordsKey, rectStr);
        write();
    }

    public void setHeightProjectorCoord(String height) {
        String[] prop = getProperty(projectorCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = prop[0]
                + "," + prop[1]
                + "," + prop[2]
                + "," + height;
        setProperty(projectorCoordsKey, rectStr);
        write();
    }

    public void setXStageCoord(String x) {
        String[] prop = getProperty(stageCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = x
                + "," + prop[1]
                + "," + prop[2]
                + "," + prop[3];
        setProperty(stageCoordsKey, rectStr);
        write();
    }

    public void setYStageCoord(String y) {
        String[] prop = getProperty(stageCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = prop[0]
                + "," + y
                + "," + prop[2]
                + "," + prop[3];
        setProperty(stageCoordsKey, rectStr);
        write();
    }

    public void setWidthStageCoord(String width) {
        String[] prop = getProperty(stageCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = prop[0]
                + "," + prop[1]
                + "," + width
                + "," + prop[3];
        setProperty(stageCoordsKey, rectStr);
        write();
    }

    public void setHeightStageCoord(String height) {
        String[] prop = getProperty(stageCoordsKey, "0,0,0,0").trim().split(",");
        String rectStr = prop[0]
                + "," + prop[1]
                + "," + prop[2]
                + "," + height;
        setProperty(stageCoordsKey, rectStr);
        write();
    }

    /**
     * Determine if the projector mode is set to manual co-ordinates or a screen
     * number.
     * <p>
     *
     * @return true if it's set to manual co-ordinates, false if it's a screen
     * number.
     */
    public boolean isProjectorModeCoords() {
        return "coords".equals(getProperty(projectorModeKey));
    }

    /**
     * Set the projector mode to be manual co-ordinates.
     */
    public void setProjectorModeCoords() {
        setProperty(projectorModeKey, "coords");
        write();
    }

    /**
     * Set the projector mode to be a screen number.
     */
    public void setProjectorModeScreen() {
        setProperty(projectorModeKey, "screen");
        write();
    }

    /**
     * Get the number of the stage screen. This is the screen that the projected
     * output will be displayed on.
     * <p>
     *
     * @return the stage screen number.
     */
    public int getStageScreen() {
        return Integer.parseInt(getProperty(stageScreenKey, "-1"));
    }

    /**
     * Set the stage screen output.
     * <p>
     *
     * @param screen the number of the screen to use for the output.
     */
    public void setStageScreen(int screen) {
        setProperty(stageScreenKey, Integer.toString(screen));
        write();
    }

    /**
     * Get the custom stage screen co-ordinates.
     * <p>
     *
     * @return the co-ordinates.
     */
    public Bounds getStageCoords() {
        String[] prop = getProperty(stageCoordsKey, "0,0,0,0").trim().split(",");
        return new BoundingBox(Integer.parseInt(prop[0]),
                Integer.parseInt(prop[1]),
                Integer.parseInt(prop[2]),
                Integer.parseInt(prop[3]));
    }

    /**
     * Set the custom stage screen co-ordinates.
     * <p>
     *
     * @param coords the co-ordinates to set.
     */
    public void setStageCoords(Bounds coords) {
        String rectStr = Integer.toString((int) coords.getMinX())
                + "," + Integer.toString((int) coords.getMinY())
                + "," + Integer.toString((int) coords.getWidth())
                + "," + Integer.toString((int) coords.getHeight());

        setProperty(stageCoordsKey, rectStr);
        write();
    }

    /**
     * Determine if the stage mode is set to manual co-ordinates or a screen
     * number.
     * <p>
     *
     * @return true if it's set to manual co-ordinates, false if it's a screen
     * number.
     */
    public boolean isStageModeCoords() {
        return "coords".equals(getProperty(stageModeKey));
    }

    /**
     * Set the stage mode to be manual co-ordinates.
     */
    public void setStageModeCoords() {
        setProperty(stageModeKey, "coords");
        write();
    }

    /**
     * Set the stage mode to be a screen number.
     */
    public void setStageModeScreen() {
        setProperty(stageModeKey, "screen");
        write();
    }

    /**
     * Get the minimum number of lines that should be displayed on each page.
     * This purely applies to font sizes, the font will be adjusted so this
     * amount of lines can fit on. This stops small lines becoming huge in the
     * preview window rather than displaying normally.
     * <p>
     *
     * @return the minimum line count.
     */
    public int getMinLines() {
        return Integer.parseInt(getProperty(minLinesKey, "10"));
    }

    /**
     * Set the min lines value.
     * <p>
     *
     * @param minLines the minimum line count.
     */
    public void setMinLines(int minLines) {
        setProperty(minLinesKey, Integer.toString(minLines));
        write();
    }

    /**
     * Determine whether the single monitor warning should be shown (this warns
     * the user they only have one monitor installed.)
     * <p>
     *
     * @return true if the warning should be shown, false otherwise.
     */
    public boolean showSingleMonitorWarning() {
        return Boolean.parseBoolean(getProperty(singleMonitorWarningKey, "true"));
    }

    /**
     * Set whether the single monitor warning should be shown.
     * <p>
     *
     * @param val true if the warning should be shown, false otherwise.
     */
    public void setSingleMonitorWarning(boolean val) {
        setProperty(singleMonitorWarningKey, Boolean.toString(val));
        write();
    }

    /**
     * Get the URL to download Quelea.
     * <p>
     *
     * @return the URL to download Quelea.
     */
    public String getDownloadLocation() {
        return "https://github.com/quelea-projection/Quelea/releases/";
    }

    /**
     * Get the URL to the Quelea website.
     * <p>
     *
     * @return the URL to the Quelea website.
     */
    public String getWebsiteLocation() {
        return getProperty(websiteLocationKey, "http://www.quelea.org/");
    }

    /**
     * Get the URL to the Quelea discussion forum.
     * <p>
     *
     * @return the URL to the Quelea discussion forum.
     */
    public String getDiscussLocation() {
        return getProperty(discussLocationKey, "https://quelea.discourse.group/");
    }

    /**
     * Get the URL to the Quelea feedback form.
     * <p>
     *
     * @return the URL to the Quelea feedback form.
     */
    public String getFeedbackLocation() {
        return getProperty(feedbackLocationKey, "https://quelea.org/feedback/");
    }

    /**
     * Get the URL used for checking the latest version.
     * <p>
     *
     * @return the URL used for checking the latest version.
     */
    public String getUpdateURL() {
        return "https://quelea-projection.github.io/changelog";
    }

    /**
     * Determine whether we should check for updates each time the program
     * starts.
     * <p>
     *
     * @return true if we should check for updates, false otherwise.
     */
    public boolean checkUpdate() {
        return Boolean.parseBoolean(getProperty(checkUpdateKey, "true"));
    }

    /**
     * Set whether we should check for updates each time the program starts.
     * <p>
     *
     * @param val true if we should check for updates, false otherwise.
     */
    public void setCheckUpdate(boolean val) {
        setProperty(checkUpdateKey, Boolean.toString(val));
        write();
    }

    /**
     * Determine whether the first letter of all displayed lines should be a
     * capital.
     * <p>
     *
     * @return true if it should be a capital, false otherwise.
     */
    public boolean checkCapitalFirst() {
        return Boolean.parseBoolean(getProperty(capitalFirstKey, "false"));
    }

    /**
     * Set whether the first letter of all displayed lines should be a capital.
     * <p>
     *
     * @param val true if it should be a capital, false otherwise.
     */
    public void setCapitalFirst(boolean val) {
        setProperty(capitalFirstKey, Boolean.toString(val));
        write();
    }

    /**
     * Determine whether the song info text should be displayed.
     * <p>
     *
     * @return true if it should be a displayed, false otherwise.
     */
    public boolean checkDisplaySongInfoText() {
        return Boolean.parseBoolean(getProperty(displaySonginfotextKey, "true"));
    }

    /**
     * Set whether the song info text should be displayed.
     * <p>
     *
     * @param val true if it should be displayed, false otherwise.
     */
    public void setDisplaySongInfoText(boolean val) {
        setProperty(displaySonginfotextKey, Boolean.toString(val));
        write();
    }

    /**
     * Get the default bible to use.
     * <p>
     *
     * @return the default bible.
     */
    public String getDefaultBible() {
        return getProperty(defaultBibleKey);
    }

    /**
     * Set the default bible.
     * <p>
     *
     * @param bible the default bible.
     */
    public void setDefaultBible(Bible bible) {
        setProperty(defaultBibleKey, bible.getName());
        write();
    }

    /**
     * Get the colour used to display chords in stage view.
     * <p>
     *
     * @return the colour used to display chords in stage view.
     */
    public Color getStageChordColor() {
        return getColor(getProperty(stageChordColorKey, "200,200,200"));
    }


    /**
     * Get the colour used to display chords in stage view.
     * <p>
     *
     * @return the colour used to display chords in stage view.
     */
    public Color getTextBackgroundColor() {
        return getColor(getProperty(lyricsTextBackgroundColor));
    }


    /**
     * Determine whether to advance the scheudle item when the current item is
     * sent live.
     * <p>
     *
     * @return true if we should auto-advance, false otherwise.
     */
    public boolean getTextBackgroundEnable() {
        return Boolean.parseBoolean(getProperty(lyricsTextBackgroundEnable, "false"));
    }

    /**
     * Set the colour used to display chords in stage view.
     * <p>
     *
     * @param color the colour used to display chords in stage view.
     */
    public void setStageChordColor(Color color) {
        setProperty(stageChordColorKey, getStr(color));
    }

    /**
     * Get the colour used to display lyrics in stage view.
     * <p>
     *
     * @return the colour used to display lyrics in stage view.
     */
    public Color getStageLyricsColor() {
        return getColor(getProperty(stageLyricsColorKey, "255,255,255"));
    }

    /**
     * Set the colour used to display lyrics in stage view.
     * <p>
     *
     * @param color the colour used to display lyrics in stage view.
     */
    public void setStageLyricsColor(Color color) {
        setProperty(stageLyricsColorKey, getStr(color));
    }

    /**
     * Set the colour used for the background in stage view.
     * <p>
     *
     * @param color the colour used for the background in stage view.
     */
    public void setStageBackgroundColor(Color color) {
        setProperty(stageBackgroundColorKey, getStr(color));
    }

    /**
     * Get the colour used for the background in stage view.
     * <p>
     *
     * @return the colour used for the background in stage view.
     */
    public Color getStageBackgroundColor() {
        return getColor(getProperty(stageBackgroundColorKey, "0,0,0"));
    }

    /**
     * Get a color from a string.
     * <p>
     *
     * @param str the string to use to get the color value.
     * @return the color.
     */
    private Color getColor(String str) {
        String[] color = str.split(",");
        double red = Double.parseDouble(color[0].trim());
        double green = Double.parseDouble(color[1].trim());
        double blue = Double.parseDouble(color[2].trim());
        if (red > 1 || green > 1 || blue > 1) {
            red /= 255;
            green /= 255;
            blue /= 255;
        }
        return new Color(red, green, blue, 1);
    }

    /**
     * Get a color value as a string.
     * <p>
     *
     * @param color the color to get as a string.
     * @return the color as a string.
     */
    public String getStr(Color color) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    /**
     * Get the colour used to signify an active list.
     * <p>
     *
     * @return the colour used to signify an active list.
     */
    public Color getActiveSelectionColor() {
        return getColor(getProperty(activeSelectionColorKey, "30,160,225"));
    }

    /**
     * Get the colour used to signify an active list.
     * <p>
     *
     * @return the colour used to signify an active list.
     */
    public Color getInactiveSelectionColor() {
        return getColor(getProperty(inactiveSelectionColorKey, "150,150,150"));
    }

    /**
     * Get the thickness of the outline to use for displaying the text.
     * <p>
     *
     * @return the outline thickness in pixels.
     */
    public int getOutlineThickness() {
        return Integer.parseInt(getProperty(outlineThicknessKey, "2"));
    }

    /**
     * Set the outline thickness.
     * <p>
     *
     * @param px the outline thickness in pixels.
     */
    public void setOutlineThickness(int px) {
        setProperty(outlineThicknessKey, Integer.toString(px));
        write();
    }

    /**
     * Get the notice box height (px).
     * <p>
     *
     * @return the notice box height.
     */
    public int getNoticeBoxHeight() {
        return Integer.parseInt(getProperty(noticeBoxHeightKey, "40"));
    }

    /**
     * Set the notice box height (px).
     * <p>
     *
     * @param height the notice box height.
     */
    public void setNoticeBoxHeight(int height) {
        setProperty(noticeBoxHeightKey, Integer.toString(height));
        write();
    }

    /**
     * Get the notice box speed.
     * <p>
     *
     * @return the notice box speed.
     */
    public int getNoticeBoxSpeed() {
        return Integer.parseInt(getProperty(noticeBoxSpeedKey, "8"));
    }

    /**
     * Set the notice box speed.
     * <p>
     *
     * @param speed the notice box speed.
     */
    public void setNoticeBoxSpeed(int speed) {
        setProperty(noticeBoxSpeedKey, Integer.toString(speed));
        write();
    }

    /**
     * Get the specially treated words that are auto-capitalised by the song
     * importer when deciding how to un-caps-lock a line of text.
     * <p>
     *
     * @return the array of God words, separated by commas in the properties
     * file.
     */
    public String[] getGodWords() {
        return getProperty(godWordsKey,
                "god,God,jesus,Jesus,christ,Christ,you,You,he,He,lamb,Lamb,"
                        + "lord,Lord,him,Him,son,Son,i,I,his,His,your,Your,king,King,"
                        + "saviour,Saviour,savior,Savior,majesty,Majesty,alpha,Alpha,omega,Omega") //Yeah.. default testing properties.
                .trim().split(",");
    }

    /**
     * Determine whether to advance the scheudle item when the current item is
     * sent live.
     * <p>
     *
     * @return true if we should auto-advance, false otherwise.
     */
    public boolean getAdvanceOnLive() {
        return Boolean.parseBoolean(getProperty(advanceOnLiveKey, "false"));
    }

    /**
     * Set whether to advance the scheudle item when the current item is sent
     * live.
     * <p>
     *
     * @param val true if we should auto-advance, false otherwise.
     */
    public void setAdvanceOnLive(boolean val) {
        setProperty(advanceOnLiveKey, Boolean.toString(val));
        write();
    }

    /**
     * Determine whether to preview the scheudle item when the background image
     * has been updated.
     * <p>
     *
     * @return true if we should preview, false otherwise.
     */
    public boolean getPreviewOnImageUpdate() {
        return Boolean.parseBoolean(getProperty(previewOnImageChangeKey, "false"));
    }

    /**
     * Determine whether to preview the scheudle item when the background image
     * has been updated.
     * <p>
     *
     * @param val true if we should preview, false otherwise.
     */
    public void setPreviewOnImageUpdate(boolean val) {
        setProperty(previewOnImageChangeKey, Boolean.toString(val));
        write();
    }

    /**
     * Get whether to use openoffice for presentations.
     * <p>
     *
     * @return true if we should use openoffice, false if we should just use the
     * basic POI images.
     */
    public boolean getUseOO() {
        return Boolean.parseBoolean(getProperty(useOoKey, "false"));
    }

    /**
     * Set whether to use openoffice for presentations.
     * <p>
     *
     * @param val if we should use openoffice, false if we should just use the
     *            basic POI images.
     */
    public void setUseOO(boolean val) {
        setProperty(useOoKey, Boolean.toString(val));
        write();
    }

    /**
     * Get the path to the openoffice installation on this machine.
     * <p>
     *
     * @return the path to the openoffice installation on this machine.
     */
    public String getOOPath() {
        return getProperty(ooPathKey, "");
    }

    /**
     * Set the path to the openoffice installation on this machine.
     * <p>
     *
     * @param path the path to the openoffice installation on this machine.
     */
    public void setOOPath(String path) {
        setProperty(ooPathKey, path);
        write();
    }

    /**
     * Get whether to use PowerPoint for presentations.
     * <p/>
     *
     * @return true if we should use PowerPoint, false if we should just use the
     * basic POI images or openoffice.
     */
    public boolean getUsePP() {
        return Boolean.parseBoolean(getProperty(usePpKey, "false"));
    }

    /**
     * Set whether to use PowerPoint for presentations.
     * <p/>
     *
     * @param val if we should use PowerPoint, false if we should just use the
     *            basic POI images or openoffice.
     */
    public void setUsePP(boolean val) {
        setProperty(usePpKey, Boolean.toString(val));
        write();
    }

    /**
     * Get the path to the PowerPoint installation on this machine.
     * <p/>
     *
     * @return the path to the PowerPoint installation on this machine.
     */
    public String getPPPath() {
        return getProperty(ppPathKey, "");
    }

    /**
     * Set the path to the PowerPoint installation on this machine.
     * <p/>
     *
     * @param path the path to the PowerPoint installation on this machine.
     */
    public void setPPPath(String path) {
        setProperty(ppPathKey, path);
        write();
    }

    /**
     * Get the path to the desired directory for recordings.
     * <p>
     *
     * @return the path to the desired directory for recordings.
     */
    public String getRecordingsPath() {
        return getProperty(recPathKey, "");
    }

    /**
     * Set the path to the desired directory for recordings.
     * <p>
     *
     * @param path the path to the desired directory for recordings.
     */
    public void setRecordingsPath(String path) {
        setProperty(recPathKey, path);
        write();
    }

    /**
     * Get the path to the desired directory for downloading.
     * <p/>
     *
     * @return the path to the desired directory for recordings.
     */
    public String getDownloadPath() {
        return getProperty(downloadPathKey, "");
    }

    /**
     * Set the path to the desired directory for downloading.
     * <p/>
     *
     * @param path the path to the desired directory for downloading.
     */
    public void setDownloadPath(String path) {
        setProperty(downloadPathKey, path);
        write();
    }

    /**
     * Determine if the recordings should be converted to MP3 files.
     * <p>
     *
     * @return true if recordings should be converted, false otherwise.
     */
    public boolean getConvertRecordings() {
        return Boolean.parseBoolean(getProperty(convertMp3Key, "false"));
    }

    /**
     * Set whether to automatically convert the recordings to MP3 files.
     * <p>
     *
     * @param val if we should use covert to MP#, false if we should just store
     *            recordings as WAV files.
     */
    public void setConvertRecordings(boolean val) {
        setProperty(convertMp3Key, Boolean.toString(val));
        write();
    }

    /**
     * Determine if the OO presentation should be always on top or not. Not user
     * controlled, but useful for testing.
     * <p>
     *
     * @return true if the presentation should be always on top, false
     * otherwise.
     */
    public boolean getOOPresOnTop() {
        return Boolean.parseBoolean(getProperty(ooOntopKey, "true"));
    }

    /**
     * Sets the logo image location for persistent use
     * <p>
     *
     * @param location File location
     */
    public void setLogoImage(String location) {
        setProperty(logoImageLocationKey, location);
        write();
    }

    /**
     * Return the location of the logo image
     * <p>
     *
     * @return the logo image
     */
    public String getLogoImageURI() {
        return "file:" + getProperty(logoImageLocationKey, "icons/logo default.png");
    }

    /**
     * Sets the port used for mobile lyrics display.
     * <p>
     *
     * @param port the port used for mobile lyrics display.
     */
    public void setMobLyricsPort(int port) {
        setProperty(mobLyricsPortKey, Integer.toString(port));
        write();
    }

    /**
     * Gets the port used for mobile lyrics display.
     * <p>
     *
     * @return the port used for mobile lyrics display.
     */
    public int getMobLyricsPort() {
        return Integer.parseInt(getProperty(mobLyricsPortKey, "1111"));
    }

    /**
     * Determine if we should use mobile lyrics.
     * <p>
     *
     * @return true if we should, false otherwise.
     */
    public boolean getUseMobLyrics() {
        return Boolean.parseBoolean(getProperty(useMobLyricsKey, "false"));
    }

    /**
     * Set if we should use mobile lyrics.
     * <p>
     *
     * @param val true if we should, false otherwise.
     */
    public void setUseMobLyrics(boolean val) {
        setProperty(useMobLyricsKey, Boolean.toString(val));
        write();
    }

    public void setUseRemoteControl(boolean val) {
        setProperty(useRemoteControlKey, Boolean.toString(val));
        write();
    }

    /**
     * Determine if we should set up remote control server.
     * <p>
     *
     * @return true if we should, false otherwise.
     */
    public boolean getUseRemoteControl() {
        return Boolean.parseBoolean(getProperty(useRemoteControlKey, "false"));
    }

    /**
     * Gets the port used for remote control server.
     * <p>
     *
     * @return the port used for mobile lyrics display.
     */
    public int getRemoteControlPort() {
        try {
            return Integer.parseInt(getProperty(remoteControlPortKey, "1112"));
        } catch (NumberFormatException e) {
            return 1112;
        }
    }

    public void setRemoteControlPort(int port) {
        setProperty(remoteControlPortKey, Integer.toString(port));
        write();
    }

    public void setRemoteControlPassword(String text) {
        setProperty(remoteControlPasswordKey, text);
        write();
    }

    public String getRemoteControlPassword() {
        return getProperty(remoteControlPasswordKey, "quelea");
    }

    public void setPlanningCenterRefreshToken(String text) {
        setProperty(planningCenterRefreshToken, text);
        write();
    }

    public String getPlanningCenterRefreshToken() {
        return getProperty(planningCenterRefreshToken, null);
    }

    public String getSmallSongTextPositionH() {
        return getProperty(smallSongTextHPositionKey, "right");
    }

    public void setSmallSongTextPositionH(String position) {
        setProperty(smallSongTextHPositionKey, position);
        write();
    }

    public String getSmallSongTextPositionV() {
        return getProperty(smallSongTextVPositionKey, "bottom");
    }

    public void setSmallSongTextPositionV(String position) {
        setProperty(smallSongTextVPositionKey, position);
        write();
    }

    public Double getSmallSongTextSize() {
        return Double.parseDouble(getProperty(smallSongTextSizeKey, "0.1"));
    }

    public void setSmallSongTextSize(double size) {
        setProperty(smallSongTextSizeKey, Double.toString(size));
        write();
    }

    public String getSmallBibleTextPositionH() {
        return getProperty(smallBibleTextHPositionKey, "right");
    }

    public void setSmallBibleTextPositionH(String position) {
        setProperty(smallBibleTextHPositionKey, position);
        write();
    }

    public String getSmallBibleTextPositionV() {
        return getProperty(smallBibleTextVPositionKey, "bottom");
    }

    public void setSmallBibleTextPositionV(String position) {
        setProperty(smallBibleTextVPositionKey, position);
        write();
    }

    public Double getSmallBibleTextSize() {
        return Double.parseDouble(getProperty(smallBibleTextSizeKey, "0.1"));
    }

    public void setSmallBibleTextSize(double size) {
        setProperty(smallBibleTextSizeKey, Double.toString(size));
        write();
    }

    public boolean getSmallSongTextShow() {
        return Boolean.parseBoolean(getProperty(showSmallSongTextKey, "true"));
    }

    public void setSmallSongTextShow(boolean show) {
        setProperty(showSmallSongTextKey, Boolean.toString(show));
        write();
    }

    public boolean getSmallBibleTextShow() {
        return Boolean.parseBoolean(getProperty(showSmallBibleTextKey, "true"));
    }

    public void setSmallBibleTextShow(boolean show) {
        setProperty(showSmallBibleTextKey, Boolean.toString(show));
        write();
    }

    /**
     * Get how many words or verses to show per slide
     *
     * @return number of words or verses (depends on use.max.bible.verses)
     */
    public int getMaxBibleVerses() {
        return Integer.parseInt(getProperty(maxBibleVersesKey, "5"));
    }

    public void setMaxBibleVerses(int number) {
        setProperty(maxBibleVersesKey, Integer.toString(number));
        write();
    }

    /**
     * Get whether the max items is verses or words
     *
     * @return true if using maximum verses per slide
     */
    public boolean getBibleUsingMaxChars() {
        return Boolean.parseBoolean(getProperty(useMaxBibleCharsKey, "true"));
    }

    public void setBibleUsingMaxChars(boolean useChars) {
        setProperty(useMaxBibleCharsKey, Boolean.toString(useChars));
        write();
    }

    /**
     * Get the maximum number of characters allowed on any one line of bible
     * text.
     * <p>
     *
     * @return the maximum number of characters allowed on any one line of bible
     * text.
     */
    public int getMaxBibleChars() {
        return Integer.parseInt(getProperty(maxBibleCharsKey, "80"));
    }

    /**
     * Set the max bible chars value.
     * <p>
     *
     * @param maxChars the maximum number of characters allowed on any one line
     *                 of bible text.
     */
    public void setMaxBibleChars(int maxChars) {
        setProperty(maxBibleCharsKey, Integer.toString(maxChars));
        write();
    }

    /**
     * Get the fade duration of the logo button text.
     * <p>
     *
     * @return the duration of the fade in milliseconds text.
     */
    public int getLogoFadeDuration() {
        String t = getProperty(logoFadeDurationKey, "");
        if (t.equals("")) {
            t = "1000";
            setProperty(logoFadeDurationKey, t);
            write();
        }
        return Integer.parseInt(t);
    }

    /**
     * Get the fade duration of the black button text.
     * <p>
     *
     * @return the duration of the fade in milliseconds text.
     */
    public int getBlackFadeDuration() {
        String t = getProperty(blackFadeDurationKey, "");
        if (t.equals("")) {
            t = "1000";
            setProperty(blackFadeDurationKey, t);
            write();
        }
        return Integer.parseInt(t);
    }

    /**
     * Get the fade duration of the clear button text.
     * <p>
     *
     * @return the duration of the fade in milliseconds text.
     */
    public int getClearFadeDuration() {
        String t = getProperty(clearFadeDurationKey, "");
        if (t.equals("")) {
            t = "1000";
            setProperty(clearFadeDurationKey, t);
            write();
        }
        return Integer.parseInt(t);
    }

    /**
     * Get the Translate ID from the properties file
     * <p>
     *
     * @return the translate ID
     */
    public String getTranslateClientID() {
        String t = getProperty(translateClientIdKey, "");
        if (t.equals("")) {
            t = "quelea-projection";
            setProperty(translateClientIdKey, t);
            write();
        }
        return t;
    }

    /**
     * Get the Translate secret key from the properties file
     * <p>
     *
     * @return the translate secret key
     */
    public String getTranslateClientSecret() {
        String t = getProperty(translateClientSecretKey, "");
        if (t.equals("")) {
            t = "wk4+wd9YJkjIHmz2qwD1oR7pP9/kuHOL6OsaOKEi80U=";
            setProperty(translateClientSecretKey, t);
            write();
        }
        return t;
    }

    public boolean getClearStageWithMain() {
        return Boolean.parseBoolean(getProperty(clearStageviewWithMainKey, "true"));
    }

    public void setClearStageWithMain(boolean clear) {
        setProperty(clearStageviewWithMainKey, Boolean.toString(clear));
        write();
    }

    /**
     * Get the directory used for storing countdown timers.
     * <p>
     *
     * @return the timer directory
     */
    public File getTimerDir() {
        return new File(getQueleaUserHome(), "timer");
    }

    public boolean getSongOverflow() {
        return Boolean.parseBoolean(getProperty(songOverflowKey, "false"));
    }

    public void setSongOverflow(boolean overflow) {
        setProperty(songOverflowKey, Boolean.toString(overflow));
        write();
    }

    public int getAutoDetectPort() {
        return Integer.parseInt(getProperty(autoDetectPortKey, "50015"));
    }

    public boolean getStageShowClock() {
        return Boolean.parseBoolean(getProperty(stageShowClockKey, "true"));
    }

    public boolean getUse24HourClock() {
        return Boolean.parseBoolean(getProperty(use24hClockKey, "true"));
    }

    public void setUse24HourClock(boolean s24h) {
        setProperty(use24hClockKey, Boolean.toString(s24h));
        write();
    }

    public boolean getBibleSplitVerses() {
        return Boolean.parseBoolean(getProperty(splitBibleVersesKey, "false"));
    }

    public void setBibleSplitVerses(boolean selected) {
        setProperty(splitBibleVersesKey, Boolean.toString(selected));
        write();
    }

    public double getLyricWidthBounds() {
        return Double.parseDouble(getProperty(lyricWidthBoundKey, "0.92"));
    }

    public double getLyricHeightBounds() {
        return Double.parseDouble(getProperty(lyricHeightBoundKey, "0.9"));
    }

    public boolean getDefaultSongDBUpdate() {
        return Boolean.parseBoolean(getProperty(defaultSongDbUpdateKey, "true"));
    }

    public boolean getShowDBSongPreview() {
        return Boolean.parseBoolean(getProperty(dbSongPreviewKey, "false"));
    }

    public void setShowDBSongPreview(boolean val) {
        setProperty(dbSongPreviewKey, Boolean.toString(val));
    }

    public boolean getImmediateSongDBPreview() {
        return Boolean.parseBoolean(getProperty("db.song.immediate.preview", "false"));
    }

    public void setImmediateSongDBPreview(boolean val) {
        setProperty("db.song.immediate.preview", Boolean.toString(val));
    }

    public void setDefaultSongDBUpdate(boolean updateInDB) {
        setProperty(defaultSongDbUpdateKey, Boolean.toString(updateInDB));
        write();
    }

    public int getWebDisplayableRefreshRate() {
        return Integer.parseInt(getProperty(webRefreshRateKey, "500"));
    }

    public String getWebProxyHost() {
        return getProperty(webProxyHostKey, null);
    }

    public String getWebProxyPort() {
        return getProperty(webProxyPortKey, null);
    }

    public String getWebProxyUser() {
        return getProperty(webProxyUserKey, null);
    }

    public String getWebProxyPassword() {
        return getProperty(webProxyPasswordKey, null);
    }

    public String getChurchCcliNum() {
        return getProperty(churchCcliNumKey, null);
    }

    /**
     * Get the directory used for storing notices.
     * <p>
     *
     * @return the notice directory
     */
    public File getNoticeDir() {
        return new File(getQueleaUserHome(), "notices");
    }

    public String[] getNewSongKeys() {
        return getProperty("new.song.keys", "Ctrl,Alt,N").split(",");
    }

    public String[] getSearchKeys() {
        return getProperty("search.keys", "Ctrl,L").split(",");
    }

    public String[] getOptionsKeys() {
        return getProperty("options.keys", "Shortcut,T").split(",");
    }

    public String[] getLiveTextKeys() {
        return getProperty("live.text.keys", "Shortcut,Shift,L").split(",");
    }

    public String[] getLogoKeys() {
        return getProperty("logo.keys", "F5").split(",");
    }

    public String[] getBlackKeys() {
        return getProperty("black.keys", "F6").split(",");
    }

    public String[] getClearKeys() {
        return getProperty("clear.keys", "F7").split(",");
    }

    public String[] getHideKeys() {
        return getProperty("hide.keys", "F8").split(",");
    }

    public String[] getAdvanceKeys() {
        return getProperty("advance.keys", "Page Down").split(",");
    }

    public String[] getPreviousKeys() {
        return getProperty("previous.keys", "Page Up").split(",");
    }

    public String[] getNoticesKeys() {
        return getProperty("notices.keys", "Ctrl,M").split(",");
    }

    public String[] getScheduleFocusKeys() {
        return getProperty("schedule.focus.keys", "Ctrl,D").split(",");
    }

    public String[] getBibleFocusKeys() {
        return getProperty("bible.focus.keys", "Ctrl,B").split(",");
    }

    /**
     * Set whether fade should be used.
     *
     * @param useFade true if fade should be used
     */
    public void setUseSlideTransition(boolean useFade) {
        setProperty(useSlideTransitionKey, Boolean.toString(useFade));
    }

    /**
     * Determine whether fade should be used.
     *
     * @return true if fade is enabled, false otherwise.
     */
    public boolean getUseSlideTransition() {
        return Boolean.parseBoolean(getProperty(useSlideTransitionKey, "false"));
    }

    /**
     * Set the slide transition in duration.
     *
     * @param millis milliseconds for fade-in effect.
     */
    public void setSlideTransitionInDuration(int millis) {
        setProperty(slideTransitionInDurationKey, Integer.toString(millis));
    }

    /**
     * Get the slide transition in duration.
     *
     * @return milliseconds for fade-in effect.
     */
    public int getSlideTransitionInDuration() {
        return Integer.parseInt(getProperty(slideTransitionInDurationKey, "750"));
    }

    /**
     * Get the slide transition out duration.
     *
     * @return milliseconds for fade-out effect.
     */
    public int getSlideTransitionOutDuration() {
        return Integer.parseInt(getProperty(slideTransitionOutDurationKey, "400"));
    }

    /**
     * Set the slide transition out duration.
     *
     * @param millis milliseconds for fade-out effect.
     */
    public void setSlideTransitionOutDuration(int millis) {
        setProperty(slideTransitionOutDurationKey, Integer.toString(millis));
    }

    public boolean getUseDarkTheme() {
        return Boolean.parseBoolean(getProperty(darkThemeKey, "false"));
    }

    public void setUseDarkTheme(boolean useDarkTheme) {
        setProperty(darkThemeKey, String.valueOf(useDarkTheme));
    }
}
