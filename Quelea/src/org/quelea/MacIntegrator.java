/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea;

import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

/**
 *
 * @author mjrb5
 */
public class MacIntegrator implements ApplicationListener {
    
    public void run() {
        org.simplericity.macify.eawt.Application macApp = new org.simplericity.macify.eawt.DefaultApplication();
        macApp.addApplicationListener(this);
    }

    @Override
    public void handleAbout(ApplicationEvent ae) {
        System.out.println("ABOUT");
    }

    @Override
    public void handleOpenApplication(ApplicationEvent ae) {
        
    }

    @Override
    public void handleOpenFile(ApplicationEvent ae) {
        
    }

    @Override
    public void handlePreferences(ApplicationEvent ae) {
        
    }

    @Override
    public void handlePrintFile(ApplicationEvent ae) {
        
    }

    @Override
    public void handleQuit(ApplicationEvent ae) {
        
    }

    @Override
    public void handleReOpenApplication(ApplicationEvent ae) {
        
    }
    
}
