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
package org.quelea.services.utils;

/**
 * A version number that can be compared against another version to see what one
 * is greatest.
 * <p/>
 * @author Michael
 */
public class Version implements Comparable<Version> {

    private final String versionStr;
    private final String unstableName;

    /**
     * Create a new version.
     * <p/>
     * @param version the version number in the form x.x.x.x (all x's must be
     * positive integers.)
     * @param unstableName
     */
    public Version(String version, String unstableName) {
        this.versionStr = version;
        this.unstableName = unstableName;
    }

    /**
     * Get the version string.
     * <p/>
     * @return the version string.
     */
    public String getVersionString() {
        return versionStr;
    }

    /**
     * Get the minor "name" for this particular version - eg. Genesis, Exodus,
     * etc.
     * <p>
     * @return the minor "name" for this particular version.
     */
    public String getMinorName() {
        String[] parts = versionStr.split("\\.");
        if(parts.length > 1) {
            switch(parts[1]) {
                case "0":
                    return "Genesis";
                case "1":
                    return "Exodus";
                default:
                    return parts[1];
            }
        }
        else {
            return "";
        }
    }

    /**
     * Get the major version number (eg. 2014.)
     * <p>
     * @return the major version number.
     */
    public String getMajorVersionNumber() {
        return versionStr.split("\\.")[0];
    }

    /**
     * Get the minor version string.
     * <p/>
     * @return the minor version string.
     */
    public String getUnstableName() {
        return unstableName;
    }

    /**
     * Determine if this version is equal to another.
     * <p/>
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
     * <p/>
     * @return a string representation of this version.
     */
    @Override
    public String toString() {
        return "Version{" + "versionStr=" + versionStr + '}';
    }

    /**
     * Compare this version to another.
     * <p/>
     * @param o the other version.
     * @return -1 if this version is less than the other one, 0 if they are the
     * same and 1 if this version is greater.
     */
    @Override
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
