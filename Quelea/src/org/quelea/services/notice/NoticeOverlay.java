/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.services.notice;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import org.quelea.services.notice.NoticeDrawer.NoticePosition;
import org.quelea.services.utils.QueleaProperties;

/**
 * The notice overlay, just used for displaying any notice text that might
 * appear. Little more than a stack pane at present.
 * <p/>
 * @author Michael
 */
public class NoticeOverlay extends StackPane {

    public NoticeOverlay() {
        if (QueleaProperties.get().getNoticePosition()==NoticePosition.TOP) {
            StackPane.setAlignment(this, Pos.TOP_CENTER);
            setAlignment(Pos.TOP_CENTER);
        } else {
            StackPane.setAlignment(this, Pos.BOTTOM_CENTER);
            setAlignment(Pos.BOTTOM_CENTER);
        }
    }
}
