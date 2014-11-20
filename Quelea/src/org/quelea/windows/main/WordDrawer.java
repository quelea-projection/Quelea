/* 
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main;

import org.quelea.data.ThemeDTO;
import org.quelea.data.displayable.TextDisplayable;

/**
 *
 * @author Ben
 */
public abstract class WordDrawer extends DisplayableDrawer {

    public abstract void setTheme(ThemeDTO theme);

    public abstract void setText(String[] text, String[] translations, String[] smallText, boolean fade, double fontSize);
    
    public abstract ThemeDTO getTheme();

    public abstract void setCapitaliseFirst(boolean shouldCapitaliseFirst);

    public abstract void setText(TextDisplayable textDisplayable, int selectedIndex);

    /**
     * Erase all the text on the getCanvas().
     */
    public void eraseText() {
        setText(null, null, null, true, -1);
    }
    
}
