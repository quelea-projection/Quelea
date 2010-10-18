package org.quelea;

import java.io.File;

/**
 * An exception thrown when some permissions are required for a particular file
 * but are not present.
 * @author Michael
 */
public class InsufficientPrivilegesException extends Exception {
    
    public enum RequiredPermissions {READ, WRITE, EXECUTE};

    /**
     * Create a new exception.
     * @param file the file the permissions are required for (but missing.)
     * @param missingPermissions one of the missing permissions.
     */
    public InsufficientPrivilegesException(File file, RequiredPermissions missingPermissions) {
        super("Insufficient privileges - " + file + " hasn't got " + missingPermissions + " access.");
    }

}
