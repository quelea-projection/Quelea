package org.quelea.windows.main.ribbon.secondPanels;

import java.awt.Font;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * PanelDrawer...
 * @author Michael
 */
public class SaveAsPanelDrawer implements SecondPanelDrawer {

    @Override
    public void draw(JPanel panel) {
        panel.removeAll();
        Graphics2D graphics = (Graphics2D) panel.getGraphics();
        graphics.clearRect(0, 0, panel.getWidth(), panel.getHeight());
        graphics.setFont(new Font("Verdana", 0, 20));
        graphics.drawString("Save the schedule", 20, 40);
        graphics.setFont(new Font("Verdana", 0, 14));
        graphics.drawString("This saves the current schedule to", 20, 60);
        graphics.drawString("disk as a particular file.", 20, 75);
    }

}
