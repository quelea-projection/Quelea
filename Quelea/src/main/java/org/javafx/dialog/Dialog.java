package org.javafx.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.presentation.powerpoint.PowerPointService;

import org.quelea.windows.style.StyleManager;
import org.quelea.windows.style.QueleaIcon;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Anton Smirnov (dev@antonsmirnov.name), Michael Berry
 */
public class Dialog extends Stage {

    protected String stacktrace;
    protected double originalWidth, originalHeight;
    protected Scene scene;
    protected BorderPane borderPanel;
    protected StackPane icon;
    protected VBox messageBox;
    protected Label messageLabel;
    protected boolean stacktraceVisible;
    protected HBox stacktraceButtonsPanel;
    protected ToggleButton viewStacktraceButton;
    protected Button copyStacktraceButton;
    protected ScrollPane scrollPane;
    protected Label stackTraceLabel;
    protected HBox buttonsPanel;
    protected Button okButton;

    /**
     * Extracts stack trace from Throwable
     */
    protected static class StacktraceExtractor {

        public String extract(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return sw.toString();
        }
    }

    /**
     * Dialog builder
     */
    public static class Builder {

        protected static final int STACKTRACE_LABEL_MAXHEIGHT = 240;
        protected static final int MESSAGE_MIN_WIDTH = 180;
        protected static final int MESSAGE_MAX_WIDTH = 800;
        protected static final int BUTTON_WIDTH = 60;
        protected static final double MARGIN = 10;
        protected static final double ICON_SIZE = 36;
        protected Dialog stage;

        public Builder create() {
            stage = new Dialog();
            stage.initOwner(QueleaApp.get().getMainWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setIconified(false);
            stage.centerOnScreen();
            stage.borderPanel = new BorderPane();

            stage.borderPanel.getStyleClass().add("quelea-dialog");

            // icon (filled in by one of the set*Icon() methods)
            stage.icon = new StackPane();
            stage.icon.getStyleClass().add("dialog-icon");
            stage.borderPanel.setLeft(stage.icon);
            BorderPane.setAlignment(stage.icon, Pos.TOP_CENTER);
            BorderPane.setMargin(stage.icon, new Insets(2 * MARGIN, 0, MARGIN, 2 * MARGIN));

            // message
            stage.messageBox = new VBox();
            stage.messageBox.setAlignment(Pos.CENTER_LEFT);

            stage.messageLabel = new Label();
            stage.messageLabel.setWrapText(true);
            stage.messageLabel.setMinWidth(MESSAGE_MIN_WIDTH);
            stage.messageLabel.setMaxWidth(MESSAGE_MAX_WIDTH);

            stage.messageBox.getChildren().add(stage.messageLabel);
            stage.borderPanel.setCenter(stage.messageBox);
            BorderPane.setAlignment(stage.messageBox, Pos.CENTER);
            BorderPane.setMargin(stage.messageBox, new Insets(2 * MARGIN, 2 * MARGIN, 2 * MARGIN, 1.5 * MARGIN));

            // buttons
            stage.buttonsPanel = new HBox();
            stage.buttonsPanel.setSpacing(MARGIN);
            stage.buttonsPanel.setAlignment(Pos.CENTER_RIGHT);
            stage.buttonsPanel.getStyleClass().add("dialog-button-bar");
            stage.borderPanel.setBottom(stage.buttonsPanel);
            stage.borderPanel.widthProperty().addListener((ov, t, t1) -> stage.buttonsPanel.layout());

            stage.scene = new Scene(stage.borderPanel);
            StyleManager.apply(stage.scene);
            stage.setScene(stage.scene);
            return this;
        }

        public Builder setOwner(Window owner) {
            if (owner != null) {
                stage.initOwner(owner);
                stage.borderPanel.setMaxWidth(owner.getWidth());
                stage.borderPanel.setMaxHeight(owner.getHeight());
            }
            return this;
        }

        public Builder setTitle(String title) {
            stage.setTitle(title);
            return this;
        }

        public Builder setMessage(String message) {
            stage.messageLabel.setText(message);
            return this;
        }

        private void alignScrollPane() {
            stage.setWidth(
                    ICON_SIZE
                    + Math.max(
                            stage.messageLabel.getWidth(),
                            (stage.stacktraceVisible
                                    ? Math.max(
                                            stage.stacktraceButtonsPanel.getWidth(),
                                            stage.stackTraceLabel.getWidth())
                                    : stage.stacktraceButtonsPanel.getWidth()))
                    + 5 * MARGIN);

            stage.setHeight(
                    Math.max(
                            ICON_SIZE,
                            stage.messageLabel.getHeight()
                            + stage.stacktraceButtonsPanel.getHeight()
                            + (stage.stacktraceVisible
                                    ? Math.min(
                                            stage.stackTraceLabel.getHeight(),
                                            STACKTRACE_LABEL_MAXHEIGHT)
                                    : 0))
                    + stage.buttonsPanel.getHeight()
                    + 3 * MARGIN);
            if (stage.stacktraceVisible) {
                stage.scrollPane.setPrefHeight(
                        stage.getHeight()
                        - stage.messageLabel.getHeight()
                        - stage.stacktraceButtonsPanel.getHeight()
                        - 2 * MARGIN);
            }

            stage.centerOnScreen();
        }

        // NOTE: invoke once during Dialog creating
        private Builder setStackTrace(Throwable t) {
            // view button
            stage.viewStacktraceButton = new ToggleButton("View stacktrace");

            // copy button
            stage.copyStacktraceButton = new Button("Copy to clipboard");
            HBox.setMargin(stage.copyStacktraceButton, new Insets(0, 0, 0, MARGIN));

            stage.stacktraceButtonsPanel = new HBox();
            stage.stacktraceButtonsPanel.getChildren().addAll(
                    stage.viewStacktraceButton, stage.copyStacktraceButton);
            VBox.setMargin(stage.stacktraceButtonsPanel, new Insets(MARGIN, MARGIN, MARGIN, 0));
            stage.messageBox.getChildren().add(stage.stacktraceButtonsPanel);

            // stacktrace text
            stage.stackTraceLabel = new Label();
            stage.stackTraceLabel.widthProperty().addListener((ov, oldValue, newValue) -> alignScrollPane());

            stage.stackTraceLabel.heightProperty().addListener((ov, oldValue, newValue) -> alignScrollPane());

            StacktraceExtractor extractor = new StacktraceExtractor();
            stage.stacktrace = extractor.extract(t);

            stage.scrollPane = new ScrollPane();
            stage.scrollPane.setContent(stage.stackTraceLabel);

            stage.viewStacktraceButton.setOnAction(t1 -> {
                stage.stacktraceVisible = !stage.stacktraceVisible;
                if (stage.stacktraceVisible) {
                    stage.messageBox.getChildren().add(stage.scrollPane);
                    stage.stackTraceLabel.setText(stage.stacktrace);

                    alignScrollPane();
                } else {
                    stage.messageBox.getChildren().remove(stage.scrollPane);

                    //alignScrollPane();
                    stage.setWidth(stage.originalWidth);
                    stage.setHeight(stage.originalHeight);
                    stage.stackTraceLabel.setText(null);
                    stage.centerOnScreen();
                }
                stage.messageBox.layout();
            });

            stage.copyStacktraceButton.setOnAction(t12 -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                Map<DataFormat, Object> map = new HashMap<>();
                map.put(DataFormat.PLAIN_TEXT, stage.stacktrace);
                clipboard.setContent(map);
            });

            stage.showingProperty().addListener((ov, oldValue, newValue) -> {
                if (newValue) {
                    stage.originalWidth = stage.getWidth();
                    stage.originalHeight = stage.getHeight();
                }
            });

            return this;
        }

