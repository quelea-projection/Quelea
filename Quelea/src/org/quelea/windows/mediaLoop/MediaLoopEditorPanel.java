/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.mediaLoop;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import org.javafx.dialog.Dialog;
import org.javafx.dialog.Dialog.Builder;
import org.quelea.data.displayable.MediaLoopDisplayable;
import org.quelea.data.mediaLoop.MediaFile;
import org.quelea.data.powerpoint.Presentation;
import org.quelea.data.powerpoint.PresentationFactory;
import org.quelea.data.powerpoint.PresentationSlide;
import org.quelea.data.powerpoint.SlideChangedListener;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.FileFilters;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.mediaLoop.mediaLoopCreator.MediaLoopCreatorWindow;
import org.quelea.windows.mediaLoop.mediaLoopCreator.MediaLoopPreview;

/**
 * The panel that manages the creation of the media loop
 * <p/>
 * @author Michael
 */
public class MediaLoopEditorPanel extends BorderPane implements SlideChangedListener {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private final TextField titleField;
    private final TextField advanceTimeField;
    private final MediaLoopPreview slidePane;
    private final Button addVideoImageButton;
    private final Button addPowerpointAsImagesButton;
    private final Label defaultAdvanceTimeLabel;
    private final TextField defaultAdvanceTimeField;
    private ArrayList<MediaFile> slides = new ArrayList();
    private String saveHash = "";
    private int currentSlide = 0;
    private String newName = "";
    private File fExists;

    /**
     * Create and initialise the mediaLoop panel.
     */
    public MediaLoopEditorPanel() {
        final VBox centrePanel = new VBox();
        GridPane topPanel = new GridPane();

        titleField = new TextField();
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        Label titleLabel = new Label(LabelGrabber.INSTANCE.getLabel("title.label"));
        GridPane.setConstraints(titleLabel, 1, 1);
        topPanel.getChildren().add(titleLabel);
        titleLabel.setLabelFor(titleField);
        GridPane.setConstraints(titleField, 2, 1);
        topPanel.getChildren().add(titleField);

        defaultAdvanceTimeField = new TextField();
        GridPane.setHgrow(defaultAdvanceTimeField, Priority.ALWAYS);
        defaultAdvanceTimeLabel = new Label(LabelGrabber.INSTANCE.getLabel("advanceTime.default.label"));
        GridPane.setConstraints(defaultAdvanceTimeLabel, 1, 2);
        topPanel.getChildren().add(defaultAdvanceTimeLabel);
        titleLabel.setLabelFor(defaultAdvanceTimeField);
        GridPane.setConstraints(defaultAdvanceTimeField, 2, 2);
        topPanel.getChildren().add(defaultAdvanceTimeField);

        centrePanel.getChildren().add(topPanel);
        slidePane = new MediaLoopPreview(true);

        final VBox mainPanel = new VBox();
        ToolBar addToolbar = new ToolBar();
        addVideoImageButton = getVideoImageButton();
        addPowerpointAsImagesButton = getAddPowerpointButton();
        Button deleteButton = getDeleteButton();
        Label advanceLabel = new Label(LabelGrabber.INSTANCE.getLabel("advanceTime.label"));
        advanceTimeField = new TextField(10 + "");

        addToolbar.getItems().add(addVideoImageButton);
        addToolbar.getItems().add(new Separator());
        addToolbar.getItems().add(addPowerpointAsImagesButton);
        addToolbar.getItems().add(new Separator());
        addToolbar.getItems().add(advanceLabel);
        addToolbar.getItems().add(advanceTimeField);
        addToolbar.getItems().add(new Separator());
        addToolbar.getItems().add(deleteButton);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(mainPanel, Priority.ALWAYS);
        mainPanel.getChildren().add(addToolbar);
        VBox.setVgrow(slidePane, Priority.ALWAYS);
        mainPanel.getChildren().add(slidePane);
        centrePanel.getChildren().add(mainPanel);
        setCenter(centrePanel);

        slidePane.addSlideChangedListener(this);
    }

    /**
     * Reset the saved hash
     */
    public void resetSaveHash() {
        saveHash = getSaveHash();
    }

    /**
     * Determine if the hash has changed
     *
     * @return true if hash has changed, false otherwise
     */
    public boolean hashChanged() {
        return !getSaveHash().equals(saveHash);
    }

    /**
     * Get the hash to be saved
     *
     * @return The hash as string
     */
    private String getSaveHash() {
        return "" + slidePane.getHash() + titleField.getText().hashCode();
    }

