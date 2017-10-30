/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.menus;

import java.lang.ref.SoftReference;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.LiveTextActionHandler;
import org.quelea.windows.main.actionhandlers.SearchBibleActionHandler;
import org.quelea.windows.main.actionhandlers.ShowOptionsActionHandler;
import org.quelea.windows.main.actionhandlers.ViewBibleActionHandler;
import org.quelea.windows.main.widgets.TestPaneDialog;

/**
 * Quelea's tools menu.
 * <p>
 * @author Michael
 */
public class ToolsMenu extends Menu {

    private final MenuItem searchBibleItem;
    private final MenuItem viewBibleItem;
    private final MenuItem liveTextItem;
    private final MenuItem testItem;
    private final MenuItem optionsItem;
    private SoftReference<TestPaneDialog> testDialog = new SoftReference<>(null);

    /**
     * Create the tools menu.
     */
    public ToolsMenu() {
        super(LabelGrabber.INSTANCE.getLabel("tools.menu"));

        viewBibleItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("view.bible.button"), new ImageView(new Image("file:icons/bible.png", 20, 20, false, true)));
        viewBibleItem.setOnAction(new ViewBibleActionHandler());
        getItems().add(viewBibleItem);

        searchBibleItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("search.bible.button"), new ImageView(new Image("file:icons/bible.png", 20, 20, false, true)));
        searchBibleItem.setOnAction(new SearchBibleActionHandler());
        getItems().add(searchBibleItem);

        testItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("test.patterns.text"), new ImageView(new Image("file:icons/testbars.png", 20, 20, false, true)));
        testItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TestPaneDialog dialog = testDialog.get();
                if (dialog == null) {
                    dialog = new TestPaneDialog();
                    testDialog = new SoftReference<>(dialog);
                }
                dialog.show();
            }
        });
        getItems().add(testItem);

        liveTextItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("send.live.text"), new ImageView(new Image("file:icons/live_text.png", 20, 20, false, true)));
        liveTextItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        liveTextItem.setOnAction(new LiveTextActionHandler());
        if(QueleaApp.get().getMobileLyricsServer()==null) {
            liveTextItem.setDisable(true);
        }
        getItems().add(liveTextItem);

        optionsItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("options.button"), new ImageView(new Image("file:icons/options.png", 20, 20, false, true)));
        optionsItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN));
        optionsItem.setOnAction(new ShowOptionsActionHandler());

        getItems().add(optionsItem);
    }

}
