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
package org.quelea.windows.library;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 * The panel that's used to display the library of media (pictures, video) and
 * songs. Items can be selected from here and added to the order of service.
 *
 * @author Michael
 */
public class LibraryPanel extends VBox {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final LibrarySongPanel songPanel;
    private final LibraryBiblePanel biblePanel;
    private final LibraryImagePanel imagePanel;
    private final LibraryVideoPanel videoPanel;
    private final LibraryTimerPanel timerPanel;
    private final Tab timerTab;
    private final TabPane tabPane;

    /**
     * Create a new library panel.
     */
    public LibraryPanel() {
        LOGGER.log(Level.INFO, "Creating library panel");
        tabPane = new TabPane();

        LOGGER.log(Level.INFO, "Creating library song panel");
        songPanel = new LibrarySongPanel();
        Tab songTab = new Tab();
        songTab.setClosable(false);
        songTab.setText(LabelGrabber.INSTANCE.getLabel("library.songs.heading"));
        songTab.setContent(songPanel);
        tabPane.getTabs().add(songTab);

        LOGGER.log(Level.INFO, "Creating library bible panel");
        biblePanel = new LibraryBiblePanel();
        Tab bibleTab = new Tab();
        bibleTab.setClosable(false);
        bibleTab.setText(LabelGrabber.INSTANCE.getLabel("library.bible.heading"));
        bibleTab.setContent(biblePanel);
        tabPane.getTabs().add(bibleTab);

        LOGGER.log(Level.INFO, "Creating library image panel");
        imagePanel = new LibraryImagePanel();
        Tab imageTab = new Tab();
        imageTab.setClosable(false);
        imageTab.setText(LabelGrabber.INSTANCE.getLabel("library.image.heading"));
        imageTab.setContent(imagePanel);
        tabPane.getTabs().add(imageTab);

        if (QueleaProperties.get().getDisplayVideoTab()) {
            LOGGER.log(Level.INFO, "Creating library video panel");
            videoPanel = new LibraryVideoPanel();
            Tab videoTab = new Tab();
            videoTab.setClosable(false);
            videoTab.setText(LabelGrabber.INSTANCE.getLabel("library.video.heading"));
            videoTab.setContent(videoPanel);
            tabPane.getTabs().add(videoTab);
        }
        else {
            videoPanel = null;
        }

        LOGGER.log(Level.INFO, "Creating library timer panel");
        timerPanel = new LibraryTimerPanel();
        timerTab = new Tab();
        timerTab.setClosable(false);
        timerTab.setText(LabelGrabber.INSTANCE.getLabel("library.timer.heading"));
        timerTab.setContent(timerPanel);
        if (QueleaProperties.get().getTimerDir().listFiles() != null
                && QueleaProperties.get().getTimerDir().listFiles().length > 0) {
            tabPane.getTabs().add(timerTab);
        }

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        getChildren().add(tabPane);
    }

    /**
     * Get the library song panel.
     *
     * @return the library song panel.
     */
    public LibrarySongPanel getLibrarySongPanel() {
        return songPanel;
    }

    /**
     * Get the library bible panel.
     *
     * @return the library bible panel.
     */
    public LibraryBiblePanel getBiblePanel() {
        return biblePanel;
    }

    /**
     * Get the library image panel.
     *
     * @return the library image panel.
     */
    public LibraryImagePanel getImagePanel() {
        return imagePanel;
    }

    /**
     * Get the library video panel.
     *
     * @return the library video panel.
     */
    public LibraryVideoPanel getVideoPanel() {
        return videoPanel;
    }

    /**
     * Get the library timer panel.
     *
     * @return the library timer panel.
     */
    public LibraryTimerPanel getTimerPanel() {
        return timerPanel;
    }

    /**
     * Method to force the display of timers folder
     */
    public void forceTimer() {
        if (!tabPane.getTabs().contains(timerTab)) {
            tabPane.getTabs().add(timerTab);
        }
        timerPanel.getTimerPanel().refresh();
    }
}
