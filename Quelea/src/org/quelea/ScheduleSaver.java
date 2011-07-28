/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quelea;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.quelea.utils.QueleaProperties;
import org.quelea.utils.Utils;
import org.quelea.windows.main.MainPanel;

/**
 *
 * @author Michael
 */
public class ScheduleSaver {

    /**
     * Save the current schedule.
     * @param saveAs true if the file location should be specified, false if the current one should be used.
     */
    public void saveSchedule(boolean saveAs) {
        MainPanel mainpanel = Application.get().getMainWindow().getMainPanel();
        Schedule schedule = mainpanel.getSchedulePanel().getScheduleList().getSchedule();
        File file = schedule.getFile();
        if (saveAs || file == null) {
            JFileChooser chooser = Utils.getScheduleFileChooser();
            if (chooser.showSaveDialog(Application.get().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                String extension = QueleaProperties.get().getScheduleExtension();
                file = chooser.getSelectedFile();
                if (!file.getName().endsWith("." + extension)) {
                    file = new File(file.getAbsoluteFile() + "." + extension);
                }
                if (file.exists()) {
                    int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), file.getName() + " already exists. Overwrite?",
                            "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
                    if (result != JOptionPane.YES_OPTION) {
                        file = null;
                    }
                }
                schedule.setFile(file);
            }
        }
        if (file != null) {
            boolean success = schedule.writeToFile();
            if (!success) {
                JOptionPane.showMessageDialog(Application.get().getMainWindow(), "Couldn't save schedule", "Error", JOptionPane.ERROR_MESSAGE, null);
            }
        }
    }

}
