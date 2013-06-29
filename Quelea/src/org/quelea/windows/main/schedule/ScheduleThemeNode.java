/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.main.schedule;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.ThemePreviewPanel;
import org.quelea.windows.newsong.EditThemeDialog;

/**
 *
 * @author Michael
 */
public class ScheduleThemeNode extends BorderPane {

    public interface UpdateThemeCallback {

        public void updateTheme(ThemeDTO theme);
    }
    private static final Logger LOGGER = LoggerUtils.getLogger();
    private VBox contentPanel;
    private ThemeDTO tempTheme;
    private EditThemeDialog themeDialog;
    private UpdateThemeCallback callback = null;
    private Popup popup;

    public ScheduleThemeNode(UpdateThemeCallback callback, Popup popup) {
        this.callback = callback;
        this.popup = popup;
        themeDialog = new EditThemeDialog();
        themeDialog.initModality(Modality.APPLICATION_MODAL);
        contentPanel = new VBox();
        contentPanel.setSpacing(5);
        refresh();
        setCenter(contentPanel);
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
            LoggerUtils.getLogger().log(Level.SEVERE, "Couldn't get themes when refreshing.", ex);
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
            ThemePreviewPanel panel = new ThemePreviewPanel(theme, popup);
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
                if(popup != null) {
                    popup.hide();
                }
                themeDialog.setTheme(null);
                themeDialog.showAndWait();
                themeDialog.toFront();
                ThemeDTO ret = themeDialog.getTheme();
                if(ret != null) {
                    try(PrintWriter pw = new PrintWriter(ret.getFile())) {
                        pw.println(ret.getTheme());
                    }
                    catch(IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't write new theme", ex);
                    }
                    refresh();
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
                if(theme == ThemeDTO.DEFAULT_THEME) {
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
        callback.updateTheme(theme);
    }
}
