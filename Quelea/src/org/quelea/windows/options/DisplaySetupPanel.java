package org.quelea.windows.options;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import org.quelea.utils.QueleaProperties;

/**
 *
 * @author Michael
 */
public class DisplaySetupPanel extends JPanel {

    private SingleDisplayPanel monitorPanel, projectorPanel;

    public DisplaySetupPanel() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 30, 0));
        monitorPanel = new SingleDisplayPanel("Control screen:", "icons/monitor.png", false);
        mainPanel.add(monitorPanel);

        projectorPanel = new SingleDisplayPanel("Projector screen:", "icons/projector.png", true);
        mainPanel.add(projectorPanel);
        syncScreens();
        add(mainPanel, BorderLayout.CENTER);
    }

    public final void syncScreens() {
        monitorPanel.setScreen(QueleaProperties.get().getControlScreen());
        projectorPanel.setScreen(QueleaProperties.get().getProjectorScreen());
    }

    public int getMonitorDisplay() {
        return monitorPanel.getOutputDisplay();
    }

    public int getProjectorDisplay() {
        return projectorPanel.getOutputDisplay();
    }

}
