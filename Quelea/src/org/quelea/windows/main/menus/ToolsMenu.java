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
package org.quelea.windows.main.menus;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.quelea.languages.LabelGrabber;
import org.quelea.utils.Utils;
import org.quelea.windows.main.actionlisteners.SearchBibleActionListener;
import org.quelea.windows.main.actionlisteners.ShowOptionsActionListener;
import org.quelea.windows.main.actionlisteners.ViewBibleActionListener;

/**
 * Quelea's tools menu.
 * @author Michael
 */
public class ToolsMenu extends JMenu {
    
    private JMenuItem searchBibleItem;
    private JMenuItem viewBibleItem;
    private JMenuItem optionsItem;
    
    /**
     * Create the tools menu.
     */
    public ToolsMenu() {
        super(LabelGrabber.INSTANCE.getLabel("tools.menu"));
        setMnemonic('t');
        
        viewBibleItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("view.bible.button"), Utils.getImageIcon("icons/bible.png", 20, 20));
//        viewBibleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        viewBibleItem.setMnemonic('v');
        viewBibleItem.addActionListener(new ViewBibleActionListener());
        add(viewBibleItem);
        
        searchBibleItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("search.bible.button"), Utils.getImageIcon("icons/bible.png", 20, 20));
//        viewBibleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        searchBibleItem.setMnemonic('s');
        searchBibleItem.addActionListener(new SearchBibleActionListener());
        add(searchBibleItem);
        
        optionsItem = new JMenuItem(LabelGrabber.INSTANCE.getLabel("options.button"), Utils.getImageIcon("icons/options.png", 20, 20));
        optionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        optionsItem.setMnemonic('o');
        optionsItem.addActionListener(new ShowOptionsActionListener());
        add(optionsItem);
    }
    
}
