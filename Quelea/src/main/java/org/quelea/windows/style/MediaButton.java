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

import javafx.css.PseudoClass;
import javafx.scene.control.Button;

/**
 * A round button showing a single {@link QueleaIcon}, used for the play /
 * pause / stop style controls of videos, audio and timers. Styled by the
 * .media-button rules in base.css.
 *
 * @author Quelea contributors
 */
public class MediaButton extends Button {

    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");

    private final double size;
    private QueleaIcon icon;

    /**
     * Create a media button.
     *
     * @param icon the icon to show.
     * @param size the diameter of the button in pixels.
     * @param primary true for the main (accent coloured) action, e.g. play.
     */
    public MediaButton(QueleaIcon icon, double size, boolean primary) {
        this.size = size;
        getStyleClass().add("media-button");
        if (primary) {
            getStyleClass().add("media-button-primary");
        }
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        setFocusTraversable(false);
        setIcon(icon);
    }

    /**
     * @return the icon currently shown.
     */
    public QueleaIcon getIcon() {
        return icon;
    }

    /**
     * Change the icon shown on this button.
     *
     * @param icon the new icon.
     */
    public final void setIcon(QueleaIcon icon) {
        this.icon = icon;
        setGraphic(icon.create(Math.round(size * 0.5)));
    }

    /**
     * Mark this button as switched on (e.g. looping enabled).
     *
     * @param active true if the button's option is on.
     */
    public void setActive(boolean active) {
        pseudoClassStateChanged(ACTIVE, active);
    }
}
