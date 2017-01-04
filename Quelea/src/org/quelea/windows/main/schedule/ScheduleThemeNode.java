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
package org.quelea.windows.main.schedule;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.MainWindow;
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
    private FlowPane themePreviews;
    private UpdateThemeCallback callback = null;
    private Stage popup;

    public ScheduleThemeNode(UpdateThemeCallback callback, Stage popup, Button themeButton) {
        this.callback = callback;
        this.popup = popup;
        themeDialog = new EditThemeDialog();
        themeDialog.initModality(Modality.APPLICATION_MODAL);
        contentPanel = new VBox();
        BorderPane.setMargin(contentPanel, new Insets(10));
        themeButton.layoutXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                MainWindow mainWindow = QueleaApp.get().getMainWindow();
                double width = mainWindow.getWidth() - t1.doubleValue() - 100;
                if (width < 50) {
                    width = 50;
                }
                contentPanel.setPrefWidth(width);
                contentPanel.setMaxWidth(width);
            }
        });
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
        } catch (Exception ex) {
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

        ThemeDTO selectedTheme = null;
        if (themePreviews != null) {
            for (Node node : themePreviews.getChildren()) {
                if (node instanceof ThemePreviewPanel) {
                    ThemePreviewPanel panel = (ThemePreviewPanel) node;
                    if (panel.getSelectButton().isSelected()) {
                        selectedTheme = panel.getTheme();
                        setTheme(selectedTheme);
                    }
                }
            }
        }

        themePreviews = new FlowPane();
        themePreviews.setAlignment(Pos.CENTER);
        themePreviews.setHgap(10);
        themePreviews.setVgap(10);

        for (final ThemeDTO theme : themes) {
            ThemePreviewPanel panel = new ThemePreviewPanel(theme, popup, this);
            panel.getSelectButton().setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent t) {
                    tempTheme = theme;
                    setTheme(theme);
                    if (QueleaApp.get().getMainWindow().getMainPanel() != null) {
                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                    }
                }
            });
            if (selectedTheme != null && selectedTheme.equals(theme)) {
                panel.getSelectButton().fire();
            }
            panel.getSelectButton().setToggleGroup(group);
            themePreviews.getChildren().add(panel);
        }
        if (QueleaApp.get().getMainWindow().getMainPanel() != null && group.getSelectedToggle() == null) {
            for (Node node : themePreviews.getChildren()) {
                if (node instanceof ThemePreviewPanel) {
                    ThemePreviewPanel panel = (ThemePreviewPanel) node;
                    if (panel.getTheme() != null && selectedTheme != null
                            && panel.getTheme().getThemeName().equals(selectedTheme.getThemeName())) {
                        setTheme(selectedTheme);
                        panel.getSelectButton().fire();
                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                    }
                }
            }
        }
        HBox buttonPanel = new HBox();
        Button newThemeButton = new Button(LabelGrabber.INSTANCE.getLabel("new.theme.text"), new ImageView(new Image("file:icons/add.png")));
        newThemeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                themeDialog.setTheme(null);
                themeDialog.showAndWait();
                ThemeDTO ret = themeDialog.getTheme();
                if (ret != null) {
                    try (PrintWriter pw = new PrintWriter(ret.getFile())) {
                        pw.println(ret.getTheme());
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't write new theme", ex);
                    }
                    refresh();
                }
                popup.show();
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
    public List<ThemeDTO> getThemes() {
        List<ThemeDTO> themesList = new ArrayList<>();
        File themeDir = new File(QueleaProperties.getQueleaUserHome(), "themes");
        if (!themeDir.exists()) {
            themeDir.mkdir();
        }
        for (File file : themeDir.listFiles()) {
            if (file.getName().endsWith(".th")) {
                String fileText = Utils.getTextFromFile(file.getAbsolutePath(), "");
                if (fileText.trim().isEmpty()) {
                    continue;
                }
                final ThemeDTO theme = ThemeDTO.fromString(fileText);
                if (theme == ThemeDTO.DEFAULT_THEME) {
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
    public void setTheme(ThemeDTO theme) {
        callback.updateTheme(theme);
    }
    
    /**
     * Select a theme to be used.
     * @param theme that is supposed to be used
     */
    public void selectTheme(ThemeDTO theme) {
        int i = 0;
        for (Node node : themePreviews.getChildren()) {
            i++;
            if (node instanceof ThemePreviewPanel) {
                ThemePreviewPanel panel = (ThemePreviewPanel) node;
                if (i == themePreviews.getChildren().size() || panel.getTheme().getThemeName().equals(theme.getThemeName())) {
                    panel.getSelectButton().fire();
                    break;
                }
            }
        }
    }

    public FlowPane getThemePreviews() {
        return themePreviews;
    }
}
