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

package org.quelea.video;

/**
 * File filter implementation for video files recognised by libvlc.
 */
public class PowerpointFileFilter extends ExtensionFileFilter {

  /**
   * From the vlc_interfaces.h include file.
   */
  private static final String[] EXTENSIONS_POWERPOINT = {
    "ppt",
    "pptx",
    "pps",
    "ppsx"
  };

  /**
   * Single instance.
   */
  public static final PowerpointFileFilter INSTANCE = new PowerpointFileFilter();
  
  /**
   * Create a new file filter.
   */
  public PowerpointFileFilter() {
    super(EXTENSIONS_POWERPOINT);
  }

    @Override
    public String getDescription() {
//        StringBuilder ret = new StringBuilder("Video files: ");
//        for(String extension : EXTENSIONS_VIDEO) {
//            ret.append(extension).append(", ");
//        }
//        return ret.substring(0,ret.length()-2);
        return "Powerpoint files";
    }
}