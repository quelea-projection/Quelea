/*
 * This file is part of Quelea, free projection software for churches. Copyright
 * (C) 2011 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * VLCJ is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * VLCJ. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009, 2010 Caprica Software Limited.
 */
package org.quelea.utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base implementation for file filters that are based on file name extensions.
 */
public abstract class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {

    /**
     * The recognised file extensions.
     */
    private final String[] extensions;
    /**
     * Set of recognised file extensions.
     */
    private final Set<String> extensionsSet = new HashSet<>();

    /**
     * Create a new file filter. 
     * 
     * @param extensions file extensions to accept
     */
    protected ExtensionFileFilter(String[] extensions) {
        this.extensions = Arrays.copyOf(extensions, extensions.length);
        for(int i=0 ; i<extensions.length ; i++) {
            this.extensions[i] = this.extensions[i].toLowerCase();
        }
        Arrays.sort(this.extensions);
        extensionsSet.addAll(Arrays.asList(this.extensions));
    }

    /**
     * Get the recognised file extensions.
     * <p>
     * A sorted copy of the array of file extensions is returned.
     * 
     * @return file extensions accepted by the filter
     */
    public String[] getExtensions() {
        return Arrays.asList(extensions).toArray(new String[extensions.length]);
    }

    /**
     * Get the set of recognised file extensions.
     * <p>
     * A new (copy) sorted set of file extensions is returned.
     * 
     * @return set of file extensions accepted by the filter
     */
    public Set<String> getExtensionSet() {
        return new TreeSet<>(extensionsSet);
    }

    /**
     * Accept the file filter if the extension is one of the given ones.
     * @param pathname the file to check.
     * @return true if the file should be accepted, false otherwise.
     */
    @Override
    public boolean accept(File pathname) {
        if(pathname.isDirectory()) {
            return true;
        }
        if(pathname.isFile()) {
            String name = pathname.getName();
            int dot = name.lastIndexOf('.');
            if(dot != -1 && dot + 1 < name.length()) {
                String extension = name.substring(dot + 1);
                return extensionsSet.contains(extension.toLowerCase());
            }
        }
        return false;
    }
}
