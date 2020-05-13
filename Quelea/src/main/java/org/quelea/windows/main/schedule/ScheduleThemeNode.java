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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import org.quelea.utils.ThemeUtils;
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
    private ThemeDTO songTempTheme;
    private ThemeDTO bibleTempTheme;
    private EditThemeDialog themeDialog;
    private FlowPane themePreviews;
    private UpdateThemeCallback songCallback = null;
    private UpdateThemeCallback bibleCallback = null;
    private Stage popup;

    public ScheduleThemeNode(UpdateThemeCallback songCallback, UpdateThemeCallback bibleCallback, Stage popup, Button themeButton) {
        this.songCallback = songCallback;
        this.bibleCallback = bibleCallback;
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
                if (width > 900) {
                    width = 900;
                }
                contentPanel.setPrefWidth(width);
                contentPanel.setMaxWidth(width);
            }
        });
        setMaxHeight(300);
        contentPanel.setSpacing(5);
        contentPanel.setPadding(new Insets(3));
        refresh();
        ScrollPane scroller = new ScrollPane(contentPanel);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setCenter(scroller);
    }

    /**
     * Update the theme on the schedule to the current temporary theme.
     */
    public void updateTheme() {
        setSongTheme(songTempTheme);
        setBibleTheme(bibleTempTheme);
    }

    /**
     * Refresh all the themes in the window - remove all the old ones, go
     * through the folder and find the themes to display.
     */
    public synchronized final void refresh() {
        ObservableList<ThemeDTO> themes;
        try {
            themes = ThemeUtils.getThemes();
        } catch (Exception ex) {
            LoggerUtils.getLogger().log(Level.SEVERE, "Couldn't get themes when refreshing.", ex);
            return;
        }
        final ToggleGroup songGroup = new ToggleGroup();
        final ToggleGroup bibleGroup = new ToggleGroup();
        contentPanel.getChildren().clear();
        final HBox northPanel = new HBox();
        Label selectThemeLabel = new Label(LabelGrabber.INSTANCE.getLabel("theme.select.text"));
        selectThemeLabel.setStyle("-fx-font-weight: bold;");
        northPanel.getChildren().add(selectThemeLabel);
        contentPanel.getChildren().add(northPanel);

        ThemeDTO selectedSongTheme = null;
        ThemeDTO selectedBibleTheme = null;
        if (themePreviews != null) {
            for (Node node : themePreviews.getChildren()) {
                if (node instanceof ThemePreviewPanel) {
                    ThemePreviewPanel panel = (ThemePreviewPanel) node;
                    if (panel.getSongSelectButton().isSelected()) {
                        selectedSongTheme = panel.getTheme();
                        setSongTheme(selectedSongTheme);
                    }
                    if (panel.getBibleSelectButton().isSelected()) {
                        selectedBibleTheme = panel.getTheme();
                        setBibleTheme(selectedBibleTheme);
                    }
                }
            }
        }
        if(selectedSongTheme==null) {
            selectedSongTheme = themes.stream().filter((t) -> t.getFile().getAbsoluteFile().equals(QueleaProperties.get().getGlobalSongThemeFile())).findAny().orElse(null);
        }
        if(selectedBibleTheme==null) {
            selectedBibleTheme = themes.stream().filter((t) -> t.getFile().getAbsoluteFile().equals(QueleaProperties.get().getGlobalBibleThemeFile())).findAny().orElse(null);
        }
        themes.add(null); //Used as "default" theme

        themePreviews = new FlowPane();
        themePreviews.setAlignment(Pos.CENTER);
        themePreviews.setHgap(10);
        themePreviews.setVgap(10);

        for (final ThemeDTO theme : themes) {
            ThemePreviewPanel panel = new ThemePreviewPanel(theme, popup, this);
            panel.getSongSelectButton().setOnAction(t -> {
                songTempTheme = theme;
                File storeFile = null;
                if(songTempTheme!=null) {
                    storeFile = songTempTheme.getFile();
                }
                QueleaProperties.get().setGlobalSongThemeFile(storeFile);
                setSongTheme(theme);
                if (QueleaApp.get().getMainWindow().getMainPanel() != null) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                }
            });
            panel.getBibleSelectButton().setOnAction(t -> {
                bibleTempTheme = theme;
                File storeFile = null;
                if(bibleTempTheme!=null) {
                    storeFile = bibleTempTheme.getFile();
                }
                QueleaProperties.get().setGlobalBibleThemeFile(storeFile);
                setBibleTheme(theme);
                if (QueleaApp.get().getMainWindow().getMainPanel() != null) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                }
            });
            if (selectedSongTheme != null && selectedSongTheme.equals(theme)) {
                panel.getSongSelectButton().fire();
            }
            if (selectedBibleTheme != null && selectedBibleTheme.equals(theme)) {
                panel.getBibleSelectButton().fire();
            }
            panel.getSongSelectButton().setToggleGroup(songGroup);
            panel.getBibleSelectButton().setToggleGroup(bibleGroup);
            themePreviews.getChildren().add(panel);
        }
        if (QueleaApp.get().getMainWindow().getMainPanel() != null && songGroup.getSelectedToggle() == null) {
            for (Node node : themePreviews.getChildren()) {
                if (node instanceof ThemePreviewPanel) {
                    ThemePreviewPanel panel = (ThemePreviewPanel) node;
                    if (panel.getTheme() != null && selectedSongTheme != null
                            && panel.getTheme().getThemeName().equals(selectedSongTheme.getThemeName())) {
                        setSongTheme(selectedSongTheme);
                        panel.getSongSelectButton().fire();
                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                    }
                    if (panel.getTheme() != null && selectedBibleTheme != null
                            && panel.getTheme().getThemeName().equals(selectedBibleTheme.getThemeName())) {
                        setBibleTheme(selectedBibleTheme);
                        panel.getBibleSelectButton().fire();
                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().refresh();
                    }
                }
            }
        }
        HBox buttonPanel = new HBox();
        Button newThemeButton = new Button(LabelGrabber.INSTANCE.getLabel("new.theme.text"), new ImageView(new Image("file:icons/ic-add.png",16,16,false,true)));
        newThemeButton.setOnAction(t -> {
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
        });
        buttonPanel.getChildren().add(newThemeButton);
        contentPanel.getChildren().add(themePreviews);
        contentPanel.getChildren().add(buttonPanel);
    }

    public void setSongTheme(ThemeDTO songTempTheme) {
        songCallback.updateTheme(songTempTheme);
    }
    
    public void setBibleTheme(ThemeDTO bibleTempTheme) {
        bibleCallback.updateTheme(bibleTempTheme);
    }
    
    public void selectSongTheme(ThemeDTO theme) {
        selectTheme(theme, true);
    }
    
    public void selectBibleTheme(ThemeDTO theme) {
        selectTheme(theme, false);
    }

    /**
     * Select a theme to be used.
     *
     * @param theme that is supposed to be used
     */
    private void selectTheme(ThemeDTO theme, boolean songTheme) {
        int i = 0;
        for (Node node : themePreviews.getChildren()) {
            i++;
            if (node instanceof ThemePreviewPanel) {
                ThemePreviewPanel panel = (ThemePreviewPanel) node;
                if (i == themePreviews.getChildren().size() || panel.getTheme().getThemeName().equals(theme.getThemeName())) {
                    if(songTheme) {
                        panel.getSongSelectButton().fire();
                    }
                    else {
                        panel.getBibleSelectButton().fire();
                    }
                    break;
                }
            }
        }
    }

    public FlowPane getThemePreviews() {
        return themePreviews;
    }
}
