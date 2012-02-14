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
import org.quelea.ScheduleSaver;

/**
 * The save schedule action listener.
 * @author Michael
 */
public class SaveScheduleActionListener implements ActionListener {
    
    private boolean saveas;
    
    /**
     * Create a new save schedule action listener.
     * @param saveas true if we're doing a "save as", false if it's just a
     * save.
     */
    public SaveScheduleActionListener(boolean saveas) {
        this.saveas = saveas;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        new ScheduleSaver().saveSchedule(saveas);
    }
    
}
