/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.style;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.stage.WindowEvent;
import org.quelea.windows.main.MainWindow;
import org.quelea.windows.splash.SplashStage;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;

/**
 * Single place that decides which stylesheets the Quelea user interface uses.
 * <p>
 * Every operator-facing window should call {@link #apply(Scene)} on its scene
 * instead of adding stylesheets itself. The projection / stage windows must
 * <b>not</b> be styled, since they are drawn on the audience screens.
 * <p>
 * Stylesheets, in load order:
 * <ol>
 * <li>{@code base.css} - component styling shared by all themes</li>
 * <li>{@code icons.css} - vector icon shapes (see {@link QueleaIcon})</li>
 * <li>{@code dark-common.css} - only for dark themes</li>
 * <li>{@code theme-<id>.css} - the colour palette of the chosen
 * {@link UiTheme}</li>
 * <li>an optional {@code ui-style.css} in the Quelea user directory, so that
 * users and churches can tweak colours, fonts or icons without rebuilding
 * Quelea</li>
 * </ol>
 *
 * @author Quelea contributors
 */
public final class StyleManager {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    private static final String STYLE_ROOT = "org/quelea/style/";
    private static final String POLISHED_KEY = "quelea.dialog.polished";
    /** Package prefix of PreferencesFX and FormsFX (the options window). */
    private static final String PREFERENCESFX_PACKAGE = "com.dlsc.";
    public static final String BASE_STYLESHEET = STYLE_ROOT + "base.css";
    public static final String ICONS_STYLESHEET = STYLE_ROOT + "icons.css";
    public static final String LIGHT_THEME_STYLESHEET = STYLE_ROOT + "theme-light.css";
    public static final String DARK_THEME_STYLESHEET = STYLE_ROOT + "theme-dark.css";
    /**
     * Rules shared by all dark themes (loaded before the palette).
     */
    public static final String DARK_COMMON_STYLESHEET = STYLE_ROOT + "dark-common.css";

    /**
     * File name of the optional user stylesheet in the Quelea user directory.
     */
    public static final String USER_STYLESHEET_NAME = "ui-style.css";

    private static List<String> stylesheets;

    /**
     * Every scene and node styled through this class, so a new theme can be
     * applied to them while Quelea is running. Weak, so closed windows can
     * still be garbage collected.
     */
    private static final Map<Scene, Boolean> STYLED_SCENES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Parent, Boolean> STYLED_PARENTS = Collections.synchronizedMap(new WeakHashMap<>());

    private static final ReadOnlyObjectWrapper<UiTheme> THEME = new ReadOnlyObjectWrapper<>();
    private static boolean followingSystem;

    private StyleManager() {
        throw new AssertionError();
    }

    /**
     * Add the Quelea stylesheets to the given scene.
     *
     * @param scene the scene to style.
     * @return the same scene, for chaining.
     */
    public static Scene apply(Scene scene) {
        scene.getStylesheets().addAll(getStylesheets());
        STYLED_SCENES.put(scene, Boolean.TRUE);
        scene.windowProperty().addListener((obs, oldWindow, window) -> {
            if (window != null && !(window instanceof MainWindow) && !(window instanceof SplashStage)) {
                window.addEventHandler(WindowEvent.WINDOW_SHOWING, e -> polishDialog(scene.getRoot()));
            }
        });
        return scene;
    }

    /**
     * Give older dialog layouts consistent spacing: form grids that were laid
     * out without any gap get a standard gap between labels and fields, and
     * the window content gets a little breathing room.
     */
    private static void polishDialog(Parent root) {
        // Only once per layout: windows that are hidden and shown again (e.g.
        // the options window) must look the same every time.
        if (root == null || root.getProperties().putIfAbsent(POLISHED_KEY, Boolean.TRUE) != null) {
            return;
        }
        if (root instanceof Region region && region.getPadding().getTop() < 8) {
            Insets p = region.getPadding();
            if (!(root instanceof BorderPane)) {
                region.setPadding(new Insets(Math.max(p.getTop(), 12), Math.max(p.getRight(), 14),
                        Math.max(p.getBottom(), 12), Math.max(p.getLeft(), 14)));
            }
        }
        polishGrids(root);
        // Let a footer that sits directly in a padded VBox root reach the window edges
        if (root instanceof VBox box) {
            Insets p = box.getPadding();
            for (Node child : box.getChildren()) {
                if (child.getStyleClass().contains("dialog-button-bar")) {
                    VBox.setMargin(child, new Insets(8, -p.getRight(), -p.getBottom(), -p.getLeft()));
                }
            }
        }
    }

