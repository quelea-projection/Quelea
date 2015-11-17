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
import org.quelea.data.ThemeDTO;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.data.displayable.Displayable;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.ScheduleList;

/**
 *
 * @author Arvid
 */
public class AddBibleVerseHandler {

    public void add() {
        ScheduleList sl = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
        Set<Bible> current = new HashSet<>();
        Displayable d = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable();
        if (d instanceof BiblePassage) {
            current.add(((BiblePassage) d).getVerses()[0].getChapter().getBook().getBible());
        }
        Bible b = null;
        if (current.size() == 1) {
            b = current.iterator().next();
        }
        if (b == null) {
            return;
        }
        Map<BiblePassage, BiblePassage> replaceMap = new IdentityHashMap<>();
        if (d instanceof BiblePassage) {
            BiblePassage passage = (BiblePassage) d;
            ThemeDTO theme = passage.getTheme();
            List<BibleVerse> newVerses = new ArrayList<>();
            int lastNumber = 0;
            int chapter = 0;
            int i = 0;
            for (BibleVerse bv : passage.getVerses()) {
                int verseNum = bv.getNum();
                int chapterNum = bv.getChapter().getNum();
                int bookNum = bv.getChapter().getBook().getBookNumber();
                newVerses.add(b.getBooks()[bookNum - 1].getChapter(chapterNum - 1).getVerse(verseNum));
                if (i == passage.getVerses().length - 1) {
                    newVerses.add(b.getBooks()[bookNum - 1].getChapter(chapterNum - 1).getVerse(verseNum + 1));
                }
                if (newVerses.get(newVerses.size() - 1) != null) {
                    lastNumber = verseNum + 1;
                } else if (newVerses.get(newVerses.size() - 1) == null) {
                    newVerses.remove(newVerses.size() - 1);
                    newVerses.add(b.getBooks()[bookNum - 1].getChapter(chapterNum).getVerse(1));
                    chapter = bv.getChapter().getNum() + 1;
                    lastNumber = 1;
                }
                i++;
            }
            BibleVerse firstVerse = newVerses.get(0);
            String passageNumber = passage.getLocation().split(" (?=\\d)")[1];
            if (chapter > 0) {
                passageNumber = passageNumber + ";" + chapter + ":" + lastNumber;
            } else if (passageNumber.contains(";") || (passageNumber.contains(","))) {
                if (passageNumber.contains(";")) {
                    if (passageNumber.substring(passageNumber.lastIndexOf(";")).contains("-")) {
                        passageNumber = passageNumber.substring(0, passageNumber.lastIndexOf("-") + 1) + lastNumber;
                    } else
                        passageNumber = passageNumber + "-" + lastNumber;
                } else {
                    if (passageNumber.substring(passageNumber.lastIndexOf(",")).contains("-")) {
                        passageNumber = passageNumber.substring(0, passageNumber.lastIndexOf("-") + 1) + lastNumber;
                    } else
                        passageNumber = passageNumber + "-" + lastNumber;
                }
            } else {
                if (passageNumber.contains("-")) {
                    passageNumber = passageNumber.substring(0, passageNumber.indexOf("-") + 1) + lastNumber;
                } else if (passageNumber.contains(":")) {
                    passageNumber = passageNumber + "-" + lastNumber;
                }
            }
            String summary = firstVerse.getChapter().getBook() + " " + passageNumber + "\n" + b.getBibleName();
            replaceMap.put(passage, new BiblePassage(summary, newVerses.toArray(new BibleVerse[newVerses.size()]), theme, passage.getMulti()));
        }
        sl.getSelectionModel().clearSelection();
        int index = -1;
        for (BiblePassage key : replaceMap.keySet()) {
            index = sl.getItems().indexOf(key);
            if (index != -1) {
                sl.getItems().remove(index);
                sl.getItems().add(index, replaceMap.get(key));
            }
        }
        if (index != -1) {
            sl.getSelectionModel().clearAndSelect(index);
        }
    }
}
