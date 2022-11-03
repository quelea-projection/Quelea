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
 * MERCHANTABILITYs or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import com.dlsc.preferencesfx.model.Setting;
import com.dlsc.preferencesfx.util.StorageHandler;
import com.google.gson.Gson;
import javafx.collections.ObservableList;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.languages.LanguageFile;
import org.quelea.services.languages.LanguageFileManager;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.QueleaPropertyKeys;
import org.quelea.windows.main.QueleaApp;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static com.dlsc.preferencesfx.util.Constants.*;

/**
 * Handles everything related to storing values of {@link Setting} using {@link Preferences}.
 *
 * @author Arvid
 */
public class PreferenceStorageHandler implements StorageHandler {

    private Preferences preferences;
    private Gson gson;

    public PreferenceStorageHandler(Class<?> saveClass) {
        preferences = Preferences.userNodeForPackage(saveClass);
        gson = new Gson();
    }

    /**
     * Stores the last selected category in TreeSearchView.
     *
     * @param breadcrumb the category path as a breadcrumb string
     */
    public void saveSelectedCategory(String breadcrumb) {
        preferences.put(SELECTED_CATEGORY, breadcrumb);
    }

    /**
     * Gets the last selected category in TreeSearchView.
     *
     * @return the breadcrumb string of the selected category. null if none is found
     */
    public String loadSelectedCategory() {
        return preferences.get(SELECTED_CATEGORY, null);
    }

    /**
     * Stores the given divider position of the MasterDetailPane.
     *
     * @param dividerPosition the divider position to be stored
     */
    public void saveDividerPosition(double dividerPosition) {
        preferences.putDouble(DIVIDER_POSITION, dividerPosition);
    }

    /**
     * Gets the stored divider position of the MasterDetailPane.
     *
     * @return the double value of the divider position. 0.2 if none is found
     */
    public double loadDividerPosition() {
        return preferences.getDouble(DIVIDER_POSITION, DEFAULT_DIVIDER_POSITION);
    }

    /**
     * Stores the window width of the PreferencesFxDialog.
     *
     * @param windowWidth the width of the window to be stored
     */
    public void saveWindowWidth(double windowWidth) {
        preferences.putDouble(WINDOW_WIDTH, windowWidth);
    }

    /**
     * Searches for the window width of the PreferencesFxDialog.
     *
     * @return the double value of the window width. 1000 if none is found
     */
    public double loadWindowWidth() {
        return preferences.getDouble(WINDOW_WIDTH, DEFAULT_PREFERENCES_WIDTH);
    }

    /**
     * Stores the window height of the PreferencesFxDialog.
     *
     * @param windowHeight the height of the window to be stored
     */
    public void saveWindowHeight(double windowHeight) {
        preferences.putDouble(WINDOW_HEIGHT, windowHeight);
    }

    /**
     * Searches for the window height of the PreferencesFxDialog.
     *
     * @return the double value of the window height. 700 if none is found
     */
    public double loadWindowHeight() {
        return preferences.getDouble(WINDOW_HEIGHT, DEFAULT_PREFERENCES_HEIGHT);
    }

    /**
     * Stores the position of the PreferencesFxDialog in horizontal orientation.
     *
     * @param windowPosX the double value of the window position in horizontal orientation
     */
    public void saveWindowPosX(double windowPosX) {
        preferences.putDouble(WINDOW_POS_X, windowPosX);
    }

    /**
     * Searches for the horizontal window position.
     *
     * @return the double value of the horizontal window position
     */
    public double loadWindowPosX() {
        return preferences.getDouble(WINDOW_POS_X, DEFAULT_PREFERENCES_POS_X);
    }

    /**
     * Stores the position of the PreferencesFxDialog in vertical orientation.
     *
     * @param windowPosY the double value of the window position in vertical orientation
     */
    public void saveWindowPosY(double windowPosY) {
        preferences.putDouble(WINDOW_POS_Y, windowPosY);
    }

    /**
     * Searches for the vertical window position.
     *
     * @return the double value of the vertical window position
     */
    public double loadWindowPosY() {
        return preferences.getDouble(WINDOW_POS_Y, DEFAULT_PREFERENCES_POS_Y);
    }

