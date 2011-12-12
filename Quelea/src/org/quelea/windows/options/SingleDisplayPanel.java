/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.quelea.utils.Utils;

/**
 * A panel used to represent a single type of display that the user can then
 * select the output for.
 *
 * @author Michael
 */
public class SingleDisplayPanel extends JPanel {

    private final boolean none;
    private final JComboBox<String> outputSelect;
    private JCheckBox custom;
    private JSpinner customX;
    private JSpinner customY;
    private JSpinner customWidth;
    private JSpinner customHeight;

    /**
     * Create a new single display panel.
     * @param caption      the bit of text at the top describing the display.
     * @param iconLocation the location of the icon to use.
     * @param none         true if "none" (i.e. no output) should be an option, false otherwise.
     * @param customPos    true if a custom position should be allowed for this display panel.
     */
    public SingleDisplayPanel(String caption, String iconLocation, boolean none,
            boolean customPos) {
        this.none = none;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel(caption));
        add(new JLabel(Utils.getImageIcon(iconLocation)));
        outputSelect = new JComboBox<>(getAvailableScreens(none));
        JPanel outputSelectPanel = new JPanel();
        outputSelectPanel.add(outputSelect);
        add(outputSelectPanel);
        if (customPos) {
            outputSelect.setEnabled(false);
            custom = new JCheckBox("Custom position");
            custom.setSelected(true);
            custom.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (custom.isSelected()) {
                        outputSelect.setEnabled(false);
                        customX.setEnabled(true);
                        customY.setEnabled(true);
                        customWidth.setEnabled(true);
                        customHeight.setEnabled(true);
                    }
                    else {
                        outputSelect.setEnabled(true);
                        customX.setEnabled(false);
                        customY.setEnabled(false);
                        customWidth.setEnabled(false);
                        customHeight.setEnabled(false);
                    }
                }
            });
            JPanel customPanel = new JPanel();
            customPanel.add(custom);
            add(customPanel);

            JPanel xyPanel = new JPanel();
            xyPanel.setLayout(new BoxLayout(xyPanel, BoxLayout.X_AXIS));
            customX = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
            customY = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
            xyPanel.add(new JLabel("X:"));
            xyPanel.add(customX);
            xyPanel.add(new JLabel("Y:"));
            xyPanel.add(customY);
            JPanel xyContainerPanel = new JPanel();
            xyContainerPanel.add(xyPanel);
            add(xyContainerPanel);
            JPanel whPanel = new JPanel();
            whPanel.setLayout(new BoxLayout(whPanel, BoxLayout.X_AXIS));
            customWidth = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
            customHeight = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
            whPanel.add(new JLabel("W:"));
            whPanel.add(customWidth);
            whPanel.add(new JLabel("H:"));
            whPanel.add(customHeight);
            JPanel whContainerPanel = new JPanel();
            whContainerPanel.add(whPanel);
            add(whContainerPanel);
        }
    }
    
    /**
     * Get the output screen currently selected in the dialog, or -1 if none is
     * selected.
     * @return the output screen currently selected in the dialog
     */
    public int getOutputScreen() {
        int screenNum;
        if (none) {
            screenNum = outputSelect.getSelectedIndex() - 1;
        }
        else {
            screenNum = outputSelect.getSelectedIndex();
        }
        return screenNum;
    }

    /**
     * Determine the output bounds that should be used.
     * @return the output bounds as a rectangle, or null if "none" is selected.
     */
    public Rectangle getOutputBounds() {
        if (custom != null && custom.isSelected()) {
            return getCoords();
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        int screen = getOutputScreen();
        if(screen==-1) {
            return null;
        }
        else {
            return gds[screen].getDefaultConfiguration().getBounds();
        }
    }

    /**
     * Determine whether the panel is set to a custom position.
     * @return the bounds for the custom position.
     */
    public boolean customPosition() {
        return custom.isSelected();
    }

    /**
     * Get the bounds currently selected on the dialog.
     * @return the bounds currently selected.
     */
    public Rectangle getCoords() {
        return new Rectangle((int) customX.getValue(), (int) customY.getValue(), (int) customWidth.getValue(), (int) customHeight.getValue());
    }

    /**
     * Set the bounds to display on the panel.
     * @param bounds the bounds to display.
     */
    public void setCoords(Rectangle bounds) {
        if (custom != null) {
            custom.setSelected(true);
        }
        customX.setValue((int) bounds.getX());
        customY.setValue((int) bounds.getY());
        customWidth.setValue((int) bounds.getWidth());
        customHeight.setValue((int) bounds.getHeight());
    }

    /**
     * Set the screen to select on the combo box.
     * @param num the index (0 based) of the screen to select.
     */
    public void setScreen(int num) {
        if (custom != null) {
            custom.setSelected(false);
        }
        int maxIndex = outputSelect.getModel().getSize() - 1;
        if (none) {
            int index = num + 1;
            if (index > maxIndex) {
                index = 0;
            }
            outputSelect.setSelectedIndex(index);
        }
        else {
            int index = num;
            if (index > maxIndex) {
                index = 0;
            }
            outputSelect.setSelectedIndex(index);
        }
    }

    /**
     * Update the display panel with the monitor information.
     */
    public void update() {
        outputSelect.setModel(getAvailableScreens(none));
    }

    /**
     * Get a list model describing the available graphical devices.
     * @return a list model describing the available graphical devices.
     */
    private ComboBoxModel<String> getAvailableScreens(boolean none) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gds = ge.getScreenDevices();
        List<String> descriptions = new ArrayList<>();
        if (none) {
            descriptions.add("<html><i>None</i></html>");
        }
        for (int i = 0; i < gds.length; i++) {
            descriptions.add("Output " + (i + 1));
        }
        return new DefaultComboBoxModel<>(descriptions.toArray(new String[descriptions.size()]));
    }

    /**
     * Test it.
     * @param args //not used.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(1, 1));
        frame.add(new SingleDisplayPanel("caption", "icons/monitor.png", true, true));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
