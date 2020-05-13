package org.quelea.windows.newsong;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * The dialog that allows the user to select a song sequence.
 * <p>
 *
 * @author Arvid
 */
public final class SequenceSelectionDialog extends Stage {

    private ListView<String> chosenSequence;
    private final Button removeButton;
    private final Button upButton;
    private final Button downButton;
    private boolean fromLeft;
    private boolean finished;

    /**
     * A direction; either up or down. Used for rearranging the order of items
     * in the sequence order.
     */
    public enum Direction {
        UP, DOWN
    }

    /**
     * Create the sequence selection dialog.
     */
    SequenceSelectionDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        setTitle(LabelGrabber.INSTANCE.getLabel("sequence.selection.dialog.title"));
        Utils.addIconsToStage(this);
        BorderPane mainPane = new BorderPane();
        setScene(new Scene(mainPane));

        finished = false;
        Label label = new Label(LabelGrabber.INSTANCE.getLabel("chosen.sequence.explanation"));
        label.setWrapText(true);
        StackPane topPane = new StackPane();
        topPane.setPadding(new Insets(15));
        topPane.getChildren().add(label);
        mainPane.setTop(topPane);

        HBox centrePane = new HBox(5);
        centrePane.setPadding(new Insets(10));
        mainPane.setCenter(centrePane);
        ListView<String> allSections = new ListView<>();
        allSections.setMaxHeight(Integer.MAX_VALUE);
        VBox allSectionsBox = new VBox(5);
        allSectionsBox.setPadding(new Insets(5));
        allSectionsBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("available.sections.label") + ": "));
        allSectionsBox.getChildren().add(allSections);
        centrePane.getChildren().add(allSectionsBox);

        VBox toolbar = new VBox();
        toolbar.setPadding(new Insets(5));
        toolbar.getChildren().add(new Label());
        ImageView removeIV = new ImageView(new Image("file:icons/ic-cancel.png"));
        removeIV.setFitWidth(16);
        removeIV.setFitHeight(16);
        removeButton = new Button("", removeIV);
        Utils.setToolbarButtonStyle(removeButton);
        removeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("remove.sequence.tooltip")));
        removeButton.setDisable(true);
        removeButton.setOnAction(e -> {
            chosenSequence.getItems().remove(chosenSequence.selectionModelProperty().get().getSelectedIndex());
        });

        ImageView upIV = new ImageView(new Image(QueleaProperties.get().getUseDarkTheme() ? "file:icons/ic-up-light.png" : "file:icons/ic-up.png"));
        upIV.setFitWidth(16);
        upIV.setFitHeight(16);
        upButton = new Button("", upIV);
        Utils.setToolbarButtonStyle(upButton);
        upButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("move.up.sequence.tooltip")));
        upButton.setDisable(true);
        upButton.setOnAction(t -> moveCurrentItem(Direction.UP));

        ImageView downIV = new ImageView(new Image(QueleaProperties.get().getUseDarkTheme() ? "file:icons/ic-down-light.png" : "file:icons/ic-down.png"));
        downIV.setFitWidth(16);
        downIV.setFitHeight(16);
        downButton = new Button("", downIV);
        Utils.setToolbarButtonStyle(downButton);
        downButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("move.down.sequence.tooltip")));
        downButton.setDisable(true);
        downButton.setOnAction(t -> moveCurrentItem(Direction.DOWN));

        toolbar.getChildren().add(removeButton);
        toolbar.getChildren().add(upButton);
        toolbar.getChildren().add(downButton);

        centrePane.getChildren().add(toolbar);

        chosenSequence = new ListView<>();
        chosenSequence.setMaxHeight(Integer.MAX_VALUE);
        VBox chosenSequenceBox = new VBox(5);
        chosenSequenceBox.setPadding(new Insets(5));
        chosenSequenceBox.getChildren().add(new Text(LabelGrabber.INSTANCE.getLabel("chosen.sequence.label") + ": "));
        chosenSequenceBox.getChildren().add(chosenSequence);
        centrePane.getChildren().add(chosenSequenceBox);

        Button doneButton = new Button(LabelGrabber.INSTANCE.getLabel("done.text"), new ImageView(new Image("file:icons/ic-tick.png",16,16,false,true)));
        doneButton.setOnAction((ActionEvent t) -> {
            finished = true;
            SequenceSelectionDialog.this.hide();
        });
        StackPane donePane = new StackPane();
        donePane.setPadding(new Insets(0, 0, 10, 0));
        donePane.getChildren().add(doneButton);
        mainPane.setBottom(donePane);

        List<String> chosenSequenceList = getSequence();
        List<String> allSectionsList = getAllSections();
        allSections.getItems().addAll(allSectionsList);
        chosenSequence.getItems().addAll(chosenSequenceList);

        final IntegerProperty dragFromIndex = new SimpleIntegerProperty(-1);

        setupCellsInAllSectionsList(allSections, dragFromIndex);

        setupCellsInSelectedSectionsList(chosenSequence, dragFromIndex);

        setWidth(500);
    }

    private void setupCellsInSelectedSectionsList(ListView<String> chosenSequence, IntegerProperty dragFromIndex) {
        chosenSequence.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            int endItem = -1;

            @Override
            public ListCell<String> call(ListView<String> lv) {
                final ListCell<String> cell = new ListCell<String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };

                cell.setOnDragDetected((MouseEvent event) -> {
                    fromLeft = false;
                    if (!cell.isEmpty()) {
                        dragFromIndex.set(cell.getIndex());
                        Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(cell.getItem());
                        db.setContent(cc);
                        db.setDragView(cell.snapshot(null, null));
                    }
                });

                cell.setOnDragOver((DragEvent event) -> {
                    if (dragFromIndex.get() >= 0 && (dragFromIndex.get() != cell.getIndex() || fromLeft)) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                });

                cell.setOnDragEntered((DragEvent event) -> {
                    if (dragFromIndex.get() >= 0 && (dragFromIndex.get() != cell.getIndex() || fromLeft)) {
                        cell.setStyle("-fx-background-color: gold;");
                        endItem = cell.getIndex();
                    } else {
                        endItem = -1;
                    }
                });

                cell.setOnDragExited((DragEvent event) -> {
                    cell.setStyle("");
                });

                cell.setOnDragDropped((DragEvent event) -> {
                    if (event.getTransferMode().equals(TransferMode.MOVE)) {
                        int dragItemsStartIndex;
                        int dragItemsEndIndex;
                        int direction;
                        if (cell.isEmpty()) {
                            dragItemsStartIndex = dragFromIndex.get();
                            dragItemsEndIndex = chosenSequence.getItems().size();
                            direction = -1;
                        } else {
                            if (cell.getIndex() < dragFromIndex.get()) {
                                dragItemsStartIndex = cell.getIndex();
                                dragItemsEndIndex = dragFromIndex.get() + 1;
                                direction = 1;
                            } else {
                                dragItemsStartIndex = dragFromIndex.get();
                                dragItemsEndIndex = cell.getIndex() + 1;
                                direction = -1;
                            }
                        }

                        List<String> rotatingItems = chosenSequence.getItems().subList(dragItemsStartIndex, dragItemsEndIndex);
                        List<String> rotatingItemsCopy = new ArrayList<>(rotatingItems);
                        Collections.rotate(rotatingItemsCopy, direction);
                        rotatingItems.clear();
                        rotatingItems.addAll(rotatingItemsCopy);
                        dragFromIndex.set(-1);
                    } else {
                        String section = event.getDragboard().getString();
                        if (section != null) {
                            addItemToSequence(endItem, section);
                        }
                    }
                });

                cell.setOnDragDone((DragEvent event) -> {
                    dragFromIndex.set(-1);
                    if (endItem > -1) {
                        chosenSequence.getSelectionModel().select(endItem);
                    }
                });
                return cell;
            }

        });

        chosenSequence.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                downButton.setDisable(false);
                upButton.setDisable(false);
                removeButton.setDisable(false);
            } else {
                downButton.setDisable(true);
                upButton.setDisable(true);
                removeButton.setDisable(true);
            }
        });

        chosenSequence.setOnDragOver((DragEvent event) -> {
            if (dragFromIndex.get() >= 0 && chosenSequence.getItems().isEmpty()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        });

        chosenSequence.setOnDragDropped(e -> {
            String section = e.getDragboard().getString();
            if (section != null && chosenSequence.getItems().isEmpty()) {
                addItemToSequence(-1, section);
            }
        });
    }

    private void setupCellsInAllSectionsList(ListView<String> allSections, IntegerProperty dragFromIndex) {
        allSections.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> lv) {
                final ListCell<String> cell = new ListCell<String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };

                cell.setOnDragDetected((MouseEvent event) -> {
                    fromLeft = true;
                    if (!cell.isEmpty()) {
                        dragFromIndex.set(cell.getIndex());
                        Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(cell.getItem());
                        db.setContent(cc);
                        db.setDragView(cell.snapshot(null, null));
                    }
                });

                cell.setOnDragDone((DragEvent event) -> {
                    dragFromIndex.set(-1);
                    chosenSequence.getSelectionModel().select(event.getDragboard().getString());
                });

                cell.setOnMouseClicked(e -> {
                    if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                        addItemToSequence(chosenSequence.getItems().size(), cell.getText());
                    }
                });
                return cell;
            }
        });
    }

    private void addItemToSequence(int pos, String section) {
        StringBuilder name = new StringBuilder();
        if (section.trim().contains(" ")) {
            for (String s : section.split(" ")) {
                name.append(s.substring(0, 1));
            }
        } else {
            name.append(section.substring(0, 1));
        }
        if (chosenSequence.getItems().size() != 0 && chosenSequence.getItems().get(0).isEmpty()) {
            chosenSequence.getItems().remove(0);
        }
        if (pos < 0 || pos > chosenSequence.getItems().size()) {
            chosenSequence.getItems().add(name.toString().trim());
        } else {
            chosenSequence.getItems().add(pos, name.toString().trim());
        }
    }

    /**
     * Get a list of the user chosen sequence.
     *
     * @return the user chosen sequence.
     */
    public List<String> getChosenSequence() {
        return chosenSequence.getItems();
    }

    private List<String> getSequence() {
        String sequence = QueleaApp.get().getMainWindow().getSongEntryWindow().getBasicSongPanel().getSequenceField().getText();
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(sequence.split(" ")));
        return list;
    }

    private List<String> getAllSections() {
        String lyrics = QueleaApp.get().getMainWindow().getSongEntryWindow().getBasicSongPanel().getLyricsField().getTextArea().getText();
        List<String> list = new ArrayList<>();
        for (String s : lyrics.split("\n")) {
            if (new LineTypeChecker(s).getLineType() == LineTypeChecker.Type.TITLE && !list.contains(s)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Move the currently selected item in the list in the specified direction.
     * <p/>
     *
     * @param direction the direction to move the selected item.
     */
    private void moveCurrentItem(Direction direction) {
        int selectedIndex = chosenSequence.selectionModelProperty().get().getSelectedIndex();
        if (selectedIndex == -1) { //Nothing selected
            return;
        }
        if (direction == Direction.UP && selectedIndex > 0) {
            Collections.swap(chosenSequence.itemsProperty().get(), selectedIndex, selectedIndex - 1);
            chosenSequence.getSelectionModel().clearSelection();
            chosenSequence.selectionModelProperty().get().select(selectedIndex - 1);
        }
        if (direction == Direction.DOWN && selectedIndex < chosenSequence.itemsProperty().get().size() - 1) {
            Collections.swap(chosenSequence.itemsProperty().get(), selectedIndex, selectedIndex + 1);
            chosenSequence.getSelectionModel().clearSelection();
            chosenSequence.selectionModelProperty().get().select(selectedIndex + 1);
        }
        requestFocus();
    }

    /**
     * Check if done was pressed or action was aborted.
     *
     * @return true if done was pressed to close the window, false otherwise.
     */
    public boolean isFinished() {
        return finished;
    }
}