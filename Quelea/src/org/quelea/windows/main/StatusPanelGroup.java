/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quelea.windows.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.quelea.Application;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class StatusPanelGroup extends JPanel {

    private List<StatusPanel> panels;

    public StatusPanelGroup() {
        panels = new ArrayList<StatusPanel>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public synchronized StatusPanel addPanel(String label) {
        StatusPanel panel = new StatusPanel(this, label, panels.size());
        add(panel);
        panels.add(panel);
        Application.get().getMainWindow().validate();
        Application.get().getMainWindow().repaint();
        return panel;
    }

    void removePanel(int index) {
        StatusPanel panel = panels.get(index);
        panel.setPreferredSize(new Dimension(1, 1));
        panel.setSize(1, 1);
        remove(panel);
        Application.get().getMainWindow().validate();
        Application.get().getMainWindow().repaint();
        panels.set(index, null);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        StatusPanelGroup group = new StatusPanelGroup();

        StatusPanel panel = group.addPanel("Hello");
        group.addPanel("Hello2");
        group.addPanel("Hello3");
        group.addPanel("Hello4");

        frame.add(group, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        Utils.sleep(1000);
        panel.done();
    }
}