    /**
     * Get the add video and image button for the media loop
     *
     * @return the add video image button
     */
    private Button getVideoImageButton() {
        Button ret = new Button(LabelGrabber.INSTANCE.getLabel("mediaLoop.add.videoImage"), new ImageView(new Image("file:icons/video file.png", 30, 30, false, true)));
        ret.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("mediaLoop.add.videoImage")));
        ret.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(FileFilters.VIDEO_IMAGE);
                List<File> files = chooser.showOpenMultipleDialog(QueleaApp.get().getMainWindow());

                if (files == null) {
                    return;
                }
                int oldSize = slides.size();
                for (File f : files) {
                    handleMediaFile(f);
                }

                slidePane.setSlides(slides);
                slidePane.select(oldSize, true);
            }
        });
        return ret;
    }

    /**
     * Get the delete slide button for the media loop
     *
     * @return the delete slide button
     */
    private Button getDeleteButton() {
        Button ret = new Button(LabelGrabber.INSTANCE.getLabel("delete.mediaLoop.slide.button"), new ImageView(new Image("file:icons/removedb.png", 30, 30, false, true)));
        ret.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("delete.mediaLoop.slide.tooltip")));
        ret.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                
                int selectedIndex = slidePane.getSelectedIndex();
                slides.remove(selectedIndex);                

                slidePane.setSlides(slides);
                if (selectedIndex < slides.size()) {
                    slidePane.select(selectedIndex);
                } 

            }
        });
        return ret;
    }

    /**
     * Add file
     *
     * @param filesToAdd the files to add to the display
     */
    public void addFiles(List<File> filesToAdd) {
        for (File fileToAdd : filesToAdd) {
            if (Utils.fileIsImage(fileToAdd)) {
                handleMediaFile(fileToAdd);
            } else if (Utils.fileIsImage(fileToAdd)) {
                handleMediaFile(fileToAdd);
            } else if (Utils.fileIsPowerpoint(fileToAdd)) {
                PresentationFactory factory = new PresentationFactory();
                handlePowerpoint(fileToAdd, factory);
            }
        }
        slidePane.setSlides(slides);

    }

    /**
     * Handle the media file to be imported
     *
     * @param file The file to be added
     */
    private void handleMediaFile(File file) {
        final Path sourceFile = file.getAbsoluteFile().toPath();
        File finalPath = null;
        int time = 10;
        final File directory = new File(QueleaProperties.get().getMediaLoopDir() + "/");
        directory.mkdirs();
        try {
            fExists = new File(directory.getPath(), file.getName());

            newName = file.getName();
            while (fExists.exists()) {
                Dialog.buildConfirmation(LabelGrabber.INSTANCE.getLabel("mediaLoop.fileExists.title"),
                        LabelGrabber.INSTANCE.getLabel("mediaLoop.fileExists.text"), QueleaApp.get().getMainWindow().getMediaLoopCreatorWindow())
                        .addLabelledButton(LabelGrabber.INSTANCE.getLabel("mediaLoop.fileExists.specifyNewName"), new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                final Builder dialog = Dialog.buildInputDialog(LabelGrabber.INSTANCE.getLabel("mediaLoop.rename.title"),
                                        Utils.getFileNameWithoutExtension(newName),
                                        QueleaApp.get().getMainWindow().getMediaLoopCreatorWindow());
                                dialog.addLabelledButton(LabelGrabber.INSTANCE.getLabel("ok.button"), new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent t) {
                                        newName = dialog.getInput() + newName.substring(Utils.getFileNameWithoutExtension(newName).length());
                                        fExists = new File(directory.getPath(), newName);
                                    }
                                });
                                dialog.build().showAndWait();

                            }
                        }).addLabelledButton(LabelGrabber.INSTANCE.getLabel("mediaLoop.fileExists.overwrite"), new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                fExists.delete();
                            }
                        })
                        .build()
                        .showAndWait();

            }
            Files.copy(sourceFile, Paths.get(directory.getPath(), newName), StandardCopyOption.COPY_ATTRIBUTES);
            finalPath = new File(directory.getPath(), newName);
        } catch (Exception ex) {
            Logger.getLogger(MediaLoopEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            time = defaultAdvanceTimeField.getText().trim().isEmpty() ? 10 : Integer.parseInt(defaultAdvanceTimeField.getText());
        } catch (NumberFormatException ex) {
            LoggerUtils.getLogger().log(Level.WARNING, ex.getMessage());
        }

        slides.add(new MediaFile(finalPath.getAbsolutePath(), time));
    }

    private void handlePowerpoint(File file, PresentationFactory presentationFactory) {
        Presentation presentation = presentationFactory.getPresentation(file);
        int loopCount = 0;
        for (PresentationSlide slide : presentation.getSlides()) {
            loopCount++;
            Image slideImage = slide.getImage();
            BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(slideImage, null);
            BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

            Graphics2D graphics = bufImageRGB.createGraphics();
            graphics.drawImage(bufImageARGB, 0, 0, null);
            int time = 10;
            try {
                time = defaultAdvanceTimeField.getText().trim().isEmpty() ? 10 : Integer.parseInt(defaultAdvanceTimeField.getText());
            } catch (NumberFormatException ex) {
                LoggerUtils.getLogger().log(Level.WARNING, ex.getMessage());
            }
            MediaFile mediaFile = new MediaFile(QueleaProperties.get().getMediaLoopDir() + "/" + Utils.getFileNameWithoutExtension(file.getName()) + loopCount + ".jpg", time);
            mediaFile.mkdirs();
            try {
                ImageIO.write(bufImageRGB, "jpg", mediaFile);
            } catch (IOException ex) {
                Logger.getLogger(MediaLoopEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            graphics.dispose();
            slides.add(mediaFile);
        }

    }

    /**
     * Get the add powerpoint as images button
     *
     * @return the add powerpoint as images button
     */
    private Button getAddPowerpointButton() {
        Button ret = new Button(LabelGrabber.INSTANCE.getLabel("mediaLoop.add.powerpoint"), new ImageView(new Image("file:icons/powerpoint.png", 30, 30, false, true)));

        ret.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("mediaLoop.add.powerpoint")));
        ret.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent t) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(FileFilters.POWERPOINT);
                int oldSize = slides.size();
                List<File> files = chooser.showOpenMultipleDialog(QueleaApp.get().getMainWindow());

                if (files == null) {
                    return;
                }
                PresentationFactory presentationFactory = new PresentationFactory();
                for (File file : files) {
                    handlePowerpoint(file, presentationFactory);
                }
                slidePane.setSlides(slides);
                slidePane.select(oldSize, true);

            }
        });

        return ret;
    }

    /**
     * Get the slide pane.
     * <p/>
     * @return the slide pane.
     */
    public MediaLoopPreview getSlidePane() {
        return slidePane;
    }

    /**
     * Get the title field.
     * <p/>
     * @return the title field.
     */
    public TextField getTitleField() {
        return titleField;
    }

    /**
     * Get the advance time field.
     * <p/>
     * @return the advance time field.
     */
    public TextField getAdvanceTimeField() {
        return advanceTimeField;
    }

    /**
     * Get the default advance time field.
     * <p/>
     * @return the advance time field.
     */
    public TextField getDefaultAdvanceTimeField() {
        return defaultAdvanceTimeField;
    }

    /**
     * Reset for a new media loop
     */
    public void resetNewMediaLoop() {
        slides = new ArrayList();
        this.titleField.setText("New Media Loop");
        this.defaultAdvanceTimeField.setText("10");
        slidePane.clear();
    }

    /**
     * Reset to edit a media loop
     *
     * @param display The displayable to edit
     */
    public void resetEditMediaLoop(MediaLoopDisplayable display) {
        slides.clear();
        for (MediaFile f : display.getMediaFiles()) {
            slides.add(f);
        }
        slidePane.setSlides(slides);

        this.titleField.setText(display.getPreviewText());
    }

    /**
     * Gets all the slides represented here
     *
     * @return an arraylist of media files, representing slides
     */
    public ArrayList<MediaFile> getMediaFiles() {
        return slides;
    }

    /**
     * Set what happens when a slide is changed
     *
     * @param newSlideIndex the index of the new slide
     */
    @Override
    public void slideChanged(int newSlideIndex) {
        if(newSlideIndex > slides.size() - 1){
            return;
        }
        if (advanceTimeField.getText().isEmpty()) {
            currentSlide = newSlideIndex;
            advanceTimeField.setText(slides.get(currentSlide).getAdvanceTime() + "");
            return;
        }
        if (!(advanceTimeField.getText().equals(slides.get(currentSlide).getAdvanceTime()))) {
            slides.get(currentSlide).setAdvanceTime(Integer.parseInt(advanceTimeField.getText()));
        }
        currentSlide = newSlideIndex;
        advanceTimeField.setText(slides.get(currentSlide).getAdvanceTime() + "");
    }
}
