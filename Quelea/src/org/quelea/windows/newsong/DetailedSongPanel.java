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
package org.quelea.windows.newsong;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.widgets.IntegerTextField;

/**
 * A panel where more detailed information about a song is entered.
 * <p>
 * @author Michael
 */
public class DetailedSongPanel extends BorderPane {

    private final TextField ccli;
    private final TextField year;
    private final TextField publisher;
    private final TextField copyright;
    private final TextField key;
    private final TextField capo;
    private final TextArea info;
    private String saveHash = "";

    /**
     * Create a new detailed song panel.
     */
    public DetailedSongPanel() {
        GridPane formPanel = new GridPane();
        formPanel.setHgap(10);
        ccli = new IntegerTextField();
        publisher = new TextField();
        year = new IntegerTextField();
        copyright = new TextField();
        key = new TextField();
        capo = new IntegerTextField();
        info = new TextArea();
        info.setWrapText(true);

        addBlock(formPanel, LabelGrabber.INSTANCE.getLabel("ccli.number.label"), ccli, 1);
        addBlock(formPanel, LabelGrabber.INSTANCE.getLabel("copyright.label"), copyright, 2);
        addBlock(formPanel, LabelGrabber.INSTANCE.getLabel("year.label"), year, 3);
        addBlock(formPanel, LabelGrabber.INSTANCE.getLabel("publisher.label"), publisher, 4);
        addBlock(formPanel, LabelGrabber.INSTANCE.getLabel("key.label"), key, 5);
        addBlock(formPanel, LabelGrabber.INSTANCE.getLabel("capo.label"), capo, 6);
        addBlock(formPanel, LabelGrabber.INSTANCE.getLabel("notes.label"), info, 7);

        setTop(formPanel);

    }

    /**
     * Add a label / input block to a panel.
     * <p>
     * @param panel the panel to add to.
     * @param labelText the label text to add to this block.
     * @param comp the component to add to this block.
     */
    private void addBlock(GridPane panel, String labelText, Node comp, int i) {
        Label label = new Label(labelText);
        label.setLabelFor(comp);
        GridPane.setConstraints(label, 1, i);
        GridPane.setConstraints(comp, 2, i);
        panel.getChildren().add(label);
        panel.getChildren().add(comp);
    }

    /**
     * Reset this panel to blank so it can contain a new song.
     */
    public void resetNewSong() {
        ccli.setText("");
        year.setText("");
        publisher.setText("");
        copyright.setText("");
        key.setText("");
        capo.setText("");
        info.setText("");
    }

    /**
     * Set this panel to edit a song.
     * <p>
     * @param song the song to edit.
     */
    public void resetEditSong(SongDisplayable song) {
        ccli.setText(song.getCcli());
        copyright.setText(song.getCopyright());
        publisher.setText(song.getPublisher());
        year.setText(song.getYear());
        key.setText(song.getKey());
        capo.setText(song.getCapo());
        info.setText(song.getInfo());
    }

    public boolean hashChanged() {
        return !getSaveHash().equals(saveHash);
    }

    public void resetSaveHash() {
        saveHash = getSaveHash();
    }

    private String getSaveHash() {
        return "" + ccli.getText().hashCode() + copyright.getText().hashCode() + publisher.getText().hashCode() + year.getText().hashCode() + key.getText().hashCode() + capo.getText().hashCode() + info.getText().hashCode();
    }

    /**
     * Get the CCLI field.
     * <p>
     * @return the CCLI field.
     */
    public TextField getCcliField() {
        return ccli;
    }

    /**
     * Get the copyright field.
     * <p>
     * @return the copyright field.
     */
    public TextField getCopyrightField() {
        return copyright;
    }

    /**
     * Get the publisher field.
     * <p>
     * @return the publisher field.
     */
    public TextField getPublisherField() {
        return publisher;
    }

    /**
     * Get the year field.
     * <p>
     * @return the year field.
     */
    public TextField getYearField() {
        return year;
    }

    /**
     * Get the info field.
     * <p>
     * @return the info field.
     */
    public TextArea getInfoField() {
        return info;
    }

    /**
     * Get the key field.
     * <p>
     * @return the key field.
     */
    public TextField getKeyField() {
        return key;
    }

    /**
     * Get the capo field.
     * <p>
     * @return the capo field.
     */
    public TextField getCapoField() {
        return capo;
    }

}
