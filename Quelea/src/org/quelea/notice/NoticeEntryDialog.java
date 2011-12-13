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
package org.quelea.notice;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.SpringUtilities;

/**
 * The entry dialog for creating a notice.
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

    /**
     * Create a new notice entry dialog.
     * @param owner the owner of this dialog.
     */
    public NoticeEntryDialog(JDialog owner) {
        super(owner, true);
        setTitle(LabelGrabber.INSTANCE.getLabel("new.notice.heading"));
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
        addBlock(mainPanel, LabelGrabber.INSTANCE.getLabel("notice.text"), text);
        addBlock(mainPanel, LabelGrabber.INSTANCE.getLabel("notice.times.text"), new JPanel() {

            {
                setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                add(times);
            }
        });
        addBlock(mainPanel, LabelGrabber.INSTANCE.getLabel("notice.infinite.question"), infinite);
        SpringUtilities.makeCompactGrid(mainPanel, 3, 2, 0, 0, 0, 0);

        JPanel southPanel = new JPanel();
        addButton = new JButton(LabelGrabber.INSTANCE.getLabel("add.notice.button"));
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
        cancelButton = new JButton(LabelGrabber.INSTANCE.getLabel("cancel.text"));
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

    /**
     * Add a "block" to a panel, consisting of a label and then a component to
     * go with that label.
     * @param panel the panel to add the "block" to.
     * @param labelText the text to display on the label.
     * @param component the component to go with the label.
     */
    private void addBlock(JPanel panel, String labelText, Component component) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(component);
        panel.add(label);
        panel.add(component);
    }

    /**
     * Get the notice text.
     * @return the notice text.
     */
    public String getNoticeText() {
        return text.getText();
    }

    /**
     * Get the number of times remaining (Integer.MAX_VALUE) if infinite.
     * @return the number of times remaining (Integer.MAX_VALUE) if infinite.
     */
    public int getTimes() {
        if (infinite.isSelected()) {
            return Integer.MAX_VALUE;
        }
        return (int) times.getValue();
    }

    /**
     * Set the dialog to be visible or not, if visible centre on parent.
     * @param visible true if visible, false otherwise.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(visible);
    }

    /**
     * Set the dialog to show the given notice.
     * @param notice the notice to show.
     */
    private void setNotice(Notice notice) {
        this.notice = notice;
        if (notice == null) {
            infinite.setSelected(false);
            times.setValue(1);
            text.setText("");
            addButton.setText(LabelGrabber.INSTANCE.getLabel("add.notice.button"));
            addButton.setEnabled(false);
        }
        else {
            infinite.setSelected(notice.getTimes() == Integer.MAX_VALUE);
            if (!infinite.isSelected()) {
                times.setValue(notice.getTimes());
            }
            text.setText(notice.getText());
            addButton.setText(LabelGrabber.INSTANCE.getLabel("edit.notice.button"));
        }
    }

    /**
     * Get a notice that the user enters.
     * @param owner the owner of the dialog that will be created.
     * @param existing any existing notice to fill the dialog with.
     * @return the user-entered notice.
     */
    public static Notice getNotice(JDialog owner, Notice existing) {
        if (dialog == null) {
            dialog = new NoticeEntryDialog(owner);
        }
        dialog.setNotice(existing);
        dialog.setVisible(true);
        return dialog.notice;
    }
}
