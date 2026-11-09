/*
 * This file is part of Quelea, free projection software for churches.
 *
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
package org.quelea.server.v2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.quelea.services.utils.Utils;

/**
 * The image and video folders of Quelea's library (img and vid in Quelea's
 * home folder), as remotes see them: the files Quelea shows in its Images
 * and Videos tabs, looked up by file name only, and files uploaded into
 * them. Nothing outside the two folders can be named, read or written.
 */
final class MediaLibrary {

    /**
     * Which folder.
     */
    enum Kind {
        IMAGE("images", 50L * 1024 * 1024, Utils.getImageExtensions()),
        VIDEO("videos", 4L * 1024 * 1024 * 1024, Utils.getVideoExtensions());

        private final String path;
        private final long maxUpload;
        private final Set<String> extensions = new TreeSet<>();

        Kind(String path, long maxUpload, List<String> extensions) {
            this.path = path;
            this.maxUpload = maxUpload;
            for (String extension : extensions) {
                this.extensions.add(extension.toLowerCase(Locale.ROOT));
            }
        }

        /**
         * @return "images" or "videos", as in the API's paths.
         */
        String path() {
            return path;
        }

        /**
         * @return the largest upload accepted, in bytes.
         */
        long maxUpload() {
            return maxUpload;
        }

        boolean accepts(String name) {
            int dot = name.lastIndexOf('.');
            return dot > 0 && extensions.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
        }

        static Kind fromPath(String path) {
            for (Kind kind : values()) {
                if (kind.path.equals(path)) {
                    return kind;
                }
            }
            throw new NoSuchElementException("No library called " + path);
        }
    }

    private MediaLibrary() {
    }

    /**
     * @param dir the folder.
     * @param kind what it holds.
     * @return the files Quelea would show, sorted by name, ignoring case.
     */
    static List<Catalog.MediaFile> list(Path dir, Kind kind) {
        List<Catalog.MediaFile> files = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return files;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                if (!name.startsWith(".") && kind.accepts(name) && Files.isRegularFile(file)) {
                    files.add(new Catalog.MediaFile(name, Files.size(file)));
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not list " + dir, ex);
        }
        files.sort(Comparator.comparing(Catalog.MediaFile::name, String.CASE_INSENSITIVE_ORDER));
        return files;
    }

    /**
     * Check a file name a remote sent: a plain name with an extension Quelea
     * shows, no folders, not hidden.
     *
     * @param kind what the folder holds.
     * @param name the name.
     * @return the name, trimmed.
     * @throws IllegalArgumentException if it is not acceptable.
     */
    static String checkName(Kind kind, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("\"name\" is required");
        }
        String trimmed = name.strip();
        if (trimmed.length() > 200 || trimmed.startsWith(".") || trimmed.contains("/") || trimmed.contains("\\")
                || trimmed.contains(File.separator) || trimmed.chars().anyMatch(c -> c < 32 || c == 127)
                || trimmed.contains(":")) {
            throw new IllegalArgumentException("\"" + name + "\" is not a plain file name");
        }
        if (!kind.accepts(trimmed)) {
            throw new IllegalArgumentException("Quelea does not show " + kind.path() + " like \"" + trimmed + "\"");
        }
        return trimmed;
    }

    /**
     * @param dir the folder.
     * @param kind what it holds.
     * @param name a file name.
     * @return the file in the folder.
     * @throws NoSuchElementException if there is no such file.
     */
    static Path resolve(Path dir, Kind kind, String name) {
        String checked;
        try {
            checked = checkName(kind, name);
        } catch (IllegalArgumentException ex) {
            throw new NoSuchElementException(ex.getMessage());
        }
        Path file = dir.resolve(checked).normalize();
        if (!file.getParent().equals(dir.normalize()) || !Files.isRegularFile(file)) {
            throw new NoSuchElementException("No " + kind.path() + " file named " + checked);
        }
        return file;
    }

    /**
     * Save an upload in the folder. It is written to a hidden temporary
     * file first and only gets its name once complete, so Quelea never
     * lists half a file. An existing file is never replaced: the new one
     * becomes "name (2).jpg" and so on.
     *
     * @param dir the folder.
     * @param kind what it holds.
     * @param name the file name the remote wants.
     * @param declaredSize the Content-Length, or -1 if unknown.
     * @param data the file's bytes.
     * @return the file as saved.
     * @throws IllegalArgumentException for a bad name.
     * @throws ApiException 413 for a file that is too large, 507 when the
     * disk is too full.
     * @throws IOException if the file could not be written.
     */
    static Catalog.MediaFile save(Path dir, Kind kind, String name, long declaredSize, InputStream data)
            throws IOException {
        String checked = checkName(kind, name);
        if (declaredSize > kind.maxUpload()) {
            throw tooLarge(kind);
        }
        Files.createDirectories(dir);
        if (declaredSize > 0 && dir.toFile().getUsableSpace() < declaredSize + 100L * 1024 * 1024) {
            throw new ApiException(507, "no_space", "There is not enough free disk space for this file");
        }
        Path temp = dir.resolve(".upload-" + UUID.randomUUID() + ".part");
        try {
            long written = 0;
            byte[] buffer = new byte[64 * 1024];
            try (OutputStream out = Files.newOutputStream(temp, StandardOpenOption.CREATE_NEW)) {
                int read;
                while ((read = data.read(buffer)) != -1) {
                    written += read;
                    if (written > kind.maxUpload()) {
                        throw tooLarge(kind);
                    }
                    out.write(buffer, 0, read);
                }
            }
            if (written == 0) {
                throw new IllegalArgumentException("The file is empty");
            }
            if (declaredSize >= 0 && written != declaredSize) {
                throw new IOException("The upload ended after " + written + " of " + declaredSize + " bytes");
            }
            for (int attempt = 1; attempt < 1000; attempt++) {
                String candidate = attempt == 1 ? checked : numbered(checked, attempt);
                try {
                    // Without REPLACE_EXISTING a move never replaces a file,
                    // also one that appeared in between.
                    Files.move(temp, dir.resolve(candidate));
                    return new Catalog.MediaFile(candidate, written);
                } catch (FileAlreadyExistsException ex) {
                    // Try the next number.
                }
            }
            throw new IOException("No free file name for " + checked);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * @param name "photo.jpg"
     * @param number 2
     * @return "photo (2).jpg"
     */
    static String numbered(String name, int number) {
        int dot = name.lastIndexOf('.');
        return name.substring(0, dot) + " (" + number + ")" + name.substring(dot);
    }

    private static ApiException tooLarge(Kind kind) {
        return new ApiException(413, "too_large",
                "The file is larger than " + (kind.maxUpload() / (1024 * 1024)) + " MB");
    }
}
