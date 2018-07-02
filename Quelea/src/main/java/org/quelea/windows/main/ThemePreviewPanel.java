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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.javafx.dialog.Dialog;
import org.quelea.data.ThemeDTO;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.windows.lyrics.LyricDrawer;
import org.quelea.windows.main.DisplayCanvas.Priority;
import org.quelea.windows.main.schedule.ScheduleThemeNode;
import org.quelea.windows.newsong.EditThemeDialog;
import org.quelea.windows.newsong.ThemePanel;
import org.quelea.windows.stage.StageDrawer;

/**
 * Panel that displays a preview of a particular theme. This is part of the
 * theme select popup window.
 * <p/>
 * @author Michael
 */
public class ThemePreviewPanel extends VBox {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private ThemeDTO theme;
    private DisplayCanvas canvas;
    private RadioButton selectButton;
    private Button removeButton;
    private Button editButton;
    private EditThemeDialog themeDialog;
    private Window popup;
    private ScheduleThemeNode parent;
    private WritableImage previewImage;

    /**
     * Create a new theme preview panel.
     * <p/>
     * @param theme the theme to preview.
     */
    public ThemePreviewPanel(ThemeDTO theme, final Window popup, final ScheduleThemeNode parent) {
        this.popup = popup;
        this.theme = theme;
        this.parent = parent;
        if (theme == null) {
            theme = ThemeDTO.DEFAULT_THEME;
        }
        final ThemeDTO updateTheme = theme;
        canvas = new DisplayCanvas(false, false, false, new DisplayCanvas.CanvasUpdater() {
            @Override
            public void updateCallback() {
                updateThemePreviewCanvas(updateTheme);
            }
        }, Priority.LOW);
        canvas.setPrefSize(200, 200);
        updateThemePreviewCanvas(theme);
        String name;
        if (theme == ThemeDTO.DEFAULT_THEME) {
            name = LabelGrabber.INSTANCE.getLabel("default.theme.text");
        } else {
            name = theme.getThemeName();
        }
        themeDialog = new EditThemeDialog();
        themeDialog.initModality(Modality.APPLICATION_MODAL);
        selectButton = new RadioButton(name);
        if (theme != ThemeDTO.DEFAULT_THEME) {
            editButton = new Button("", new ImageView(new Image("file:icons/edit32.png", 16, 16, false, true)));
            editButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("edit.theme.tooltip")));
            editButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent t) {
                    themeDialog.setTheme(ThemePreviewPanel.this.theme);
                    popup.hide();
                    themeDialog.showAndWait();
                    ThemeDTO ret = themeDialog.getTheme();
                    if (ret != null) {
                        try (PrintWriter pw = new PrintWriter(ret.getFile())) {
                            pw.println(ret.getTheme());
                            ThemePreviewPanel.this.theme = ret;
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Couldn't edit theme", ex);
                        }
                    }
                    parent.refresh();
                }
            });

            removeButton = new Button("", new ImageView(new Image("file:icons/delete.png", 16, 16, false, true)));
            removeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("remove.theme.tooltip")));
            removeButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent t) {
                    popup.hide();
                    final boolean[] deleted = new boolean[1];
                    Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("delete.theme.confirm.title"), LabelGrabber.INSTANCE.getLabel("delete.theme.question"), null).addYesButton(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            ThemePreviewPanel.this.theme.getFile().delete();
                            deleted[0] = true;
                        }
                    }).addNoButton(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            //Nothing needed here
                        }
                    }).build().showAndWait();
                    if (deleted[0]) {
                        parent.getThemePreviews().getChildren().remove(ThemePreviewPanel.this);
                    }
                }
            });
        }
        HBox buttonPanel = new HBox();
        if (canvas != null) {
            canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    t.consume();
                    selectButton.fire();
                }
            });
        }
        buttonPanel.getChildren().add(selectButton);
        if (theme != ThemeDTO.DEFAULT_THEME) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            buttonPanel.getChildren().add(spacer);
            buttonPanel.getChildren().add(editButton);
            buttonPanel.getChildren().add(removeButton);
        }
        HBox canvasPanel = new HBox();
        canvasPanel.getChildren().add(canvas);
        getChildren().add(canvasPanel);
        getChildren().add(buttonPanel);
    }

    /**
     * Get the select radio button used to select this theme.
     * <p/>
     * @return the select radio button.
     */
    public RadioButton getSelectButton() {
        return selectButton;
    }

    /**
     * Get the theme in use on this preview panel.
     * <p/>
     * @return the theme in use on this preview panel.
     */
    public ThemeDTO getTheme() {
        return theme;
    }

    private void updateThemePreviewCanvas(ThemeDTO theme) {
        WordDrawer drawer;
        if (canvas.isStageView()) {
            drawer = new StageDrawer();
        } else {
            drawer = new LyricDrawer();
        }
        drawer.setCanvas(canvas);
        drawer.setTheme(theme);
        drawer.setText(ThemePanel.SAMPLE_LYRICS, new String[0], new String[0], false, -1);
    }
    
    public Image getThemePreviewImage() {
        WordDrawer drawer;
        if (canvas.isStageView()) {
            drawer = new StageDrawer();
        } else {
            drawer = new LyricDrawer();
        }
        previewImage = new WritableImage(200, 200);
        canvas.snapshot(new SnapshotParameters(), previewImage);
        BufferedImage bi = SwingFXUtils.fromFXImage((WritableImage) previewImage, null);
        SwingFXUtils.toFXImage(bi, previewImage);

        return previewImage;
    }
    
}
