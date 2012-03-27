/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.windows.main.actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.quelea.Application;
import org.quelea.displayable.PresentationDisplayable;
import org.quelea.languages.LabelGrabber;
import org.quelea.powerpoint.PowerpointFileFilter;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.Utils;
import org.quelea.windows.main.StatusPanel;

/**
 * The action listener for adding a powerpoint presentation.
 * @author Michael
 */
public class AddPowerpointActionListener implements ActionListener {
    
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Add the presentation
     * @param e the event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new PowerpointFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.showOpenDialog(Application.get().getMainWindow());
        File file = fileChooser.getSelectedFile();
        if(file != null) {
            new Thread() {

                private StatusPanel panel;
                private boolean halt;

                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            panel = Application.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("adding.presentation.status"));
                            panel.getProgressBar().setIndeterminate(true);
                            panel.getCancelButton().addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    panel.done();
                                    halt = true;
                                }
                            });
                        }
                    });
                    try {
                        final PresentationDisplayable displayable = new PresentationDisplayable(fileChooser.getSelectedFile());
                        if(!halt) {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    Application.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList().getModel().addElement(displayable);
                                }
                            });
                        }
                    }
                    catch (OfficeXmlFileException ex) {
                        if(!halt) {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(Application.get().getMainWindow(), LabelGrabber.INSTANCE.getLabel("pptx.error"), LabelGrabber.INSTANCE.getLabel("adding.presentation.error.title"), JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        }
                    }
                    catch (RuntimeException ex) {
                        LOGGER.log(Level.WARNING, "Couldn't import presentation", ex);
                    }
                    while(panel == null) {
                        Utils.sleep(1000); //Quick bodge but hey, it works
                    }
                    panel.done();
                }
            }.start();
        }
    }
}