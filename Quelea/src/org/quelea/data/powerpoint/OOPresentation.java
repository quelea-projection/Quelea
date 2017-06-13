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
package org.quelea.data.powerpoint;

import com.sun.star.animations.XAnimationNode;
import com.sun.star.awt.PosSize;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.document.XEventBroadcaster;
import com.sun.star.document.XEventListener;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.presentation.XPresentation;
import com.sun.star.presentation.XPresentation2;
import com.sun.star.presentation.XPresentationSupplier;
import com.sun.star.presentation.XSlideShowController;
import com.sun.star.presentation.XSlideShowListener;
import com.sun.star.sdbc.SQLException;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ooo.connector.BootstrapSocketConnector;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;

/**
 * A presentation to be displayed using the openoffice API. This requries
 * openoffice to be installed.
 *
 * @author Michael
 */
public class OOPresentation implements XEventListener {

    private static final Logger LOGGER = Logger.getLogger(OOPresentation.class.getName());
    private static XComponentContext xOfficeContext;
    private static boolean init;
    private XPresentation2 xPresentation;
    private XSlideShowController controller;
    private XComponent doc;
    private boolean disposed;
    private List<SlideChangedListener> slideListeners;

    /**
     * Initialise the library - this involves connecting to openoffice to
     * initialise the office context object (which is used to create
     * presentations.)
     *
     * @param ooPath the path to the the "program" folder inside the openoffice
     * directory.
     * @return true if successfully initialised, false if an error occurs.
     */
    public static boolean init(String ooPath) {
        try {
            xOfficeContext = Helper.connect(ooPath);
            init = true;
            LOGGER.log(Level.INFO, "Openoffice initialised ok");
            return true;
        } catch (BootstrapException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't connect to openoffice instance", ex);
            return false;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Couldn't connect to openoffice instance", ex);
            return false;
        }

    }

    public static void closeOOApp() {
        Helper.dispose();
    }

    /**
     * Determine if the library is initialised.
     *
     * @return true if it's already initialised, false otherwise.
     */
    public static boolean isInit() {
        return init;
    }

