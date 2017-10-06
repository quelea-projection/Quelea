package org.quelea.windows.newsong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;

/**
 * The dialog that allows the user to select a subset of fonts to display in the
 * theme panel.
 * <p>
 * @author Michael
 */
public class FontSelectionDialog extends Stage {

    private ListView<String> allFontSelection;
    private ListView<String> chosenFontSelection;
    private boolean dragFromLeft;

    /**
     * Create the font selection dialog.
     */
    public FontSelectionDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        setTitle(LabelGrabber.INSTANCE.getLabel("font.selection.dialog.title"));
        Utils.addIconsToStage(this);
        setOnHiding(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                QueleaProperties.get().setChosenFonts(chosenFontSelection.getItems());
            }
        });
        BorderPane mainPane = new BorderPane();
        setScene(new Scene(mainPane));

        Label label = new Label(LabelGrabber.INSTANCE.getLabel("chosen.fonts.explanation"));
        label.setWrapText(true);
        StackPane topPane = new StackPane();
        topPane.setPadding(new Insets(15));
        topPane.getChildren().add(label);
        mainPane.setTop(topPane);

        HBox centrePane = new HBox(5);
        centrePane.setPadding(new Insets(10));
        mainPane.setCenter(centrePane);
        allFontSelection = new ListView<>();
        allFontSelection.setMaxHeight(Integer.MAX_VALUE);
        VBox allFontBox = new VBox(5);
        allFontBox.setPadding(new Insets(5));
        allFontBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("ignored.fonts.label") + ": "));
        allFontBox.getChildren().add(allFontSelection);
        centrePane.getChildren().add(allFontBox);

        chosenFontSelection = new ListView<>();
        chosenFontSelection.setMaxHeight(Integer.MAX_VALUE);
        VBox chosenFontBox = new VBox(5);
        chosenFontBox.setPadding(new Insets(5));
        chosenFontBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("chosen.fonts.label") + ": "));
        chosenFontBox.getChildren().add(chosenFontSelection);
        centrePane.getChildren().add(chosenFontBox);

        Button doneButton = new Button(LabelGrabber.INSTANCE.getLabel("done.text"), new ImageView(new Image("file:icons/tick.png")));
        doneButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                FontSelectionDialog.this.hide();
            }
        });
        StackPane donePane = new StackPane();
        donePane.setPadding(new Insets(0, 0, 10, 0));
        donePane.getChildren().add(doneButton);
        mainPane.setBottom(donePane);

        String[] allFonts = Utils.getAllFonts();
        List<String> chosenFontsList = QueleaProperties.get().getChosenFonts();
        List<String> allFontsList = new ArrayList<>();
        for(String fontName : allFonts) {
            if(!chosenFontsList.contains(fontName)) {
                allFontsList.add(fontName);
            }
        }
        allFontSelection.getItems().addAll(allFontsList);
        chosenFontSelection.getItems().addAll(chosenFontsList);
        Collections.sort(allFontSelection.getItems());
        Collections.sort(chosenFontSelection.getItems());

        final Callback<ListView<String>, ListCell<String>> allFontsCallback = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> p) {

                final ListCell<String> listCell = new ListCell<String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(null);
                        if(empty) {
                            setText(null);
                        }
                        else {
                            setText(item);
                        }
                    }
                };
                listCell.setOnDragDetected(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        dragFromLeft = true;
                        Dragboard db = listCell.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(listCell.getItem());
                        db.setContent(content);
                        event.consume();
                    }
                });
                return listCell;
            }
        };
        final Callback<ListView<String>, ListCell<String>> chosenFontsCallback = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> p) {

                final ListCell<String> listCell = new ListCell<String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(null);
                        if(empty) {
                            setText(null);
                        }
                        else {
                            setText(item);
                        }
                    }
                };
                listCell.setOnDragDetected(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        dragFromLeft = false;
                        Dragboard db = listCell.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(listCell.getItem());
                        db.setContent(content);
                        event.consume();
                    }
                });
                return listCell;
            }
        };
        allFontSelection.setCellFactory(allFontsCallback);
        chosenFontSelection.setCellFactory(chosenFontsCallback);
        allFontSelection.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if(event.getDragboard().getString() != null) {
                    event.acceptTransferModes(TransferMode.ANY);
                }
            }
        });
        allFontSelection.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent t) {
                if(!dragFromLeft) {
                    String font = t.getDragboard().getString();
                    if(font != null) {
                        chosenFontSelection.getItems().remove(font);
                        allFontSelection.getItems().add(font);
                        Collections.sort(allFontSelection.getItems());
                    }
                }
            }
        });
        chosenFontSelection.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if(event.getDragboard().getString() != null) {
                    event.acceptTransferModes(TransferMode.ANY);
                }
            }
        });
        chosenFontSelection.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent t) {
                if(dragFromLeft) {
                    String font = t.getDragboard().getString();
                    if(font != null) {
                        chosenFontSelection.getItems().add(font);
                        Collections.sort(chosenFontSelection.getItems());
                        allFontSelection.getItems().remove(font);
                    }
                }
            }
        });

        setWidth(500);
    }

    /**
     * Get a list of the user chosen fonts.
     * @return the user chosen fonts.
     */
    public List<String> getChosenFonts() {
        return chosenFontSelection.getItems();
    }

}