    private static void polishGrids(Parent parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child.getClass().getName().startsWith(PREFERENCESFX_PACKAGE)) {
                continue; // PreferencesFX / FormsFX lay out their own forms
            }
            if (child instanceof GridPane grid) {
                if (grid.getHgap() < 8) {
                    grid.setHgap(10);
                }
                if (grid.getVgap() < 6) {
                    grid.setVgap(8);
                }
                // A form directly inside the window layout: keep it off the window edges
                Insets gp = grid.getPadding();
                boolean tightPadding = Math.max(Math.max(gp.getTop(), gp.getBottom()), Math.max(gp.getLeft(), gp.getRight())) < 8;
                if (grid.getParent() instanceof BorderPane && tightPadding) {
                    grid.setPadding(new Insets(14, 16, 14, 16));
                }
            }
            if (child instanceof Parent p) {
                polishGrids(p);
            }
        }
    }

    /**
     * Add the Quelea stylesheets to a parent node. Useful for nodes that are
     * shown in a scene Quelea does not create itself (e.g. a DialogPane).
     *
     * @param parent the node to style.
     * @return the same node, for chaining.
     */
    public static <T extends Parent> T apply(T parent) {
        parent.getStylesheets().addAll(getStylesheets());
        STYLED_PARENTS.put(parent, Boolean.TRUE);
        return parent;
    }

    /**
     * Mark the main (OK / Done / Add) button of a dialog. The button gets the
     * accent colour, and if it sits in a row that only holds buttons, that
     * row is styled as the dialog's footer: right aligned, with its own
     * background (see .primary-button and .dialog-button-bar in base.css).
     *
     * @param button the dialog's primary button.
     */
    public static void markPrimaryButton(Button button) {
        if (!button.getStyleClass().contains("primary-button")) {
            button.getStyleClass().add("primary-button");
        }
        button.parentProperty().addListener((obs, oldParent, newParent) -> markButtonBar(newParent));
        markButtonBar(button.getParent());
    }

    private static void markButtonBar(Parent parent) {
        if (parent instanceof BorderPane pane && pane.getBottom() instanceof Button) {
            // A lone button at the bottom of a window: give it a proper footer.
            // (Deferred, since we are being called while it is added to the pane.)
            Platform.runLater(() -> {
                if (pane.getBottom() instanceof Button button) {
                    BorderPane.setMargin(button, null);
                    pane.setBottom(new HBox(button));
                    resizeIfShowing(pane);
                }
            });
            return;
        }
        if (parent instanceof StackPane stack && !stack.getStyleClass().contains("dialog-button-bar")
                && stack.getChildren().stream().allMatch(node -> node instanceof ButtonBase)) {
            // A button centred in its own StackPane: turn that pane into the footer
            stack.getStyleClass().add("dialog-button-bar");
            stack.setAlignment(Pos.CENTER_RIGHT);
            resizeIfShowing(stack);
            return;
        }
        if (!(parent instanceof HBox box) || box.getStyleClass().contains("dialog-button-bar")) {
            return;
        }
        boolean onlyButtons = box.getChildren().stream()
                .allMatch(node -> node instanceof ButtonBase || node.getClass() == Region.class);
        if (onlyButtons) {
            box.getStyleClass().add("dialog-button-bar");
            box.setAlignment(Pos.CENTER_RIGHT);
            box.setMaxWidth(Double.MAX_VALUE);
            resizeIfShowing(box);
        }
    }

    private static void resizeIfShowing(Parent node) {
        if (node.getScene() != null && node.getScene().getWindow() instanceof Stage stage && stage.isShowing()) {
            stage.sizeToScene();
        }
    }

    /**
     * @return true if the dark colour palette is in use.
     */
    public static boolean isDark() {
        return getTheme().isDark();
    }

    /**
     * @return the interface theme currently shown.
     */
    public static UiTheme getTheme() {
        if (THEME.get() == null) {
            THEME.set(UiTheme.current());
        }
        return THEME.get();
    }

    /**
     * The interface theme currently shown. Code that draws its own colours
     * (for example HTML previews) can listen to this to redraw when the theme
     * changes.
     *
     * @return the read-only theme property.
     */
    public static ReadOnlyObjectProperty<UiTheme> themeProperty() {
        getTheme();
        return THEME.getReadOnlyProperty();
    }

    /**
     * Switch every open (and every already created) Quelea window to another
     * theme straight away, without a restart. Must be called on the JavaFX
     * application thread.
     *
     * @param theme the theme to show.
     */
    public static void applyTheme(UiTheme theme) {
        List<String> oldSheets = getStylesheets();
        synchronized (StyleManager.class) {
            stylesheets = null;
            THEME.set(theme);
        }
        List<String> newSheets = getStylesheets();
        LOGGER.log(Level.INFO, "Switched interface theme to {0}", theme.getId());
        synchronized (STYLED_SCENES) {
            for (Scene scene : STYLED_SCENES.keySet()) {
                replace(scene.getStylesheets(), oldSheets, newSheets);
            }
        }
        synchronized (STYLED_PARENTS) {
            for (Parent parent : STYLED_PARENTS.keySet()) {
                replace(parent.getStylesheets(), oldSheets, newSheets);
            }
        }
    }

    /**
     * Swap the Quelea stylesheets in a list, keeping any others where they are.
     */
    private static void replace(List<String> sheets, List<String> oldSheets, List<String> newSheets) {
        int index = sheets.indexOf(BASE_STYLESHEET);
        sheets.removeAll(oldSheets);
        sheets.addAll(index < 0 ? sheets.size() : Math.min(index, sheets.size()), newSheets);
    }

    /**
     * With the "follow system" theme, switch between light and dark as soon
     * as the operating system does.
     */
    private static void followSystemColourScheme() {
        if (followingSystem) {
            return;
        }
        followingSystem = true;
        try {
            Platform.getPreferences().colorSchemeProperty().addListener((obs, oldScheme, newScheme) -> {
                if (getTheme() == UiTheme.SYSTEM) {
                    UiTheme.refreshSystemColourScheme();
                    applyTheme(UiTheme.SYSTEM);
                }
            });
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Couldn't follow the system colour scheme", ex);
        }
    }

    /**
     * Get the stylesheets, in load order, for the current theme.
     *
     * @return an unmodifiable list of stylesheet URLs.
     */
    public static synchronized List<String> getStylesheets() {
        if (stylesheets == null) {
            List<String> list = new ArrayList<>();
            list.add(BASE_STYLESHEET);
            list.add(ICONS_STYLESHEET);
            UiTheme theme = getTheme();
            LOGGER.log(Level.INFO, "Using interface theme {0}", theme.getId());
            if (theme == UiTheme.SYSTEM && Platform.isFxApplicationThread()) {
                followSystemColourScheme();
            }
            if (theme.isDark()) {
                list.add(DARK_COMMON_STYLESHEET);
            }
            list.add(theme.getStylesheet());
            File userSheet = new File(QueleaProperties.get().getQueleaUserHome(), USER_STYLESHEET_NAME);
            if (userSheet.isFile()) {
                LOGGER.log(Level.INFO, "Using user stylesheet {0}", userSheet.getAbsolutePath());
                list.add(userSheet.toURI().toString());
            }
            stylesheets = Collections.unmodifiableList(list);
        }
        return stylesheets;
    }
}
