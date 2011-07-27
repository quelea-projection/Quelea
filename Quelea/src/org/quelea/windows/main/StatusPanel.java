package org.quelea.windows.main;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import org.quelea.utils.Utils;

/**
 *
 * @author Michael
 */
public class StatusPanel extends JPanel {

    private JProgressBar progressBar;
    private JLabel label;
    private JButton cancelButton;
    private StatusPanelGroup group;
    private int index;

    StatusPanel(StatusPanelGroup group, String labelText, int index) {
        this.group = group;
        this.index = index;
        label = new JLabel(labelText);
        progressBar = new JProgressBar();
        cancelButton = new JButton(Utils.getImageIcon("icons/cross.png", 15, 15));
        cancelButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(label);
        add(progressBar);
        add(cancelButton);
    }

    public void done() {
        group.removePanel(index);
    }

    public void setLabelText(String text) {
        label.setText(text);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void setActive(boolean active) {
        setVisible(active);
    }
}
