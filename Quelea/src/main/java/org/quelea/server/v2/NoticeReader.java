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
import java.util.List;
import javafx.scene.paint.Color;
import org.quelea.services.notice.Notice;
import org.quelea.services.notice.NoticeDialog;

/**
 * Reads Quelea's notices for remotes. Must be used on the FX thread, where
 * the notice lists live.
 */
final class NoticeReader {

    private final NoticeDialog dialog;
    private final NoticeIds ids;

    NoticeReader(NoticeDialog dialog, NoticeIds ids) {
        this.dialog = dialog;
        this.ids = ids;
    }

    /**
     * @return the notices being shown and the saved ones.
     */
    Notices notices() {
        List<Notices.Saved> saved = new ArrayList<>();
        for (Notice notice : dialog.getTemplates().getItems()) {
            int times = notice.getOriginalTimes();
            saved.add(new Notices.Saved(notice.getText(), times == Integer.MAX_VALUE ? 0 : times,
                    hex(notice.getColor().getColor())));
        }
        return new Notices(active(), saved);
    }

    /**
     * @return the notices being shown, in the order they were added.
     */
    List<Notices.Active> active() {
        List<Notices.Active> active = new ArrayList<>();
        for (Notice notice : dialog.getCurrentNotices()) {
            // Quelea counts down notices that run until removed too, from
            // Integer.MAX_VALUE: what they started with tells them apart.
            boolean untilRemoved = notice.getOriginalTimes() == Integer.MAX_VALUE;
            active.add(new Notices.Active(ids.idFor(notice), notice.getText(),
                    untilRemoved ? null : notice.getTimes(), hex(notice.getColor().getColor())));
        }
        return active;
    }

    /**
     * Forget the ids of notices that are no longer shown.
     */
    void pruneIds() {
        ids.retainOnly(dialog.getCurrentNotices());
    }

    /**
     * @param id a notice id.
     * @return the notice being shown with that id, or null.
     */
    Notice find(String id) {
        Notice notice = ids.find(id);
        return notice != null && dialog.getCurrentNotices().stream().anyMatch(n -> n == notice) ? notice : null;
    }

    NoticeDialog dialog() {
        return dialog;
    }

    static String hex(Color color) {
        return String.format("#%02x%02x%02x", Math.round(color.getRed() * 255),
                Math.round(color.getGreen() * 255), Math.round(color.getBlue() * 255));
    }
}
