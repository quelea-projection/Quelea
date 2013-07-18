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

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;
import com.sun.jna.platform.win32.WinDef;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quelea.services.utils.LoggerUtils;

/**
 *
 * @author Michael
 */
public class PowerpointSlideShowRunner {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private static boolean supported = false;

    static {
//        try {
//            System.setProperty("java.library.path", "lib");
//            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
//            fieldSysPath.setAccessible(true);
//            fieldSysPath.set(null, null);
//            app = new ActiveXComponent("PowerPoint.Application");
//            supported = true;
//        }
//        catch(Exception ex) {
//            LOGGER.log(Level.INFO, "Couldn't init powerpoint", ex);
//        }
    }

    public static boolean isSupported() {
        return supported;
    }

    public static interface SlideChangeListener {

        void slideChanged(int oldSlide, int newSlide);
    }

    public static interface PresentationFinishedListener {

        void presentationFinished();
    }
    private int x = 1900;
    private int y = 0;
    private int width = 300;
    private int height = 300;
    private static ActiveXComponent app;
    private Dispatch presentation;
    private Dispatch settings;
    private Dispatch slideView;
    private boolean isRunning;
    private String fileLocation;
    private boolean init;
    private int numSlides;
    private int currentSlide;
    private WinDef.HWND presHandle;
    private WinDef.HWND slideHandle;
    private boolean loop;
    private List<PowerpointSlideShowRunner.SlideChangeListener> slideAdvanceListeners = new ArrayList<>();
    private List<PowerpointSlideShowRunner.PresentationFinishedListener> presentationFinishedListeners = new ArrayList<>();

    public PowerpointSlideShowRunner(String fileLocation) {
        this.fileLocation = fileLocation;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                PowerpointSlideShowRunner.this.destroy();
            }
        });
    }

    public void init() {
        if(!init) {
            isRunning = false;
            ComThread.runAndWait(new Runnable() {
                @Override
                public void run() {
                    long millis = System.currentTimeMillis();
                    Dispatch presentations = app.getProperty("Presentations").toDispatch();
//                    NativeWindowHandler.resetIgnore();
                    System.out.println("Time3: " + (System.currentTimeMillis() - millis));
                    presentation = Dispatch.call(presentations, "Open", fileLocation).toDispatch();
                    System.out.println("Time4: " + (System.currentTimeMillis() - millis));
                    presHandle = NativeWindowHandler.getPowerpointWindow();
                    System.out.println("Time5: " + (System.currentTimeMillis() - millis));
//                    app.setProperty("WindowState", new Variant(2));
                    settings = Dispatch.get(presentation, "SlideShowSettings").toDispatch();
                    EnumVariant slides = new EnumVariant(Dispatch.get(presentation, "Slides").toDispatch());
                    numSlides = 0;
                    while(slides.hasMoreElements()) {
                        slides.nextElement();
                        numSlides++;
                    }
                    int loopInt = Dispatch.get(settings, "LoopUntilStopped").getInt();
                    loop = loopInt == -1;
                }
            });
            init = true;
        }
    }

    public boolean isInit() {
        return init;
    }

    public void setPos(double x, double y, double width, double height) {
        this.x = (int) (x * 0.75);
        this.y = (int) (y * 0.75);
        this.width = (int) (width * 0.75);
        this.height = (int) (height * 0.75);
    }

    public boolean isLoop() {
        return loop;
    }

    public void goForward() {
        ComThread.runAndWait(new Runnable() {
            @Override
            public void run() {
                Dispatch.call(slideView, "Next");
            }
        });
    }

    public void goBack() {
        ComThread.runAndWait(new Runnable() {
            @Override
            public void run() {
                Dispatch.call(slideView, "Previous");
            }
        });
    }

    public void goToSlide(final int slide) {
        ComThread.runAndWait(new Runnable() {
            @Override
            public void run() {
                Dispatch.call(slideView, "GotoSlide", slide);
            }
        });
    }

    public void stop() {
        ComThread.runAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    Dispatch.call(slideView, "Exit");
                }
                catch(Exception ex) {
                }
                isRunning = false;
            }
        });
    }

    public void destroy() {
        init = false;
        NativeWindowHandler.kill(slideHandle);
        NativeWindowHandler.kill(presHandle);
    }

    public void run() {
        if(isRunning) {
            return;
        }
        isRunning = true;

        ComThread.runAndWait(new Runnable() {
            @Override
            public void run() {
                NativeWindowHandler.resetIgnore();
                Dispatch.call(settings, "Run");
                EnumVariant slideWindows = new EnumVariant(app.getProperty("SlideShowWindows").toDispatch());
                Dispatch slideWindow = slideWindows.nextElement().toDispatch();
                Dispatch.call(slideWindow, "Activate");
                Dispatch.put(slideWindow, "Left", x);
                Dispatch.put(slideWindow, "Top", y);
                Dispatch.put(slideWindow, "Width", width);
                Dispatch.put(slideWindow, "Height", height);
                slideHandle = NativeWindowHandler.getSlideshowWindow();
                NativeWindowHandler.forceToFront(slideHandle);
                slideView = Dispatch.call(slideWindow, "View").toDispatch();
                Dispatch.call(slideView, "First");
            }
        });

        new Thread() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException ex) {
                    }
                    int nextCurrentSlide = 0;
                    try {
                        nextCurrentSlide = ComThread.runAndWait(new Callable<Integer>() {
                            @Override
                            public Integer call() throws Exception {
                                return Dispatch.get(slideView, "CurrentShowPosition").getInt();
                            }
                        });
                    }
                    catch(Exception ex) {
                        LOGGER.log(Level.INFO, "Breaking from PP monitor loop");
                        break;
                    }
                    int oldCurrentSlide = currentSlide;
                    if(oldCurrentSlide != nextCurrentSlide && nextCurrentSlide != numSlides + 1) {
                        currentSlide = nextCurrentSlide;
                        fireSlideAdvancedListeners(oldCurrentSlide, currentSlide);
                    }
                    if(nextCurrentSlide == numSlides + 1) {
                        ComThread.runAndWait(new Runnable() {
                            @Override
                            public void run() {
                                app.invoke("Quit", new Variant[0]);
                            }
                        });
                        firePresentationFinishedListeners();
                        break;
                    }
                }
            }
        }.start();
    }

    private void fireSlideAdvancedListeners(int oldSlide, int newSlide) {
        for(PowerpointSlideShowRunner.SlideChangeListener listener : slideAdvanceListeners) {
            listener.slideChanged(oldSlide, newSlide);
        }
    }

    public void addSlideAdvanceListener(PowerpointSlideShowRunner.SlideChangeListener listener) {
        slideAdvanceListeners.add(listener);
    }

    private void firePresentationFinishedListeners() {
        for(PowerpointSlideShowRunner.PresentationFinishedListener listener : presentationFinishedListeners) {
            listener.presentationFinished();
        }
    }

    public void addPresentationFinishedListener(PowerpointSlideShowRunner.PresentationFinishedListener listener) {
        presentationFinishedListeners.add(listener);
    }
}