    /**
     * Serializes a given Object and saves it to the preferences using the given key.
     *
     * @param breadcrumb the key which is used to save the serialized Object
     * @param object     the Object which will be saved
     */
    // asciidoctor Documentation - tag::storageHandlerSave[]
    public void saveObject(String breadcrumb, Object object) {
        if (object == null) {
            object = "";
        }
        switch (breadcrumb) {
            case QueleaPropertyKeys.languageFileKey:
                if (object instanceof LanguageFile && !object.equals(new LanguageFile(QueleaProperties.get().getLanguageFile()))) {
                    QueleaProperties.get().setLanguageFile(((LanguageFile) object).getFile().getName());
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("language.changed"), LabelGrabber.INSTANCE.getLabel("language.changed.message"), QueleaApp.get().getMainWindow());
                }
                break;
            case QueleaPropertyKeys.darkThemeKey:
                if ((QueleaProperties.get().getUseDarkTheme() && object.toString().equals(LabelGrabber.INSTANCE.getLabel("default.theme.label"))) ||
                        (!QueleaProperties.get().getUseDarkTheme() && object.toString().equals(LabelGrabber.INSTANCE.getLabel("dark.theme.label")))) {
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("theme.changed"), LabelGrabber.INSTANCE.getLabel("theme.changed.message"), QueleaApp.get().getMainWindow());
                }
                QueleaProperties.get().setUseDarkTheme(object.toString().equals(LabelGrabber.INSTANCE.getLabel("dark.theme.label")));
                break;
            case QueleaPropertyKeys.projectorModeKey:
                if (Boolean.parseBoolean(object.toString())) {
                    QueleaProperties.get().setProjectorModeCoords();
                } else {
                    QueleaProperties.get().setProjectorModeScreen();
                }
                break;
            case QueleaPropertyKeys.stageModeKey:
                if (Boolean.parseBoolean(object.toString())) {
                    QueleaProperties.get().setStageModeCoords();
                } else {
                    QueleaProperties.get().setStageModeScreen();
                }
                break;
            case QueleaPropertyKeys.projectorHCoordKey:
                QueleaProperties.get().setHeightProjectorCoord(object.toString());
                break;
            case QueleaPropertyKeys.projectorWCoordKey:
                QueleaProperties.get().setWidthProjectorCoord(object.toString());
                break;
            case QueleaPropertyKeys.projectorXCoordKey:
                QueleaProperties.get().setXProjectorCoord(object.toString());
                break;
            case QueleaPropertyKeys.projectorYCoordKey:
                QueleaProperties.get().setYProjectorCoord(object.toString());
                break;
            case QueleaPropertyKeys.stageHCoordKey:
                QueleaProperties.get().setHeightStageCoord(object.toString());
                break;
            case QueleaPropertyKeys.stageWCoordKey:
                QueleaProperties.get().setWidthStageCoord(object.toString());
                break;
            case QueleaPropertyKeys.stageXCoordKey:
                QueleaProperties.get().setXStageCoord(object.toString());
                break;
            case QueleaPropertyKeys.stageYCoordKey:
                QueleaProperties.get().setYStageCoord(object.toString());
                break;
            case QueleaPropertyKeys.stageScreenKey:
                if (object.toString().equals(LabelGrabber.INSTANCE.getLabel("none.text"))) {
                    QueleaProperties.get().setStageScreen(-1);
                } else {
                    int newPos = Integer.parseInt(
                            object.toString().replace(LabelGrabber.INSTANCE.getLabel("output.text"), "").trim()) - 1;
                    QueleaProperties.get().setStageScreen(newPos);
                }
                break;
            case QueleaPropertyKeys.projectorScreenKey:
                if (object.toString().equals(LabelGrabber.INSTANCE.getLabel("none.text"))) {
                    QueleaProperties.get().setProjectorScreen(-1);
                } else {
                    int newPos = Integer.parseInt(
                            object.toString().replace(LabelGrabber.INSTANCE.getLabel("output.text"), "").trim()) - 1;
                    QueleaProperties.get().setProjectorScreen(newPos);
                }
                break;
            case QueleaPropertyKeys.controlScreenKey:
                int newPos = Integer.parseInt(
                        object.toString().replace(LabelGrabber.INSTANCE.getLabel("output.text"), "").trim()) - 1;
                QueleaProperties.get().setControlScreen(newPos);
                break;
            case QueleaPropertyKeys.noticePositionKey:
            case QueleaPropertyKeys.smallBibleTextHPositionKey:
            case QueleaPropertyKeys.smallBibleTextVPositionKey:
            case QueleaPropertyKeys.smallSongTextHPositionKey:
            case QueleaPropertyKeys.smallSongTextVPositionKey:
            case QueleaPropertyKeys.smallSongTextShowOnSlidesKey:
            case QueleaPropertyKeys.stageTextAlignmentKey:
                String pos = LabelGrabber.INSTANCE.getEngKey(object.toString());
                if (pos != null) {
                    pos = pos.toLowerCase();
                    if (breadcrumb.contains("alignment"))
                        pos = pos.toUpperCase();
                    QueleaProperties.get().setProperty(breadcrumb, pos);
                }
                break;
            case QueleaPropertyKeys.mobLyricsPortKey:
            case QueleaPropertyKeys.remoteControlPortKey:
            case QueleaPropertyKeys.remoteControlPasswordKey:
            case QueleaPropertyKeys.useRemoteControlKey:
            case QueleaPropertyKeys.useMobLyricsKey:
                if (QueleaProperties.get().getProperty(breadcrumb) != null && !QueleaProperties.get().getProperty(breadcrumb).equals(object.toString())) {
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("server.changed.label"), LabelGrabber.INSTANCE.getLabel("server.changed.message"), QueleaApp.get().getMainWindow());
                }
                if (breadcrumb.equals(QueleaPropertyKeys.remoteControlPasswordKey) && object.toString().trim().isEmpty() && false) {//The empty password check disabled by default
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("password.empty.label"), LabelGrabber.INSTANCE.getLabel("password.empty.message"), QueleaApp.get().getMainWindow());
                    QueleaProperties.get().setProperty(breadcrumb, "quelea");
                } else {
                    QueleaProperties.get().setProperty(breadcrumb, object.toString());
                }
                break;
            case QueleaPropertyKeys.usePpKey:
                if (QueleaProperties.get().getUsePP() != Boolean.parseBoolean(object.toString())) {
                    Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("presentation.changed.label"), LabelGrabber.INSTANCE.getLabel("presentation.changed.message"), QueleaApp.get().getMainWindow());
                }
                QueleaProperties.get().setProperty(breadcrumb, object.toString());
                break;
            case QueleaPropertyKeys.dbSongPreviewKey:
                if (object.toString().equals(LabelGrabber.INSTANCE.getLabel("db.song.preview.label.databasepreview"))) {
                    QueleaProperties.get().setShowDBSongPreview(true);
                    QueleaProperties.get().setImmediateSongDBPreview(false);
                } else if (object.toString().equals(LabelGrabber.INSTANCE.getLabel("db.song.preview.label.previewpane"))) {
                    QueleaProperties.get().setShowDBSongPreview(false);
                    QueleaProperties.get().setImmediateSongDBPreview(true);
                } else {
                    QueleaProperties.get().setShowDBSongPreview(false);
                    QueleaProperties.get().setImmediateSongDBPreview(false);
                }
                break;
            case QueleaPropertyKeys.smallBibleTextSizeKey:
            case QueleaPropertyKeys.smallSongTextSizeKey:
                QueleaProperties.get().setProperty(breadcrumb, String.valueOf(Double.parseDouble(object.toString())));
                break;
            case QueleaPropertyKeys.defaultSongDbUpdateKey:
                QueleaProperties.get().setDefaultSongDBUpdate(!Boolean.parseBoolean(object.toString()));
                break;
            default:
                QueleaProperties.get().setProperty(breadcrumb, object.toString());
        }
    }

    // asciidoctor Documentation - end::storageHandlerSave[]

    /**
     * Searches in the preferences after a serialized Object using the given key,
     * deserializes and returns it. Returns a default Object if nothing is found.
     *
     * @param breadcrumb    the key which is used to search the serialized Object
     * @param defaultObject the Object which will be returned if nothing is found
     * @return the deserialized Object or the default Object if nothing is found
     */
    // asciidoctor Documentation - tag::storageHandlerLoad[]
    public Object loadObject(String breadcrumb, Object defaultObject) {
        if (breadcrumb.equals(QueleaPropertyKeys.languageFileKey)) {
            return LanguageFileManager.INSTANCE.getCurrentFile();
        }
        String property = QueleaProperties.get().getProperty(breadcrumb);
        if (property == null) {
            return defaultObject;
        } else {
            switch (breadcrumb) {
                case QueleaPropertyKeys.mobLyricsPortKey:
                case QueleaPropertyKeys.remoteControlPortKey:
                    return property;
                case QueleaPropertyKeys.darkThemeKey:
                    return LabelGrabber.INSTANCE.getLabel(QueleaProperties.get().getUseDarkTheme() ? "dark.theme.label" : "default.theme.label");
                case QueleaPropertyKeys.projectorModeKey:
                case QueleaPropertyKeys.stageModeKey:
                    return property.equals("coords");
                case QueleaPropertyKeys.projectorHCoordKey:
                    return QueleaProperties.get().getProjectorCoords().getHeight();
                case QueleaPropertyKeys.projectorWCoordKey:
                    return QueleaProperties.get().getProjectorCoords().getWidth();
                case QueleaPropertyKeys.projectorXCoordKey:
                    return QueleaProperties.get().getProjectorCoords().getMinX();
                case QueleaPropertyKeys.projectorYCoordKey:
                    return QueleaProperties.get().getProjectorCoords().getMinY();
                case QueleaPropertyKeys.stageHCoordKey:
                    return QueleaProperties.get().getStageCoords().getHeight();
                case QueleaPropertyKeys.stageWCoordKey:
                    return QueleaProperties.get().getStageCoords().getWidth();
                case QueleaPropertyKeys.stageXCoordKey:
                    return QueleaProperties.get().getStageCoords().getMinX();
                case QueleaPropertyKeys.stageYCoordKey:
                    return QueleaProperties.get().getStageCoords().getMinY();
                case QueleaPropertyKeys.stageScreenKey:
                    int screenNum = QueleaProperties.get().getStageScreen();
                    if (screenNum == -1) {
                        return LabelGrabber.INSTANCE.getLabel("none.text");
                    } else {
                        return LabelGrabber.INSTANCE.getLabel("output.text") + " " + (screenNum + 1);
                    }
                case QueleaPropertyKeys.projectorScreenKey:
                    screenNum = QueleaProperties.get().getProjectorScreen();
                    if (screenNum == -1) {
                        return LabelGrabber.INSTANCE.getLabel("none.text");
                    } else {
                        return LabelGrabber.INSTANCE.getLabel("output.text") + " " + (screenNum + 1);
                    }
                case QueleaPropertyKeys.controlScreenKey:
                    screenNum = QueleaProperties.get().getControlScreen();
                    return LabelGrabber.INSTANCE.getLabel("output.text") + " " + (screenNum + 1);
                case QueleaPropertyKeys.dbSongPreviewKey:
                    if (QueleaProperties.get().getShowDBSongPreview()) {
                        return LabelGrabber.INSTANCE.getLabel("db.song.preview.label.databasepreview");
                    } else if (QueleaProperties.get().getImmediateSongDBPreview()) {
                        return LabelGrabber.INSTANCE.getLabel("db.song.preview.label.previewpane");
                    } else {
                        return LabelGrabber.INSTANCE.getLabel("db.song.preview.label.control");
                    }
                case QueleaPropertyKeys.defaultSongDbUpdateKey:
                    return !QueleaProperties.get().getDefaultSongDBUpdate();
                case QueleaPropertyKeys.elevantoClientIdKey:
                    return QueleaProperties.get().getElevantoClientId();
                case QueleaPropertyKeys.churchCcliNumKey:
                    return QueleaProperties.get().getChurchCcliNum();
                default:
                    try {
                        Object object = gson.fromJson(property, Object.class);
                        if (breadcrumb.contains("position") || breadcrumb.contains("alignment") || breadcrumb.contains("show.on.slides")) {
                            return LabelGrabber.INSTANCE.getLabel(object.toString().toLowerCase());
                        } else {
                            return object;
                        }
                    } catch (com.google.gson.JsonSyntaxException e) {
                        return property;
                    }
            }
        }
    }

    // asciidoctor Documentation - end::storageHandlerLoad[]

    /**
     * Searches in the preferences after a serialized ArrayList using the given key,
     * deserializes and returns it as ObservableArrayList.
     * When an ObservableList is deserialzed, Gson returns an ArrayList
     * and needs to be wrapped into an ObservableArrayList. This is only needed for loading.
     *
     * @param breadcrumb            the key which is used to search the serialized ArrayList
     * @param defaultObservableList the default ObservableList
     *                              which will be returned if nothing is found
     * @return the deserialized ObservableList or the default ObservableList if nothing is found
     */
    public ObservableList loadObservableList(
            String breadcrumb,
            ObservableList defaultObservableList
    ) {
//        String serializedDefault = gson.toJson(defaultObservableList);
//        String json = preferences.get(hash(breadcrumb), serializedDefault);
//        return FXCollections.observableArrayList(gson.fromJson(json, ArrayList.class));
        return defaultObservableList;
    }

    /**
     * Clears the preferences.
     *
     * @return true if successful, false if there was an exception.
     */
    public boolean clearPreferences() {
        try {
            preferences.clear();
        } catch (BackingStoreException e) {
            return false;
        }
        return true;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public <T> T loadObject(String breadcrumb, Class<T> type, T defaultObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> ObservableList<T> loadObservableList(String breadcrumb, Class<T> type, ObservableList<T> defaultObservableList) {
        throw new UnsupportedOperationException();
    }

}
