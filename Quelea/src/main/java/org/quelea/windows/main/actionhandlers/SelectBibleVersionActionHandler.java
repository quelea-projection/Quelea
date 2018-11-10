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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.bible.CustomVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.SelectBibleVersionDialog;
import org.quelea.windows.main.schedule.ScheduleList;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The action handler used when adding bible versions.
 * <p>
 *
 * @author Arvid
 */
public class SelectBibleVersionActionHandler implements EventHandler<ActionEvent> {

    private final SelectBibleVersionDialog dialog = new SelectBibleVersionDialog();

    @Override
    public void handle(ActionEvent event) {
        ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();

        Map<BiblePassage, BiblePassage> replaceMap = new IdentityHashMap<>();
        Displayable d = sl.getSelectionModel().getSelectedItem();
        if (d instanceof BiblePassage) {
            BiblePassage passage = (BiblePassage) d;
            ArrayList<Bible> bibles = dialog.getAddVersion(null, passage.getBibleVersions());
            ThemeDTO theme = passage.getTheme();
            List<CustomVerse> newVerses = getNewVerses(bibles, passage);
            StringBuilder sb = new StringBuilder();
            if (newVerses == null) {
                return;
            }
            CustomVerse firstVerse = newVerses.get(0);
            for (Bible b : bibles) {
                sb.append(" + ").append(b.getBibleName());
            }
            String summary = firstVerse.getChapter().getBook() + " " + passage.getLocation().split(" (?=\\d)")[1] + "\n" + sb.toString().replaceFirst(" \\+ ", "");
            replaceMap.put(passage, new BiblePassage(summary, newVerses.toArray(new CustomVerse[0]), theme, passage.getMulti(), bibles.size() > 1, bibles));

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

    public static List<CustomVerse> getNewVerses(List<Bible> bibles, BiblePassage passage) {
        List<CustomVerse> newVerses = new ArrayList<>();
        for (BibleVerse verse : passage.getVerses()) {
            CustomVerse verseCopy = new CustomVerse(verse.copyVerse(verse));
            int verseNum = verse.getNum();
            int chapterNum = verse.getChapter().getNum();
            int bookNum = verse.getChapter().getBook().getBookNumber();
            newVerses.add(verseCopy);
            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            for (Bible b : bibles) {
                if (b == null) {
                    return null;
                }
                BibleVerse translation = b.getBooks()[bookNum - 1].getChapter(chapterNum - 1).getVerse(verseNum);
                if (translation != null) {
                    if (i == 0)
                        verseCopy.setBibleVerse(translation);
                    stringBuilder.append("\n\n").append(translation.getText().replaceAll(" {2}", " ").trim());
                }
                i++;
            }
            verseCopy.setVerseText(stringBuilder.toString().replaceFirst("\n\n", ""));
        }
        return newVerses;

    }

}
