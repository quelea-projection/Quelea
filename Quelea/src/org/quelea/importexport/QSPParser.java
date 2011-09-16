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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quelea.importexport;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.quelea.displayable.Song;
import org.quelea.utils.SongPack;

/**
 *
 * @author Michael
 */
public class QSPParser implements SongParser {

    @Override
    public List<Song> getSongs(File location) throws IOException {
        return SongPack.fromFile(location).getSongs();
    }

}
