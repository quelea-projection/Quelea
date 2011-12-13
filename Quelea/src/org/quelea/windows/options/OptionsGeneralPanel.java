/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.quelea.Application;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.SpringUtilities;

/**
 * A panel where the general options in the program are set.
 * @author Michael
 */
public class OptionsGeneralPanel extends JPanel implements PropertyPanel {

    private final JCheckBox startupUpdateCheckBox;
    private final JCheckBox capitalFirstCheckBox;
    private final JCheckBox oneMonitorWarnCheckBox;
    private final JCheckBox displaySongInfoCheckBox;
    private final JCheckBox oneLineModeCheckBox;
    private final JSlider borderThicknessSlider;
    private final JSlider maxCharsSlider;
    private final JSlider minLinesSlider;

    /**
     * Create a new general panel.
     */
    public OptionsGeneralPanel() {
        setName(LabelGrabber.INSTANCE.getLabel("general.options.heading"));
        JPanel generalPanel = new JPanel(); //Add stuff to generalpanel to avoid leaking this
        generalPanel.setLayout(new SpringLayout());
        int rows = 0;

        JLabel startupLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("check.for.update.label"));
        generalPanel.add(startupLabel);
        startupUpdateCheckBox = new JCheckBox();
        startupLabel.setLabelFor(startupUpdateCheckBox);
        generalPanel.add(startupUpdateCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy
        rows++;

        JLabel warnLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("1.monitor.warn.label"));
        generalPanel.add(warnLabel);
        oneMonitorWarnCheckBox = new JCheckBox();
        warnLabel.setLabelFor(oneMonitorWarnCheckBox);
        generalPanel.add(oneMonitorWarnCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy
        rows++;

        JLabel capitalFirstLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("capitalise.start.line.label"));
        generalPanel.add(capitalFirstLabel);
        capitalFirstCheckBox = new JCheckBox();
        startupLabel.setLabelFor(capitalFirstCheckBox);
        generalPanel.add(capitalFirstCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy
        rows++;

        JLabel displaySongInfoLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("display.song.info.label"));
        generalPanel.add(displaySongInfoLabel);
        displaySongInfoCheckBox = new JCheckBox();
        startupLabel.setLabelFor(displaySongInfoCheckBox);
        generalPanel.add(displaySongInfoCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy
        rows++;

        JLabel oneLineModeLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("one.line.mode.label"));
        generalPanel.add(oneLineModeLabel);
        oneLineModeCheckBox = new JCheckBox();
        startupLabel.setLabelFor(oneLineModeCheckBox);
        generalPanel.add(oneLineModeCheckBox);
        generalPanel.add(new JLabel()); //Keep springlayout happy
        rows++;

        JLabel borderThicknessLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("text.border.thickness.label"));
        generalPanel.add(borderThicknessLabel);
        borderThicknessSlider = new JSlider(0, 5);
        generalPanel.add(borderThicknessSlider);
        borderThicknessLabel.setLabelFor(borderThicknessSlider);
        final JLabel borderThicknessValue = new JLabel(Integer.toString(borderThicknessSlider.getValue()));
        generalPanel.add(borderThicknessValue);
        borderThicknessValue.setLabelFor(borderThicknessSlider);
        borderThicknessSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                borderThicknessValue.setText(Integer.toString(borderThicknessSlider.getValue()));
            }
        });
        rows++;

        JLabel maxCharsLabel = new JLabel(LabelGrabber.INSTANCE.getLabel("max.chars.line.label"));
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
        rows++;

        JLabel minLinesLabel = new JLabel("<html>" + LabelGrabber.INSTANCE.getLabel("min.emulated.lines.label") + "<i> (" + LabelGrabber.INSTANCE.getLabel("advanced.label") + ")</i></html>");
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
        rows++;

        SpringUtilities.makeCompactGrid(generalPanel, rows, 3, 6, 6, 6, 6);
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
        displaySongInfoCheckBox.setSelected(props.checkDisplaySongInfoText());
        oneLineModeCheckBox.setSelected(props.getOneLineMode());
        maxCharsSlider.setValue(props.getMaxChars());
        minLinesSlider.setValue(props.getMinLines());
        borderThicknessSlider.setValue(props.getOutlineThickness());
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
        boolean checkDisplayInfo = getDisplaySongInfoCheckBox().isSelected();
        props.setDisplaySongInfoText(checkDisplayInfo);
        boolean oneLineMode = getOneLineModeCheckBox().isSelected();
        props.setOneLineMode(oneLineMode);
        //One line mode needs to be updated manually
        Application.get().getMainWindow().getMainPanel().getPreviewPanel().updateOneLineMode();
        Application.get().getMainWindow().getMainPanel().getLivePanel().updateOneLineMode();
        int maxCharsPerLine = getMaxCharsSlider().getValue();
        props.setMaxChars(maxCharsPerLine);
        int minLines = getMinLinesSlider().getValue();
        props.setMinLines(minLines);
        int borderThickness = getBorderThicknessSlider().getValue();
        props.setOutlineThickness(borderThickness);
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
     * Get the display song info checkbox.
     * @return the display song info checkbox.
     */
    public JCheckBox getDisplaySongInfoCheckBox() {
        return displaySongInfoCheckBox;
    }

    /**
     * Get the "one monitor warning" checkbox.
     * @return the "one monitor warning" checkbox.
     */
    public JCheckBox getOneMonitorWarningCheckBox() {
        return oneMonitorWarnCheckBox;
    }

    /**
     * Get the "one line mode" checkbox.
     * @return the "one line mode" checkbox.
     */
    public JCheckBox getOneLineModeCheckBox() {
        return oneLineModeCheckBox;
    }
    
    /**
     * Get the border thickness slider.
     * @return the border thickness slider.
     */
    public JSlider getBorderThicknessSlider() {
        return borderThicknessSlider;
    }
}
