package org.quelea.windows.main.ribbon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.quelea.Application;
import org.quelea.ScheduleSaver;
import org.quelea.print.Printer;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;

/**
 * Manages populating the ribbon.
 * @author Michael
 */
public class RibbonPopulator {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private JRibbon ribbon;
    private final ScheduleTask scheduleTask;
    private final DatabaseTask databaseTask;
    private final ProjectorTask projectorTask;
    private final RibbonMenu ribbonMenu;

    /**
     * Create a new ribbon populator for a given ribbon.
     * @param ribbon the ribbon to populate.
     */
    public RibbonPopulator(JRibbon ribbon) {
        this.ribbon = ribbon;
        LOGGER.log(Level.INFO, "Creating schedule ribbon task");
        scheduleTask = new ScheduleTask();
        LOGGER.log(Level.INFO, "Creating database ribbon task");
        databaseTask = new DatabaseTask();
        LOGGER.log(Level.INFO, "Creating projector ribbon task");
        projectorTask = new ProjectorTask();
        LOGGER.log(Level.INFO, "Creating ribbon menu");
        ribbonMenu = new RibbonMenu();
        LOGGER.log(Level.INFO, "Done populating ribbon.");
    }

    public void populate() {
        ribbon.addTask(scheduleTask);
        ribbon.addTask(databaseTask);
        ribbon.addTask(projectorTask);
        ribbon.setApplicationMenu(ribbonMenu);
        addTaskbar();
    }

    private void addTaskbar() {
        JButton saveButton = new JButton(Utils.getImageIcon("icons/filesave.png", 15, 15));
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ScheduleSaver().saveSchedule(false);
            }
        });
        saveButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        saveButton.setBorderPainted(false);
        saveButton.setToolTipText("Save schedule");
        ribbon.addTaskbarComponent(saveButton);

        JButton printButton = new JButton(Utils.getImageIcon("icons/fileprint.png", 15, 15));
        printButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Printer.getInstance().print(Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule());
            }
        });
        printButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        printButton.setBorderPainted(false);
        printButton.setToolTipText("Print schedule");
        ribbon.addTaskbarComponent(printButton);
    }
}