    /**
     * Create a new presentation from a particular file. This must be the path
     * to a presentation file that openoffice supports - so either odp, ppt or
     * pptx at the time of writing. Note that the static init() method on this
     * class must be called successfully before attempting to create any
     * presentation objects, otherwise an IllegalStateException will be thrown.
     *
     * @param file the path to the presentation file.
     * @throws Exception if something goes wrong creating the presentation.
     */
    public OOPresentation(String file) throws Exception {
        if (!init) {
            LOGGER.log(Level.SEVERE, "BUG: Tried to create OOPresentation before it was initialised");
            throw new IllegalStateException("I'm not initialised yet! init() needs to be called before creating presentations.");
        }
        slideListeners = new ArrayList<>();
        File sourceFile = new File(file);
        StringBuilder sURL = new StringBuilder("file:///");
        sURL.append(sourceFile.getCanonicalPath().replace('\\', '/'));
        PropertyValue[] props = new PropertyValue[1];
        props[0] = new PropertyValue();
        props[0].Name = "Silent";
        props[0].Value = true;

        doc = Helper.createDocument(xOfficeContext, sURL.toString(), "_blank", 0, props);
        XModel xModel = UnoRuntime.queryInterface(XModel.class, doc);
        XEventBroadcaster xDocEventBroadcaster = UnoRuntime.queryInterface(com.sun.star.document.XEventBroadcaster.class, xModel);
        xDocEventBroadcaster.addEventListener(this);
        xModel.getCurrentController().getFrame().getContainerWindow().setPosSize(0, 0, 1, 1, PosSize.SIZE);
        xModel.getCurrentController().getFrame().getContainerWindow().setVisible(false);
        XPresentationSupplier xPresSupplier = UnoRuntime.queryInterface(XPresentationSupplier.class, doc);
        XPresentation xPresentation_ = xPresSupplier.getPresentation();
        xPresentation = UnoRuntime.queryInterface(XPresentation2.class, xPresentation_);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                checkDisposed();
            }
        });

    }

    /**
     * Add a slide listener to this presentation.
     *
     * @param listener the listener to add.
     */
    public void setSlideListener(SlideChangedListener listener) {
        slideListeners.clear();
        slideListeners.add(listener);
    }

    /**
     * Start the presentation, displaying it in a full screen window.
     *
     * @param display the 0 based index of the display to display the
     * presentation on.
     */
    public void start(int display) {
        if (display < 0) {
            LOGGER.log(Level.INFO, "Not starting presentation, negative display selected");
            return;
        }
        display++; //Openoffice requires base 1, we want base 0.
        try {
            xPresentation.setPropertyValue("Display", display);
            xPresentation.setPropertyValue("IsAutomatic", true);
            xPresentation.setPropertyValue("StartWithNavigator", true);
            if (QueleaProperties.get().getOOPresOnTop()) {
                xPresentation.setPropertyValue("IsAlwaysOnTop", true);
            }
        } catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException ex) {
            LOGGER.log(Level.SEVERE, "Error setting presentation properties", ex);
        }
        if (!xPresentation.isRunning()) {
            xPresentation.start();
        }
        while (controller == null) { //Block until we get a controller.
            controller = xPresentation.getController();
            Utils.sleep(50);
        }
        controller.addSlideShowListener(new XSlideShowListener() {
            @Override
            public void paused() {
            }

            @Override
            public void resumed() {
            }

            @Override
            public void slideTransitionStarted() {
                for (SlideChangedListener listener : slideListeners) {
                    listener.slideChanged(controller.getCurrentSlideIndex());
                }
            }

            @Override
            public void slideTransitionEnded() {
//                System.out.println("slide transition end");
            }

            @Override
            public void slideAnimationsEnded() {
//                System.out.println("slide animations end");
            }

            @Override
            public void slideEnded(boolean bln) {
//                System.out.println("slide end");
            }

            @Override
            public void hyperLinkClicked(String string) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void beginEvent(XAnimationNode xan) {
//                System.out.println("begin event");
            }

            @Override
            public void endEvent(XAnimationNode xan) {
//                System.out.println("end event");
            }

            @Override
            public void repeat(XAnimationNode xan, int i) {
//                System.out.println("repeat");
            }

            @Override
            public void disposing(EventObject eo) {
//                System.out.println("disposing");
            }
        });
    }

    /**
     * Determine if this presentation is running.
     *
     * @return true if its running, false otherwise.
     */
    public boolean isRunning() {
        if (xPresentation != null) {
            return xPresentation.isRunning();
        } else {
            return false;
        }
    }

    /**
     * Stop the presentation if it's running.
     */
    public void stop() {
        if (controller != null) {
            xPresentation.end();
            controller.deactivate();
            controller = null;
        }
    }

    /**
     * Advance forward one step. This will involve either advancing to the next
     * animation in the slide, or advancing to the next slide (depending on the
     * presentation.)
     */
    public void goForward() {
        if (controller != null && controller.getNextSlideIndex() != -1) {
            int idx = controller.getCurrentSlideIndex();
            controller.gotoNextEffect();
            Utils.sleep(50);
            if(controller.getCurrentSlideIndex()!=idx) {
                controller.gotoSlideIndex(controller.getCurrentSlideIndex());
            }
            Utils.sleep(50);
        }
    }

    /**
     * Go backwards one step.
     */
    public void goBack() {
        if (controller != null) {
            controller.gotoPreviousEffect();
            Utils.sleep(50);
        }
    }

    /**
     * Navigate directly to the slide at the given index.
     *
     * @param index the index of the slide to navigate to.
     */
    public void gotoSlide(int index) {
        if (controller != null) {
            controller.gotoSlideIndex(index);
        }
    }

    /**
     * Clear up this presentation, releasing all the resources associated with
     * it (all the underlying OO library objects.) This must be called before
     * this presentation is eligible for GC to prevent memory leaks. In the
     * event that it isn't called before it's garbage collected, a warning will
     * be printed since this should be classed as a bug.
     */
    public void dispose() {
        if (!disposed) {
            if (controller != null && controller.isActive()) {
                controller.deactivate();
            }
            if (xPresentation != null) {
                xPresentation.end();
            }
            if (doc != null) {
                XCloseable xcloseable = UnoRuntime.queryInterface(XCloseable.class, doc);
                try {
                    if (xcloseable != null) {
                        xcloseable.close();
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Error occured when closing presentation", ex);
                }
                doc.dispose();
            }
            disposed = true;
        }
    }

    /**
     * If the object hasn't been disposed, clean it up at this point and display
     * a warning.
     *
     * @throws Throwable if something goes wrong in finalisation.
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        checkDisposed();
    }

    /**
     * If the object hasn't been disposed, clean it up at this point and display
     * a warning.
     */
    private void checkDisposed() {
        if (!disposed) {
            LOGGER.log(Level.WARNING, "BUG: Presentation was not correctly disposed!");
            dispose();
        }
    }

    @Override
    public void disposing(EventObject ev) {
        //Nothing needed here
    }

    @Override
    public void notifyEvent(com.sun.star.document.EventObject ev) {
        XModel xModel = UnoRuntime.queryInterface(XModel.class, ev.Source);
        XController xController = xModel.getCurrentController();
        xController.getFrame().getContainerWindow().setEnable(false);
    }

    /**
     * Helper methods for doing openoffice specific stuff.
     */
    private static class Helper {

        private static XComponent bridgeComponent;
        private static XBridge bridge;
        private static XConnection connection;
        public static final String DEFAULT_HOST = "localhost";
        public static final int DEFAULT_PORT = 8100;
        public static final String RUN_ARGS = "socket,host=" + DEFAULT_HOST + ",port=" + DEFAULT_PORT + ",tcpNoDelay=1";

        /**
         * Connect to an office, if no office is running a new instance is
         * started. A new connection is established and the service manger from
         * the running office is returned.
         *
         * @param path the path to the openoffice install.
         */
        private static XComponentContext connect(String path) throws BootstrapException, Exception {
            File progPath = new File(path, "program");
            xOfficeContext = BootstrapSocketConnector.bootstrap(progPath.getAbsolutePath());
            XComponentContext localContext = Bootstrap.createInitialComponentContext(null);
            XMultiComponentFactory localServiceManager = localContext.getServiceManager();
            XConnector connector = UnoRuntime.queryInterface(XConnector.class,
                    localServiceManager.createInstanceWithContext("com.sun.star.connection.Connector",
                            localContext));
            connection = connector.connect(RUN_ARGS);
            XBridgeFactory bridgeFactory = UnoRuntime.queryInterface(XBridgeFactory.class,
                    localServiceManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", localContext));
            bridge = bridgeFactory.createBridge("", "urp", connection, null);
            bridgeComponent = UnoRuntime.queryInterface(XComponent.class, bridge);
            bridgeComponent.addEventListener(new com.sun.star.lang.XEventListener() {
                @Override
                public void disposing(EventObject eo) {
                }
            });
            return xOfficeContext;

        }

        public static void dispose() {
            try {
                if (bridgeComponent != null) {
                    connection.flush();;
                    connection.close();
                    bridgeComponent.dispose();
                    bridgeComponent = null;
                    try {
                        Process p = Runtime.getRuntime().exec("taskkill /F /IM soffice.bin");
                        //@todo the only way to kill this process. to be added other system support
                    } catch (IOException e) {
                    }

                }
            } catch (DisposedException ex) {
                throw new RuntimeException(ex.getMessage());
            } catch (com.sun.star.io.IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }

        /**
         * Creates and instantiates a new document
         *
         * @throws Exception if something goes wrong creating the document.
         */
        private static XComponent createDocument(XComponentContext xOfficeContext, String sURL, String sTargetFrame, int nSearchFlags, PropertyValue[] aArgs) throws Exception {
            XComponentLoader aLoader = UnoRuntime.queryInterface(XComponentLoader.class, xOfficeContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", xOfficeContext));
            XComponent xComponent = UnoRuntime.queryInterface(XComponent.class, aLoader.loadComponentFromURL(sURL, sTargetFrame, nSearchFlags, aArgs));

            if (xComponent == null) {
                throw new Exception("Could not create document: " + sURL);
            }
            return xComponent;
        }
    }
}
