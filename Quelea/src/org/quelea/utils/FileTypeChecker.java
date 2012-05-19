/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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

import java.io.File;

/**
 * Interface for defining file type checkers - contains methods that should be
 * implemented to determine if the chosen file or path is of the given type.
 *
 * @author Michael
 */
public interface FileTypeChecker {

    /**
     * Check if the given file is of the type we're checking.
     * @param file the file to check.
     * @return true if it is of that type, false otherwise.
     */
    boolean isType(File file);

    /**
     * Check if the given path is of the type we're checking.
     *
     * @param path the file to check.
     * @return true if it is of that type, false otherwise.
     */
    boolean isType(String path);
}
