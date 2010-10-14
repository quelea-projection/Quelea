package org.quelea.mainwindow;

import java.awt.Component;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import org.quelea.Utils;
import org.quelea.display.components.Song;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * @author Michael
 */
public class ScheduleList extends JList {

    /**
     * A direction; either up or down. Used for rearranging the order of items
     * in the service.
     */
    public enum Direction {UP, DOWN}

    /**
     * Used for displaying summaries of items in the service in the schedule
     * list.
     */
    private static class SummaryRenderer extends JLabel implements ListCellRenderer {

        /**
         * @inheritDoc
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value==null || !(value instanceof Song)) {
                return new JLabel();
            }
            setBorder(new EmptyBorder(5, 5, 5, 5));
            Song songValue = (Song)value;
            setText("<html>"+songValue.getTitle()+"<br/><i>"+songValue.getAuthor()+"</i></html>");
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
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new SummaryRenderer());
    }

    /**
     * Remove the currently selected item in the list, or do nothing if there
     * is no selected item.
     */
    public void removeCurrentItem() {
        DefaultListModel model = (DefaultListModel) getModel();
        int selectedIndex = getSelectedIndex();
        if(selectedIndex != -1) {
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
        if(selectedIndex == -1) { //Nothing selected
            return;
        }
        if(direction==Direction.UP && selectedIndex > 0) {
            Object temp = model.get(selectedIndex-1);
            model.set(selectedIndex-1, model.get(selectedIndex));
            model.set(selectedIndex, temp);
            setSelectedIndex(selectedIndex-1);
        }
        if (direction == Direction.DOWN && selectedIndex < model.getSize()-1) {
            Object temp = model.get(selectedIndex + 1);
            model.set(selectedIndex + 1, model.get(selectedIndex));
            model.set(selectedIndex, temp);
            setSelectedIndex(selectedIndex + 1);
        }
    }

}
