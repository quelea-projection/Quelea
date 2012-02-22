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
package org.quelea.windows.library;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.quelea.SongDatabase;
import org.quelea.SortedListModel;
import org.quelea.displayable.Displayable;
import org.quelea.displayable.Song;
import org.quelea.displayable.TextSection;
import org.quelea.displayable.TransferDisplayable;
import org.quelea.lucene.SearchIndex;
import org.quelea.utils.DatabaseListener;
import org.quelea.utils.LoggerUtils;
import org.quelea.utils.QueleaProperties;

/**
 * The list that displays the songs in the library.
 *
 * @author Michael
 */
public class LibrarySongList extends JList<Song> implements DatabaseListener {

    /**
     * The toString() method on song returns XML, we don't want to print that so
     * this is a bit of a hack to display the title instead.
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
                    return getListHTML();
                }
            };
            return super.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
        }
    }
    private static final Logger LOGGER = LoggerUtils.getLogger();
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
        Color inactiveColor = QueleaProperties.get().getInactiveSelectionColor();
        if(inactiveColor == null) {
            originalSelectionColour = getSelectionBackground();
        }
        else {
            originalSelectionColour = inactiveColor;
        }
        addFocusListener(new FocusListener() {
            
            @Override
            public void focusGained(FocusEvent e) {
                if(getModel().getSize() > 0) {
                    setSelectionBackground(QueleaProperties.get().getActiveSelectionColor());
                }
            }
            
            @Override
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
                if(getSelectedValue() != null) {
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
                if(e.isPopupTrigger() && LibrarySongList.this.popup) {
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
        update();
        SongDatabase.get().registerDatabaseListener(this);
    }
    private ExecutorService filterService = Executors.newSingleThreadExecutor();
    private Future<?> filterFuture;

    /**
     * Filter the results in this list by a specific search term.
     *
     * @param search the search term to use.
     */
    public void filter(final String search) {
        if(filterFuture != null) {
            filterFuture.cancel(true);
        }
        filterFuture = filterService.submit(new Runnable() {
            
            @Override
            public void run() {
                final DefaultListModel<Song> model = new DefaultListModel<>();
                
                Song[] titleSongs = SongDatabase.get().getIndex().filterSongs(search, SearchIndex.FilterType.TITLE);
                for(Song song : titleSongs) {
                    song.setLastSearch(search);
                    model.addElement(song);
                }
                Song[] lyricSongs = SongDatabase.get().getIndex().filterSongs(search, SearchIndex.FilterType.LYRICS);
                for(Song song : lyricSongs) {
                    song.setLastSearch(null);
                    model.addElement(song);
                }
                
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
     * Filter songs in this song list by a certain number of tags. Only songs
     * that contain all the tags will be displayed and pass the filter.
     *
     * @param filterTags the tags to filter on.
     */
    public void filterByTag(final List<String> filterTags) {
        if(filterFuture != null) {
            filterFuture.cancel(true);
        }
        filterFuture = filterService.submit(new Runnable() {
            
            @Override
            public void run() {
                final SortedListModel<Song> model = new SortedListModel<>();
                for(int i = 0; i < fullModel.getSize(); i++) {
                    boolean add = true;
                    Song s = fullModel.getElementAt(i);
                    String[] songTags = s.getTags();
                    for(String filterTag : filterTags) {
                        if(filterTag.trim().isEmpty()) {
                            continue;
                        }
                        boolean inPlace = false;
                        for(String songtag : songTags) {
                            if(filterTag.trim().equalsIgnoreCase(songtag.trim())) {
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
     *
     * @return the popup menu.
     */
    public LibraryPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Get the tooltip text for items in the list.
     *
     * @param evt the mouse event generating the tooltip.
     * @return the tooltip text.
     */
    @Override
    public String getToolTipText(MouseEvent evt) {
        int index = locationToIndex(evt.getPoint());
        if(index < 0) {
            return null;
        }
        Song song = getModel().getElementAt(index);
        TextSection[] sections = song.getSections();
        if(sections.length > 0 && sections[0] != null && sections[0].getText(false, false).length > 0) {
            return sections[0].getText(false, false)[0] + "...";
        }
        return null;
    }

    /**
     * Update the contents of the list.
     */
    @Override
    public final void update() {
        final Song[] songs = SongDatabase.get().getSongs();
        Runnable runner = new Runnable() {
            
            @Override
            public void run() {
                fullModel.clear();
                for(Song song : songs) {
                    fullModel.add(song);
                }
            }
        };
        
        if(SwingUtilities.isEventDispatchThread()) {
            runner.run();
        }
        else {
            SwingUtilities.invokeLater(runner);
        }
        
    }
}
