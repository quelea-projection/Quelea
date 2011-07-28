package org.quelea.windows.main.ribbon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.quelea.Application;
import org.quelea.Schedule;
import org.quelea.ScheduleSaver;
import org.quelea.print.Printer;
import org.quelea.utils.Utils;
import org.quelea.windows.help.AboutDialog;
import org.quelea.windows.main.MainPanel;
import org.quelea.windows.options.OptionsDialog;

/**
 *
 * @author Michael
 */
public class RibbonMenu extends RibbonApplicationMenu {
    
    private final OptionsDialog optionsDialog;
    private final AboutDialog aboutDialog;
    

    public RibbonMenu() {
        optionsDialog = new OptionsDialog(Application.get().getMainWindow());
        aboutDialog = new AboutDialog(Application.get().getMainWindow());
        
        RibbonApplicationMenuEntryPrimary newMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/filenew.png", 100, 100), "New Schedule", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmClear()) {
                    Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().clearSchedule();
                }
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        addMenuEntry(newMenuEntry);
        RibbonApplicationMenuEntryPrimary openMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/fileopen.png", 100, 100), "Open Schedule", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmClear()) {
                    JFileChooser chooser = Utils.getScheduleFileChooser();
                    if (chooser.showOpenDialog(Application.get().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                        Schedule schedule = Schedule.fromFile(chooser.getSelectedFile());
                        if (schedule == null) {
                            JOptionPane.showMessageDialog(Application.get().getMainWindow(),
                                    "There was a problem opening the schedule. Perhaps it's corrupt, or is not a schedule saved by Quelea.",
                                    "Error opening schedule", JOptionPane.ERROR_MESSAGE, null);
                        } else {
                            Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().setSchedule(schedule);
                        }
                    }
                }
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        addMenuEntry(openMenuEntry);
        RibbonApplicationMenuEntryPrimary saveMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/filesave.png", 100, 100), "Save Schedule", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ScheduleSaver().saveSchedule(false);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        addMenuEntry(saveMenuEntry);
        RibbonApplicationMenuEntryPrimary saveAsMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/filesaveas.png", 100, 100), "Save Schedule as...", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ScheduleSaver().saveSchedule(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        addMenuEntry(saveAsMenuEntry);
        RibbonApplicationMenuEntryPrimary printMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/fileprint.png", 100, 100), "Print Schedule", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Printer.getInstance().print(Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule());
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        addMenuEntry(printMenuEntry);
        RibbonApplicationMenuEntryPrimary optionsMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/options.png", 100, 100), "Options", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                optionsDialog.setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        addMenuEntry(optionsMenuEntry);
        RibbonApplicationMenuEntryPrimary exitMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/exit.png", 100, 100), "Exit", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        addMenuEntry(exitMenuEntry);
    }
    
    /**
     * Confirm whether it's ok to clear the current schedule.
     * @return true if this is ok, false otherwise.
     */
    private boolean confirmClear() {
        MainPanel mainpanel = Application.get().getMainWindow().getMainPanel();
        if (mainpanel.getSchedulePanel().getScheduleList().isEmpty()) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(Application.get().getMainWindow(), "This will clear the current schedule. Is this OK?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
        if (result == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }
}
