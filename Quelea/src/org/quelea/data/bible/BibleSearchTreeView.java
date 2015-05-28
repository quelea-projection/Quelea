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
package org.quelea.data.bible;

import java.util.Collection;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * The TreeView responsible for showing search results in a TreeView model
 *
 * @author Ben
 */
public class BibleSearchTreeView extends TreeView<BibleInterface> {

    private TreeItem<BibleInterface> root;
    private FlowPane textPane;
    private ScrollPane sp;
    private ComboBox bibles;
    private boolean all = true;
    private int size;

    /**
     * Constructs a TreeView object with a blank root BibleInterface item.
     * <p/>
     * @param chapterPane the scroll pane that will display the verse selected
     * @param bibles the combobox of bibles to filter with.
     */
    public BibleSearchTreeView(ScrollPane chapterPane, ComboBox bibles) {
        this.bibles = bibles;
        setRoot(new TreeItem<BibleInterface>());
        root = getRoot();
        root.setExpanded(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sp = chapterPane;
        textPane = (FlowPane) sp.getContent();
        this.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.RIGHT) {
                    trigger(t);
                }
            }
        });
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                trigger(t);
            }
        });
    }

    private void trigger(Event t) {
        BibleSearchTreeView tv = (BibleSearchTreeView) t.getSource();
        TreeItem<BibleInterface> ti = tv.getSelectionModel().getSelectedItem();
        if (ti != null) {
            if (ti.getValue() instanceof BibleVerse) {
                textPane.getChildren().clear();
                BibleChapter chapter = (BibleChapter) ti.getValue().getParent();
                BibleVerse[] verses = chapter.getVerses();
                BibleVerse selected = (BibleVerse) ti.getValue();

                int x = selected.getNum() - 1;
                for (int i = 0; i < verses.length; i++) {
                    Text text = new Text(verses[i].toString() + " ");
                    if (i == x) {
                        text.setFont(Font.font("Sans", FontWeight.BOLD, 14));
                    } else {
                        text.setFont(Font.font("Sans", 14));
                    }
                    textPane.getChildren().add(text);
                    text.wrappingWidthProperty().bind(sp.widthProperty().subtract(20)); //-20 to account for scroll bar width
                }
            } else {
                ti.setExpanded(!ti.isExpanded());
            }
        } else {
            tv.selectionModelProperty().get().selectFirst();
        }
    }

    /**
     * Resets the root and expands it.
     * <p/>
     */
    public void reset() {
        this.setShowRoot(false);
        resetRoot();
        root.setExpanded(true);
    }

    /**
     * Adds the filtered results into the treeview. Sorts them from the verse's
     * parents
     * <p/>
     * @param verse The bible verse to add into the tree.
     */
    public void add(BibleVerse verse) {
        BibleChapter chapter = (BibleChapter) verse.getParent();
        BibleBook book = (BibleBook) chapter.getParent();
        Bible bible = (Bible) book.getParent();

        // Get the current bible
        TreeItem<BibleInterface> cbible;
        if (all) {
            cbible = existsOrCreateInt(root.getChildren(), bible);
        } else {
            cbible = root;
        }

        // Get the current book
        TreeItem<BibleInterface> cbook = existsOrCreateInt(cbible.getChildren(), book);

        //Get the current chapter.
        TreeItem<BibleInterface> cchapter = existsOrCreateInt(cbook.getChildren(), chapter);

        //See if verse is in results, or add it.
        TreeItem<BibleInterface> cverse = new TreeItem<BibleInterface>(verse);
        if (!cchapter.getChildren().contains(cverse)) {
            cchapter.getChildren().add(cverse);
            size++;
        }
    }

    public int size() {
        return size;
    }
    /**
     * Checks if the bible section is already listed in the current tree
     */
    private TreeItem<BibleInterface> existsOrCreateInt(Collection<TreeItem<BibleInterface>> coll, BibleInterface toFind) {
        for (TreeItem<BibleInterface> i : coll) {
            if (i.getValue().getName().equals(toFind.getName())) {
                return i;
            }
        }
        TreeItem<BibleInterface> temp = new TreeItem<>(toFind);
        coll.add(temp);
        return temp;
    }

    /**
     * Resets the root based on the current selected index of the combobox. If
     * all, a blank root is created, if a bible is selected it becomes the root
     */
    public void resetRoot() {
        if (bibles.getSelectionModel().getSelectedIndex() != 0) {
            String bib = (String) bibles.getSelectionModel().getSelectedItem();
            for (Bible b : BibleManager.get().getBibles()) {
                if (b.getName().equals(bib)) {
                    setRoot(new TreeItem<BibleInterface>(b));
                    all = false;
                }
            }
        } else {
            setRoot(new TreeItem<BibleInterface>());
            all = true;
        }
        root = getRoot();
        size = 0;
        this.setShowRoot(false);
    }
}