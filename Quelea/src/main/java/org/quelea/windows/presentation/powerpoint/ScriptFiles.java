/*
 * This file is part of Quelea, free projection software for churches.
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
package org.quelea.windows.presentation.powerpoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * The macOS PowerPoint scripts live in the jar; osascript needs real files, so
 * they are copied to a temporary folder once per run.
 */
final class ScriptFiles {

    static final String APPLESCRIPT = "powerpoint.applescript";
    static final String BRIDGE = "pptbridge.js";
    private static final String RESOURCE_DIR = "/org/quelea/powerpoint/";

    private static Path dir;

    private ScriptFiles() {
    }

    /**
     * @param name {@link #APPLESCRIPT} or {@link #BRIDGE}.
     * @return the extracted file.
     * @throws IOException if the resource is missing or cannot be copied.
     */
    static synchronized File get(String name) throws IOException {
        if (dir == null) {
            dir = Files.createTempDirectory("quelea-powerpoint");
            dir.toFile().deleteOnExit();
        }
        Path target = dir.resolve(name);
        if (!Files.exists(target)) {
            try (InputStream in = ScriptFiles.class.getResourceAsStream(RESOURCE_DIR + name)) {
                if (in == null) {
                    throw new IOException("Missing resource " + RESOURCE_DIR + name);
                }
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            target.toFile().deleteOnExit();
        }
        return target.toFile();
    }
}
