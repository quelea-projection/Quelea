/*
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2012 Michael Berry
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

import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import org.quelea.displayable.TextAlignment;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.SpringUtilities;
import org.quelea.utils.Utils;
import org.quelea.windows.newsong.ColourButton;

/**
 * The panel that shows the stage view options.
 *
 * @author mjrb5
 */
public class OptionsStageViewPanel extends JPanel implements PropertyPanel {

    private JCheckBox showChordsCheckBox;
    private JComboBox<String> lineAlignment;
    private JComboBox<String> fontSelection;
    private ColourButton backgroundColourButton;
    private ColourButton chordColourButton;
    private ColourButton lyricsColourButton;

    /**
     * Create the stage view options panel.
     */
    public OptionsStageViewPanel() {
        setName(LabelGrabber.INSTANCE.getLabel("stage.options.heading"));

        JPanel stagePanel = new JPanel();
        stagePanel.setLayout(new SpringLayout());

        stagePanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("stage.show.chords")));
        showChordsCheckBox = new JCheckBox();
        stagePanel.add(showChordsCheckBox);

        stagePanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("stage.line.alignment")));
        lineAlignment = new JComboBox<>();
        lineAlignment.setEditable(false);
        for(TextAlignment alignment : TextAlignment.values()) {
            lineAlignment.addItem(alignment.toFriendlyString());
        }
        stagePanel.add(lineAlignment);

        stagePanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("stage.font.selection")));
        fontSelection = new JComboBox<>();
        fontSelection.setEditable(false);
        for(String font : Utils.getAllFonts()) {
            fontSelection.addItem(font);
        }
        stagePanel.add(fontSelection);

        stagePanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("stage.background.colour")));
        backgroundColourButton = new ColourButton(Color.BLACK);
        stagePanel.add(backgroundColourButton);

        stagePanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("stage.lyrics.colour")));
        lyricsColourButton = new ColourButton(Color.BLACK);
        stagePanel.add(lyricsColourButton);

        stagePanel.add(new JLabel(LabelGrabber.INSTANCE.getLabel("stage.chord.colour")));
        chordColourButton = new ColourButton(Color.BLACK);
        stagePanel.add(chordColourButton);

        add(stagePanel);
        SpringUtilities.makeCompactGrid(stagePanel, 6, 2, 6, 6, 6, 6);
        readProperties();
    }

    /**
     * Set the properties based on the values in this frame.
     */
    @Override
    public void setProperties() {
        QueleaProperties.get().setShowChords(showChordsCheckBox.isSelected());
        QueleaProperties.get().setStageTextAlignment(TextAlignment.parse(lineAlignment.getItemAt(lineAlignment.getSelectedIndex())));
        QueleaProperties.get().setStageTextFont(fontSelection.getItemAt(fontSelection.getSelectedIndex()));
        QueleaProperties.get().setStageBackgroundColor(backgroundColourButton.getColour());
        QueleaProperties.get().setStageChordColor(chordColourButton.getColour());
        QueleaProperties.get().setStageLyricsColor(lyricsColourButton.getColour());
    }

    /**
     * Read the properties into this frame.
     */
    @Override
    public final void readProperties() {
        showChordsCheckBox.setSelected(QueleaProperties.get().getShowChords());
        lyricsColourButton.setColour(QueleaProperties.get().getStageLyricsColor());
        backgroundColourButton.setColour(QueleaProperties.get().getStageBackgroundColor());
        chordColourButton.setColour(QueleaProperties.get().getStageChordColor());
        fontSelection.setSelectedItem(QueleaProperties.get().getStageTextFont());
        lineAlignment.setSelectedItem(QueleaProperties.get().getStageTextAlignment());
    }
}
