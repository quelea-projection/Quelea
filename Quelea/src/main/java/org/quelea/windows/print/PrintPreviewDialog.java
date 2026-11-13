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
package org.quelea.windows.print;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.print.PdfPageRenderer;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.style.QueleaIcon;
import org.quelea.windows.style.StyleManager;

/**
 * Shows the pages that are about to be printed, lets the user choose what to
 * include, and only hands the PDF on for printing once the user has approved
 * it.
 *
 * @param <T> the type of the options the user can choose between.
 */
public class PrintPreviewDialog<T> extends Stage {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    /**
     * Resolution the preview pages are rendered at.
     */
    private static final float PREVIEW_DPI = 110;
    private static final double PAGE_MARGIN = 24;

    /**
     * Writes the PDF for one of the options.
     */
    @FunctionalInterface
    public interface PdfWriter {

        /**
         * Write the PDF.
         *
         * @param pdf the file to write to.
         * @throws IOException if the PDF couldn't be written.
         */
        void write(File pdf) throws IOException;
    }

    /**
     * One of the options the user can choose between.
     *
     * @param value the option.
     * @param label the text shown for the option.
     * @param writer writes the PDF for the option. It is called on a
     * background thread, so it should only use data gathered beforehand.
     * @param <T> the type of the option.
     */
    public record Choice<T>(T value, String label, PdfWriter writer) {
    }

    /**
     * What the user approved for printing.
     *
     * @param value the chosen option.
     * @param pdf the PDF that was previewed, ready to be printed.
     * @param <T> the type of the option.
     */
    public record Approved<T>(T value, File pdf) {
    }

    private final List<Choice<T>> choices;
    private final Map<Integer, File> generated = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final VBox pagesBox;
    private final ScrollPane scrollPane;
    private final ProgressIndicator progress;
    private final Label messageLabel;
    private final Label pageCountLabel;
    private final Button printButton;
    private final ReadOnlyBooleanWrapper ready = new ReadOnlyBooleanWrapper(false);
    private int request;
    private int selected = -1;
    private File readyFile;
    private int readyPageCount;
    private Approved<T> approved;
    private final String filePrefix;

