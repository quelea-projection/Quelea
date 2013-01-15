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

import org.quelea.windows.main.actionhandlers.ExitActionHandler;
import org.quelea.windows.main.actionhandlers.EditSongScheduleActionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.quelea.data.bible.BibleBrowseDialog;
import org.quelea.data.bible.BibleSearchDialog;
import org.quelea.data.tags.TagDialog;
import org.quelea.services.notice.NoticeDialog;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.menus.MainMenuBar;
import org.quelea.windows.main.toolbars.MainToolbar;
import org.quelea.windows.newsong.SongEntryWindow;
import org.quelea.windows.options.OptionsDialog;

/**
 * The main window used to control the projection.
 * @author Michael
 */
public class MainWindow extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final MainPanel mainpanel;
    private SongEntryWindow songEntryWindow;
    private NoticeDialog noticeDialog;
    private final MainMenuBar menuBar;
    private final MainToolbar mainToolbar;
    private final TagDialog tagDialog;
    private OptionsDialog optionsDialog;
    private final BibleSearchDialog bibleSearchDialog;
    private final BibleBrowseDialog bibleBrowseDialog;

    /**
     * Create a new main window.
     * @param setApplicationWindow true if this main window should be set as
     * the application-wide main window, false otherwise.
     */
    public MainWindow(boolean setApplicationWindow) {
        setTitle("Quelea " + QueleaProperties.VERSION.getVersionString());
        
        BorderPane mainPane = new BorderPane();
        VBox.setVgrow(mainPane, Priority.SOMETIMES);
        noticeDialog = new NoticeDialog();
        
        LOGGER.log(Level.INFO, "Creating main window");
        if(setApplicationWindow) {
            QueleaApp.get().setMainWindow(this);
        }
        setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {

            @Override
            public void handle(javafx.stage.WindowEvent t) {
                new ExitActionHandler().exit();
            }
        });
        
        getIcons().add(new Image("file:icons/logo.png"));
        
        LOGGER.log(Level.INFO, "Creating tag dialog");
        tagDialog = new TagDialog();
        
        LOGGER.log(Level.INFO, "Creating options dialog");
        optionsDialog = new OptionsDialog();
        
        LOGGER.log(Level.INFO, "Creating bible search dialog");
        bibleSearchDialog = new BibleSearchDialog();
        LOGGER.log(Level.INFO, "Creating bible browse dialog");
        bibleBrowseDialog = new BibleBrowseDialog();

        mainpanel = new MainPanel();
        songEntryWindow = new SongEntryWindow();
        mainpanel.getSchedulePanel().getScheduleList().getPopupMenu().getEditSongButton().setOnAction(new EditSongScheduleActionHandler());
        
        menuBar = new MainMenuBar();
        
        HBox toolbarPanel = new HBox();
        mainToolbar = new MainToolbar();
        HBox.setHgrow(mainToolbar, Priority.ALWAYS);
        toolbarPanel.getChildren().add(mainToolbar);
        
        if(Utils.isMac()) {
            menuBar.setUseSystemMenuBar(true);
        }
        
        VBox menuBox = new VBox();
        menuBox.setFillWidth(true);
        menuBox.getChildren().add(menuBar);
        menuBox.getChildren().add(toolbarPanel);
        menuBox.getChildren().add(mainPane);
        
        mainPane.setCenter(mainpanel);
        setScene(new Scene(menuBox));
        LOGGER.log(Level.INFO, "Created main window.");
    }

    /**
     * Get the main panel on this window.
     * @return the main panel part of this window.
     */
    public MainPanel getMainPanel() {
        return mainpanel;
    }
    
    /**
     * Get the notice dialog on this main window.
     * @return the notice dialog.
     */
    public NoticeDialog getNoticeDialog() {
        return noticeDialog;
    }

    /**
     * Get the tag dialog on this main window.
     * @return the tag dialog.
     */
    public TagDialog getTagDialog() {
        return tagDialog;
    }

    /**
     * Get the options dialog on this main window.
     * @return the options dialog.
     */
    public OptionsDialog getOptionsDialog() {
        return optionsDialog;
    }

    /**
     * Get the bible search dialog on this main window.
     * @return the bible search dialog.
     */
    public BibleSearchDialog getBibleSearchDialog() {
        return bibleSearchDialog;
    }
    
    /**
     * Get the bible browse dialog on this main window.
     * @return the bible browse dialog.
     */
    public BibleBrowseDialog getBibleBrowseDialog() {
        return bibleBrowseDialog;
    }
    
    /**
     * Get the new song window used for this window.
     * @return the song entry window.
     */
    public SongEntryWindow getSongEntryWindow() {
        return songEntryWindow;
    }

}
