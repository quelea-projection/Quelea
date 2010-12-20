package org.quelea.windows.main;

import java.awt.Component;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import org.quelea.Schedule;
import org.quelea.Utils;
import org.quelea.display.Displayable;
import org.quelea.display.Song;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * @author Michael
 */
public class ScheduleList extends JList {

    private Schedule schedule;

    /**
     * A direction; either up or down. Used for rearranging the order of items
     * in the service.
     */
    public enum Direction {

        UP, DOWN
    }

    /**
     * Used for displaying summaries of items in the service in the schedule
     * list.
     */
    private static class SummaryRenderer extends JLabel implements ListCellRenderer {

        /**
         * @inheritDoc
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (!(value instanceof Song)) {
                return new JLabel();
            }
            setBorder(new EmptyBorder(5, 5, 5, 5));
            Song songValue = (Song) value;
            setText("<html>" + songValue.getTitle() + "<br/><i>" + songValue.getAuthor() + "</i></html>");
            setIcon(Utils.getImageIcon("icons/lyrics.png"));
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    /**
     * Create a new schedule list with a given model.
     * @param model the model to display.
     */
    public ScheduleList(DefaultListModel model) {
        super(model);
        schedule = new Schedule();
        setDragEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new SummaryRenderer());
    }

    /**
     * Get the current schedule in use on this list.
     * @return
     */
    public Schedule getSchedule() {
        schedule.clear();
        for(int i=0 ; i<getModel().getSize() ; i++) {
            schedule.add((Displayable)getModel().getElementAt(i));
        }
        return schedule;
    }

    /**
     * Erase everything in the current schedule and set the contents of this
     * list to the current schedule.
     * @param schedule the schedule.
     */
    public void setSchedule(Schedule schedule) {
        clearSchedule();
        for(Displayable displayable : schedule) {
            ((DefaultListModel)getModel()).addElement(displayable);
        }
        this.schedule = schedule;
    }

    /**
     * Clear the current schedule without warning.
     */
    public void clearSchedule() {
        ((DefaultListModel)getModel()).clear();
    }

    /**
     * Determine whether the schedule list is empty.
     * @return true if it's empty, false otherwise.
     */
    public boolean isEmpty() {
        return getModel().getSize()==0;
    }

    /**
     * Remove the currently selected item in the list, or do nothing if there
     * is no selected item.
     */
    public void removeCurrentItem() {
        DefaultListModel model = (DefaultListModel) getModel();
        int selectedIndex = getSelectedIndex();
        if (selectedIndex != -1) {
            model.remove(getSelectedIndex());
        }
    }

    /**
     * Move the currently selected item in the list in the specified direction.
     * @param direction the direction to move the selected item.
     */
    public void moveCurrentItem(Direction direction) {
        DefaultListModel model = (DefaultListModel) getModel();
        int selectedIndex = getSelectedIndex();
        if (selectedIndex == -1) { //Nothing selected
            return;
        }
        if (direction == Direction.UP && selectedIndex > 0) {
            Object temp = model.get(selectedIndex - 1);
            model.set(selectedIndex - 1, model.get(selectedIndex));
            model.set(selectedIndex, temp);
            setSelectedIndex(selectedIndex - 1);
        }
        if (direction == Direction.DOWN && selectedIndex < model.getSize() - 1) {
            Object temp = model.get(selectedIndex + 1);
            model.set(selectedIndex + 1, model.get(selectedIndex));
            model.set(selectedIndex, temp);
            setSelectedIndex(selectedIndex + 1);
        }
    }
}
