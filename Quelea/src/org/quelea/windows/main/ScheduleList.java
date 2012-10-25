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
package org.quelea.windows.main;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.quelea.QueleaApp;
import org.quelea.Schedule;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.utils.LoggerUtils;
import org.quelea.windows.library.ContextMenuListCell;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * <p/>
 * @author Michael
 */
public class ScheduleList extends ListView<Displayable> {

    private Schedule schedule;
    private final ScheduleSongPopupMenu popupMenu;
    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * A direction; either up or down. Used for rearranging the order of items
     * in the service.
     */
    public enum Direction {

        UP, DOWN
    }

    /**
     * Create a new schedule list.
     */
    public ScheduleList() {
        popupMenu = new ScheduleSongPopupMenu();
        Callback<ListView<Displayable>, ListCell<Displayable>> callback = new Callback<ListView<Displayable>, ListCell<Displayable>>() {
            @Override
            public ListCell<Displayable> call(ListView<Displayable> p) {
                return new ListCell<Displayable>() {
                    @Override
                    public void updateItem(Displayable item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null) {
                            setGraphic(item.getPreviewIcon());
                            setText(item.getPreviewText());
                        }
                    }
                };
            }
        };
        setCellFactory(ContextMenuListCell.<Displayable>forListView(popupMenu, callback));
        schedule = new Schedule();
        setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCharacter().equals(" ")) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().goLive();
                }
            }
        });
        itemsProperty().addListener(new ChangeListener<ObservableList<Displayable>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<Displayable>> ov, ObservableList<Displayable> t, ObservableList<Displayable> t1) {
                if(isEmpty()) {
                    QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().clear();
                }
                if(getSelectionModel().isEmpty()) {
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().clear();
                }
                else {
                    Displayable newDisplayable = getSelectionModel().getSelectedItem();
                    QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().setDisplayable(newDisplayable, 0);
                }
            }
        });
    }

    public void add(Displayable displayable) {
        itemsProperty().get().add(displayable);
    }

    /**
     * Get the current schedule in use on this list.
     * <p/>
     * @return the current schedule in use on this list.
     */
    public Schedule getSchedule() {
        boolean equal = true;
        if(itemsProperty().get().size() == schedule.getSize()) {
            for(int i = 0; i < itemsProperty().get().size(); i++) {
                if(!itemsProperty().get().get(i).equals(schedule.getDisplayable(i))) {
                    equal = false;
                }
            }
        }
        else {
            equal = false;
        }
        if(equal) {
            return schedule;
        }
        schedule.clear();
        for(int i = 0; i < itemsProperty().get().size(); i++) {
            schedule.add(itemsProperty().get().get(i));
        }
        return schedule;
    }

    /**
     * Erase everything in the current schedule and set the contents of this
     * list to the current schedule.
     * <p/>
     * @param schedule the schedule.
     */
    public void setSchedule(Schedule schedule) {
        clearSchedule();
        for(Displayable displayable : schedule) {
            if(displayable instanceof Song) {
                ((Song) displayable).matchID();
            }
            itemsProperty().get().add(displayable);
        }
        this.schedule = schedule;
    }

    /**
     * Clear the current schedule without warning.
     */
    public void clearSchedule() {
        itemsProperty().get().clear();
    }

    /**
     * Get the popup menu on this schedule list.
     * <p/>
     * @return the popup menu.
     */
    public ScheduleSongPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Determine whether the schedule list is empty.
     * <p/>
     * @return true if it's empty, false otherwise.
     */
    public boolean isEmpty() {
        return itemsProperty().get().isEmpty();
    }

    /**
     * Remove the currently selected item in the list, or do nothing if there is
     * no selected item.
     */
    public void removeCurrentItem() {
        int selectedIndex = selectionModelProperty().get().getSelectedIndex();
        if(selectedIndex != -1) {
            Displayable d = selectionModelProperty().get().getSelectedItem();
            Displayable live = QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().getDisplayable();
            if(d == live) {
                QueleaApp.get().getMainWindow().getMainPanel().getLivePanel().clear();
            }
            Displayable preview = QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().getDisplayable();
            if(d == preview) {
                QueleaApp.get().getMainWindow().getMainPanel().getPreviewPanel().clear();
            }
            if(d == null) {
                LOGGER.log(Level.WARNING, "Tried to remove null from schedule?");
            }
            else {
                d.dispose();
            }
            itemsProperty().get().remove(selectedIndex);
        }
    }

    /**
     * Move the currently selected item in the list in the specified direction.
     * <p/>
     * @param direction the direction to move the selected item.
     */
    public void moveCurrentItem(Direction direction) {
        int selectedIndex = selectionModelProperty().get().getSelectedIndex();
        if(selectedIndex == -1) { //Nothing selected
            return;
        }
        if(direction == Direction.UP && selectedIndex > 0) {
            Displayable temp = itemsProperty().get().get(selectedIndex - 1);
            itemsProperty().get().set(selectedIndex - 1, itemsProperty().get().get(selectedIndex));
            itemsProperty().get().set(selectedIndex, temp);
            selectionModelProperty().get().select(selectedIndex - 1);
        }
        if(direction == Direction.DOWN && selectedIndex < itemsProperty().get().size() - 1) {
            Displayable temp = itemsProperty().get().get(selectedIndex + 1);
            itemsProperty().get().set(selectedIndex + 1, itemsProperty().get().get(selectedIndex));
            itemsProperty().get().set(selectedIndex, temp);
            selectionModelProperty().get().select(selectedIndex + 1);
        }
    }
}
