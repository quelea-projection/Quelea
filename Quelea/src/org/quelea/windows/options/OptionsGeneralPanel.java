package org.quelea.windows.options;

import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.SpringUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A panel where the general options in the program are set.
 * @author Michael
 */
public class OptionsGeneralPanel extends JPanel implements PropertyPanel {

    private final JCheckBox startupUpdateCheckBox;
    private final JCheckBox capitalFirstCheckBox;
    private final JCheckBox oneMonitorWarnCheckBox;
    private final JSlider maxCharsSlider;
    private final JSlider minLinesSlider;

    /**
     * Create a new general panel.
     */
    public OptionsGeneralPanel() {
        setName("General");
        JPanel generalPanel = new JPanel(); //Add stuff to generalpanel to avoid leaking this
        generalPanel.setLayout(new SpringLayout());

        JLabel startupLabel = new JLabel("Check for update on startup");
        generalPanel.add(startupLabel);
        startupUpdateCheckBox = new JCheckBox();
        startupLabel.setLabelFor(startupUpdateCheckBox);
        generalPanel.add(startupUpdateCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy

        JLabel warnLabel = new JLabel("Warn if there's just one monitor");
        generalPanel.add(warnLabel);
        oneMonitorWarnCheckBox = new JCheckBox();
        warnLabel.setLabelFor(oneMonitorWarnCheckBox);
        generalPanel.add(oneMonitorWarnCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy

        JLabel capitalFirstLabel = new JLabel("Capitalise the start of each line");
        generalPanel.add(capitalFirstLabel);
        capitalFirstCheckBox = new JCheckBox();
        startupLabel.setLabelFor(capitalFirstCheckBox);
        generalPanel.add(capitalFirstCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy

        JLabel maxCharsLabel = new JLabel("Maximum number of characters per line");
        generalPanel.add(maxCharsLabel);
        maxCharsSlider = new JSlider(10, 80);
        generalPanel.add(maxCharsSlider);
        maxCharsLabel.setLabelFor(maxCharsSlider);
        final JLabel maxCharsValue = new JLabel(Integer.toString(maxCharsSlider.getValue()));
        generalPanel.add(maxCharsValue);
        maxCharsValue.setLabelFor(maxCharsSlider);
        maxCharsSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                maxCharsValue.setText(Integer.toString(maxCharsSlider.getValue()));
            }
        });

        JLabel minLinesLabel = new JLabel("<html>Minimum number of emulated lines<i> (Advanced)</i></html>");
        generalPanel.add(minLinesLabel);
        minLinesSlider = new JSlider(1, 20);
        generalPanel.add(minLinesSlider);
        minLinesLabel.setLabelFor(minLinesSlider);
        final JLabel minLinesValue = new JLabel(Integer.toString(minLinesSlider.getValue()));
        generalPanel.add(minLinesValue);
        minLinesValue.setLabelFor(minLinesSlider);
        minLinesSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                minLinesValue.setText(Integer.toString(minLinesSlider.getValue()));
            }
        });
        SpringUtilities.makeCompactGrid(generalPanel, 5, 3, 6, 6, 6, 6);
        add(generalPanel);
        readProperties();
    }

    /**
     * @inheritDoc
     */
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        startupUpdateCheckBox.setSelected(props.checkUpdate());
        capitalFirstCheckBox.setSelected(props.checkCapitalFirst());
        oneMonitorWarnCheckBox.setSelected(props.showSingleMonitorWarning());
        maxCharsSlider.setValue(props.getMaxChars());
        minLinesSlider.setValue(props.getMinLines());
    }

    /**
     * @inheritDoc
     */
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        boolean checkUpdate = getStartupUpdateCheckBox().isSelected();
        props.setCheckUpdate(checkUpdate);
        boolean showWarning = getOneMonitorWarningCheckBox().isSelected();
        props.setSingleMonitorWarning(showWarning);
        boolean checkCapital = getCapitalFirstCheckBox().isSelected();
        props.setCapitalFirst(checkCapital);
        int maxCharsPerLine = getMaxCharsSlider().getValue();
        props.setMaxChars(maxCharsPerLine);
        int minLines = getMinLinesSlider().getValue();
        props.setMinLines(minLines);
    }

    /**
     * Get the max chars slider.
     * @return the max chars slider.
     */
    public JSlider getMaxCharsSlider() {
        return maxCharsSlider;
    }

    /**
     * Get the min lines slider.
     * @return the min lines slider.
     */
    public JSlider getMinLinesSlider() {
        return minLinesSlider;
    }

    /**
     * Get the startup readProperties checkbox.
     * @return the startup readProperties checkbox.
     */
    public JCheckBox getStartupUpdateCheckBox() {
        return startupUpdateCheckBox;
    }

    /**
     * Get the capitalise first character in each line checkbox.
     * @return the capitalise first character in each line checkbox.
     */
    public JCheckBox getCapitalFirstCheckBox() {
        return capitalFirstCheckBox;
    }

    /**
     * Get the "one monitor warning" checkbox.
     * @return the "one monitor warning" checkbox.
     */
    public JCheckBox getOneMonitorWarningCheckBox() {
        return oneMonitorWarnCheckBox;
    }
}
