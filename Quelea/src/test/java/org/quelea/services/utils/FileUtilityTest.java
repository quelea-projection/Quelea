package org.quelea.services.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("File type and directory utilities")
public class FileUtilityTest {
    @TempDir Path directory;

    @BeforeAll
    public static void initializeProperties() throws Exception {
        if (QueleaProperties.get() == null) {
            QueleaProperties.init(Files.createTempDirectory("quelea-file-utility-test").toString());
        }
        Files.createDirectories(QueleaProperties.get().getImageDir().toPath());
    }

    @Test
    public void recognizesMediaAndTimerExtensionsWithoutConfusingOtherTypes() {
        Assertions.assertTrue(Utils.fileIsImage(directory.resolve("photo.PNG").toFile()));
        Assertions.assertTrue(Utils.fileIsVideo(directory.resolve("clip.MP4").toFile()));
        Assertions.assertTrue(Utils.fileIsTimer(directory.resolve("clock.cdt").toFile()));
        Assertions.assertFalse(Utils.fileIsTimer(directory.resolve("clock.txt").toFile()));
        Assertions.assertFalse(Utils.hasExtension(directory.resolve("README").toFile(), "pdf"));
        Assertions.assertFalse(Utils.hasExtension(new java.io.File("."), "txt"));
        Assertions.assertFalse(Utils.hasExtension(directory.resolve("notes.").toFile(), "notes"));
    }

    @Test
    public void extensionMatchingIsIndependentOfTurkishLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Assertions.assertTrue(Utils.hasExtension(directory.resolve("SONGS.ZIP").toFile(), "zip"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void checksTheSpecifiedDirectoryAndDoesNotIncludeSubdirectories() throws Exception {
        Path selectedDirectory = Files.createDirectory(directory.resolve("selected"));
        Path inside = Files.writeString(selectedDirectory.resolve("photo.png"), "test");
        Path child = Files.createDirectory(selectedDirectory.resolve("child"));
        Path nested = Files.writeString(child.resolve("nested.png"), "test");
        Assertions.assertTrue(Utils.isInDir(selectedDirectory.toFile(), inside.toFile()));
        Assertions.assertFalse(Utils.isInDir(selectedDirectory.toFile(), nested.toFile()));
        Assertions.assertFalse(Utils.isInDir(directory.resolve("missing").toFile(), inside.toFile()));
    }

    @Test
    public void copiesNestedDirectoriesAndBinaryContents() throws Exception {
        Path source = Files.createDirectory(directory.resolve("source"));
        Path nested = Files.createDirectory(source.resolve("nested"));
        byte[] content = {0, 1, 2, (byte) 255};
        Files.write(nested.resolve("binary.dat"), content);
        Path destination = directory.resolve("copy");
        Utils.copyFile(source.toFile(), destination.toFile());
        Assertions.assertArrayEquals(content, Files.readAllBytes(destination.resolve("nested/binary.dat")));
    }

    @Test
    public void copyingAFileOntoItselfDoesNotDestroyTheSource() throws Exception {
        byte[] content = {0, 1, 2, (byte) 255};
        Path source = Files.write(directory.resolve("important.dat"), content);
        Utils.copyFile(source.toFile(), source.toFile());
        Assertions.assertArrayEquals(content, Files.readAllBytes(source));
    }

    @Test
    public void failedCopyDoesNotCreateAnEmptyDestination() throws Exception {
        Path source = directory.resolve("missing.dat");
        Path destination = directory.resolve("destination.dat");
        Assertions.assertThrows(java.io.IOException.class, () -> Utils.copyFile(source.toFile(), destination.toFile()));
        Assertions.assertFalse(Files.exists(destination));
    }

    @Test
    public void failedCopyDoesNotReplaceExistingDestination() throws Exception {
        Path destination = Files.writeString(directory.resolve("existing.dat"), "keep me");
        Assertions.assertThrows(java.io.IOException.class,
                () -> Utils.copyFile(directory.resolve("missing.dat").toFile(), destination.toFile()));
        Assertions.assertEquals("keep me", Files.readString(destination));
    }

    @Test
    public void copyingToHardLinkDoesNotDestroySource() throws Exception {
        Path source = Files.writeString(directory.resolve("original.dat"), "keep me");
        Path alias = directory.resolve("alias.dat");
        try {
            Files.createLink(alias, source);
        } catch (UnsupportedOperationException | java.io.IOException | SecurityException ex) {
            org.junit.jupiter.api.Assumptions.abort("Hard links unavailable: " + ex);
        }
        Utils.copyFile(source.toFile(), alias.toFile());
        Assertions.assertEquals("keep me", Files.readString(source));
        Assertions.assertEquals("keep me", Files.readString(alias));
    }

    @Test
    public void copyingToSymbolicLinkUpdatesItsTarget() throws Exception {
        Path source = Files.writeString(directory.resolve("source.dat"), "new contents");
        Path target = Files.writeString(directory.resolve("target.dat"), "old contents");
        Path link = directory.resolve("link.dat");
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | java.io.IOException | SecurityException ex) {
            org.junit.jupiter.api.Assumptions.abort("Symbolic links unavailable: " + ex);
        }
        Utils.copyFile(source.toFile(), link.toFile());
        Assertions.assertTrue(Files.isSymbolicLink(link));
        Assertions.assertEquals("new contents", Files.readString(target));
    }
}
