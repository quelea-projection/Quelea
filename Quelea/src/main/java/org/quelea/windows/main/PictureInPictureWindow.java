package org.quelea.windows.main;

import java.io.File;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.stage.Window;
import javafx.util.Duration;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.Utils;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.windows.style.StyleManager;

/** An independent window, so minimising the operator window does not hide it. */
final class PictureInPictureWindow {
    private static final double COMPACT_SIZE = 96;
    private static final double MIN_PREVIEW_WIDTH = 240;
    private static final double MIN_PREVIEW_HEIGHT = 135;
    private static final long NATIVE_RESIZE_INTERVAL = 12_000_000L;

    /** Prevent native resize requests from generating an unbounded pulse storm. */
    static final class NativeResizeGate {
        private boolean updated;
        private long lastUpdate;

        boolean shouldUpdate(long now, boolean finalFrame) {
            if (!updated || finalFrame || now - lastUpdate >= NATIVE_RESIZE_INTERVAL) {
                updated = true;
                lastUpdate = now;
                return true;
            }
            return false;
        }
    }
    private final Stage main;
    private final Supplier<? extends Stage> projection;
    private final BooleanSupplier hideProjection;
    private final PictureInPictureProjection projectionVisibility = new PictureInPictureProjection();
    private final Stage window = new Stage();
    private final PictureInPictureMirror mirror;
    private final ImageView slide = new ImageView();
    private final StackPane webHost = new StackPane();
    private final Button restore;
    private final ToggleButton pin;
    private final Label restoreHint;
    private boolean positioned;
    private boolean returning;
    private boolean ownerHiding;
    private boolean restoredFocus;
    private boolean pointerInside;
    private double pointerScreenX = Double.NaN;
    private double pointerScreenY = Double.NaN;
    private boolean keyboardReveal;
    private boolean keyboardNavigating;
    private boolean interacting;
    private boolean expanded;
    private boolean anchorRight;
    private boolean anchorBottom;
    private boolean viewportReady;
    private double frameWidth;
    private double frameHeight;
    private double positionOffsetX;
    private double positionOffsetY;
    private double transitionAnchorX;
    private double transitionAnchorY;
    private double previewWidth = 400;
    private double previewHeight = 225;
    private Timeline transition;
    private final PauseTransition collapseDelay = new PauseTransition(Duration.millis(180));
    private final PauseTransition viewportInit = new PauseTransition(Duration.millis(150));
    private final PauseTransition positionInit = new PauseTransition(Duration.millis(150));
    private final PauseTransition controlsSettle = new PauseTransition(Duration.millis(50));
    private final DoubleProperty controlsOpacity = new SimpleDoubleProperty(1);
    private final BooleanProperty resizingGrip = new SimpleBooleanProperty();
    private Timeline controlsFade;
    private StackPane logoCover;
    private final PauseTransition restoreCheck = new PauseTransition(Duration.seconds(1));
    private final PauseTransition activationCheck = new PauseTransition(Duration.millis(250));

    PictureInPictureWindow(Stage main, Supplier<? extends Node> source,
            Supplier<Image> slideImage, Runnable onClosed, Supplier<? extends Stage> projection) {
        this(main, source, slideImage, onClosed, projection, () -> true);
    }

