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
package org.quelea.windows.presentation;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JNA stuff for mucking around with windows on a native level. Windows only,
 * and even then a hack that should be avoided really...
 * <p/>
 * @author Michael
 */
public class NativeWindowHandler {

    private static List<WinDef.HWND> ignore = new ArrayList<>();

    public static interface Callback {

        void ret(WinDef.HWND hwnd);
    }

    private static interface User32 extends StdCallLibrary {

        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);

        int GetWindowTextA(WinDef.HWND hWnd, byte[] lpString, int nMaxCount);

        WinDef.HWND GetForegroundWindow();

        boolean SetForegroundWindow(WinDef.HWND hWnd);

        boolean AttachThreadInput(int idAttach, int idAttachTo, boolean fAttach);

        int GetWindowThreadProcessId(WinDef.HWND hWnd, int pref);

        void SendMessageA(WinDef.HWND hWnd, int msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);
    }

    public static void resetIgnore() {
        ignore.clear();
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            int count = 0;

            @Override
            @SuppressWarnings("deprecation")
            public boolean callback(final WinDef.HWND hWnd, Pointer arg1) {
                final byte[] windowText = new byte[512];
                final byte[] x = new byte[1];
                Thread t = new Thread() {
                    public void run() {
                        user32.GetWindowTextA(hWnd, windowText, 512);
                        x[0] = 1;
                        synchronized(this) {
                            notify();
                        }
                    }
                };
                t.start();
                if(x[0] == 0) {
                    synchronized(this) {
                        try {
                            wait(1);
                        }
                        catch(InterruptedException ex) {
                            Logger.getLogger(NativeWindowHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if(x[0] == 0) {
                        t.stop();
                        return true;
                    }
                }
                String wText = Native.toString(windowText);

                if(wText.isEmpty()) {
                    return true;
                }
                ignore.add(hWnd);
                return true;
            }
        }, null);
    }
    private static WinDef.HWND powerRet = null;

    public static WinDef.HWND getPowerpointWindow() {
        return getWindow("Microsoft PowerPoint");
    }

    public static WinDef.HWND getSlideshowWindow() {
        return getWindow("PowerPoint Slide");
    }

    public static WinDef.HWND getWindow(final String name) {
        powerRet = null;
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            int count = 0;

            @Override
            @SuppressWarnings("deprecation")
            public boolean callback(final WinDef.HWND hWnd, Pointer arg1) {
                final byte[] windowText = new byte[512];
                final byte[] x = new byte[1];
                Thread t = new Thread() {
                    public void run() {
                        user32.GetWindowTextA(hWnd, windowText, 512);
                        x[0] = 1;
                        synchronized(this) {
                            notify();
                        }
                    }
                };
                t.start();
                if(x[0] == 0) {
                    synchronized(this) {
                        try {
                            wait(1);
                        }
                        catch(InterruptedException ex) {
                            Logger.getLogger(NativeWindowHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if(x[0] == 0) {
                        t.stop();
                        return true;
                    }
                }
                String wText = Native.toString(windowText);
                if(wText.isEmpty()) {
                    return true;
                }
                if(wText.contains(name)) {
                    if(!ignore.contains(hWnd)) {
                        powerRet = hWnd;
                        return true;
                    }
                }
                return true;
            }
        }, null);
        return powerRet;
    }

    public static void forceToFront(WinDef.HWND hwnd) {
        if(hwnd == null) {
            System.err.println("NULL HWND");
        }
        WinDef.HWND foreground = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.AttachThreadInput(User32.INSTANCE.GetWindowThreadProcessId(foreground, 0), User32.INSTANCE.GetWindowThreadProcessId(hwnd, 0), true);
        User32.INSTANCE.SetForegroundWindow(hwnd);
        User32.INSTANCE.SetForegroundWindow(foreground);
    }

    public static void kill(WinDef.HWND hwnd) {
        User32.INSTANCE.SendMessageA(hwnd, 0x10, new WinDef.WPARAM(), new WinDef.LPARAM());
    }
}
