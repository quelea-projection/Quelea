/* 
 * This file is part of Quelea, free projection software for churches.
 * Copyright (C) 2011 Michael Berry
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
package org.quelea.tags;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.quelea.languages.LabelGrabber;
import org.quelea.windows.library.LibrarySongList;

/**
 * A dialog used for finding songs with certain tags.
 * @author Michael
 */
public class TagDialog extends Stage {

    private TagEntryPanel tagEntryPanel;
    private LibrarySongList list;

    /**
     * Create a new tag dialog.
     */
    public TagDialog() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(LabelGrabber.INSTANCE.getLabel("filter.tag"));
        
        getIcons().add(new Image("file:icons/tag.png", 16, 16, false, true));
        list = new LibrarySongList(false);
        
        BorderPane mainPane = new BorderPane();
        
        tagEntryPanel = new TagEntryPanel(list, false, true);
        mainPane.setTop(tagEntryPanel);
        mainPane.setCenter(list);
        
        setScene(new Scene(mainPane));
    }
    
    /**
     * Force a reload of all the tags in the panel.
     */
    public void reloadTags() {
        tagEntryPanel.reloadTags();
    }
}
