package org.quelea.windows.options;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Updatable;

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 * @author Michael
 */
public class DisplaySetupPanel extends JPanel implements Updatable {

    private final SingleDisplayPanel monitorPanel, projectorPanel;

    /**
     * Create a new display setup panel.
     */
    public DisplaySetupPanel() {
        setName("Display");
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 30, 0));
        monitorPanel = new SingleDisplayPanel("Control screen:", "icons/monitor.png", false);
        mainPanel.add(monitorPanel);
        projectorPanel = new SingleDisplayPanel("Projector screen:", "icons/projector.png", true);
        mainPanel.add(projectorPanel);
        update();
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Synchronise the panel with the current information in the properties file.
     */
    public final void update() {
        monitorPanel.update();
        projectorPanel.update();
        monitorPanel.setScreen(QueleaProperties.get().getControlScreen());
        projectorPanel.setScreen(QueleaProperties.get().getProjectorScreen());
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
