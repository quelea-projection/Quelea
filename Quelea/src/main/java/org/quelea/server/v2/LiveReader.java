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
package org.quelea.server.v2;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageGroupDisplayable;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.data.displayable.TextDisplayable;
import org.quelea.data.displayable.TextSection;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.toolbars.MainToolbar;

/**
 * Reads Quelea's live state and schedule. Must be called on the FX thread.
 */
final class LiveReader {

    private final MainPanel mainPanel;
    private final MainToolbar toolbar;
    private final ItemIds ids;
    private final LongSupplier scheduleRevision;

    LiveReader(MainPanel mainPanel, MainToolbar toolbar, ItemIds ids, LongSupplier scheduleRevision) {
        this.mainPanel = mainPanel;
        this.toolbar = toolbar;
        this.ids = ids;
        this.scheduleRevision = scheduleRevision;
    }

    LiveState state() {
        LivePanel lp = mainPanel.getLivePanel();
        Displayable d = lp.getDisplayable();
        return new LiveState(
                ids.idFor(d),
                ItemText.title(d),
                ItemTypes.of(d),
                sectionIndex(),
                sectionCount(),
                lp.getLogoed(),
                lp.getBlacked(),
                lp.getCleared(),
                toolbar.recordingProperty().get(),
                scheduleRevision.getAsLong());
    }

    ScheduleSnapshot schedule() {
        Displayable live = mainPanel.getLivePanel().getDisplayable();
        Displayable preview = mainPanel.getPreviewPanel().getDisplayable();
        List<IndexedDisplayable> items = mainPanel.getSchedulePanel().getScheduleList().getItems();
        List<String> itemIds = scheduleIds();
        List<ScheduleSnapshot.Entry> entries = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Displayable d = items.get(i).displayable();
            entries.add(new ScheduleSnapshot.Entry(itemIds.get(i), ItemTypes.of(d), ItemText.title(d), d == live, d == preview));
        }
        return new ScheduleSnapshot(scheduleRevision.getAsLong(), entries);
    }

    /**
     * The ids of the schedule items, in schedule order. Quelea can put the
     * same displayable in the schedule twice (a song added twice from the
     * library); its second entry gets "-2" after the id, the third "-3",
     * so that every entry can be addressed.
     *
     * @return one id per schedule item.
     */
    List<String> scheduleIds() {
        Map<Displayable, Integer> seen = new IdentityHashMap<>();
        List<String> result = new ArrayList<>();
        for (IndexedDisplayable item : mainPanel.getSchedulePanel().getScheduleList().getItems()) {
            Displayable d = item.displayable();
            int occurrence = seen.merge(d, 1, Integer::sum);
            String id = ids.idFor(d);
            result.add(occurrence == 1 ? id : id + "-" + occurrence);
        }
        return result;
    }

    /**
     * @param itemId a schedule item id.
     * @return the item's position in the schedule, or -1.
     */
    int indexOfId(String itemId) {
        return itemId == null ? -1 : scheduleIds().indexOf(itemId);
    }

    LiveSections sections() {
        LivePanel lp = mainPanel.getLivePanel();
        Displayable d = lp.getDisplayable();
        List<LiveSections.Section> sections = new ArrayList<>();
        if (d instanceof TextDisplayable) {
            // The live list, not getSections(): its entries are what the
            // section index counts.
            for (TextSection section : lp.getLyricsPanel().getLyricsList().getItems()) {
                List<String> lines = lines(section.getText(false, false));
                List<String> chordLines = lines(section.getText(true, false));
                // Bible passages carry HTML for verse numbers.
                sections.add(new LiveSections.Section(section.getTitle(), lines,
                        chordLines.equals(lines) ? null : chordLines));
            }
        } else {
            for (int i = 0; i < sectionCount(); i++) {
                sections.add(new LiveSections.Section(null, List.of(), null));
            }
        }
        return new LiveSections(ids.idFor(d), sectionIndex(), sections);
    }

    private static List<String> lines(String[] text) {
        List<String> lines = new ArrayList<>(text.length);
        for (String line : text) {
            if (line != null) {
                lines.add(ItemText.plain(line));
            }
        }
        return lines;
    }

    /**
     * @return the zero-based selected section of the live item, or -1.
     */
    int sectionIndex() {
        LivePanel lp = mainPanel.getLivePanel();
        Displayable d = lp.getDisplayable();
        // The slide previews number their slides from 1, with 0 or -1 for none.
        if (d instanceof PresentationDisplayable) {
            return Math.max(-1, lp.getPresentationPanel().getPresentationPreview().getSelectedIndex() - 1);
        }
        if (d instanceof PdfDisplayable) {
            return Math.max(-1, lp.getPdfPanel().getPresentationPreview().getSelectedIndex() - 1);
        }
        if (d instanceof ImageGroupDisplayable) {
            return Math.max(-1, lp.getImageGroupPanel().getPresentationPreview().getSelectedIndex() - 1);
        }
        if (d instanceof TextDisplayable) {
            return lp.getLyricsPanel().getCurrentIndex();
        }
        return -1;
    }

    /**
     * @return the number of sections or slides in the live item.
     */
    int sectionCount() {
        LivePanel lp = mainPanel.getLivePanel();
        Displayable d = lp.getDisplayable();
        if (d instanceof PresentationDisplayable) {
            return lp.getPresentationPanel().getPresentationPreview().size();
        }
        if (d instanceof PdfDisplayable) {
            return lp.getPdfPanel().getPresentationPreview().size();
        }
        if (d instanceof ImageGroupDisplayable) {
            return lp.getImageGroupPanel().getPresentationPreview().size();
        }
        if (d instanceof TextDisplayable) {
            return lp.getLyricsPanel().getLyricsList().getItems().size();
        }
        return 0;
    }

    /**
     * @return the displayables that item ids must be kept for.
     */
    List<Displayable> displayablesInUse() {
        List<Displayable> inUse = new ArrayList<>();
        for (IndexedDisplayable item : mainPanel.getSchedulePanel().getScheduleList().getItems()) {
            inUse.add(item.displayable());
        }
        inUse.add(mainPanel.getLivePanel().getDisplayable());
        inUse.add(mainPanel.getPreviewPanel().getDisplayable());
        return inUse;
    }
}
