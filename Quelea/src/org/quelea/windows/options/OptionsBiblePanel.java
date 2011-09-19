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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import org.quelea.bible.Bible;
import org.quelea.bible.BibleManager;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.SpringUtilities;
import org.quelea.bible.BibleChangeListener;
import org.quelea.utils.FileFilters;
import org.quelea.utils.Utils;

/**
 * The panel that shows the bible options
 * @author Michael
 */
public class OptionsBiblePanel extends JPanel implements PropertyPanel, BibleChangeListener {

    private final JComboBox<Bible> defaultBibleComboBox;
    private final JSpinner maxVersesSpinner;

    /**
     * Create the options bible panel.
     */
    public OptionsBiblePanel() {
        setName("Bible");
        JPanel biblePanel = new JPanel();
        biblePanel.setLayout(new SpringLayout());

        JLabel defaultLabel = new JLabel("Default bible");
        biblePanel.add(defaultLabel);
        BibleManager.get().registerBibleChangeListener(this);
        defaultBibleComboBox = new JComboBox<>(BibleManager.get().getBibles());
        defaultLabel.setLabelFor(defaultBibleComboBox);
        biblePanel.add(defaultBibleComboBox);

        JLabel maxVerseLabel = new JLabel("Maximum allowed verses");
        biblePanel.add(maxVerseLabel);
        maxVersesSpinner = new JSpinner(new SpinnerNumberModel(200, 1, 5000, 1));
        maxVerseLabel.setLabelFor(maxVersesSpinner);
        biblePanel.add(maxVersesSpinner);
        
        final JButton addBibleButton = new JButton("Add bible");
        addBibleButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(FileFilters.XML_BIBLE);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.showDialog(SwingUtilities.getWindowAncestor(addBibleButton), "Add bible");
                File file = chooser.getSelectedFile();
                try {
                    Utils.copyFile(file, new File(QueleaProperties.get().getBibleDir(), file.getName()));
                }
                catch(IOException ex) {
                    JOptionPane.showMessageDialog(chooser, "Sorry, couldn't copy the bible.", "Error copying", JOptionPane.ERROR);
                }
            }
        });
        biblePanel.add(addBibleButton);
        biblePanel.add(new JLabel());

        SpringUtilities.makeCompactGrid(biblePanel, 3, 2, 6, 6, 6, 6);
        add(biblePanel);
        readProperties();
    }

    /**
     * Update all the bibles in the panel.
     */
    @Override
    public void updateBibles() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DefaultComboBoxModel<Bible> model = ((DefaultComboBoxModel<Bible>) defaultBibleComboBox.getModel());
                model.removeAllElements();
                for (Bible bible : BibleManager.get().getBibles()) {
                    model.addElement(bible);
                }
            }
        });
    }

    /**
     * @inheritDoc
     */
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        String selectedBibleName = props.getDefaultBible();
        for (int i = 0; i < defaultBibleComboBox.getModel().getSize(); i++) {
            Bible bible = defaultBibleComboBox.getItemAt(i);
            if (bible.getName().equals(selectedBibleName)) {
                defaultBibleComboBox.setSelectedIndex(i);
            }
        }
        maxVersesSpinner.setValue(props.getMaxVerses());
    }

    /**
     * @inheritDoc
     */
    public void setProperties() {
        QueleaProperties props = QueleaProperties.get();
        Bible bible = (Bible) getDefaultBibleBox().getSelectedItem();
        props.setDefaultBible(bible);
        int maxVerses = (Integer) getMaxVersesSpinner().getValue();
        props.setMaxVerses(maxVerses);
    }

    /**
     * Get the default bible combo box.
     * @return the default bible combo box.
     */
    public JComboBox<Bible> getDefaultBibleBox() {
        return defaultBibleComboBox;
    }

    /**
     * Get the max verses spinner.
     * @return the max verses spinner.
     */
    public JSpinner getMaxVersesSpinner() {
        return maxVersesSpinner;
    }
}
