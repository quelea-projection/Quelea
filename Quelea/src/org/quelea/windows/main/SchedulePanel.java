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
package org.quelea.windows.main;

import javax.swing.event.ListDataEvent;
import org.quelea.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ListDataListener;
import org.quelea.Application;

/**
 * The panel displaying the schedule / order of service. Items from here are loaded into the preview panel where they
 * are viewed and then projected live. Items can be added here from the library.
 * @author Michael
 */
public class SchedulePanel extends JPanel {

    private final ScheduleList scheduleList;
    private final JButton removeButton;
    private final JButton upButton;
    private final JButton downButton;
    private final JButton themeButton;
    private final ScheduleThemePopupWindow themeMenu;

    /**
     * Create and initialise the schedule panel.
     */
    public SchedulePanel() {
        setLayout(new BorderLayout());
        scheduleList = new ScheduleList();
        scheduleList.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                themeMenu.updateTheme();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                themeMenu.updateTheme();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                themeMenu.updateTheme();
            }
        });

        themeMenu = new ScheduleThemePopupWindow(scheduleList);
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            @Override
            public void eventDispatched(AWTEvent event) {
                MouseEvent mouseEvent = (MouseEvent) event;
                if (mouseEvent.getClickCount() > 0) {
                    Rectangle bounds = Application.get().getMainWindow().getBounds();
                    if (bounds.contains(new Point(mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen()))) {
                        Rectangle popupBounds = themeMenu.getBounds();
                        if (!popupBounds.contains(new Point(mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen()))) {
                            if (event.getSource() != themeButton) {
                                themeMenu.setVisible(false);
                            }
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
        themeButton = new JButton(Utils.getImageIcon("icons/settings.png", 16, 16));
        themeButton.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                themeMenu.setSize(themeMenu.getPreferredSize());
                int x = (int)themeButton.getLocationOnScreen().getX();
                int y = (int)themeButton.getLocationOnScreen().getY()+themeButton.getHeight();
                themeMenu.setLocation(x, y);
                themeMenu.setVisible(true);
            }
        });
        themeButton.setToolTipText("Adjust theme for service");

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        removeButton = new JButton(Utils.getImageIcon("icons/remove.png"));
        removeButton.setToolTipText("Remove song");
        removeButton.setRequestFocusEnabled(false);
        removeButton.setEnabled(false);
        removeButton.addActionListener(new RemoveSongScheduleActionListener());

        upButton = new JButton(Utils.getImageIcon("icons/up.png"));
        upButton.setToolTipText("Move selected item up");
        upButton.setRequestFocusEnabled(false);
        upButton.setEnabled(false);
        upButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.UP);
            }
        });

        downButton = new JButton(Utils.getImageIcon("icons/down.png"));
        downButton.setToolTipText("Move selected item down");
        downButton.setRequestFocusEnabled(false);
        downButton.setEnabled(false);
        downButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scheduleList.moveCurrentItem(ScheduleList.Direction.DOWN);
            }
        });

        scheduleList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (scheduleList.getSelectedIndex() == -1) {
                    removeButton.setEnabled(false);
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                }
                else {
                    removeButton.setEnabled(true);
                    upButton.setEnabled(true);
                    downButton.setEnabled(true);
                }
            }
        });

        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.add(new JLabel("<html><b>Order of Service</b></html>"));
        header.add(Box.createHorizontalGlue());
        header.add(themeButton);

        toolbar.add(removeButton);
        toolbar.add(upButton);
        toolbar.add(downButton);

        add(header, BorderLayout.NORTH);
        JScrollPane scheduleListScroll = new JScrollPane(scheduleList);
        scheduleListScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(scheduleListScroll, BorderLayout.CENTER);
        add(toolbar, BorderLayout.EAST);
    }

    /**
     * Get the schedule list backing this panel.
     * @return the schedule list.
     */
    public ScheduleList getScheduleList() {
        return scheduleList;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SchedulePanel());
        frame.pack();
        frame.setVisible(true);
    }
}