        /**
         * Show a vector icon at the left of the dialog. The colour comes from
         * the .dialog-icon-* style class (see base.css).
         */
        protected void setIcon(QueleaIcon icon, String kind) {
            Region iconNode = icon.create(ICON_SIZE);
            iconNode.getStyleClass().add("dialog-icon-" + kind);
            stage.icon.getChildren().setAll(iconNode);
        }

        public Builder setWarningIcon() {
            setIcon(QueleaIcon.WARNING, "warning");
            return this;
        }

        public Builder setErrorIcon() {
            setIcon(QueleaIcon.ERROR, "error");
            return this;
        }

        protected Builder setThrowableIcon() {
            setIcon(QueleaIcon.BUG, "error");
            return this;
        }

        protected Builder setInfoIcon() {
            setIcon(QueleaIcon.INFO, "info");
            return this;
        }

        protected Builder setConfirmationIcon() {
            setIcon(QueleaIcon.HELP, "info");
            return this;
        }

        protected Builder addOkButton() {
            stage.okButton = new Button("OK");
            stage.okButton.setPrefWidth(BUTTON_WIDTH);
            stage.okButton.setOnAction(t -> stage.close());
            stage.buttonsPanel.getChildren().add(stage.okButton);
            return this;
        }

        protected Builder addConfirmationButton(String buttonCaption, final EventHandler<ActionEvent> actionHandler) {
            Button confirmationButton = new Button(buttonCaption);
            confirmationButton.setMinWidth(BUTTON_WIDTH);
            confirmationButton.setOnAction(t -> {
                stage.close();
                if (actionHandler != null) {
                    actionHandler.handle(t);
                }
            });

            stage.buttonsPanel.getChildren().add(confirmationButton);
            return this;
        }
        
        
        protected Builder addApplyButton(String buttonCaption, final EventHandler<ActionEvent> actionHandler) {
            Button applyButton = new Button(buttonCaption);
            applyButton.setMinWidth(BUTTON_WIDTH);
            applyButton.setOnAction(t -> {
                if (actionHandler != null) {
                    actionHandler.handle(t);
                }
            });

            stage.buttonsPanel.getChildren().add(applyButton);
            return this;
        }

        /**
         * Add Yes button to confirmation dialog
         * <p/>
         * @param actionHandler action handler
         * @return
         */
        public Builder addYesButton(EventHandler<ActionEvent> actionHandler) {
            return addConfirmationButton(LabelGrabber.INSTANCE.getLabel("yes.text"), actionHandler);
        }

