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
package org.quelea.services.languages.spelling;

import java.util.Objects;

/**
 * A particular suggestion - wrapper of the suggestion and its Levenshtein
 * distance.
 * <p/>
 * @author Michael
 */
public class Suggestion implements Comparable<Suggestion> {

    private String word;
    private int distance;

    /**
     * Create a new suggestion.
     * <p/>
     * @param word the suggestion word.
     * @param distance the distance from whatever the misspelt word was.
     */
    public Suggestion(String word, int distance) {
        this.word = word;
        this.distance = distance;
    }

    /**
     * Get the suggestion word (string.)
     * <p/>
     * @return the suggestion word (string.)
     */
    public String getWord() {
        return word;
    }

    /**
     * Get the suggestion distance (levenshtein.)
     * <p/>
     * @return the suggestion distance (levenshtein.)
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Compare to another suggestion - suggestions with a lower distance are
     * ranked lower (sorted first.)
     * <p/>
     * @param o the other suggestion.
     * @return 1 if this suggestion is greater than the other suggestion, 0 if
     * they are equal, -1 if it is less than the other suggestion.
     */
    @Override
    public int compareTo(Suggestion o) {
        return Integer.compare(distance, o.distance);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.word);
        hash = 83 * hash + this.distance;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final Suggestion other = (Suggestion) obj;
        if(!Objects.equals(this.word, other.word)) {
            return false;
        }
        if(this.distance != other.distance) {
            return false;
        }
        return true;
    }
}
