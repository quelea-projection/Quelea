package org.quelea.windows.options;

import org.quelea.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel used to represent a single type of display that the user can then select the output for.
 * @author Michael
 */
public class SingleDisplayPanel extends JPanel {

    private final boolean none;
    private final JComboBox<String> outputSelect;

    /**
     * Create a new single display panel.
     * @param caption      the bit of text at the top describing the display.
     * @param iconLocation the location of the icon to use.
     * @param none         true if "none" (i.e. no output) should be an option, false otherwise.
     */
    public SingleDisplayPanel(String caption, String iconLocation, boolean none) {
        this.none = none;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel(caption));
        add(new JLabel(Utils.getImageIcon(iconLocation)));
        outputSelect = new JComboBox<>(getAvailableScreens(none));
        JPanel outputSelectPanel = new JPanel();
        outputSelectPanel.add(outputSelect);
        add(outputSelectPanel);
    }

    /**
     * Determine the output display that should be used.
     * @return the output display as an index starting from 0, or -1 if "none" is selected.
     */
    public int getOutputDisplay() {
        if(none) {
            return outputSelect.getSelectedIndex() - 1;
        }
        else {
            return outputSelect.getSelectedIndex();
        }
    }

    /**
     * Set the screen to select on the combo box.
     * @param num the index (0 based) of the screen to select.
     */
    public void setScreen(int num) {
        int maxIndex = outputSelect.getModel().getSize() - 1;
        if(none) {
            int index = num + 1;
            if(index > maxIndex) {
                index = 0;
            }
            outputSelect.setSelectedIndex(index);
        }
        else {
            int index = num;
            if(index > maxIndex) {
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
        if(none) {
            descriptions.add("<html><i>None</i></html>");
        }
        for(int i = 0; i < gds.length; i++) {
            descriptions.add("Output " + (i + 1));
        }
        return new DefaultComboBoxModel<>(descriptions.toArray(new String[descriptions.size()]));
    }

}
