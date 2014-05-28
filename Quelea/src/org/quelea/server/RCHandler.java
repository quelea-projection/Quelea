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
package org.quelea.server;

import javafx.application.Platform;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;

/**
 * Handles the RemoteControlServer commands.
 *
 * @author Ben Goodwin
 */
class RCHandler {

    static void logo() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleLogo();
            }
        });
    }

    static void black() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleBlack();
            }
        });
    }

    static void clear() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleClear();
            }
        });

    }

    static void next() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
            }
        });
    }

    static void prev() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
            }
        });

    }

    static void nextItem() {
        final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
        int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
        current++;
        System.out.println("" + current);
        if (current < p.getSchedulePanel().getScheduleList().getItems().size()) {
            p.getPreviewPanel().setDisplayable(p.getSchedulePanel().getScheduleList().getItems().get(current), 0);
        } else {
            p.getPreviewPanel().setDisplayable(p.getSchedulePanel().getScheduleList().getItems().get(p.getSchedulePanel().getScheduleList().getItems().size() - 1), 0);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                p.getPreviewPanel().goLive();
            }
        });
    }

    static void prevItem() {
        final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
        int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
        current--;
        System.out.println("" + current);
        if (current > 0) {
            p.getPreviewPanel().setDisplayable(p.getSchedulePanel().getScheduleList().getItems().get(current), 0);
        } else {
            p.getPreviewPanel().setDisplayable(p.getSchedulePanel().getScheduleList().getItems().get(0), 0);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                p.getPreviewPanel().goLive();
            }
        });
    }
}
