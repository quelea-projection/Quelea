package org.quelea.windows.library;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import org.quelea.SongDatabase;
import org.quelea.SortedListModel;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.displayable.TextSection;
import org.quelea.displayable.TransferDisplayable;
import org.quelea.utils.DatabaseListener;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.quelea.utils.QueleaProperties;

/**
 * The list that displays the songs in the library.
 * @author Michael
 */
public class LibrarySongList extends JList<Song> implements DatabaseListener {

    /**
     * The toString() method on song returns XML, we don't want to print that so this is a bit of a hack to display the
     * title instead.
     */
    private static class SongRenderer extends DefaultListCellRenderer {

        /**
         * @inheritDoc
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
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
    private final SortedListModel<Song> fullModel;
    private final LibraryPopupMenu popupMenu;
    private final Color originalSelectionColour;
    private final boolean popup;

    /**
     * Create a new library song list.
     */
    public LibrarySongList(boolean popup) {
        super(new SortedListModel<Song>());
        this.popup = popup;
        setCellRenderer(new SongRenderer());
        originalSelectionColour = getSelectionBackground();
        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (getModel().getSize() > 0) {
                    setSelectionBackground(QueleaProperties.get().getActiveSelectionColor());
                }
            }

            public void focusLost(FocusEvent e) {
                setSelectionBackground(originalSelectionColour);
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupMenu = new LibraryPopupMenu();
        fullModel = (SortedListModel<Song>) super.getModel();
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DragGestureListener() {

            @Override
            public void dragGestureRecognized(DragGestureEvent dge) {
                if (getSelectedValue() != null) {
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
                if (e.isPopupTrigger()&&LibrarySongList.this.popup) {
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
    private ExecutorService filterService = Executors.newSingleThreadExecutor();
    private Future<?> filterFuture;

    /**
     * Filter the results in this list by a specific search term.
     * @param search the search term to use.
     * @param beep true if the system should beep if the search returns 0 results,
     * false otherwise. At present ignored - never beep.
     */
    public void filter(final String search, final boolean beep) {
        if (filterFuture != null) {
            filterFuture.cancel(true);
        }
        filterFuture = filterService.submit(new Runnable() {

            public void run() {
                final SortedListModel<Song> model = new SortedListModel<>();
                for (int i = 0; i < fullModel.getSize(); i++) {
                    Song s = fullModel.getElementAt(i);
                    if (s.search(search.toLowerCase())) {
                        model.add(s);
                    }
                }
//                if (beep && model.getSize() == 0) {
//                    java.awt.Toolkit.getDefaultToolkit().beep();
//                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        LibrarySongList.this.setModel(model);
                    }
                });
            }
        });

    }

    public void filterByTag(final List<String> filterTags, final boolean beep) {
        if (filterFuture != null) {
            filterFuture.cancel(true);
        }
        filterFuture = filterService.submit(new Runnable() {

            public void run() {
                final SortedListModel<Song> model = new SortedListModel<>();
                for (int i = 0; i < fullModel.getSize(); i++) {
                    boolean add = true;
                    Song s = fullModel.getElementAt(i);
                    String[] songTags = s.getTags();
                    for (String filterTag : filterTags) {
                        if (filterTag.trim().isEmpty()) {
                            continue;
                        }
                        boolean inPlace = false;
                        for (String songtag : songTags) {
                            if (filterTag.trim().equalsIgnoreCase(songtag.trim())) {
                                inPlace = true;
                            }
                        }
                        if(!inPlace) {
                            add = false;
                        }
                    }
                    if(add) {
                        model.add(s);
                    }
                }
//                if (beep && model.getSize() == 0) {
//                    java.awt.Toolkit.getDefaultToolkit().beep();
//                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        LibrarySongList.this.setModel(model);
                    }
                });
            }
        });

    }

    /**
     * Get the popup menu associated with this list.
     * @return the popup menu.
     */
    public LibraryPopupMenu getPopupMenu() {
        return popupMenu;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SortedListModel<Song> getModel() {
        return (SortedListModel<Song>) super.getModel();
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
        Song song = getModel().getElementAt(index);
        TextSection[] sections = song.getSections();
        if (sections.length > 0 && sections[0] != null && sections[0].getText(false, false).length > 0) {
            return sections[0].getText(false, false)[0] + "...";
        }
        return null;
    }

    /**
     * Update the contents of the list.
     */
    public final void update() {
        fullModel.clear();
        for (Song song : SongDatabase.get().getSongs()) {
            fullModel.add(song);
        }
    }
}
