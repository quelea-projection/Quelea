package org.quelea.windows.presentation.powerpoint;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

class WindowsPowerPointScriptsTest {
    @TempDir Path directory;

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void scriptAndPresentationPathsCanContainSpaces() throws Exception {
        Path folder = Files.createDirectory(directory.resolve("folder with spaces"));
        Path script = folder.resolve("echo arguments.vbs");
        Files.writeString(script, "WScript.Echo WScript.Arguments(0)\r\n");
        String argument = folder.resolve("presentation & notes.pptx").toString();
        Method run = WindowsPowerPointScripts.class.getDeclaredMethod("sendVBSSignal", String.class);
        run.setAccessible(true);
        String result = (String) run.invoke(null, "\"" + script + "\" \"" + argument + "\"");
        Assertions.assertEquals(argument, result);
    }
}
