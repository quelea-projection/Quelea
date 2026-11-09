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
import org.quelea.data.displayable.Displayable;
import org.quelea.data.displayable.ImageGroupDisplayable;
import org.quelea.data.displayable.IndexedDisplayable;
import org.quelea.data.displayable.PdfDisplayable;
import org.quelea.data.displayable.PresentationDisplayable;
import org.quelea.windows.main.LivePanel;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.schedule.ScheduleList;
import org.quelea.windows.main.toolbars.MainToolbar;

/**
 * {@link QueleaControl} backed by Quelea's real user interface. Every call
 * runs on the FX thread through {@link FxCall}, so callers on network threads
 * never touch JavaFX directly and never wait for it longer than the timeout.
 */
final class FxQueleaControl implements QueleaControl {

    private final MainPanel mainPanel;
    private final MainToolbar toolbar;
    private final ItemIds ids;
    private final LiveReader reader;
    private final FxCall fx;

    FxQueleaControl(MainPanel mainPanel, MainToolbar toolbar, ItemIds ids, LiveReader reader, FxCall fx) {
        this.mainPanel = mainPanel;
        this.toolbar = toolbar;
        this.ids = ids;
        this.reader = reader;
        this.fx = fx;
    }

    @Override
    public LiveState getState() {
        return fx.call(reader::state);
    }

    @Override
    public ScheduleSnapshot getSchedule() {
        return fx.call(reader::schedule);
    }

    @Override
    public LiveState next() {
        return command(() -> mainPanel.getLivePanel().advance());
    }

    @Override
    public LiveState previous() {
        return command(() -> mainPanel.getLivePanel().previous());
    }

    @Override
    public LiveState gotoSection(int index) {
        return command(() -> {
            LivePanel lp = mainPanel.getLivePanel();
            Displayable d = lp.getDisplayable();
            int count = reader.sectionCount();
            if (d == null || index < 0 || index >= count) {
                throw new IllegalArgumentException("Section " + index + " is out of range (0 to " + (count - 1) + ")");
            }
            // The slide previews number their slides from 1.
            if (d instanceof PresentationDisplayable) {
                lp.getPresentationPanel().getPresentationPreview().select(index + 1);
            } else if (d instanceof PdfDisplayable) {
                lp.getPdfPanel().getPresentationPreview().select(index + 1);
            } else if (d instanceof ImageGroupDisplayable) {
                lp.getImageGroupPanel().getPresentationPreview().select(index + 1);
            } else {
                lp.getLyricsPanel().select(index);
            }
        });
    }

    @Override
    public LiveState nextItem() {
        return command(() -> {
            ScheduleList schedule = scheduleList();
            int size = schedule.getItems().size();
            if (size == 0) {
                return;
            }
            int current = indexOf(schedule.getItems(), mainPanel.getLivePanel().getDisplayable());
            goLiveAt(schedule, Math.min(current + 1, size - 1));
        });
    }

    @Override
    public LiveState previousItem() {
        return command(() -> {
            ScheduleList schedule = scheduleList();
            if (schedule.getItems().isEmpty()) {
                return;
            }
            int current = indexOf(schedule.getItems(), mainPanel.getLivePanel().getDisplayable());
            goLiveAt(schedule, Math.max(current - 1, 0));
        });
    }

    @Override
    public LiveState goLive(String itemId) {
        return command(() -> {
            ScheduleList schedule = scheduleList();
            int index = indexOf(schedule.getItems(), ids.find(itemId));
            if (index < 0) {
                throw new NoSuchItemException(itemId);
            }
            goLiveAt(schedule, index);
        });
    }

    @Override
    public LiveState setScreen(Boolean logo, Boolean black, Boolean clear) {
        return command(() -> {
            LivePanel lp = mainPanel.getLivePanel();
            // The toggles do nothing when their button is disabled, for
            // example clear on an item that cannot be cleared.
            if (logo != null && logo != lp.getLogoed()) {
                lp.toggleLogo();
            }
            if (black != null && black != lp.getBlacked()) {
                lp.toggleBlack();
            }
            if (clear != null && clear != lp.getCleared()) {
                lp.toggleClear();
            }
        });
    }

    @Override
    public LiveState setRecording(boolean recording) {
        return command(() -> {
            boolean current = toolbar.recordingProperty().get();
            if (recording && !current) {
                toolbar.startRecording();
            } else if (!recording && current) {
                toolbar.stopRecording();
            }
        });
    }

    private LiveState command(Runnable action) {
        return fx.call(() -> {
            action.run();
            return reader.state();
        });
    }

    private ScheduleList scheduleList() {
        return mainPanel.getSchedulePanel().getScheduleList();
    }

    private void goLiveAt(ScheduleList schedule, int index) {
        schedule.getSelectionModel().clearSelection();
        schedule.getSelectionModel().select(index);
        mainPanel.getPreviewPanel().goLive();
    }

    /**
     * Find a displayable in the schedule by identity.
     *
     * @return its position, or -1.
     */
    private static int indexOf(List<IndexedDisplayable> items, Displayable d) {
        if (d == null) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).displayable() == d) {
                return i;
            }
        }
        return -1;
    }
}
