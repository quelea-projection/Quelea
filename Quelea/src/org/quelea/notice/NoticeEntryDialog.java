package org.quelea.notice;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.quelea.utils.SpringUtilities;

/**
 *
 * @author Michael
 */
public class NoticeEntryDialog extends JDialog {

    private static NoticeEntryDialog dialog;
    private JTextField text;
    private JSpinner times;
    private JCheckBox infinite;
    private JButton addButton;
    private JButton cancelButton;
    private Notice notice;

    public NoticeEntryDialog(JDialog owner) {
        super(owner, true);
        setTitle("New notice");
        text = new JTextField(50);
        text.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }

            private void check() {
                addButton.setEnabled(!text.getText().trim().isEmpty());
            }
        });
        times = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        infinite = new JCheckBox();
        infinite.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (infinite.isSelected()) {
                    times.setEnabled(false);
                }
                else {
                    times.setEnabled(true);
                }
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new SpringLayout());
        addBlock(mainPanel, "Notice", text);
        addBlock(mainPanel, "Amount of times", new JPanel() {

            {
                setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                add(times);
            }
        });
        addBlock(mainPanel, "Infinite?", infinite);
        SpringUtilities.makeCompactGrid(mainPanel, 3, 2, 0, 0, 0, 0);

        JPanel southPanel = new JPanel();
        addButton = new JButton("Add notice");
        getRootPane().setDefaultButton(addButton);
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int numberTimes;
                if (infinite.isSelected()) {
                    numberTimes = Integer.MAX_VALUE;
                }
                else {
                    numberTimes = (int) times.getValue();
                }
                if (notice == null) {
                    notice = new Notice(text.getText(), numberTimes);
                }
                else {
                    notice.setText(text.getText());
                    notice.setTimes(numberTimes);
                }
                setVisible(false);
            }
        });
        southPanel.add(addButton);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                notice = null;
                setVisible(false);
            }
        });
        southPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
        pack();
    }

    private void addBlock(JPanel panel, String labelText, Component component) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(component);
        panel.add(label);
        panel.add(component);
    }

    public String getNoticeText() {
        return text.getText();
    }

    public int getTimes() {
        if (infinite.isSelected()) {
            return Integer.MAX_VALUE;
        }
        return (int) times.getValue();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(visible);
    }

    private void setNotice(Notice notice) {
        this.notice = notice;
        if (notice == null) {
            infinite.setSelected(false);
            times.setValue(1);
            text.setText("");
            addButton.setText("Add notice");
            addButton.setEnabled(false);
        }
        else {
            infinite.setSelected(notice.getTimes() == Integer.MAX_VALUE);
            if (!infinite.isSelected()) {
                times.setValue(notice.getTimes());
            }
            text.setText(notice.getText());
            addButton.setText("Edit notice");
        }
    }

    public static Notice getNotice(JDialog owner, Notice existing) {
        if (dialog == null) {
            dialog = new NoticeEntryDialog(owner);
        }
        dialog.setNotice(existing);
        dialog.setVisible(true);
        return dialog.notice;
    }
}
