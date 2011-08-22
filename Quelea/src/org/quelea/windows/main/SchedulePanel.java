package org.quelea.windows.main;

import javax.swing.event.ListDataEvent;
import org.quelea.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ListDataListener;

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
    private final ScheduleThemePopupMenu themeMenu;

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
        
        themeMenu = new ScheduleThemePopupMenu(scheduleList);
        themeButton = new JButton(Utils.getImageIcon("icons/settings.png", 16, 16));
        themeButton.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                themeMenu.show(e.getComponent(), themeButton.getX()-e.getComponent().getX(), themeButton.getY()-e.getComponent().getY()+themeButton.getHeight());
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
                if(scheduleList.getSelectedIndex() == -1) {
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
