package org.quelea.windows.main;

import org.quelea.Schedule;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.displayable.TransferDisplayable;
import org.quelea.utils.QueleaProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The schedule list, all the items that are to be displayed in the service.
 * @author Michael
 */
public class ScheduleList extends JList {

    private Schedule schedule;
    private final SchedulePopupMenu popupMenu;
    private final Color originalSelectionColour;
    private boolean internalDrag;

    /**
     * A direction; either up or down. Used for rearranging the order of items in the service.
     */
    public enum Direction {

        UP, DOWN
    }

    /**
     * Used for displaying summaries of items in the service in the schedule list.
     */
    private static class SummaryRenderer extends JLabel implements ListCellRenderer {

        /**
         * @inheritDoc
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setBorder(new EmptyBorder(5, 5, 5, 5));
            Displayable displayableValue = (Displayable) value;
            setText(displayableValue.getPreviewText());
            setIcon(displayableValue.getPreviewIcon());
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
        originalSelectionColour = getSelectionBackground();
        addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (getModel().getSize() > 0) {
                    setSelectionBackground(QueleaProperties.get().getActiveSelectionColor());
                }
            }

            public void focusLost(FocusEvent e) {
                setSelectionBackground(originalSelectionColour);
            }
        });
        popupMenu = new SchedulePopupMenu();
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            /**
             * Display the popup if appropriate. This should be done when the
             * mouse is pressed and released for platform-independence.
             */
            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger() && e.getPoint() != null) {
                    int index = locationToIndex(e.getPoint());
                    Rectangle Rect = getCellBounds(index, index);
                    index = Rect.contains(e.getPoint().x, e.getPoint().y) ? index : -1;
                    if (index != -1 && getModel().getElementAt(index) instanceof Song && ((Song) getModel().getElementAt(index)).getID() != -1) {
                        setSelectedIndex(index);
                        popupMenu.show(ScheduleList.this, e.getX(), e.getY());
                    }
                }
            }
        });
        setDropMode(DropMode.INSERT);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DragGestureListener() {

            public void dragGestureRecognized(DragGestureEvent dge) {
                if (getSelectedValue() != null) {
                    internalDrag = true;
                    dge.startDrag(DragSource.DefaultMoveDrop, new TransferDisplayable((Displayable) getModel().getElementAt(locationToIndex(dge.getDragOrigin()))));
                }
            }
        });
        setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(TransferDisplayable.DISPLAYABLE_FLAVOR);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }


                Transferable transferable = support.getTransferable();
                Displayable data;
                try {
                    data = (Displayable) transferable.getTransferData(TransferDisplayable.DISPLAYABLE_FLAVOR);
                }
                catch (Exception ex) {
                    return false;
                }

                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int index = dl.getIndex();
                if (index == -1) {
                    index = getModel().getSize();
                }

                DefaultListModel model = (DefaultListModel) getModel();
                if (internalDrag) {
                    int val = Math.abs(getSelectedIndex() - index);
                    if (getSelectedIndex() < index) {
                        for (int i = 0; i < val; i++) {
                            moveCurrentItem(Direction.DOWN);
                        }
                    }
                    else {
                        for (int i = 0; i < val; i++) {
                            moveCurrentItem(Direction.UP);
                        }
                    }
                }
                else {
                    model.add(index, data);
                }
                internalDrag = false;
                return true;
            }

            @Override
            protected void exportDone(JComponent c, Transferable data, int action) {
                ((DefaultListModel) getModel()).remove(getSelectedIndex());
            }
        });
        schedule = new Schedule();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new SummaryRenderer());
    }

    /**
     * Get the current schedule in use on this list.
     * @return
     */
    public Schedule getSchedule() {
        schedule.clear();
        for (int i = 0; i < getModel().getSize(); i++) {
            schedule.add((Displayable) getModel().getElementAt(i));
        }
        return schedule;
    }

    /**
     * Erase everything in the current schedule and set the contents of this list to the current schedule.
     * @param schedule the schedule.
     */
    public void setSchedule(Schedule schedule) {
        clearSchedule();
        for (Displayable displayable : schedule) {
            if (displayable instanceof Song) {
                ((Song) displayable).matchID();
            }
            ((DefaultListModel) getModel()).addElement(displayable);
        }
        this.schedule = schedule;
    }

    /**
     * Clear the current schedule without warning.
     */
    public void clearSchedule() {
        ((DefaultListModel) getModel()).clear();
    }

    /**
     * Get the popup menu on this schedule list.
     * @return the popup menu.
     */
    public SchedulePopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Determine whether the schedule list is empty.
     * @return true if it's empty, false otherwise.
     */
    public boolean isEmpty() {
        return getModel().getSize() == 0;
    }

    /**
     * Remove the currently selected item in the list, or do nothing if there is no selected item.
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
