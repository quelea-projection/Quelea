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
package org.quelea.windows.options.customprefs;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.SelectBibleVersionDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the bible versions chosen for new passages and a button to change them.
 * The field holds the bible names, one per line.
 *
 * @author Arvid
 */
public class BibleVersionsSelector extends SimpleControl<StringField, StackPane> {

    private final HBox hBox = new HBox(5);
    private Label versionsLabel;
    private Button selectButton;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParts() {
        super.initializeParts();
        node = new StackPane();
        versionsLabel = new Label();
        selectButton = new Button(LabelGrabber.INSTANCE.getLabel("select.bible.versions.button"));
        selectButton.setOnAction(event -> {
            List<Bible> selected = new ArrayList<>();
            for (String name : field.getUserInput().split("\n")) {
                Bible bible = BibleManager.get().getBibleFromName(name);
                if (bible != null) {
                    selected.add(bible);
                }
            }
            List<Bible> bibles = new SelectBibleVersionDialog().getVersions(selected);
            if (bibles != null) {
                List<String> names = new ArrayList<>();
                for (Bible bible : bibles) {
                    names.add(bible.getBibleName());
                }
                field.userInputProperty().set(String.join("\n", names));
            }
        });
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(selectButton, versionsLabel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutParts() {
        HBox.setHgrow(versionsLabel, Priority.ALWAYS);
        node.setAlignment(Pos.CENTER_LEFT);
        node.getChildren().add(hBox);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupBindings() {
        super.setupBindings();
        versionsLabel.textProperty().bind(field.userInputProperty().map(names -> names.replace("\n", ", ")));
    }
}
