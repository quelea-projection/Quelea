package org.quelea.windows.options;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import org.quelea.bible.Bible;
import org.quelea.bible.BibleManager;
import org.quelea.utils.PropertyPanel;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.SpringUtilities;

/**
 * The panel that shows the bible options
 * @author Michael
 */
public class OptionsBiblePanel extends JPanel implements PropertyPanel {

    private final JComboBox defaultBibleComboBox;
    private final JSpinner maxVersesSpinner;

    public OptionsBiblePanel() {
        setName("Bible");
        JPanel biblePanel = new JPanel();
        biblePanel.setLayout(new SpringLayout());

        JLabel defaultLabel = new JLabel("Default bible");
        biblePanel.add(defaultLabel);
        defaultBibleComboBox = new JComboBox(BibleManager.get().getBibles());
        defaultLabel.setLabelFor(defaultBibleComboBox);
        biblePanel.add(defaultBibleComboBox);

        JLabel maxVerseLabel = new JLabel("Maximum allowed verses");
        biblePanel.add(maxVerseLabel);
        maxVersesSpinner = new JSpinner(new SpinnerNumberModel(200, 1, 5000, 1));
        maxVerseLabel.setLabelFor(maxVersesSpinner);
        biblePanel.add(maxVersesSpinner);

        SpringUtilities.makeCompactGrid(biblePanel, 2, 2, 6, 6, 6, 6);
        add(biblePanel);
        readProperties();
    }

    /**
     * @inheritDoc
     */
    public final void readProperties() {
        QueleaProperties props = QueleaProperties.get();
        String selectedBibleName = props.getDefaultBible();
        for (int i = 0; i < defaultBibleComboBox.getModel().getSize(); i++) {
            Bible bible = (Bible) defaultBibleComboBox.getItemAt(i);
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
        Bible bible = (Bible)getDefaultBibleBox().getSelectedItem();
        props.setDefaultBible(bible);
        int maxVerses = (Integer)getMaxVersesSpinner().getValue();
        props.setMaxVerses(maxVerses);
    }

    /**
     * Get the default bible combo box.
     * @return the default bible combo box.
     */
    public JComboBox getDefaultBibleBox() {
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
