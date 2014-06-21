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
package org.quelea.data.displayable;

import org.quelea.data.ThemeDTO;

/**
 * A displayable object that displays text such as liturgy, songs or bible passages.
 * @author Michael
 */
public interface TextDisplayable extends Displayable {

    /**
     * Get the text sections in this displayable.
     * @return the text sections.
     */
    TextSection[] getSections();
    
    /**
     * Get the current theme
     * @return the theme
     */
    ThemeDTO getTheme();
    
    /**
     * Set the current theme
     * @param theme the theme to set 
     */
    void setTheme(ThemeDTO theme);

}
