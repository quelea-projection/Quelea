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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.main.toolbars.MainToolbar;

/**
 * Listens to Quelea's user interface and publishes a {@link RemoteEvent}
 * whenever something a remote shows changes, no matter whether the change
 * came from the mouse, a keyboard shortcut, v1 or v2.
 * <p>
 * Everything here runs on the FX thread: the listeners are FX listeners, and
 * {@link #install()} must be called on the FX thread.
 */
final class LiveStateWatcher {

    private final MainPanel mainPanel;
    private final MainToolbar toolbar;
    private final ItemIds ids;
    private final RemoteEventBus bus;
    private final AtomicLong scheduleRevision = new AtomicLong();
    private final LiveReader reader;

    private final ListChangeListener<IndexedDisplayable> scheduleListener = change -> scheduleChanged();
    private Displayable lastLive;
    private boolean settleScheduled;
    private String lastSectionItem;
    private int lastSectionIndex = Integer.MIN_VALUE;
    private boolean installed;
    private final NoticeReader notices;
    private boolean noticesScheduled;
    private List<Notices.Active> lastNotices = List.of();

    LiveStateWatcher(MainPanel mainPanel, MainToolbar toolbar, ItemIds ids, RemoteEventBus bus,
            NoticeReader notices) {
        this.notices = notices;
        this.mainPanel = mainPanel;
        this.toolbar = toolbar;
        this.ids = ids;
        this.bus = bus;
        this.reader = new LiveReader(mainPanel, toolbar, ids, scheduleRevision::get);
    }

    /**
     * @return the current schedule revision. Safe to call from any thread.
     */
    long scheduleRevision() {
        return scheduleRevision.get();
    }

    LiveReader reader() {
        return reader;
    }

    /**
     * Attach the listeners. Must be called on the FX thread, once.
     */
    void install() {
        if (installed) {
            return;
        }
        installed = true;
        LivePanel lp = mainPanel.getLivePanel();
        lp.addDisplayableListener(this::liveChanged);

        lp.getLyricsPanel().getLyricsList().getSelectionModel().selectedIndexProperty()
                .addListener((obs, oldIndex, newIndex) -> publishSection());
        lp.getPresentationPanel().getPresentationPreview().addSlideChangedListener(slide -> publishSection());
        lp.getPdfPanel().getPresentationPreview().addSlideChangedListener(slide -> publishSection());
        lp.getImageGroupPanel().getPresentationPreview().addSlideChangedListener(slide -> publishSection());

        ChangeListener<Boolean> screenListener = (obs, oldValue, newValue) -> bus.publish(
                new RemoteEvent.ScreenChanged(lp.getLogoed(), lp.getBlacked(), lp.getCleared()));
        lp.logoProperty().addListener(screenListener);
        lp.blackProperty().addListener(screenListener);
        lp.clearProperty().addListener(screenListener);

        toolbar.recordingProperty().addListener(
                (obs, oldValue, newValue) -> bus.publish(new RemoteEvent.RecordingChanged(newValue)));

        ScheduleList scheduleList = mainPanel.getSchedulePanel().getScheduleList();
        scheduleList.getItems().addListener(scheduleListener);
        scheduleList.itemsProperty().addListener((obs, oldItems, newItems) -> {
            moveScheduleListener(oldItems, newItems);
            scheduleChanged();
        });

        // The notice dialog rebuilds its list after every pass, clearing
        // and refilling it: report once the list has settled, and only
        // if something a remote shows actually changed.
        notices.dialog().getCurrentNotices().addListener((ListChangeListener<Object>) change -> {
            if (!noticesScheduled) {
                noticesScheduled = true;
                Platform.runLater(this::noticesSettled);
            }
        });

        lastLive = lp.getDisplayable();
    }

    private void noticesSettled() {
        noticesScheduled = false;
        notices.pruneIds();
        List<Notices.Active> now = notices.active();
        if (!now.equals(lastNotices)) {
            lastNotices = now;
            bus.publish(new RemoteEvent.NoticesChanged(now));
        }
    }

    private void moveScheduleListener(ObservableList<IndexedDisplayable> oldItems,
            ObservableList<IndexedDisplayable> newItems) {
        if (oldItems != null) {
            oldItems.removeListener(scheduleListener);
        }
        if (newItems != null) {
            newItems.addListener(scheduleListener);
        }
    }

    private void liveChanged(Displayable d) {
        // setDisplayable is also called when the same item is shown again,
        // for example to jump to a section: only a new item is a live.item.
        if (d != lastLive) {
            lastLive = d;
            bus.publish(new RemoteEvent.LiveItemChanged(
                    ids.idFor(d),
                    ItemText.title(d),
                    ItemTypes.of(d),
                    reader.sectionCount()));
        }
        publishSection();
    }

    private void publishSection() {
        Displayable live = mainPanel.getLivePanel().getDisplayable();
        if (live != lastLive) {
            // A new item is being loaded and its list fires selection events
            // before LivePanel reports the item. liveChanged publishes the
            // section right after live.item, so clients never see a section
            // of an item they have not been told about.
            return;
        }
        String itemId = ids.idFor(live);
        int index = reader.sectionIndex();
        if (index == lastSectionIndex && Objects.equals(itemId, lastSectionItem)) {
            return;
        }
        lastSectionItem = itemId;
        lastSectionIndex = index;
        bus.publish(new RemoteEvent.LiveSectionChanged(itemId, index));
    }

    private void scheduleChanged() {
        // The revision changes at once, so a change made right after this
        // one is checked against it.
        scheduleRevision.incrementAndGet();
        // Pruning ids and telling clients waits until the whole operation
        // is done. Moving an item swaps two list entries one at a time, and
        // in between the moved item is briefly not in the list at all:
        // pruning then would give it a new id.
        if (!settleScheduled) {
            settleScheduled = true;
            Platform.runLater(this::scheduleSettled);
        }
    }

    private void scheduleSettled() {
        settleScheduled = false;
        ids.retainOnly(reader.displayablesInUse());
        bus.publish(new RemoteEvent.ScheduleChanged(scheduleRevision.get()));
    }
}
