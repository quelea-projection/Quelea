package org.quelea.windows.main.ribbon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import org.quelea.windows.main.ribbon.secondPanels.ExitPanelDrawer;
import org.quelea.windows.main.ribbon.secondPanels.NewPanelDrawer;
import org.quelea.windows.main.ribbon.secondPanels.OpenPanelDrawer;
import org.quelea.windows.main.ribbon.secondPanels.OptionsPanelDrawer;
import org.quelea.windows.main.ribbon.secondPanels.PrintPanelDrawer;
import org.quelea.windows.main.ribbon.secondPanels.SaveAsPanelDrawer;
import org.quelea.windows.main.ribbon.secondPanels.SavePanelDrawer;
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
        newMenuEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            @Override
            public void menuEntryActivated(JPanel pnl) {
                new NewPanelDrawer().draw(pnl);
            }
        });
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
        openMenuEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            @Override
            public void menuEntryActivated(JPanel pnl) {
                new OpenPanelDrawer().draw(pnl);
            }
        });
        addMenuEntry(openMenuEntry);
        RibbonApplicationMenuEntryPrimary saveMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/filesave.png", 100, 100), "Save Schedule", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ScheduleSaver().saveSchedule(false);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        saveMenuEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            @Override
            public void menuEntryActivated(JPanel pnl) {
                new SavePanelDrawer().draw(pnl);
            }
        });
        addMenuEntry(saveMenuEntry);
        RibbonApplicationMenuEntryPrimary saveAsMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/filesaveas.png", 100, 100), "Save Schedule as...", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ScheduleSaver().saveSchedule(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        saveAsMenuEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            @Override
            public void menuEntryActivated(JPanel pnl) {
                new SaveAsPanelDrawer().draw(pnl);
            }
        });
        addMenuEntry(saveAsMenuEntry);
        RibbonApplicationMenuEntryPrimary printMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/fileprint.png", 100, 100), "Print Schedule", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Printer.getInstance().print(Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule());
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        printMenuEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            @Override
            public void menuEntryActivated(JPanel pnl) {
                new PrintPanelDrawer().draw(pnl);
            }
        });
        addMenuEntry(printMenuEntry);
        RibbonApplicationMenuEntryPrimary optionsMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/options.png", 100, 100), "Options", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                optionsDialog.setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        optionsMenuEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            @Override
            public void menuEntryActivated(JPanel pnl) {
                new OptionsPanelDrawer().draw(pnl);
            }
        });
        addMenuEntry(optionsMenuEntry);
        RibbonApplicationMenuEntryPrimary exitMenuEntry = new RibbonApplicationMenuEntryPrimary(
                RibbonUtils.getRibbonIcon("icons/exit.png", 100, 100), "Exit", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        exitMenuEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {

            @Override
            public void menuEntryActivated(JPanel pnl) {
                new ExitPanelDrawer().draw(pnl);
            }
        });
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
