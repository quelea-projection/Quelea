package org.quelea.windows.main.ribbon.secondPanels;

import java.awt.Font;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * PanelDrawer...
 * @author Michael
 */
public class OptionsPanelDrawer implements SecondPanelDrawer {

    @Override
    public void draw(JPanel panel) {
        panel.removeAll();
        Graphics2D graphics = (Graphics2D) panel.getGraphics();
        graphics.clearRect(0, 0, panel.getWidth(), panel.getHeight());
        graphics.setFont(new Font("Verdana", 0, 20));
        graphics.drawString("Options", 20, 40);
        graphics.setFont(new Font("Verdana", 0, 14));
        graphics.drawString("Display the options.", 20, 60);
    }

}
