/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.newsong.EditThemeDialog;

/**
 *
 * @author Michael
 */
public class ScheduleThemeNode extends BorderPane {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private VBox contentPanel;
    private ThemeDTO tempTheme;
    private ScheduleList schedule;
    private EditThemeDialog themeDialog;

    public ScheduleThemeNode(final ScheduleList schedule) {
        this.schedule = schedule;
        themeDialog = new EditThemeDialog();
        contentPanel = new VBox();
        contentPanel.setSpacing(5);
        refresh();
        setCenter(contentPanel);
        startWatching();
    }

    /**
     * Start the watcher thread. This runs in the background and if any theme
     * changes are detected on the folder it updates itself.
     */
    private void startWatching() {
        try {
            final WatchService watcher = FileSystems.getDefault().newWatchService();
            final Path themePath = FileSystems.getDefault().getPath(new File(QueleaProperties.getQueleaUserHome(), "themes").getAbsolutePath());
            themePath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            new Thread() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    while(true) {
                        WatchKey key;
                        try {
                            key = watcher.take();
                        }
                        catch(InterruptedException ex) {
                            return;
                        }

                        for(WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if(kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }

                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path filename = ev.context();
                            if(!filename.toFile().toString().toLowerCase().endsWith(".th")) {
                                continue;
                            }

                            if(!key.reset()) {
                                break;
                            }
                            Utils.sleep(200); //TODO: Bodge
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();
                                }
                            });

                        }
                    }
                }
            }.start();
        }
        catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Exception in logger thread.", ex);
        }
    }

    /**
     * Get the temporary theme to be used on the schedule, the one currently
     * selected by the user. Or null if it's default or nothing is selected.
     * <p/>
     * @return the user's chosen theme.
     */
    public ThemeDTO getTempTheme() {
        return tempTheme;
    }

    /**
     * Update the theme on the schedule to the current temporary theme.
     */
    public void updateTheme() {
        setTheme(tempTheme);
    }

    /**
     * Refresh all the themes in the window - remove all the old ones, go
     * through the folder and find the themes to display.
     */
    public synchronized final void refresh() {
        List<ThemeDTO> themes;
        try {
            themes = getThemes();
        }
        catch(Exception ex) {
            LoggerUtils.getLogger().log(Level.SEVERE, "Cannot get themes when refreshing. Something majorly broke here.");
            return;
        }
        themes.add(null);
        final ToggleGroup group = new ToggleGroup();
        contentPanel.getChildren().clear();
        final HBox northPanel = new HBox();
        Label selectThemeLabel = new Label(LabelGrabber.INSTANCE.getLabel("theme.select.text"));
        selectThemeLabel.setStyle("-fx-font-weight: bold;");
        northPanel.getChildren().add(selectThemeLabel);
        contentPanel.getChildren().add(northPanel);
        final HBox themePreviews = new HBox();
        themePreviews.setSpacing(10);
        themePreviews.setFillHeight(true);
        for(final ThemeDTO theme : themes) {
            ThemePreviewPanel panel = new ThemePreviewPanel(theme);
            panel.getSelectButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent t) {
                    tempTheme = theme;
                    setTheme(theme);
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                }
            });
            group.getToggles().add(panel.getSelectButton());
            themePreviews.getChildren().add(panel);
        }
        HBox buttonPanel = new HBox();
        Button newThemeButton = new Button(LabelGrabber.INSTANCE.getLabel("new.theme.text"), new ImageView(new Image("file:icons/add.png")));
        newThemeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themeDialog.setTheme(null);
                themeDialog.show();
                ThemeDTO ret = themeDialog.getTheme();
                if(ret != null) {
                    try(PrintWriter pw = new PrintWriter(ret.getFile())) {
                        pw.println(ret.getTheme());
                    }
                    catch(IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't write new theme", ex);
                    }
                }
            }
        });
        buttonPanel.getChildren().add(newThemeButton);
        contentPanel.getChildren().add(themePreviews);
        contentPanel.getChildren().add(buttonPanel);
    }

    /**
     * Get a list of themes currently in use on this window.
     * <p/>
     * @return the list of themes displayed.
     */
    private List<ThemeDTO> getThemes() {
        List<ThemeDTO> themesList = new ArrayList<>();
        File themeDir = new File(QueleaProperties.getQueleaUserHome(), "themes");
        if(!themeDir.exists()) {
            themeDir.mkdir();
        }
        for(File file : themeDir.listFiles()) {
            if(file.getName().endsWith(".th")) {
                String fileText = Utils.getTextFromFile(file.getAbsolutePath(), "");
                final ThemeDTO theme = ThemeDTO.fromString(fileText);
                if(theme==ThemeDTO.DEFAULT_THEME) {
                    LOGGER.log(Level.WARNING, "Error parsing theme file: {0}", fileText);
                    continue;  //error
                }
                theme.setFile(file);
                themesList.add(theme);
            }
        }
        return themesList;
    }

    /**
     * Set the schedule to a given theme.
     * <p/>
     * @param theme the theme to set.
     */
    private void setTheme(ThemeDTO theme) {
        if(schedule == null) {
            LOGGER.log(Level.WARNING, "Null schedule, not setting theme");
            return;
        }
        for(int i = 0; i < schedule.itemsProperty().get().size(); i++) {
            Displayable displayable = schedule.itemsProperty().get().get(i);
            if(displayable instanceof TextDisplayable) {
                TextDisplayable textDisplayable = (TextDisplayable) displayable;
                for(TextSection section : textDisplayable.getSections()) {
                    section.setTempTheme(theme);
                }
            }
        }
    }
}
