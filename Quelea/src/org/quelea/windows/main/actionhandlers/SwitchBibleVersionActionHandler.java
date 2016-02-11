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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.SwitchBibleVersionDialog;
import org.quelea.windows.main.schedule.ScheduleList;

/**
 * The action handler used when switching bible versions.
 *
 * @author Michael
 */
public class SwitchBibleVersionActionHandler implements EventHandler<ActionEvent> {

    private final SwitchBibleVersionDialog dialog = new SwitchBibleVersionDialog();

    @Override
    public void handle(ActionEvent event) {
        ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
        Set<Bible> excludes = new HashSet<>();
        for (Displayable d : sl.getSelectionModel().getSelectedItems()) {
            if (d instanceof BiblePassage) {
                excludes.add(((BiblePassage) d).getVerses()[0].getChapter().getBook().getBible());
            }
        }
        Bible exclude = null;
        if (excludes.size() == 1) {
            exclude = excludes.iterator().next();
        }
        Bible b = dialog.getSwitchVersion(exclude);
        if (b == null) {
            return;
        }
        Map<BiblePassage, BiblePassage> replaceMap = new IdentityHashMap<>();
        for (Displayable d : sl.getSelectionModel().getSelectedItems()) {
            if (d instanceof BiblePassage) {
                BiblePassage passage = (BiblePassage) d;
                ThemeDTO theme = passage.getTheme();
                List<BibleVerse> newVerses = new ArrayList<>();
                for (BibleVerse verse : passage.getVerses()) {
                    int verseNum = verse.getNum();
                    int chapterNum = verse.getChapter().getNum();
                    int bookNum = verse.getChapter().getBook().getBookNumber();
                    newVerses.add(b.getBooks()[bookNum - 1].getChapter(chapterNum - 1).getVerse(verseNum));
                }
                BibleVerse firstVerse = newVerses.get(0);
                String summary = firstVerse.getChapter().getBook() + " " + passage.getLocation().split(" (?=\\d)")[1] + "\n" + b.getBibleName();
                replaceMap.put(passage, new BiblePassage(summary, newVerses.toArray(new BibleVerse[newVerses.size()]), theme, passage.getMulti()));
            }
        }
        List<Integer> selected = new ArrayList<>(sl.getSelectionModel().getSelectedIndices());
        sl.getSelectionModel().clearSelection();
        for (BiblePassage key : replaceMap.keySet()) {
            int index = sl.getItems().indexOf(key);
            if (index != -1) {
                sl.getItems().remove(index);
                sl.getItems().add(index, replaceMap.get(key));
            }
        }
        if (selected.size() > 0) {
            int[] selectRange = new int[selected.size() - 1];
            for (int i = 1; i < selected.size(); i++) {
                selectRange[i - 1] = selected.get(i);
            }
            sl.getSelectionModel().selectIndices(selected.get(0), selectRange);
        }
    }

}
