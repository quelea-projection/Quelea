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
package org.quelea.data.powerpoint;

/**
 * A common interface for different presentation types.
 * @author mjrb5
 */
public interface Presentation {
    
    /**
     * Get the presentation slide at the given index.
     * @param index the index of the slide to get.
     * @return the slide at the given index.
     */
    PresentationSlide getSlide(int index);
    
    /**
     * Get all the presentation slides in this presentation.
     * @return an array of all the presentation slides in this presentation,
     * in order.
     */
    PresentationSlide[] getSlides();
    
}
