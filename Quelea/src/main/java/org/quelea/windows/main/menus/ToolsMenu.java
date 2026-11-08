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

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.ShortcutManager;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.actionhandlers.LiveTextActionHandler;
import org.quelea.windows.main.actionhandlers.SearchBibleActionHandler;
import org.quelea.windows.main.actionhandlers.ShowOptionsActionHandler;
import org.quelea.windows.main.actionhandlers.UniversalSearchActionHandler;
import org.quelea.windows.main.actionhandlers.ViewBibleActionHandler;
import org.quelea.windows.main.widgets.TestPaneDialog;

import org.quelea.windows.style.QueleaIcon;
/**
 * Quelea's tools menu.
 * <p>
 *
 * @author Michael
 */
public class ToolsMenu extends Menu {

    private final MenuItem universalSearchItem;
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

        universalSearchItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("universal.search.menu"), QueleaIcon.SEARCH.create());
        universalSearchItem.setAccelerator(ShortcutManager.getKeyCodeCombination(QueleaProperties.get().getUniversalSearchKeys()));
        universalSearchItem.setOnAction(new UniversalSearchActionHandler());
        getItems().add(universalSearchItem);

        viewBibleItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("view.bible.button"), QueleaIcon.BIBLE.create());
        viewBibleItem.setOnAction(new ViewBibleActionHandler());
        getItems().add(viewBibleItem);

        searchBibleItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("search.bible.button"), QueleaIcon.SEARCH.create());
        searchBibleItem.setOnAction(new SearchBibleActionHandler());
        getItems().add(searchBibleItem);

        testItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("test.patterns.text"), QueleaIcon.TEST_PATTERN.create());
        testItem.setOnAction(t -> {
            TestPaneDialog dialog = testDialog.get();
            if (dialog == null) {
                dialog = new TestPaneDialog();
                testDialog = new SoftReference<>(dialog);
            }
            dialog.show();
        });
        getItems().add(testItem);

        liveTextItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("send.live.text"), QueleaIcon.LIVE_TEXT.create());
        liveTextItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        liveTextItem.setOnAction(new LiveTextActionHandler());
        if (QueleaApp.get().getMobileLyricsServer() == null) {
            liveTextItem.setDisable(true);
        }
        getItems().add(liveTextItem);

        optionsItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("options.button"), QueleaIcon.SETTINGS.create());
        optionsItem.setAccelerator(ShortcutManager.getKeyCodeCombination(QueleaProperties.get().getOptionsKeys()));
        optionsItem.setOnAction(new ShowOptionsActionHandler());

        getItems().add(optionsItem);
    }

}
