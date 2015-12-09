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
package org.quelea.data.bible;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;

/**
 * The popup menu that appears on a "searched" bible chapter.
 *
 * @author Michael
 */
public class BibleSearchPopupMenu extends ContextMenu {

    private MenuItem viewVerseItem;
    private BibleChapter currentChapter;

    /**
     * Create the bible search popup menu.
     */
    public BibleSearchPopupMenu() {
        viewVerseItem = new MenuItem(LabelGrabber.INSTANCE.getLabel("open.in.browser"));
        viewVerseItem.setOnAction(new EventHandler<javafx.event.ActionEvent>() {

            @Override
            public void handle(javafx.event.ActionEvent t) {
                trigger();
            }
        });
        getItems().add(viewVerseItem);
    }

    /**
     * Event trigger
     */
    private void trigger() {
        if (currentChapter != null) {
            BibleBrowseDialog dialog = QueleaApp.get().getMainWindow().getBibleBrowseDialog();
            dialog.setChapter(currentChapter);
            dialog.show();
        }
    }

    /**
     * Set the current chapter the menu should jump to.
     *
     * @param currentChapter the current chapter.
     */
    public void setCurrentChapter(BibleChapter currentChapter) {
        this.currentChapter = currentChapter;
    }
}
