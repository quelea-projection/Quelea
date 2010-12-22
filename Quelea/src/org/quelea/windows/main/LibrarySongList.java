package org.quelea.windows.main;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import org.quelea.SortedListModel;
import org.quelea.display.Song;
import org.quelea.display.SongSection;

/**
 * The list that displays the songs in the library.
 * @author Michael
 */
public class LibrarySongList extends JList {

    private final SortedListModel tempModel;
    private final SortedListModel fullModel;
    private final LibraryPopupMenu popupMenu;

    /**
     * Create a new library song list.
     */
    public LibrarySongList() {
        super(new SortedListModel());
        SortedListModel model = (SortedListModel) super.getModel();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupMenu = new LibraryPopupMenu();
        fullModel = model;
        tempModel = new SortedListModel();
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = locationToIndex(e.getPoint());
                    Rectangle Rect = getCellBounds(index, index);
                    index = Rect.contains(e.getPoint().x, e.getPoint().y) ? index : -1;
                    if(index != -1) {
                        setSelectedIndex(index);
                        popupMenu.show(LibrarySongList.this, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    /**
     * Filter the results in this list by a specific search term.
     * @param search the search term to use.
     */
    public void filter(String search) {
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
        SongSection[] sections = song.getSections();
        if (sections.length > 0) {
            return sections[0].getLyrics()[0] + "...";
        }
        return null;
    }
}
