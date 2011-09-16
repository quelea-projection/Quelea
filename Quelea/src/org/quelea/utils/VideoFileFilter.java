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
/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2009, 2010 Caprica Software Limited.
 */
package org.quelea.utils;

/**
 * File filter implementation for video files recognised by libvlc.
 */
public class VideoFileFilter extends ExtensionFileFilter {

    /**
     * From the vlc_interfaces.h include file.
     */
    private static final String[] EXTENSIONS_VIDEO = {
        "amv",
        "asf",
        "avi",
        "divx",
        "dv",
        "flv",
        "gxf",
        "iso",
        "m1v",
        "m2v",
        "m2t",
        "m2ts",
        "m4v",
        "mkv",
        "mov",
        "mp2",
        "mp4",
        "mpeg",
        "mpeg1",
        "mpeg2",
        "mpeg4",
        "mpg",
        "mts",
        "mxf",
        "nsv",
        "nuv",
        "ogg",
        "ogm",
        "ogv",
        "ogx",
        "ps",
        "rec",
        "rm",
        "rmvb",
        "tod",
        "ts",
        "vob",
        "vro",
        "wmv"
    };
    /**
     * Single instance.
     */
    public static final VideoFileFilter INSTANCE = new VideoFileFilter();

    /**
     * Create a new file filter.
     */
    public VideoFileFilter() {
        super(EXTENSIONS_VIDEO);
    }

    @Override
    public String getDescription() {
//        StringBuilder ret = new StringBuilder("Video files: ");
//        for(String extension : EXTENSIONS_VIDEO) {
//            ret.append(extension).append(", ");
//        }
//        return ret.substring(0,ret.length()-2);
        return "Video files";
    }
}