    /**
     * Create the dialog.
     *
     * @param title the title of the window.
     * @param fileName a name for the PDF files (shown in the printer queue.)
     * @param choices the options the user can choose between (at least one.)
     * If there's only one, no choice is shown.
     * @param initial the option to select first (the first one if it isn't
     * one of the choices.)
     */
    public PrintPreviewDialog(String title, String fileName, List<Choice<T>> choices, T initial) {
        if (choices.isEmpty()) {
            throw new IllegalArgumentException("At least one choice is needed");
        }
        filePrefix = toFilePrefix(fileName);
        this.choices = List.copyOf(choices);
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "print-preview");
            thread.setDaemon(true);
            return thread;
        });
        setTitle(title);
        Utils.addIconsToStage(this);
        Window owner = getMainWindow();
        if (owner != null) {
            initOwner(owner);
            initModality(Modality.WINDOW_MODAL);
        } else {
            initModality(Modality.APPLICATION_MODAL);
        }

        VBox top = new VBox(8);
        top.setPadding(new Insets(16, 18, 12, 18));
        Label instructions = new Label(LabelGrabber.INSTANCE.getLabel("print.preview.instructions"));
        instructions.setWrapText(true);
        top.getChildren().add(instructions);
        int initialIndex = 0;
        for (int i = 0; i < this.choices.size(); i++) {
            if (this.choices.get(i).value().equals(initial)) {
                initialIndex = i;
            }
        }
        pageCountLabel = new Label();
        pageCountLabel.getStyleClass().add("muted-label");
        if (this.choices.size() > 1) {
            ToggleGroup group = new ToggleGroup();
            VBox options = new VBox(6);
            options.getChildren().add(new Label(LabelGrabber.INSTANCE.getLabel("print.preview.include")));
            for (int i = 0; i < this.choices.size(); i++) {
                final int index = i;
                RadioButton radio = new RadioButton(this.choices.get(i).label());
                radio.setToggleGroup(group);
                radio.setSelected(i == initialIndex);
                radio.selectedProperty().addListener((obs, was, isSelected) -> {
                    if (isSelected) {
                        select(index);
                    }
                });
                options.getChildren().add(radio);
            }
            top.getChildren().add(options);
        }
        top.getChildren().add(pageCountLabel);

        pagesBox = new VBox(PAGE_MARGIN);
        pagesBox.setAlignment(Pos.TOP_CENTER);
        pagesBox.setPadding(new Insets(PAGE_MARGIN));
        pagesBox.getStyleClass().add("print-preview-pages");
        scrollPane = new ScrollPane(pagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // the pages are scaled to fit
        scrollPane.getStyleClass().add("print-preview-area");
        scrollPane.viewportBoundsProperty().addListener((obs, old, bounds) -> fitPages());
        progress = new ProgressIndicator();
        progress.setMaxSize(48, 48);
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(360);
        messageLabel.getStyleClass().add("muted-label");
        VBox busyBox = new VBox(12, progress, messageLabel);
        busyBox.setAlignment(Pos.CENTER);
        busyBox.setMouseTransparent(true);
        StackPane center = new StackPane(scrollPane, busyBox);
        center.setPadding(new Insets(0, 18, 16, 18));

        printButton = new Button(LabelGrabber.INSTANCE.getLabel("print.button"), QueleaIcon.PRINT.create());
        printButton.setDefaultButton(true);
        printButton.disableProperty().bind(ready.not());
        printButton.setOnAction(e -> {
            if (readyFile != null) {
                approved = new Approved<>(this.choices.get(selected).value(), readyFile);
            }
            hide();
        });
        Button cancelButton = new Button(LabelGrabber.INSTANCE.getLabel("cancel.button"), QueleaIcon.CLOSE.create());
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> hide());
        HBox buttonPanel = new HBox(8, printButton, cancelButton);
        StyleManager.markPrimaryButton(printButton);

        BorderPane root = new BorderPane(center);
        root.setTop(top);
        root.setBottom(buttonPanel);
        root.getStyleClass().add("quelea-dialog");
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, Math.min(760, screen.getWidth() * 0.9), Math.min(900, screen.getHeight() * 0.88));
        StyleManager.apply(scene);
        setScene(scene);
        setMinWidth(420);
        setMinHeight(360);

        setOnHidden(e -> cleanUp());
        select(initialIndex);
    }

    /**
     * Make a prefix for a temporary file out of any text (temporary file
     * names need at least three characters and no path separators.)
     */
    static String toFilePrefix(String name) {
        String prefix = name == null ? "" : name.replaceAll("[^\\p{L}\\p{N} _-]", "").trim();
        if (prefix.length() > 40) {
            prefix = prefix.substring(0, 40).trim();
        }
        return prefix.length() < 3 ? "print" + prefix : prefix + " ";
    }

    private static Window getMainWindow() {
        QueleaApp app = QueleaApp.get();
        return app == null ? null : app.getMainWindow();
    }

    /**
     * Show the dialog and wait for the user to approve or cancel printing.
     *
     * @return what was approved, or empty if the user cancelled.
     */
    public Optional<Approved<T>> showAndGetApproved() {
        showAndWait();
        return Optional.ofNullable(approved);
    }

    /**
     * @return true once the preview of the selected option is shown and it
     * can be printed.
     */
    public ReadOnlyBooleanProperty readyProperty() {
        return ready.getReadOnlyProperty();
    }

    /**
     * @return the PDF of the selected option, once it's ready (null before.)
     */
    public File getReadyFile() {
        return readyFile;
    }

    /**
     * @return the number of pages of the selected option, once it's ready.
     */
    public int getReadyPageCount() {
        return readyPageCount;
    }

    /**
     * Select one of the options and preview it.
     *
     * @param index the index of the option.
     */
    final void select(int index) {
        selected = index;
        final int thisRequest = ++request;
        ready.set(false);
        readyFile = null;
        progress.setVisible(true);
        messageLabel.setText(LabelGrabber.INSTANCE.getLabel("print.preview.generating"));
        messageLabel.setVisible(true);
        pageCountLabel.setText("");
        pagesBox.setOpacity(0.35);
        Choice<T> choice = choices.get(index);
        executor.submit(() -> {
            try {
                File pdf = generated.get(index);
                if (pdf == null) {
                    pdf = File.createTempFile(filePrefix, ".pdf");
                    pdf.deleteOnExit();
                    choice.writer().write(pdf);
                    generated.put(index, pdf);
                }
                PdfPageRenderer.Pages pages = PdfPageRenderer.render(pdf, PREVIEW_DPI);
                List<Image> images = new ArrayList<>();
                for (BufferedImage page : pages.pages()) {
                    images.add(SwingFXUtils.toFXImage(page, null));
                    page.flush();
                }
                final File readyPdf = pdf;
                Platform.runLater(() -> {
                    if (thisRequest == request) {
                        showPages(readyPdf, images, pages.pageCount());
                    }
                });
            } catch (Exception | LinkageError ex) {
                LOGGER.log(Level.WARNING, "Couldn't prepare the print preview", ex);
                Platform.runLater(() -> {
                    if (thisRequest == request) {
                        showError();
                    }
                });
            }
        });
    }

    private void showPages(File pdf, List<Image> images, int pageCount) {
        List<Node> pageNodes = new ArrayList<>();
        for (Image image : images) {
            ImageView view = new ImageView(image);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            StackPane page = new StackPane(view);
            page.getStyleClass().add("print-preview-page");
            page.setMaxWidth(Region.USE_PREF_SIZE);
            pageNodes.add(page);
        }
        pagesBox.getChildren().setAll(pageNodes);
        pagesBox.setOpacity(1);
        fitPages();
        scrollPane.setVvalue(0);
        progress.setVisible(false);
        messageLabel.setVisible(false);
        if (images.size() < pageCount) {
            pageCountLabel.setText(LabelGrabber.INSTANCE.getLabel("print.preview.pages.truncated")
                    .replace("$1", Integer.toString(images.size()))
                    .replace("$2", Integer.toString(pageCount)));
        } else {
            pageCountLabel.setText(LabelGrabber.INSTANCE.getLabel("print.preview.pages")
                    .replace("$1", Integer.toString(pageCount)));
        }
        readyFile = pdf;
        readyPageCount = pageCount;
        ready.set(true);
    }

    private void showError() {
        pagesBox.getChildren().clear();
        pagesBox.setOpacity(1);
        progress.setVisible(false);
        messageLabel.setText(LabelGrabber.INSTANCE.getLabel("print.preview.error"));
        messageLabel.setVisible(true);
        readyFile = null;
        ready.set(false);
    }

    /**
     * Scale the pages to the width of the preview area (never larger than
     * they were rendered.)
     */
    private void fitPages() {
        double available = scrollPane.getViewportBounds().getWidth() - 2 * PAGE_MARGIN;
        if (available <= 0) {
            return;
        }
        for (Node node : pagesBox.getChildren()) {
            if (node instanceof StackPane page && !page.getChildren().isEmpty()
                    && page.getChildren().get(0) instanceof ImageView view && view.getImage() != null) {
                view.setFitWidth(Math.min(view.getImage().getWidth(), available));
            }
        }
    }

    /**
     * Stop generating previews and remove the PDFs that weren't approved.
     */
    private void cleanUp() {
        request++;
        executor.shutdownNow();
        File keep = approved == null ? null : approved.pdf();
        for (File file : generated.values()) {
            if (!file.equals(keep)) {
                file.delete();
            }
        }
    }
}
