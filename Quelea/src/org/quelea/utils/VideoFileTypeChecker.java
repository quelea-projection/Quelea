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
 * Check whether the type of the file represents a video.
 *
 * @author Michael
 */
public class VideoFileTypeChecker implements FileTypeChecker {

    @Override
    public boolean isType(File file) {
        return Utils.hasExtension(file, "mpg")
                || Utils.hasExtension(file, "mpeg")
                || Utils.hasExtension(file, "mp4")
                || Utils.hasExtension(file, "avi")
                || Utils.hasExtension(file, "mkv")
                || Utils.hasExtension(file, "wmv")
                || Utils.hasExtension(file, "mov");
    }

    @Override
    public boolean isType(String path) {
        return isType(new File(path));
    }
}