        /**
         * Add No button to confirmation dialog
         * <p/>
         * @param actionHandler action handler
         * @return
         */
        public Builder addNoButton(EventHandler<ActionEvent> actionHandler) {
            return addConfirmationButton(LabelGrabber.INSTANCE.getLabel("no.text"), actionHandler);
        }

        /**
         * Add Cancel button to confirmation dialog
         * <p/>
         * @param actionHandler action handler
         * @return
         */
        public Builder addCancelButton(EventHandler<ActionEvent> actionHandler) {
            return addConfirmationButton(LabelGrabber.INSTANCE.getLabel("cancel.text"), actionHandler);
        }

        public Builder addLabelledButton(String label, EventHandler<ActionEvent> actionHandler) {
            return addConfirmationButton(label, actionHandler);
        }

        /**
         * Build dialog
         * <p/>
         * @return dialog instance
         */
        public Dialog build() {
            if (stage.buttonsPanel.getChildren().size() == 0) {
                throw new RuntimeException("Add one dialog button at least");
            }

            stage.buttonsPanel.getChildren().get(0).requestFocus();
            // Style the first button as the primary action (see .primary-button in base.css)
            stage.buttonsPanel.getChildren().get(0).getStyleClass().add("primary-button");
            return stage;
        }
    }

    /**
     * Show information dialog box as parentWindow child
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     */
    public static void showInfo(String title, String message, Window owner) {
        new Builder()
                .create()
                .setOwner(owner)
                .setTitle(title)
                .setInfoIcon()
                .setMessage(message)
                .addOkButton()
                .build()
                .show();
    }

    /**
     * Show information dialog box as parentStage child
     * <p/>
     * @param title dialog title
     * @param message dialog message
     */
    public static void showInfo(String title, String message) {
        showInfo(title, message, null);
    }

    /**
     * Show warning dialog box as parentStage child
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     */
    public static void showWarning(String title, String message, Window owner) {
        new Builder()
                .create()
                .setOwner(owner)
                .setTitle(title)
                .setWarningIcon()
                .setMessage(message)
                .addOkButton()
                .build()
                .show();
    }

    /**
     * Show warning dialog box
     * <p/>
     * @param title dialog title
     * @param message dialog message
     */
    public static void showWarning(String title, String message) {
        showWarning(title, message, null);
    }

    /**
     * Show error dialog box
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     */
    public static void showError(String title, String message, Window owner) {
        new Builder()
                .create()
                .setOwner(owner)
                .setTitle(title)
                .setErrorIcon()
                .setMessage(message)
                .addOkButton()
                .build()
                .show();
    }

    /**
     * Show error dialog box
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     */
    public static void showAndWaitError(String title, String message, Window owner) {
        new Builder()
                .create()
                .setOwner(owner)
                .setTitle(title)
                .setErrorIcon()
                .setMessage(message)
                .addOkButton()
                .build()
                .showAndWait();
    }

    /**
     * Show error dialog box
     * <p/>
     * @param title dialog title
     * @param message dialog message
     */
    public static void showError(String title, String message) {
        showError(title, message, null);
    }

    /**
     * Show error dialog box
     * <p/>
     * @param title dialog title
     * @param message dialog message
     */
    public static void showAndWaitError(String title, String message) {
        showAndWaitError(title, message, null);
    }

    /**
     * Show error dialog box with stacktrace
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @param t throwable
     * @param owner parent window
     */
    public static void showThrowable(String title, String message, Throwable t, Window owner) {
        new Builder()
                .create()
                .setOwner(owner)
                .setTitle(title)
                .setThrowableIcon()
                .setMessage(message)
                .setStackTrace(t)
                .addOkButton()
                .build()
                .show();
    }

    /**
     * Show error dialog box with stacktrace
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @param t throwable
     */
    public static void showThrowable(String title, String message, Throwable t) {
        showThrowable(title, message, t, null);
    }

    /**
     * Build confirmation dialog builder
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     * @return
     */
    public static Builder buildConfirmation(String title, String message, Window owner) {
        return new Builder()
                .create()
                .setOwner(owner)
                .setTitle(title)
                .setConfirmationIcon()
                .setMessage(message);
    }

    /**
     * Build confirmation dialog builder
     * <p/>
     * @param title dialog title
     * @param message dialog message
     * @return
     */
    public static Builder buildConfirmation(String title, String message) {
        return buildConfirmation(title, message, null);
    }

    /**
     * Build focus switcher
     * <p/>
     * @return
     */
    public static Builder buildFocusSwitcher() {
        return new Builder()
                .create()
                .setTitle(LabelGrabber.INSTANCE.getLabel("focus.switcher.title"))
                .setMessage(LabelGrabber.INSTANCE.getLabel("focus.switcher.message"))
                .addApplyButton(LabelGrabber.INSTANCE.getLabel("focus.quelea"), (ActionEvent event) -> {
                    QueleaApp.get().getMainWindow().toFront();
                })
                .addApplyButton(LabelGrabber.INSTANCE.getLabel("focus.pp"), (ActionEvent event) -> {
                    PowerPointService.get().focus();
                });
    }
}
