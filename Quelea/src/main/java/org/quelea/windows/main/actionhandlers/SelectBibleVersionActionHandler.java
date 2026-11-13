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
package org.quelea.windows.main.actionhandlers;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.javafx.dialog.Dialog;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.SelectBibleVersionDialog;
import org.quelea.windows.main.schedule.ScheduleList;

/**
 * The action handler used when choosing the bible versions of a passage.
 *
 * @author Michael
 */
public class SelectBibleVersionActionHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent event) {
        ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
        List<IndexedDisplayable> passages = new ArrayList<>();
        for (IndexedDisplayable d : sl.getSelectionModel().getSelectedItems()) {
            if (d.displayable() instanceof BiblePassage) {
                passages.add(d);
            }
        }
        if (passages.isEmpty()) {
            return;
        }
        List<Bible> bibles = new SelectBibleVersionDialog().getVersions(((BiblePassage) passages.get(0).displayable()).getBibleVersions());
        if (bibles == null) {
            return;
        }
        Map<IndexedDisplayable, BiblePassage> replaceMap = new IdentityHashMap<>();
        List<String> missing = new ArrayList<>();
        for (IndexedDisplayable item : passages) {
            BiblePassage passage = (BiblePassage) item.displayable();
            BiblePassage newPassage = passage.withVersions(bibles);
            if (newPassage == null) {
                missing.add(passage.getLocation());
            } else {
                replaceMap.put(item, newPassage);
                missing.addAll(newPassage.getMissingVerses());
            }
        }
        List<Integer> selected = new ArrayList<>(sl.getSelectionModel().getSelectedIndices());
        sl.getSelectionModel().clearSelection();
        //Find the list entries by identity, as the same passage can be in the schedule more than once
        for (Map.Entry<IndexedDisplayable, BiblePassage> entry : replaceMap.entrySet()) {
            for (int index = 0; index < sl.getItems().size(); index++) {
                if (sl.getItems().get(index) == entry.getKey()) {
                    sl.getItems().remove(index);
                    sl.getItems().add(index, new IndexedDisplayable(entry.getValue(), index));
                    break;
                }
            }
        }
        if (!selected.isEmpty()) {
            int[] selectRange = new int[selected.size() - 1];
            for (int i = 1; i < selected.size(); i++) {
                selectRange[i - 1] = selected.get(i);
            }
            sl.getSelectionModel().selectIndices(selected.get(0), selectRange);
        }
        showMissingVerses(missing);
    }

    /**
     * Show a passage in the versions set in the options, if there are any.
     *
     * @param passage the passage to show.
     * @param bible   the bible the passage was picked from.
     * @return the passage to add to the schedule.
     */
    public static BiblePassage applyDefaultVersions(BiblePassage passage, Bible bible) {
        List<Bible> bibles = BibleManager.get().getDefaultVersions(bible);
        if (bibles.size() < 2) {
            return passage;
        }
        BiblePassage newPassage = passage.withVersions(bibles);
        if (newPassage == null) {
            return passage;
        }
        showMissingVerses(newPassage.getMissingVerses());
        return newPassage;
    }

    private static void showMissingVerses(List<String> missing) {
        if (!missing.isEmpty()) {
            Dialog.showInfo(LabelGrabber.INSTANCE.getLabel("no.verse.title"),
                    LabelGrabber.INSTANCE.getLabel("no.verse.message") + "\n\n" + String.join("\n", missing),
                    QueleaApp.get().getMainWindow());
        }
    }

}
