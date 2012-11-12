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
package org.quelea.utils;

/**
 * A version number that can be compared against another version to see what one is greatest.
 * @author Michael
 */
public class Version implements Comparable<Version> {

    private final String versionStr;

    /**
     * Create a new version.
     * @param version the version number in the form x.x.x.x (all x's must be positive integers.)
     */
    public Version(String version) {
        this.versionStr = version;
    }

    /**
     * Get the version string.
     * @return the version string.
     */
    public String getVersionString() {
        return versionStr;
    }

    /**
     * Determine if this version is equal to another.
     * @param obj the other object.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final Version other = (Version) obj;
        if((this.versionStr == null) ? (other.versionStr != null) : !this.versionStr.equals(other.versionStr)) {
            return false;
        }
        return true;
    }

    /**
     * Generate a hashcode for this version.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.versionStr != null ? this.versionStr.hashCode() : 0);
        return hash;
    }

    /**
     * Generate a string representation of this version.
     * @return a string representation of this version.
     */
    @Override
    public String toString() {
        return "Version{" + "versionStr=" + versionStr + '}';
    }

    /**
     * Compare this version to another.
     * @param o the other version.
     * @return -1 if this version is less than the other one, 0 if they are the same and 1 if this version is greater.
     */
    public int compareTo(Version o) {
        String[] theseParts = versionStr.split("\\.");
        String[] otherParts = o.versionStr.split("\\.");
        int minLength;
        if(theseParts.length < otherParts.length) {
            minLength = theseParts.length;
        }
        else {
            minLength = otherParts.length;
        }
        for(int i = 0; i < minLength; i++) {
            int thisNum = Integer.parseInt(theseParts[i]);
            int otherNum = Integer.parseInt(otherParts[i]);
            if(thisNum > otherNum) {
                return 1;
            }
            else if(thisNum < otherNum) {
                return -1;
            }
        }
        return Integer.valueOf(versionStr.length()).compareTo(o.versionStr.length());
    }
}
