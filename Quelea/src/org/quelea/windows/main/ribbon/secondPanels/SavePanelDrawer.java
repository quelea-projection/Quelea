package org.quelea.windows.main.ribbon.secondPanels;

import java.awt.Font;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author Michael
 */
public class SavePanelDrawer implements SecondPanelDrawer {

    @Override
    public void draw(JPanel panel) {
        panel.removeAll();
        Graphics2D graphics = (Graphics2D) panel.getGraphics();
        graphics.setFont(new Font("Verdana", 0, 20));
        graphics.drawString("Save the schedule", 20, 40);
    }

}
