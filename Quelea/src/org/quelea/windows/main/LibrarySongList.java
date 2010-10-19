package org.quelea.windows.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.quelea.SortedListModel;
import org.quelea.display.Song;

/**
 * The list that displays the songs in the library.
 * @author Michael
 */
public class LibrarySongList extends JList {

    private SortedListModel tempModel;
    private SortedListModel fullModel;
    private LibraryPopupMenu popupMenu;

    /**
     * Create a new library song list.
     */
    public LibrarySongList() {
        super(new SortedListModel());
        SortedListModel model = (SortedListModel)super.getModel();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupMenu = new LibraryPopupMenu();
        fullModel = model;
        tempModel = new SortedListModel();
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            private void checkPopup(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)
                        && !isSelectionEmpty()
                        && locationToIndex(e.getPoint())
                        == getSelectedIndex()) {
                    popupMenu.show(LibrarySongList.this, e.getX(), e.getY());
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
        for(int i=0 ; i<fullModel.getSize() ; i++) {
            Song s = (Song)fullModel.getElementAt(i);
            if(s.search(search)) {
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

}
