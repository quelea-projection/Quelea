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
package org.quelea.windows.web;

import com.sun.glass.ui.Robot;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.quelea.data.displayable.WebDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.AbstractPanel;
import org.quelea.windows.main.DisplayCanvas;
import org.quelea.windows.main.DisplayableDrawer;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * A panel used in the live / preview panels for displaying webpages.
 *
 * Doesn't work with Flash or Google Presentation but works well with HTML5.
 *
 * <p/>
 * @author Arvid
 */
public class WebPanel extends AbstractPanel {

    private WebDrawer drawer;
    private WebDisplayable wd;
    private final ImageView imagePreview;
    private final ImageView loading;
    private final StackPane imagePane;
    private Button back;
    private Button forward;
    private Button reload;
    private Button go;
    private Button plus;
    private Button minus;
    private TextField url;
    private Separator separator;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Create a new web panel.
     */
    public WebPanel() {
        imagePane = new StackPane();
        imagePreview = new ImageView();
        loading = new ImageView(new Image("file:icons/loading.gif"));
        drawer = new WebDrawer();
        VBox centerBit = new VBox(5);
        centerBit.setAlignment(Pos.CENTER);
        BorderPane.setMargin(centerBit, new Insets(10));
        loading.setPreserveRatio(true);
        loading.setFitHeight(50);
        imagePane.getChildren().add(loading);
        imagePreview.fitHeightProperty().bind(heightProperty().subtract(200));
        imagePreview.fitWidthProperty().bind(widthProperty().subtract(20));
        imagePane.getChildren().add(imagePreview);
        centerBit.getChildren().add(imagePane);
        setCenter(centerBit);
        setMinWidth(200);
        setStyle("-fx-background-color:grey;");
        HBox navigationBar = new HBox(5);
        navigationBar.setAlignment(Pos.CENTER);
        setupNavigationBarContent();
        separator = new Separator(Orientation.VERTICAL);
        separator.setVisible(false);
        navigationBar.getChildren().add(back);
        navigationBar.getChildren().add(forward);
        navigationBar.getChildren().add(reload);
        navigationBar.getChildren().add(separator);
        navigationBar.getChildren().add(url);
        navigationBar.getChildren().add(go);
        separator = new Separator(Orientation.VERTICAL);
        separator.setVisible(false);
        navigationBar.getChildren().add(separator);
        navigationBar.getChildren().add(plus);
        navigationBar.getChildren().add(minus);
        setBottom(navigationBar);
        imagePreview.setPreserveRatio(true);

        imagePreview.setOnMouseClicked(e -> {
            if (wd != null) {
                final Point2D sceneCoord = new Point2D(QueleaApp.get().getProjectionWindow().getX(), QueleaApp.get().getProjectionWindow().getY());
                click(sceneCoord.getX() + (e.getX() * (QueleaApp.get().getProjectionWindow().getWidth() / imagePreview.getBoundsInParent().getWidth())), sceneCoord.getY() + (e.getY() * (QueleaApp.get().getProjectionWindow().getHeight() / imagePreview.getBoundsInParent().getHeight())));
            }
        });
        imagePreview.setOnScroll(e -> {
            if (wd != null) {
                Robot r = com.sun.glass.ui.Application.GetApplication().createRobot();
                QueleaApp.get().getProjectionWindow().requestFocus();
                if (e.getDeltaY() < 0) {
                    r.keyPress(java.awt.event.KeyEvent.VK_DOWN);
                    r.keyRelease(java.awt.event.KeyEvent.VK_DOWN);
                } else if (e.getDeltaY() > 0) {
                    r.keyPress(java.awt.event.KeyEvent.VK_UP);
                    r.keyRelease(java.awt.event.KeyEvent.VK_UP);
                } else if (e.getDeltaX() < 0) {
                    r.keyPress(java.awt.event.KeyEvent.VK_RIGHT);
                    r.keyRelease(java.awt.event.KeyEvent.VK_RIGHT);
                } else if (e.getDeltaX() > 0) {
                    r.keyPress(java.awt.event.KeyEvent.VK_LEFT);
                    r.keyRelease(java.awt.event.KeyEvent.VK_LEFT);
                }
            }
        });
        imagePreview.setImage(new Image("file:icons/web preview.png"));
        addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent t) -> {
            if (t.getCode().equals(KeyCode.PAGE_DOWN) || t.getCode().equals(KeyCode.DOWN)) {
                t.consume();
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
            } else if (t.getCode().equals(KeyCode.PAGE_UP) || t.getCode().equals(KeyCode.UP)) {
                t.consume();
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
            }
        });

    }

    @Override
    public DisplayableDrawer getDrawer(DisplayCanvas canvas) {
        drawer.setCanvas(canvas);
        return drawer;
    }

    /**
     * Clear the panel and all canvases associated with it.
     */
    @Override
    public void removeCurrentDisplayable() {
        super.removeCurrentDisplayable();
    }

    /**
     * Show a given web displayable on the panel.
     * <p/>
     * @param displayable the web displayable.
     */
    public void showDisplayable(WebDisplayable displayable) {
        wd = displayable;
        setCurrentDisplayable(displayable);
        updateCanvas();
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }

    /**
     * Check this Advances the current slide.
     * <p/>
     */
    public void advance() {
        MainPanel qmp = QueleaApp.get().getMainWindow().getMainPanel();
        boolean lastItemTest = qmp.getLivePanel().getDisplayable() == qmp.getSchedulePanel().getScheduleList().getItems().get(qmp.getSchedulePanel().getScheduleList().getItems().size() - 1);
        if (QueleaProperties.get().getAdvanceOnLive() && QueleaProperties.get().getSongOverflow() && !lastItemTest) {
            qmp.getPreviewPanel().goLive();
        }
    }

    /**
     * Check this Moves to the previous slide.
     * <p/>
     */
    public void previous() {
        MainPanel qmp = QueleaApp.get().getMainWindow().getMainPanel();
        boolean firstItemTest = qmp.getSchedulePanel().getScheduleList().getItems().get(0) == qmp.getLivePanel().getDisplayable();
        if (QueleaProperties.get().getAdvanceOnLive() && QueleaProperties.get().getSongOverflow() && !firstItemTest) {
            //Assuming preview panel is one ahead, and should be one behind
            int index = qmp.getSchedulePanel().getScheduleList().getSelectionModel().getSelectedIndex();
            if (qmp.getLivePanel().getDisplayable() == qmp.getSchedulePanel().getScheduleList().getItems().get(qmp.getSchedulePanel().getScheduleList().getItems().size() - 1)) {
                index -= 1;
            } else {
                index -= 2;
            }
            if (index >= 0) {
                qmp.getSchedulePanel().getScheduleList().getSelectionModel().clearAndSelect(index);
                qmp.getPreviewPanel().selectLastLyric();
                qmp.getPreviewPanel().goLive();
            }
        }
    }

    /**
     * Send mouse click signal to web page.
     *
     * @param x x position to click
     * @param y y position to click
     */
    private static void click(double x, double y) {
        QueleaApp.get().getProjectionWindow().requestFocus();
        java.awt.Point originalLocation = java.awt.MouseInfo.getPointerInfo().getLocation();
        try {
            java.awt.Robot robot = new java.awt.Robot();
            robot.mouseMove((int) x, (int) y);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.mouseMove((int) originalLocation.getX(), (int) originalLocation.getY());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed clicking the web view", e);
        }
    }

    /**
     * Get ImageView to display a preview in panel.
     *
     * @return an ImageView
     */
    public ImageView getImagePreview() {
        return imagePreview;
    }

    /**
     * Add/Remove loading GIF.
     */
    public void setLoading() {
        if (drawer.isLoading() && !imagePane.getChildren().contains(this.loading)) {
            imagePane.getChildren().add(this.loading);
        } else if (!drawer.isLoading()) {
            imagePane.getChildren().remove(this.loading);
        }
    }

    public void addWebView(WebDisplayable displayable) {
        removeWebView();
        imagePane.getChildren().add(displayable.getWebView());
    }

    public WebView removeWebView() {
        imagePane.getChildren().clear();
        imagePane.getChildren().add(imagePreview);
        return wd.getWebView();
    }

    public void blockButtons(boolean block) {
        back.setDisable(block);
        forward.setDisable(block);
        go.setDisable(block);
        reload.setDisable(block);
        url.setDisable(block);
        plus.setDisable(block);
        minus.setDisable(block);
    }

    public void setText() {
        if (wd != null) {
            url.setText(wd.getUrl());
        }
    }

    private void setupNavigationBarContent() {
        back = new Button("", new ImageView(new Image("file:icons/arrow-back.png")));
        back.setOnMouseClicked(e -> {
            if (wd != null) {
                wd.back();
            }
        });
        forward = new Button("", new ImageView(new Image("file:icons/arrow-forward.png")));
        forward.setOnMouseClicked(e -> {
            if (wd != null) {
                wd.forward();
            }
        });
        reload = new Button("", new ImageView(new Image("file:icons/reload.png")));
        reload.setOnMouseClicked(e -> {
            if (wd != null) {
                wd.reload();
            }
        });
        url = new TextField();
        url.setPrefHeight(32);
        url.setMaxHeight(32);
        url.setStyle(""
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-font-family: fantasy;");
        url.setTooltip(new Tooltip("Change URL"));
        url.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                if (wd != null) {
                    wd.setUrl(url.getText());
                }
            }
        });
        go = new Button("", new ImageView(new Image("file:icons/send.png")));
        go.setOnMouseClicked(e -> {
            if (wd != null) {
                wd.setUrl(url.getText());
            }
        });
        plus = new Button("", new ImageView(new Image("file:icons/zoom-in.png")));
        plus.setOnMouseClicked(e -> {
            if (wd != null) {
                wd.zoom(true);
            }
        });
        minus = new Button("", new ImageView(new Image("file:icons/zoom-out.png")));
        minus.setOnMouseClicked(e -> {
            if (wd != null) {
                wd.zoom(false);
            }
        });
    }

}