    PictureInPictureWindow(Stage main, Supplier<? extends Node> source,
            Supplier<Image> slideImage, Runnable onClosed, Supplier<? extends Stage> projection,
            BooleanSupplier hideProjection) {
        this.main = main;
        this.projection = projection;
        this.hideProjection = hideProjection;
        window.initStyle(StageStyle.UNDECORATED);
        window.setTitle(LabelGrabber.INSTANCE.getLabel("picture.in.picture.title"));
        Utils.addIconsToStage(window);
        window.setAlwaysOnTop(true);
        window.setMinWidth(COMPACT_SIZE);
        window.setMinHeight(COMPACT_SIZE);
        // Some Linux window managers report frame extents even for an
        // undecorated stage. Start large enough for them to measure the scene.
        window.setWidth(Utils.isLinux() ? 200 : COMPACT_SIZE);
        window.setHeight(Utils.isLinux() ? 200 : COMPACT_SIZE);

        ImageView frame = new ImageView();
        frame.getStyleClass().add("pip-frame");
        frame.setPreserveRatio(true);
        slide.setPreserveRatio(true);
        StackPane content = new StackPane(slide, webHost, frame);
        StackPane preview = new StackPane(content);
        content.setMinSize(0, 0);
        preview.setMinSize(0, 0);
        webHost.setMinSize(0, 0);
        preview.setStyle("-fx-background-color: black;");
        preview.setFocusTraversable(true);
        preview.setAccessibleText(LabelGrabber.INSTANCE.getLabel("picture.in.picture.preview"));
        preview.setAccessibleHelp(LabelGrabber.INSTANCE.getLabel("picture.in.picture.preview.help"));
        // Keep the web view and PowerPoint slide beneath the transparent
        // projection overlays in exactly the same aspect-ratio rectangle.
        content.maxWidthProperty().bind(Bindings.createDoubleBinding(() ->
                Math.min(preview.getWidth(), preview.getHeight() * aspectRatio(frame)),
                preview.widthProperty(), preview.heightProperty(), frame.imageProperty()));
        content.maxHeightProperty().bind(Bindings.createDoubleBinding(() ->
                Math.min(preview.getHeight(), preview.getWidth() / aspectRatio(frame)),
                preview.widthProperty(), preview.heightProperty(), frame.imageProperty()));
        content.prefWidthProperty().bind(content.maxWidthProperty());
        content.prefHeightProperty().bind(content.maxHeightProperty());
        frame.fitWidthProperty().bind(content.widthProperty());
        frame.fitHeightProperty().bind(content.heightProperty());
        slide.fitWidthProperty().bind(content.widthProperty());
        slide.fitHeightProperty().bind(content.heightProperty());
        webHost.setMouseTransparent(true);
        mirror = new PictureInPictureMirror(() -> {
            slide.setImage(slideImage.get());
            return source.get();
        }, frame);

        restore = new Button("", QueleaIcon.RETURN_TO_WINDOW.create(18));
        restore.getStyleClass().add("pip-return");
        String returnLabel = LabelGrabber.INSTANCE.getLabel("picture.in.picture.return");
        restore.setAccessibleText(returnLabel);
        restore.setTooltip(new Tooltip(returnLabel + " (" + MainWindow.PICTURE_IN_PICTURE_SHORTCUT.getDisplayText() + ")"));
        restore.setOnAction(e -> returnToMain());
        StackPane.setAlignment(restore, Pos.TOP_RIGHT);
        StackPane.setMargin(restore, new Insets(8));

        Region pinIcon = QueleaIcon.PIN.create(18);
        pinIcon.setMinWidth(11);
        pinIcon.setPrefWidth(11);
        pinIcon.setMaxWidth(11);
        pin = new ToggleButton("", pinIcon);
        pin.setMinSize(34, 34);
        pin.setPrefSize(34, 34);
        pin.setMaxSize(34, 34);
        pin.getStyleClass().add("pip-pin");
        pin.setVisible(false);
        pin.setManaged(false);
        Tooltip pinTooltip = new Tooltip();
        pin.setTooltip(pinTooltip);
        Runnable updatePinLabel = () -> {
            String label = LabelGrabber.INSTANCE.getLabel(pin.isSelected()
                    ? "picture.in.picture.unpin" : "picture.in.picture.pin");
            pin.setAccessibleText(label);
            pinTooltip.setText(label);
        };
        updatePinLabel.run();
        pin.selectedProperty().addListener((obs, wasPinned, pinned) -> {
            updatePinLabel.run();
            updateExpansion();
        });
        StackPane.setAlignment(pin, Pos.TOP_RIGHT);
        StackPane.setMargin(pin, new Insets(8, 50, 8, 8));

        Region resizeIcon = new Region();
        resizeIcon.getStyleClass().add("pip-resize-icon");
        resizeIcon.setMinSize(12, 12);
        resizeIcon.setPrefSize(12, 12);
        resizeIcon.setMaxSize(12, 12);
        StackPane resizeGrip = new StackPane(resizeIcon);
        resizeGrip.getStyleClass().add("pip-resize-grip");
        resizeGrip.setMinSize(24, 24);
        resizeGrip.setPrefSize(24, 24);
        resizeGrip.setMaxSize(24, 24);
        resizeGrip.setCursor(Cursor.SE_RESIZE);
        Tooltip.install(resizeGrip, new Tooltip(LabelGrabber.INSTANCE.getLabel("picture.in.picture.resize")));
        StackPane.setAlignment(resizeGrip, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(resizeGrip, new Insets(5));

        restore.opacityProperty().bind(controlsOpacity);
        pin.opacityProperty().bind(controlsOpacity);
        resizeGrip.opacityProperty().bind(controlsOpacity);
        restore.mouseTransparentProperty().bind(controlsOpacity.lessThan(0.98));
        pin.mouseTransparentProperty().bind(controlsOpacity.lessThan(0.98));
        // The resize grip must retain the mouse grab until the drag ends.
        resizeGrip.mouseTransparentProperty().bind(controlsOpacity.lessThan(0.98).and(resizingGrip.not()));
        controlsSettle.setOnFinished(e -> {
            if (transition == null && !resizingGrip.get() && window.isShowing()) {
                controlsFade = new Timeline(new KeyFrame(Duration.millis(75),
                        new KeyValue(controlsOpacity, 1, Interpolator.EASE_BOTH)));
                controlsFade.play();
            }
        });

        restoreHint = new Label(LabelGrabber.INSTANCE.getLabel("picture.in.picture.return.taskbar"));
        restoreHint.getStyleClass().add("pip-hint");
        restoreHint.setVisible(false);
        restoreHint.setManaged(false);
        StackPane.setAlignment(restoreHint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(restoreHint, new Insets(8, 34, 8, 8));

        ImageView queleaLogo = new ImageView(new Image(new File("icons/logo512.png").toURI().toString()));
        queleaLogo.setPreserveRatio(true);
        queleaLogo.setSmooth(true);
        queleaLogo.getStyleClass().add("pip-logo-mark");
        logoCover = new StackPane(queleaLogo);
        logoCover.getStyleClass().add("pip-logo-cover");
        logoCover.setMouseTransparent(true);
        logoCover.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Leave the right side clear for the return and resize controls.
        StackPane.setAlignment(queleaLogo, Pos.CENTER_LEFT);
        StackPane.setMargin(queleaLogo, new Insets(0, 0, 0, 11));
        queleaLogo.fitWidthProperty().bind(Bindings.min(44, logoCover.widthProperty().multiply(0.5)));
        queleaLogo.fitHeightProperty().bind(Bindings.min(44, logoCover.heightProperty().multiply(0.5)));

        StackPane root = new StackPane(preview, logoCover, restore, pin, resizeGrip, restoreHint);
        root.setMinSize(0, 0);
        root.getStyleClass().add("pip-window");
        root.setOnMouseEntered(e -> { notePointerPosition(e); pointerInside = true; updateExpansion(); });
        root.setOnMouseExited(e -> { notePointerPosition(e); pointerInside = false; updateExpansion(); });
        collapseDelay.setOnFinished(e -> {
            if (!wantsExpanded() && !interacting) setExpanded(false);
        });
        installMoveAndReturn(preview);
        installResize(resizeGrip);
        Scene scene = new Scene(root, Color.BLACK);
        scene.widthProperty().addListener((obs, before, after) -> sceneResized());
        scene.heightProperty().addListener((obs, before, after) -> sceneResized());
        StyleManager.applyWithoutDialogPolish(scene);
        scene.getAccelerators().put(MainWindow.PICTURE_IN_PICTURE_SHORTCUT, this::returnToMain);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && !event.isAltDown() && !event.isControlDown() && !event.isMetaDown()) {
                keyboardNavigating = true;
            }
            if (event.getCode() == KeyCode.SPACE && !event.isShortcutDown()
                    && !event.isAltDown() && !event.isControlDown() && !event.isMetaDown()
                    && !(scene.getFocusOwner() instanceof ButtonBase)) {
                keyboardReveal = true;
                updateExpansion();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                returnToMain();
                event.consume();
            } else if (event.isShortcutDown()
                    && (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.ADD)) {
                resizeBy(40);
                event.consume();
            } else if (event.isShortcutDown()
                    && (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT)) {
                resizeBy(-40);
                event.consume();
            } else if (event.isAltDown() && !event.isControlDown() && !event.isMetaDown()) {
                switch (event.getCode()) {
                    case LEFT -> { moveWindow(window.getX() - 20, window.getY()); event.consume(); }
                    case RIGHT -> { moveWindow(window.getX() + 20, window.getY()); event.consume(); }
                    case UP -> { moveWindow(window.getX(), window.getY() - 20); event.consume(); }
                    case DOWN -> { moveWindow(window.getX(), window.getY() + 20); event.consume(); }
                    default -> { }
                }
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SPACE && keyboardReveal) {
                keyboardReveal = false;
                updateExpansion();
                event.consume();
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            boolean changed = notePointerPosition(event);
            if (keyboardNavigating) {
                keyboardNavigating = false;
                changed = true;
            }
            if (changed) updateExpansion();
        });
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (notePointerPosition(event)) updateExpansion();
        });
        scene.focusOwnerProperty().addListener((obs, before, focused) -> updateExpansion());
        window.setScene(scene);
        viewportInit.setOnFinished(e -> {
            if (!window.isShowing()) return;
            frameWidth = Math.max(0, window.getWidth() - scene.getWidth());
            frameHeight = Math.max(0, window.getHeight() - scene.getHeight());
            double right = window.getX() + window.getWidth();
            window.setWidth(COMPACT_SIZE + frameWidth);
            window.setHeight(COMPACT_SIZE + frameHeight);
            double requestedX = right - window.getWidth();
            double requestedY = window.getY();
            if (frameWidth > 0 || frameHeight > 0) {
                // Some compositors add frame offsets to reported positions.
                // Measure away from screen edges, where a compositor can clamp
                // a move, before revealing the compact window.
                window.setOpacity(0);
                var screens = Screen.getScreensForRectangle(requestedX, requestedY, window.getWidth(), window.getHeight());
                Rectangle2D bounds = (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getVisualBounds();
                double probeX = bounds.getMinX() + Math.max(0, (bounds.getWidth() - window.getWidth()) / 2);
                double probeY = bounds.getMinY() + Math.max(0, (bounds.getHeight() - window.getHeight()) / 2);
                window.setX(probeX);
                window.setY(probeY);
                positionInit.setOnFinished(ignored -> {
                    if (!window.isShowing()) return;
                    positionOffsetX = probeX - window.getX();
                    positionOffsetY = probeY - window.getY();
                    moveWindow(requestedX, requestedY);
                    viewportReady = true;
                    window.setOpacity(1);
                    updateExpansion();
                });
                positionInit.playFromStart();
            } else {
                positionOffsetX = positionOffsetY = 0;
                window.setX(requestedX);
                window.setY(requestedY);
                viewportReady = true;
                window.setOpacity(1);
                updateExpansion();
            }
        });
        window.focusedProperty().addListener((obs, before, focused) -> {
            if (!focused) {
                keyboardReveal = false;
                keyboardNavigating = false;
                updateExpansion();
            }
        });
        window.setOnCloseRequest(e -> {
            e.consume();
            returnToMain();
        });
        window.setOnHidden(e -> {
            keyboardReveal = false;
            keyboardNavigating = false;
            pointerInside = false;
            pointerScreenX = pointerScreenY = Double.NaN;
            interacting = false;
            collapseDelay.stop();
            viewportInit.stop();
            positionInit.stop();
            controlsSettle.stop();
            if (controlsFade != null) controlsFade.stop();
            controlsFade = null;
            controlsOpacity.set(1);
            resizingGrip.set(false);
            window.setOpacity(1);
            viewportReady = false;
            if (transition != null) transition.stop();
            transition = null;
            // A rapid close can happen before Linux has reported frame sizes.
            // Keep enough space for those frames until the next measurement.
            double compactWidth = Utils.isLinux() && frameWidth == 0 ? 200 : COMPACT_SIZE + frameWidth;
            double compactHeight = Utils.isLinux() && frameHeight == 0 ? 200 : COMPACT_SIZE + frameHeight;
            double x = expanded && anchorRight ? window.getX() + window.getWidth() - compactWidth : window.getX();
            double y = expanded && anchorBottom ? window.getY() + window.getHeight() - compactHeight : window.getY();
            moveWindow(x, y);
            expanded = false;
            pin.setVisible(false);
            pin.setManaged(false);
            logoCover.setOpacity(1);
            window.setWidth(compactWidth);
            window.setHeight(compactHeight);
            mirror.stop();
            slide.setImage(null);
            projectionVisibility.end(!ownerHiding && main.isShowing(), projection.get());
            onClosed.run();
            if (!ownerHiding && main.isShowing() && !main.isIconified()) {
                main.toFront();
                main.requestFocus();
            }
        });
        main.addEventHandler(WindowEvent.WINDOW_HIDING, e -> {
            ownerHiding = true;
            restoreCheck.stop();
            activationCheck.stop();
            window.hide();
        });
        main.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> ownerHiding = false);
        main.focusedProperty().addListener((obs, wasFocused, focused) -> {
            if (focused) {
                restoredFocus = true;
                restoreCheck.stop();
                activationCheck.stop();
            }
        });
        activationCheck.setOnFinished(e -> {
            if (!restoredFocus && !window.isShowing() && main.isShowing() && !main.isIconified() && !main.isFocused()
                    && !hasOwnedDialog()) {
                // Some Linux window managers unminimise without raising the
                // window and ignore focus requests. Re-show it once, promptly
                // after the user's return action, if no dialog can be hidden.
                main.hide();
                main.show();
                main.toFront();
                main.requestFocus();
            }
        });
        restoreCheck.setOnFinished(e -> {
            if (!restoredFocus && !window.isShowing() && main.isShowing() && main.isIconified()) {
                if (hasOwnedDialog()) {
                    // Hiding the owner would also hide its dialogs. Keep the
                    // preview available and let the user activate the owner
                    // with the window manager instead.
                    restoreHint.setManaged(true);
                    restoreHint.setVisible(true);
                    if (Utils.isLinux()) window.setOpacity(0);
                    window.show();
                    viewportInit.playFromStart();
                    keyboardReveal = true;
                    updateExpansion();
                    projectionVisibility.begin(projection.get(), hideProjection.getAsBoolean());
                    mirror.start();
                } else {
                    // Some window managers reject deiconification. Re-showing
                    // the same stage restores it without rebuilding its scene.
                    main.hide();
                    main.setIconified(false);
                    main.show();
                    main.toFront();
                    main.requestFocus();
                }
            }
        });
        main.iconifiedProperty().addListener((obs, wasIconified, iconified) -> {
            if (!returning && wasIconified && !iconified && window.isShowing()) {
                window.hide();
            }
        });
    }

    private static double aspectRatio(ImageView frame) {
        Image image = frame.getImage();
        return image == null || image.getWidth() <= 0 || image.getHeight() <= 0
                ? 16.0 / 9.0 : image.getWidth() / image.getHeight();
    }

    private void updateExpansion() {
        if (!window.isShowing() || !viewportReady) return;
        if (wantsExpanded()) {
            collapseDelay.stop();
            setExpanded(true);
        } else if (!interacting) {
            collapseDelay.playFromStart();
        }
    }

    private boolean wantsExpanded() {
        Node focus = window.getScene().getFocusOwner();
        return pointerInside || keyboardReveal || pin.isSelected()
                || (keyboardNavigating && (focus == pin || focus == restore));
    }

    /** Return to mouse hover after real pointer movement, not a window resize. */
    private boolean notePointerPosition(MouseEvent event) {
        double x = event.getScreenX();
        double y = event.getScreenY();
        boolean moved = !Double.isNaN(pointerScreenX) && (x != pointerScreenX || y != pointerScreenY);
        pointerScreenX = x;
        pointerScreenY = y;
        if (moved && keyboardNavigating) {
            keyboardNavigating = false;
            return true;
        }
        return false;
    }

    private void setExpanded(boolean reveal) {
        if (expanded == reveal) return;
        boolean reversing = transition != null;
        boolean reversingCollapse = reveal && reversing && !expanded;
        if (transition != null) transition.stop();
        stopControlsRestore();
        if (reveal && !reversingCollapse) {
            var screens = Screen.getScreensForRectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight());
            Rectangle2D bounds = (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getVisualBounds();
            anchorRight = window.getX() + previewWidth + frameWidth > bounds.getMaxX();
            anchorBottom = window.getY() + previewHeight + frameHeight > bounds.getMaxY();
        }
        expanded = reveal;
        double startWidth = window.getWidth(), startHeight = window.getHeight();
        double targetWidth = (reveal ? previewWidth : COMPACT_SIZE) + frameWidth;
        double targetHeight = (reveal ? previewHeight : COMPACT_SIZE) + frameHeight;
        double anchorX = reversing ? transitionAnchorX : anchorRight ? window.getX() + startWidth : window.getX();
        double anchorY = reversing ? transitionAnchorY : anchorBottom ? window.getY() + startHeight : window.getY();
        transitionAnchorX = anchorX;
        transitionAnchorY = anchorY;
        double startOpacity = logoCover.getOpacity();
        var progress = new SimpleDoubleProperty(0);
        NativeResizeGate resizeGate = new NativeResizeGate();
        progress.addListener((obs, before, value) -> {
            double amount = value.doubleValue();
            if (resizeGate.shouldUpdate(System.nanoTime(), amount >= 1)) {
                double width = startWidth + (targetWidth - startWidth) * amount;
                double height = startHeight + (targetHeight - startHeight) * amount;
                window.setWidth(width);
                window.setHeight(height);
                if (anchorRight) window.setX(anchorX - width + positionOffsetX);
                if (anchorBottom) window.setY(anchorY - height + positionOffsetY);
            }
            logoCover.setOpacity(startOpacity + ((reveal ? 0 : 1) - startOpacity) * amount);
        });
        // Fade controls before resizing: native window and scene geometry can
        // settle on different pulses, making edge-aligned icons appear to jump.
        transition = new Timeline(
                new KeyFrame(Duration.millis(40), e -> setPinVisible(reveal),
                        new KeyValue(controlsOpacity, 0, Interpolator.EASE_BOTH),
                        new KeyValue(progress, 0)),
                new KeyFrame(Duration.millis(220), new KeyValue(progress, 1, Interpolator.EASE_BOTH)));
        transition.setOnFinished(e -> {
            transition = null;
            setPinVisible(expanded);
            scheduleControlsRestore();
        });
        transition.play();
    }

    private void finishTransition() {
        if (transition != null) {
            transition.jumpTo(transition.getTotalDuration());
            transition.stop();
            transition = null;
            setPinVisible(expanded);
            scheduleControlsRestore();
        }
    }

    private void setPinVisible(boolean visible) {
        pin.setVisible(visible);
        pin.setManaged(visible);
    }

    private void stopControlsRestore() {
        controlsSettle.stop();
        if (controlsFade != null) controlsFade.stop();
        controlsFade = null;
    }

    private void scheduleControlsRestore() {
        if (transition == null && !resizingGrip.get() && window.isShowing()) {
            controlsSettle.playFromStart();
        }
    }

    private void sceneResized() {
        if (transition == null && !resizingGrip.get() && controlsOpacity.get() < 1) {
            // A late native resize should not make the controls reappear at an
            // intermediate position while the window manager is still settling.
            stopControlsRestore();
            controlsOpacity.set(0);
            scheduleControlsRestore();
        }
    }

    private void moveWindow(double x, double y) {
        window.setX(x + positionOffsetX);
        window.setY(y + positionOffsetY);
    }

    void toggle() {
        restoreCheck.stop();
        activationCheck.stop();
        if (window.isShowing()) {
            returnToMain();
        } else {
            restoreHint.setVisible(false);
            restoreHint.setManaged(false);
            if (!positioned || Screen.getScreensForRectangle(window.getX(), window.getY(),
                    window.getWidth(), window.getHeight()).isEmpty()) {
                positionOnMainScreen();
                positioned = true;
            }
            if (Utils.isLinux()) window.setOpacity(0);
            window.show();
            viewportInit.playFromStart();
            try {
                projectionVisibility.begin(projection.get(), hideProjection.getAsBoolean());
                mirror.start();
                main.setIconified(true);
            } catch (RuntimeException ex) {
                window.hide();
                throw ex;
            }
        }
    }

    boolean setProjectionShowing(Stage target, boolean showing) {
        return projectionVisibility.request(target, showing);
    }

    void refreshProjectionVisibility() {
        projectionVisibility.setHiding(hideProjection.getAsBoolean(), projection.get());
    }

    private void installMoveAndReturn(StackPane preview) {
        final double[] origin = new double[4];
        final boolean[] moved = {false};
        preview.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                interacting = true;
                finishTransition();
                origin[0] = event.getScreenX();
                origin[1] = event.getScreenY();
                origin[2] = window.getX();
                origin[3] = window.getY();
                moved[0] = false;
            }
        });
        preview.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double dx = event.getScreenX() - origin[0];
                double dy = event.getScreenY() - origin[1];
                if (Math.abs(dx) > 3 || Math.abs(dy) > 3 || moved[0]) {
                    moved[0] = true;
                    preview.setCursor(Cursor.CLOSED_HAND);
                    moveWindow(origin[2] + dx, origin[3] + dy);
                }
            }
        });
        preview.setOnMouseReleased(event -> {
            preview.setCursor(Cursor.DEFAULT);
            interacting = false;
            updateExpansion();
        });
        preview.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !moved[0]
                    && event.isStillSincePress()) {
                returnToMain();
            }
        });
    }

    private void installResize(StackPane grip) {
        final double[] origin = new double[4];
        grip.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                interacting = true;
                resizingGrip.set(true);
                finishTransition();
                stopControlsRestore();
                controlsOpacity.set(0);
                origin[0] = event.getScreenX();
                origin[1] = event.getScreenY();
                origin[2] = window.getWidth();
                origin[3] = window.getHeight();
                event.consume();
            }
        });
        grip.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                previewWidth = Math.max(MIN_PREVIEW_WIDTH, origin[2] - frameWidth + event.getScreenX() - origin[0]);
                previewHeight = Math.max(MIN_PREVIEW_HEIGHT, origin[3] - frameHeight + event.getScreenY() - origin[1]);
                window.setWidth(previewWidth + frameWidth);
                window.setHeight(previewHeight + frameHeight);
                event.consume();
            }
        });
        grip.setOnMouseReleased(event -> {
            resizingGrip.set(false);
            interacting = false;
            updateExpansion();
            scheduleControlsRestore();
            event.consume();
        });
    }

    private void resizeBy(double widthDelta) {
        finishTransition();
        double ratio = previewWidth / previewHeight;
        previewWidth = Math.max(MIN_PREVIEW_WIDTH, previewWidth + widthDelta);
        previewHeight = Math.max(MIN_PREVIEW_HEIGHT, previewWidth / ratio);
        if (expanded) {
            stopControlsRestore();
            controlsOpacity.set(0);
            window.setWidth(previewWidth + frameWidth);
            window.setHeight(previewHeight + frameHeight);
            scheduleControlsRestore();
        }
    }

    private void returnToMain() {
        // Request activation while the user's click or shortcut still belongs
        // to the floating window. Some window managers reject an activation
        // requested only after the last focused window has disappeared.
        returning = true;
        restoredFocus = false;
        try {
            if (main.isShowing()) {
                if (main.isIconified()) {
                    main.setIconified(false);
                }
                main.toFront();
                main.requestFocus();
            }
            window.hide();
            if (main.isShowing() && !restoredFocus) {
                restoreCheck.playFromStart();
                if (Utils.isLinux()) activationCheck.playFromStart();
            }
        } finally {
            returning = false;
        }
    }

    private boolean hasOwnedDialog() {
        return Window.getWindows().stream().anyMatch(w -> w instanceof Stage stage
                && stage.getOwner() == main && stage.isShowing());
    }

    boolean isShowing() {
        return window.isShowing();
    }

    boolean containsWebView(Node view) {
        return webHost.getChildren().contains(view);
    }

    void setWebView(Node view) {
        if (view != null && containsWebView(view)) {
            return;
        }
        webHost.getChildren().clear();
        if (view != null) {
            webHost.getChildren().add(view);
        }
    }

    private void positionOnMainScreen() {
        var screens = Screen.getScreensForRectangle(main.getX(), main.getY(), main.getWidth(), main.getHeight());
        Rectangle2D bounds = (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getVisualBounds();
        window.setX(Math.max(bounds.getMinX(), bounds.getMaxX() - window.getWidth() - 20));
        window.setY(bounds.getMinY() + 20);
    }
}
