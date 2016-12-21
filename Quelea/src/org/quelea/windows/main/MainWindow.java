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
package org.quelea.windows.main;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.quelea.data.bible.BibleBrowseDialog;
import org.quelea.data.bible.BibleSearchDialog;
import org.quelea.services.notice.NoticeDialog;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.SceneInfo;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.TranslationChoiceDialog;
import org.quelea.windows.main.actionhandlers.ExitActionHandler;
import org.quelea.windows.main.menus.MainMenuBar;
import org.quelea.windows.main.toolbars.MainToolbar;
import org.quelea.windows.newsong.SongEntryWindow;
import org.quelea.windows.options.OptionsDialog;

/**
 * The main window used to control the projection.
 * <p/>
 * @author Michael
 */
public class MainWindow extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final MainPanel mainpanel;
    private SongEntryWindow songEntryWindow;
    private TranslationChoiceDialog translationChoiceDialog;
    private NoticeDialog noticeDialog;
    private final MainMenuBar menuBar;
    private final MainToolbar mainToolbar;
    private OptionsDialog optionsDialog;
    private final BibleSearchDialog bibleSearchDialog;
    private final BibleBrowseDialog bibleBrowseDialog;

    /**
     * Create a new main window.
     * <p/>
     * @param setApplicationWindow true if this main window should be set as the
     * application-wide main window, false otherwise.
     */
    public MainWindow(boolean setApplicationWindow) {
        setTitle("Quelea " + QueleaProperties.VERSION.getVersionString());
        Utils.addIconsToStage(this);

        BorderPane mainPane = new BorderPane();
        VBox.setVgrow(mainPane, Priority.SOMETIMES);
        noticeDialog = new NoticeDialog();

        LOGGER.log(Level.INFO, "Creating main window");
        if (setApplicationWindow) {
            QueleaApp.get().setMainWindow(this);
        }
        setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
            @Override
            public void handle(javafx.stage.WindowEvent t) {
                new ExitActionHandler().exit(t);
            }
        });

        LOGGER.log(Level.INFO, "Creating options dialog");
        optionsDialog = new OptionsDialog();

        LOGGER.log(Level.INFO, "Creating bible search dialog");
        bibleSearchDialog = new BibleSearchDialog();
        LOGGER.log(Level.INFO, "Creating bible browse dialog");
        bibleBrowseDialog = new BibleBrowseDialog();

        mainpanel = new MainPanel();
        LOGGER.log(Level.INFO, "Creating song entry window");
        songEntryWindow = new SongEntryWindow();
        LOGGER.log(Level.INFO, "Creating translation dialog");
        translationChoiceDialog = new TranslationChoiceDialog();

        menuBar = new MainMenuBar();

        HBox toolbarPanel = new HBox();
        mainToolbar = new MainToolbar();
        HBox.setHgrow(mainToolbar, Priority.ALWAYS);
        toolbarPanel.getChildren().add(mainToolbar);

        if (Utils.isMac()) {
            LOGGER.log(Level.INFO, "Is mac: true, using system menu bar");
            menuBar.setUseSystemMenuBar(true);
        }

        LOGGER.log(Level.INFO, "Creating menu box");
        VBox menuBox = new VBox();
        menuBox.setFillWidth(true);
        menuBox.getChildren().add(menuBar);
        menuBox.getChildren().add(toolbarPanel);
        menuBox.getChildren().add(mainPane);

        mainPane.setCenter(mainpanel);
        setScene(new Scene(menuBox));
        LOGGER.log(Level.INFO, "Setting scene info");
        SceneInfo sceneInfo = QueleaProperties.get().getSceneInfo();
        if (sceneInfo != null && !Utils.isOffscreen(sceneInfo)) { //Shouldn't be null unless something goes wrong, but guard against it anyway
            Platform.runLater(new Runnable() {
                public void run() {
                    setWidth(sceneInfo.getWidth());
                    setHeight(sceneInfo.getHeight());
                    setX(sceneInfo.getX());
                    setY(sceneInfo.getY());
                    setMaximized(sceneInfo.isMaximised());
                }
            });

        }
        LOGGER.log(Level.INFO, "Created main window.");
    }

    /**
     * Get the main panel on this window.
     * <p/>
     * @return the main panel part of this window.
     */
    public MainPanel getMainPanel() {
        return mainpanel;
    }

    /**
     * Get the notice dialog on this main window.
     * <p/>
     * @return the notice dialog.
     */
    public NoticeDialog getNoticeDialog() {
        return noticeDialog;
    }

    /**
     * Get the options dialog on this main window.
     * <p/>
     * @return the options dialog.
     */
    public OptionsDialog getOptionsDialog() {
        return optionsDialog;
    }

    /**
     * Get the toolbar.
     * <p>
     * @return the toolbar.
     */
    public MainToolbar getMainToolbar() {
        return mainToolbar;
    }

    /**
     * Get the main menu bar.
     *
     * @return MainMenuBar.
     */
    public MainMenuBar getMainMenuBar() {
        return menuBar;
    }

    /**
     * Get the bible search dialog on this main window.
     * <p/>
     * @return the bible search dialog.
     */
    public BibleSearchDialog getBibleSearchDialog() {
        return bibleSearchDialog;
    }

    /**
     * Get the bible browse dialog on this main window.
     * <p/>
     * @return the bible browse dialog.
     */
    public BibleBrowseDialog getBibleBrowseDialog() {
        return bibleBrowseDialog;
    }

    /**
     * Get the new song window used for this window.
     * <p/>
     * @return the song entry window.
     */
    public SongEntryWindow getSongEntryWindow() {
        return songEntryWindow;
    }

    /**
     * Get the translation choice dialog for this window.
     *
     * @return the translation choice dialog for this window.
     */
    public TranslationChoiceDialog getTranslationChoiceDialog() {
        return translationChoiceDialog;
    }

}
