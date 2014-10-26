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

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.scene.layout.Background;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.multimedia.VLCWindow;

/**
 * Handles the RemoteControlServer commands.
 *
 * @author Ben Goodwin
 */
public class RCHandler {

    private static ArrayList<String> devices = new ArrayList<String>();

    public static void logo() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleLogo();
            }
        });
    }

    public static void black() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleBlack();
            }
        });
    }

    public static void clear() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().toggleClear();
            }
        });

    }

    public static void next() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().advance();
            }
        });
    }

    public static void prev() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().previous();
            }
        });

    }

    public static void nextItem() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
                int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
                current++;
                p.getSchedulePanel().getScheduleList().getSelectionModel().clearSelection();
                if (current < p.getSchedulePanel().getScheduleList().getItems().size()) {
                    p.getSchedulePanel().getScheduleList().getSelectionModel().select(current);
                } else {
                    p.getSchedulePanel().getScheduleList().getSelectionModel().select(p.getSchedulePanel().getScheduleList().getItems().size() - 1);
                }
                p.getPreviewPanel().goLive();
            }
        });
    }

    public static void prevItem() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final MainPanel p = QueleaApp.get().getMainWindow().getMainPanel();
                int current = p.getSchedulePanel().getScheduleList().getItems().indexOf(p.getLivePanel().getDisplayable());
                current--;
                p.getSchedulePanel().getScheduleList().getSelectionModel().clearSelection();
                if (current > 0) {
                    p.getSchedulePanel().getScheduleList().getSelectionModel().select(current);
                } else {
                    p.getSchedulePanel().getScheduleList().getSelectionModel().select(0);
                }
                p.getPreviewPanel().goLive();
            }
        });
    }

    public static int currentLyricSection() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().getCurrentIndex();
    }

    public static void setLyrics(final String index) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLyricsPanel().select(Integer.parseInt(index.substring(2)));
            }
        });
    }

    public static boolean authenticate(final String password) {
        return password.equals(QueleaProperties.get().getRemoteControlPassword());
    }

    public static void addDevice(String ip) {
        devices.add(ip);
    }

    public static boolean isLoggedOn(String ip) {
        boolean found = false;
        for (String s : devices) {
            if (s.equals(ip)) {
                found = true;
            }
        }
        return found;
    }

    public static void logout(String ip) {
        devices.remove(ip);
    }

    public static void logAllOut() {
        devices.clear();
    }

    public static boolean getLogo() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getLogoed();
    }

    public static boolean getBlack() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getBlacked();
    }

    public static boolean getClear() {
        return QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getCleared();
    }

    public static String videoStatus() {
        if (VLCWindow.INSTANCE.isPlaying()) {
            return LabelGrabber.INSTANCE.getLabel("pause");
        } else {
            return LabelGrabber.INSTANCE.getLabel("play");
        }
    }

    public static void play() {
        if (VLCWindow.INSTANCE.isPlaying()) {
            VLCWindow.INSTANCE.pause();
        } else {
            VLCWindow.INSTANCE.play();
        }
    }
}
