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
package org.quelea;

import java.io.File;

/**
 * An exception thrown when some permissions are required for a particular file but are not present.
 * @author Michael
 */
public class InsufficientPrivilegesException extends Exception {

    public enum RequiredPermissions {

        READ, WRITE, EXECUTE
    };

    /**
     * Create a new exception.
     * @param file               the file the permissions are required for (but missing.)
     * @param missingPermissions one of the missing permissions.
     */
    public InsufficientPrivilegesException(File file, RequiredPermissions missingPermissions) {
        super("Insufficient privileges - " + file + " hasn't got " + missingPermissions + " access.");
    }
}
