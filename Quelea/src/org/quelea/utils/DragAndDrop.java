/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.utils;

import javafx.event.EventHandler;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import org.quelea.QueleaApp;
import org.quelea.windows.library.LibrarySongList;
import org.quelea.windows.main.ScheduleList;

/**
 * This class contains and controls all of the drag and drop functions associated with the UI
 * 
 * @author begoodwi
 */
class DragAndDrop {

    static public void enable() {
        setLibToSchedDD();
    }

    static private void setLibToSchedDD() {
        
        final LibrarySongList source = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel().getLibrarySongPanel().getSongList();
        final ScheduleList target = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel().getScheduleList();

        source.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                Dragboard db = source.startDragAndDrop(TransferMode.ANY);

                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString("");
                db.setContent(content);
                t.consume();
            }
        });
        
        target.setOnDragOver(new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent t) {
                /* accept it only if it is from source and if it has a string data */
                if (t.getGestureSource() == source) {
                    t.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                
                t.consume();
            }
        });

        
        target.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent t) {
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = t.getDragboard();
                //int id = Integer.parseInt(db.getString());
                //target.add(SongDatabase.get().getSong(id));
                target.add(source.getSelectedValue());
                
                /* let the source know whether the string was successfully 
                 * transferred and used */
                t.setDropCompleted(true);
                t.consume();
            }
        });
        
        
    }
}