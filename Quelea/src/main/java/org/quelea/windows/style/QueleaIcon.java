/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.style;

import java.util.Locale;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Vector icons used in the Quelea user interface.
 * <p>
 * The shape of each icon is defined in {@code org/quelea/style/icons.css}
 * (as {@code .icon-<name>}) and its colour comes from the active theme, so
 * one icon works in both light and dark mode and stays sharp at any size.
 * Usage:
 * <pre>
 *     Button save = new Button("", QueleaIcon.SAVE.create());
 * </pre>
 *
 * @author Quelea contributors
 */
public enum QueleaIcon {

    NEW_SCHEDULE, OPEN, SAVE, SAVE_AS, PRINT, PDF, EXIT,
    NEW_SONG, QUICK_INSERT, ADD_ITEM, ADD,
    PRESENTATION, VIDEO, TIMER, DVD, WEB, IMAGES, NOTICES, RECORD,
    CLOSE, DELETE, UP, DOWN, LIBRARY_ADD,
    GO_LIVE, THEME, LOGO, BLACK, CLEAR_TEXT, HIDE_OUTPUT,
    BIBLE, SEARCH, LIVE_TEXT, TEST_PATTERN, SETTINGS,
    HELP, FORUM, UPDATE, INFO, IMPORT, EXPORT, LIST,
    BOLD, ITALIC, ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT,
    CHECK, EDIT, PREVIEW, TRANSLATE, LYRICS, LYRICS_CHORDS, LYRICS_COPY, AUDIO,
    SPELLCHECK, DICTIONARY, TRANSPOSE, NON_BREAK, LOOP, LOOP_ON, PLAY, PAUSE, STOP,
    VOLUME_UP, VOLUME_DOWN, BACK, FORWARD, REFRESH, SEND, ZOOM_IN, ZOOM_OUT,
    PREVIOUS, NEXT, MONITOR, STAGE_VIEW, SERVER, IMPORT_EXPORT, WARNING, ERROR, BUG,
    FILE, SONG_PACK, TEXT_FILE, WEBSITE_LINK, CLOUD_IMPORT, GENERAL, COPY;

    /**
     * Size used for icons on the main toolbar.
     */
    public static final double TOOLBAR_SIZE = 20;
    /**
     * Size used for icons in menus and small panel buttons.
     */
    public static final double SMALL_SIZE = 16;

    private final String styleClass;

    QueleaIcon() {
        styleClass = "icon-" + name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * @return the CSS style class that holds this icon's shape.
     */
    public String getStyleClass() {
        return styleClass;
    }

    /**
     * Create a node showing this icon at the small (menu) size.
     *
     * @return the icon node.
     */
    public Region create() {
        return create(SMALL_SIZE);
    }

    /**
     * Create this icon on a small rounded tile, as used for the item type
     * icons in the schedule (see .icon-tile in base.css).
     *
     * @return the tile node.
     */
    public StackPane createTile() {
        StackPane tile = new StackPane(create(18));
        tile.getStyleClass().add("icon-tile");
        tile.setMinSize(30, 30);
        tile.setPrefSize(30, 30);
        tile.setMaxSize(30, 30);
        return tile;
    }

    /**
     * Create a node showing this icon.
     *
     * @param size width and height in pixels.
     * @return the icon node.
     */
    public Region create(double size) {
        Region region = new Region();
        region.getStyleClass().addAll("icon", styleClass);
        region.setMinSize(size, size);
        region.setPrefSize(size, size);
        region.setMaxSize(size, size);
        return region;
    }
}
