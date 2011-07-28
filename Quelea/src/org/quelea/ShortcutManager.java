/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quelea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.quelea.print.Printer;
import org.quelea.windows.main.MainPanel;

/**
 * Manage adding the shortcuts for the application.
 * @author Michael
 */
public class ShortcutManager {

    /**
     * Add in the shortcuts to the main panel.
     */
    public void addShortcuts() {
        final MainPanel mainPanel = Application.get().getMainWindow().getMainPanel();
        mainPanel.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.getLibraryPanel().getLibrarySongPanel().getSearchBox().requestFocusInWindow();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        mainPanel.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ScheduleSaver().saveSchedule(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        mainPanel.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Printer.getInstance().print(Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getSchedule());
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
