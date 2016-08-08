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
import java.io.File;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import org.javafx.dialog.Dialog;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageGroupDisplayable;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.data.displayable.WebDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.lyrics.SelectLyricsPanel;
import org.quelea.windows.main.actionhandlers.AddBibleVerseHandler;
import org.quelea.windows.multimedia.VLCWindow;
import org.quelea.windows.presentation.PowerPointHandler;

/**
 * The panel displaying the live lyrics selection - changes made on this panel
 * are reflected on the live projection.
 * <p/>
 * @author Michael
 */
public class LivePanel extends LivePreviewPanel {

    private final HBox loopBox;
    private final ToggleButton loop;
    private final TextField loopDuration;

    private final ToggleButton logo;
    private final ToggleButton black;
    private final ToggleButton clear;
    private final ToggleButton hide;

    private final ToolBar header;
    private Displayable oldD;
    private WritableImage webPreviewImage;
    private ScheduledExecutorService updateWebPreview;

    /**
     * Create a new live lyrics panel.
     */
    public LivePanel() {
        getPresentationPanel().setLive();
        getPdfPanel().setLive();
         getVideoPanel().setLive();
        getImageGroupPanel().setLive();
        header = new ToolBar();
        Label headerLabel = new Label(LabelGrabber.INSTANCE.getLabel("live.heading"));
        headerLabel.setStyle("-fx-font-weight: bold;");
        header.getItems().add(headerLabel);
        loop = new ToggleButton(LabelGrabber.INSTANCE.getLabel("loop.label") + ":");
        loopDuration = new TextField("10");
        loopDuration.setMaxWidth(40);
        loopDuration.setMinWidth(40);
        loopDuration.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                String text = t.getCharacter();
                if (text.isEmpty()) {
                    return;
                }
                char arr[] = text.toCharArray();
                char ch = arr[text.toCharArray().length - 1];
                if (!(ch >= '0' && ch <= '9')) {
                    t.consume();
                }
                try {
                    String newText = loopDuration.getText() + ch;
                    int num = Integer.parseInt(newText);
                    if (num > 100 || num <= 0) {
                        t.consume();
                    }
                } catch (NumberFormatException ex) {
                    t.consume();
                }
            }
        });
        loopBox = new HBox(5);
        loopBox.getChildren().add(new Label("   "));
        loopBox.getChildren().add(loop);
        loopBox.getChildren().add(loopDuration);
        loopBox.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("seconds.label")));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getItems().add(spacer);
        ImageView logoIV;
        if (Utils.isMac()) {
            logoIV = new ImageView(new Image("file:icons/logo48.png"));
        } else {
            logoIV = new ImageView(new Image("file:icons/logo16.png"));
        }
        logoIV.setFitHeight(16);
        logoIV.setFitWidth(16);
        logo = new ToggleButton("", logoIV);
        Utils.setToolbarButtonStyle(logo);
        logo.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("logo.screen.tooltip") + " (F5)"));
        logo.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (t.getButton().equals(MouseButton.SECONDARY)) {
                    FileChooser chooser = new FileChooser();
                    if (QueleaProperties.get().getLastDirectory() != null) {
                        chooser.setInitialDirectory(QueleaProperties.get().getLastDirectory());
                    }
                    chooser.getExtensionFilters().add(FileFilters.IMAGES);
                    chooser.setInitialDirectory(QueleaProperties.get().getImageDir().getAbsoluteFile());
                    File file = chooser.showOpenDialog(QueleaApp.get().getMainWindow());
                    if (file != null) {
                        QueleaProperties.get().setLastDirectory(file.getParentFile());
                        QueleaProperties.get().setLogoImage(file.getAbsolutePath());
                        updateLogo();
                    }
                }
            }
        });
        logo.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                HashSet<DisplayCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for (DisplayCanvas canvas : canvases) {
                    canvas.setLogoDisplaying(logo.isSelected());
                }
            }
        });
        header.getItems().add(logo);
        black = new ToggleButton("", new ImageView(new Image("file:icons/black.png")));
        Utils.setToolbarButtonStyle(black);
        black.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("black.screen.tooltip") + " (F6)"));
        black.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                if (getDisplayable() instanceof PresentationDisplayable && QueleaProperties.get().getUsePP()) {
                    PowerPointHandler.screenBlack();
                }
                HashSet<DisplayCanvas> canvases = new HashSet<>();
                canvases.addAll(getCanvases());
                for (DisplayCanvas canvas : canvases) {
                    canvas.setBlacked(black.isSelected());
                }
            }
        });
        header.getItems().add(black);
        ImageView clearIV = new ImageView(new Image("file:icons/clear.png"));
        clearIV.setFitWidth(16);
        clearIV.setFitHeight(16);
        clear = new ToggleButton("", clearIV);
        Utils.setToolbarButtonStyle(clear);
        clear.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("clear.text.tooltip") + " (F7)"));
        clear.setOnAction((javafx.event.ActionEvent t) -> {
            HashSet<DisplayCanvas> canvases = new HashSet<>();
            canvases.addAll(getCanvases());
            for (DisplayCanvas canvas : canvases) {
                if (canvas.isStageView() && !QueleaProperties.get().getClearStageWithMain()) {
                    canvas.setCleared(false);
                } else {
                    canvas.setCleared(clear.isSelected());
                }
            }
        });
        header.getItems().add(clear);
        ImageView hideIV = new ImageView(new Image("file:icons/cross.png"));
        hideIV.setFitWidth(16);
        hideIV.setFitHeight(16);
        hide = new ToggleButton("", hideIV);
        Utils.setToolbarButtonStyle(hide);
        hide.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("hide.display.output.tooltip") + " (F8)"));
        hide.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                int projectorScreen = QueleaProperties.get().getProjectorScreen();
                int stageScreen = QueleaProperties.get().getStageScreen();
                final ObservableList<Screen> monitors = Screen.getScreens();

                DisplayStage appWindow = QueleaApp.get().getProjectionWindow();
                DisplayStage stageWindow = QueleaApp.get().getStageWindow();

                final boolean lyricsHidden;
                if (!QueleaProperties.get().isProjectorModeCoords() && (projectorScreen >= monitors.size() || projectorScreen < 0)) {
                    lyricsHidden = true;
                } else {
                    lyricsHidden = false;
                }

                final boolean stageHidden;
                if (!QueleaProperties.get().isStageModeCoords() && (stageScreen >= monitors.size() || stageScreen < 0)) {
                    stageHidden = true;
                } else {
                    stageHidden = false;
                }

                if (!lyricsHidden) {
                    if (hide.isSelected()) {
                        appWindow.hide();
                    } else {
                        appWindow.show();
                    }
                }
                if (!stageHidden) {
                    if (hide.isSelected()) {
                        stageWindow.hide();
                    } else {
                        stageWindow.show();
                    }
                }
                VLCWindow.INSTANCE.refreshPosition();
