package org.quelea.windows.library;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import org.quelea.SongDatabase;
import org.quelea.SortedListModel;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.displayable.TextSection;
import org.quelea.displayable.TransferDisplayable;
import org.quelea.utils.DatabaseListener;

/**
 * The list that displays the songs in the library.
 * @author Michael
 */
public class LibrarySongList extends JList implements DatabaseListener {

    /**
     * The toString() method on song returns XML, we don't want to print that
     * so this is a bit of a hack to display the title instead.
     */
    private static class SongRenderer extends DefaultListCellRenderer {

        /**
         * @inheritDoc
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Song s = new Song((Song) value) {

                @Override
                public String toString() {
                    return getTitle();
                }
            };
            return super.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
        }
    }
    private final SortedListModel tempModel;
    private final SortedListModel fullModel;
    private final LibraryPopupMenu popupMenu;

    /**
     * Create a new library song list.
     */
    public LibrarySongList() {
        super(new SortedListModel());
        setCellRenderer(new SongRenderer());
        SortedListModel model = (SortedListModel) super.getModel();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupMenu = new LibraryPopupMenu();
        fullModel = model;
        tempModel = new SortedListModel();
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DragGestureListener() {

            public void dragGestureRecognized(DragGestureEvent dge) {
                if (getSelectedValue()!=null) {
                    dge.startDrag(DragSource.DefaultCopyDrop, new TransferDisplayable((Displayable) getModel().getElementAt(locationToIndex(dge.getDragOrigin()))));
                }
            }
        });
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
                if (e.isPopupTrigger()) {
                    int index = locationToIndex(e.getPoint());
                    Rectangle Rect = getCellBounds(index, index);
                    index = Rect.contains(e.getPoint().x, e.getPoint().y) ? index : -1;
                    if (index != -1) {
                        setSelectedIndex(index);
                        popupMenu.show(LibrarySongList.this, e.getX(), e.getY());
                    }
                }
            }
        });
        update();
        SongDatabase.get().registerDatabaseListener(this);
    }

    /**
     * Filter the results in this list by a specific search term.
     * @param search the search term to use.
     */
    public void filter(String search) {
        search = search.toLowerCase();
        tempModel.clear();
        for (int i = 0; i < fullModel.getSize(); i++) {
            Song s = (Song) fullModel.getElementAt(i);
            if (s.search(search)) {
                tempModel.add(s);
            }
        }
        setModel(tempModel);
    }

    /**
     * Get the popup menu associated with this list.
     * @return the popup menu.
     */
    public LibraryPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Get the tooltip text for items in the list.
     * @param evt the mouse event generating the tooltip.
     * @return the tooltip text.
     */
    @Override
    public String getToolTipText(MouseEvent evt) {
        int index = locationToIndex(evt.getPoint());
        if (index < 0) {
            return null;
        }
        Song song = (Song) getModel().getElementAt(index);
        TextSection[] sections = song.getSections();
        if (sections.length > 0) {
            return sections[0].getText()[0] + "...";
        }
        return null;
    }

//    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
//    private final Runnable updateRunnable = new Runnable() {
//
//        public void run() {
//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    SortedListModel model = (SortedListModel) getModel();
//                    model.clear();
//                    for (Song song : SongDatabase.get().getSongs()) {
//                        model.add(song);
//                    }
//                }
//            });
//        }
//    };
    /**
     * Update the contents of the list.
     */
    public final void update() {
//        executor.remove(updateRunnable);
//        executor.schedule(updateRunnable, 1, TimeUnit.SECONDS);
        SortedListModel model = (SortedListModel) getModel();
        model.clear();
        for (Song song : SongDatabase.get().getSongs()) {
            model.add(song);
        }
    }
}
