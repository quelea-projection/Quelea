package org.quelea.windows.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.quelea.Application;

/**
 *
 * @author Michael
 */
public class RemoveSongScheduleActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        ScheduleList scheduleList = Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();
        int selectedIndex = scheduleList.getSelectedIndex();
        scheduleList.removeCurrentItem();
        if (selectedIndex == scheduleList.getModel().getSize()) {
            selectedIndex--;
        }
        if (selectedIndex >= 0) {
            scheduleList.setSelectedIndex(selectedIndex);
        }
    }
}