//                VLCWindow.INSTANCE.setHideButton(hide.isSelected());
            }
        });
//        header.getItems().add(hide);
        setTop(header);
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                LivePanel lp = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel();
                if (t.getCharacter().equals(" ")) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
                }
                if (getDisplayable() instanceof PresentationDisplayable) {
                    if (t.getCharacter().matches("\\d")) {
                        try {
                            int i = Integer.parseInt(t.getCharacter());
                            if (i > 0 && i < 10) {
                                if (QueleaProperties.get().getUsePP()) {
                                    PowerPointHandler.gotoSlide(i);
                                    String result = PowerPointHandler.getCurrentSlide();
                                    if (!result.contains("not running") && !result.equals("")) {
                                        int slide = Integer.parseInt(result);
                                        QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getPresentationPanel().getPresentationPreview().select(slide, true);
                                    }
                                } else {
                                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getPresentationPanel().getPresentationPreview().select(i);
                                }
                            }
                        } catch (Exception ex) {
                            LoggerUtils.getLogger().log(Level.INFO, "Could not cast keycode into integer for slide selection.", ex);
                        }
                    }
                } else if (t.getCharacter().matches("c") || t.getCharacter().matches("b") || t.getCharacter().matches("p") || t.getCharacter().matches("t") || t.getCharacter().matches("\\d")) {
                    final int selectedIndex = lp.getLyricsPanel().getCurrentIndex();
                    final int slideIndex = getSlideIndex(selectedIndex, t.getCharacter());
                    if (slideIndex > -1) {
                        Platform.runLater(() -> {
                            lp.getLyricsPanel().select(slideIndex);
                        });
                    } else if (t.getCharacter().matches("\\d")) {
                        try {
                            int i = Integer.parseInt(t.getCharacter());
                            final int index = (i > 0) ? (i - 1) : 10;
                            Platform.runLater(() -> {
                                if (lp.getDisplayable() instanceof PresentationDisplayable) {
                                    lp.getPresentationPanel().getPresentationPreview().select(i);
                                } else if (lp.getDisplayable() instanceof PdfDisplayable) {
                                    lp.getPdfPanel().getPresentationPreview().select(i);
                                } else {
                                    lp.getLyricsPanel().select(index);
                                }
                            });
                        } catch (Exception ex) {
                            LoggerUtils.getLogger().log(Level.INFO, "Could not cast keycode into integer for slide selection.", ex);
                        }
                    }
                }
                if (t.getCharacter().matches("\\+")) {
                    if (getDisplayable() instanceof BiblePassage) {
                        new AddBibleVerseHandler().add();
                        QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
                        int last = lp.getLyricsPanel().getLyricsList().getItems().size() - 1;
                        lp.getLyricsPanel().select(last);
                    }
                }
            }

            private boolean matches(String shortcut, String sectionTitle) {
                if (shortcut.equalsIgnoreCase("c") && sectionTitle.toLowerCase().startsWith("chorus")) {
                    return true;
                }
                if (shortcut.equalsIgnoreCase("p") && sectionTitle.toLowerCase().contains("pre-chorus")) {
                    return true;
                }
                if (shortcut.equalsIgnoreCase("b") && sectionTitle.toLowerCase().contains("bridge")) {
                    return true;
                }
                if (shortcut.equalsIgnoreCase("t") && sectionTitle.toLowerCase().contains("tag")) {
                    return true;
                }
                if (shortcut.matches("\\d") && sectionTitle.toLowerCase().contains("verse " + shortcut)) {
                    return true;
                }
                return false;
            }

            private int getSlideIndex(int selectedIndex, String shortcutKey) {
                if (getDisplayable() instanceof TextDisplayable) {
                    TextDisplayable displayable = (TextDisplayable) getDisplayable();
                    TextSection[] sections = displayable.getSections();
                    for (int i = (selectedIndex + 1) % sections.length; i != selectedIndex; i = ((i + 1) % sections.length)) {
                        if (matches(shortcutKey, sections[i].getTitle())) {
                            return i;
                        }
                    }
                    return -1;
                } else {
                    return -1;
                }
            }
        });
        this.getLyricsPanel().getLyricsList().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode().equals(KeyCode.PAGE_DOWN)) { // || t.getCode().equals(KeyCode.DOWN)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
                } else if (t.getCode().equals(KeyCode.PAGE_UP)) { // || t.getCode().equals(KeyCode.UP)) {
                    t.consume();
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
                }
            }
        });
    }

    /**
     * Set the displayable to be shown on this live panel.
     * <p/>
     * @param d the displayable to show.
     * @param index the index to use for the displayable, if relevant.
     */
    @Override
    public void setDisplayable(Displayable d, int index) {
        super.setDisplayable(d, index);
        loop.setSelected(false);
        if (d instanceof PresentationDisplayable || d instanceof PdfDisplayable || d instanceof ImageGroupDisplayable) {
            if (!header.getItems().contains(loopBox)) {
                header.getItems().add(1, loopBox);
            }
            if (d instanceof PresentationDisplayable) {
                if (QueleaProperties.get().getUsePP() && (oldD == null || !oldD.equals(d))) {
                    String filePath;
                    filePath = "\"" + d.getResources().toString().replace("[", "").replace("]", "") + "\"";
                    String openPP = PowerPointHandler.openPresentation(filePath);
                    if (openPP.contains("not started")) {
                        Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("presentation.not.started.label"), LabelGrabber.INSTANCE.getLabel("presentation.not.started.message"));
                        LoggerUtils.getLogger().log(Level.INFO, "PowerPoint couldn't be started.");
                    } else if (openPP.contains("running")) {
                        if (!(oldD instanceof PresentationDisplayable)) {
                            Dialog.showAndWaitError(LabelGrabber.INSTANCE.getLabel("close.all.presentations.label"), LabelGrabber.INSTANCE.getLabel("close.all.presentations.message"));
                        }
                        PowerPointHandler.closePresentation();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(LivePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        openPP = PowerPointHandler.openPresentation(filePath);
                        if (openPP.contains("not started")) {
                            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("presentation.not.started.label"), LabelGrabber.INSTANCE.getLabel("presentation.not.started.message"));
                            LoggerUtils.getLogger().log(Level.INFO, "PowerPoint couldn't be started.");
                        }
                    }
                }
            }
        } else {
            header.getItems().remove(loopBox);
            if (oldD != null && oldD instanceof PresentationDisplayable) {
                PowerPointHandler.closePresentation();
            }
        }
        if (d == null) {
            clear.setSelected(false);
            clear.setDisable(true);
        } else {
            clear.setDisable(!d.supportClear());
            if (!d.supportClear()) {
                clear.setSelected(false);
            }
        }
         if (d instanceof WebDisplayable) {
            updateWebPreview = Executors.newSingleThreadScheduledExecutor();
            updateWebPreview.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (d != null && d instanceof WebDisplayable) {
                                getWebPanel().setLoading();
                                getWebPanel().getImagePreview().setImage(geWebPreviewImage());
                            }
                        }
                    });
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
        if (oldD instanceof WebDisplayable) {
            ((WebDisplayable) oldD).dispose();
            updateWebPreview.shutdown();
        }
        HashSet<DisplayCanvas> canvases = new HashSet<>();
        canvases.addAll(getCanvases());
        for (DisplayCanvas canvas : canvases) {
            canvas.setBlacked(black.isSelected());
            if (canvas.isStageView() && !QueleaProperties.get().getClearStageWithMain()) {
                canvas.setCleared(false);
            } else {
                canvas.setCleared(clear.isSelected());
            }
            if (d instanceof WebDisplayable) {
                canvas.makeClickable(true);
            } else {
                canvas.makeClickable(false);
            }
        }
        oldD = d;
    }

    /**
     * Toggle the "black" button.
     */
    public void toggleBlack() {
        if (!black.isDisable()) {
            black.fire();
        }
    }

    /**
     * Toggle the "clear" button.
     */
    public void toggleClear() {
        if (!clear.isDisable()) {
            clear.fire();
        }
    }

    /**
     * Toggle the "hide" button.
     */
    public void toggleHide() {
        if (!hide.isDisable()) {
            hide.fire();
        }
    }

    /**
     * Toggle the "logo" button.
     */
    public void toggleLogo() {
        if (!logo.isDisable()) {
            logo.fire();
        }
    }

    /**
     * Determine if the loop button is selected.
     * <p>
     * @return true if it's selected, false otherwise.
     */
    public boolean isLoopSelected() {
        return loop.isSelected();
    }

    public TextField getLoopDurationTextField() {
        return loopDuration;
    }

    /**
     * Get the hide button.
     * <p>
     * @return the hide button.
     */
    public ToggleButton getHide() {
        return hide;
    }

    /**
     * Calls updateLogo() on each canvas. A tidy method.
     */
    private void updateLogo() {
        HashSet<DisplayCanvas> canvases = new HashSet<>();
        canvases.addAll(getCanvases());
        for (DisplayCanvas canvas : canvases) {
            canvas.updateLogo();
        }
    }

    public void updateCanvases() {
        HashSet<DisplayCanvas> canvases = new HashSet<>();
        canvases.addAll(getCanvases());
        for (DisplayCanvas canvas : canvases) {
            canvas.update();
        }
    }

    /**
     * Determine if content is currently being shown on this panel, if not it
     * may be showing the logo, cleared, blacked or hidden.
     * <p>
     * @return true if content is showing, false otherwise.
     */
    public boolean isContentShowing() {
        return !(logo.isSelected() || clear.isSelected() || black.isSelected() || hide.isSelected());
    }

    public boolean getLogoed() {
        return logo.isSelected();
    }

    public boolean getBlacked() {
        return black.isSelected();
    }

    public boolean getCleared() {
        return clear.isSelected();
    }

    public void setBlacked(boolean isBlack) {
        this.black.setSelected(isBlack);
    }

    public void stopLoop() {
        loop.setSelected(false);
    }

    /**
     * Get a preview image of the web view or move it to the main panel if it's
     * not visible.
     *
     * @return a screenshot image of the web view
     */
    private Image geWebPreviewImage() {
        DisplayCanvas canvas = QueleaApp.get().getProjectionWindow().getCanvas();
        if (QueleaApp.get().getProjectionWindow().isShowing() && isContentShowing()) {
            Double d = canvas.getBoundsInLocal().getHeight();
            int h = d.intValue();
            Double d2 = canvas.getBoundsInLocal().getWidth();;
            int w = d2.intValue();
            webPreviewImage = new WritableImage(w, h);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            canvas.snapshot(params, webPreviewImage);
            BufferedImage bi = SwingFXUtils.fromFXImage((WritableImage) webPreviewImage, null);
            SwingFXUtils.toFXImage(bi, webPreviewImage);
            WebView wv = getWebPanel().removeWebView();
            if (!canvas.getChildren().contains(wv)) {
                canvas.getChildren().add(wv);
            }
            return webPreviewImage;
        } else {
            getWebPanel().addWebView();
            return new Image("file:icons/web preview.png");
        }
    }

}

