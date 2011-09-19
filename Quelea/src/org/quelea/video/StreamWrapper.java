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
package org.quelea.video;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A helper class for wrapping input and output streams returned from remote
 * processes. These can used for a quick and dirty form of RMI between the Java
 * VMs.
 * @author Michael
 */
public class StreamWrapper {

    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Create a new stream wrapper.
     * @param inputStream the input stream to wrap.
     * @param outputStream the output stream to wrap.
     */
    StreamWrapper(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * Get the input stream.
     * @return the input stream.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Get the output stream.
     * @return the output stream.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }
}
