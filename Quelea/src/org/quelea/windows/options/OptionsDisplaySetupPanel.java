package org.quelea.windows.options;

import org.quelea.Application;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import org.quelea.windows.main.LyricWindow;
import org.quelea.windows.main.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 * @author Michael
 */
public class OptionsDisplaySetupPanel extends JPanel implements PropertyPanel {

    private final SingleDisplayPanel monitorPanel, projectorPanel;

    /**
     * Create a new display setup panel.
     */
    public OptionsDisplaySetupPanel() {
        setName("Display");
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 30, 0));
        monitorPanel = new SingleDisplayPanel("Control screen:", "icons/monitor.png", false);
        mainPanel.add(monitorPanel);
        projectorPanel = new SingleDisplayPanel("Projector screen:", "icons/projector.png", true);
        mainPanel.add(projectorPanel);
        readProperties();
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * @inheritDoc
     */
    public final void readProperties() {
        monitorPanel.update();
        projectorPanel.update();
        monitorPanel.setScreen(QueleaProperties.get().getControlScreen());
        projectorPanel.setScreen(QueleaProperties.get().getProjectorScreen());
    }

    /**
     * @inheritDoc
     */
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        MainWindow mainWindow = Application.get().getMainWindow();
        LyricWindow lyricWindow = Application.get().getLyricWindow();
        props.setControlScreen(getControlDisplay());
        props.setProjectorScreen(getProjectorDisplay());

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        if(getProjectorDisplay() == -1) {
            if(lyricWindow != null) {
                lyricWindow.setVisible(false);
            }
        }
        else {
            if(lyricWindow == null) {
                lyricWindow = new LyricWindow(gds[getProjectorDisplay()].getDefaultConfiguration().getBounds());
            }
            lyricWindow.setVisible(true);
            lyricWindow.setArea(gds[getProjectorDisplay()].getDefaultConfiguration().getBounds());
        }
        if(!Utils.isFrameOnScreen(mainWindow, getControlDisplay())) {
            Utils.centreOnMonitor(mainWindow, getControlDisplay());
        }
    }

    /**
     * Get the display that the control window should be sent to.
     * @return the display that the control window should be sent to.
     */
    public int getControlDisplay() {
        return monitorPanel.getOutputDisplay();
    }

    /**
     * Get the display that the projector window should be sent to.
     * @return the display that the projector window should be sent to.
     */
    public int getProjectorDisplay() {
        return projectorPanel.getOutputDisplay();
    }

}
