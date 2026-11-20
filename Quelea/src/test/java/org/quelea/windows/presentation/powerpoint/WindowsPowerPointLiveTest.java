package org.quelea.windows.presentation.powerpoint;

import java.awt.Dimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.quelea.services.utils.QueleaProperties;

/** Opt-in test: opens a temporary slide show in a fresh Windows PowerPoint. */
@EnabledOnOs(OS.WINDOWS)
@EnabledIfSystemProperty(named = "quelea.test.powerpoint.windows", matches = "true")
class WindowsPowerPointLiveTest {
    @TempDir Path directory;
    private QueleaProperties previousProperties;
    private boolean propertiesChanged;

    @AfterEach
    void restoreProperties() throws Exception {
        if (propertiesChanged) {
            var instance = QueleaProperties.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(null, previousProperties);
        }
    }

    @Test
    void opensAndControlsARealPresentationWithSpaces() throws Exception {
        boolean alreadyRunning = ProcessHandle.allProcesses().anyMatch(p -> p.info().command()
                .map(command -> command.toLowerCase(java.util.Locale.ROOT).endsWith("\\powerpnt.exe"))
                .orElse(false));
        Assumptions.assumeFalse(alreadyRunning, "PowerPoint is already running; do not disturb existing work");
        String powerpoint = System.getProperty("quelea.test.powerpoint.path", "C:\\Program Files\\Microsoft Office\\Root\\Office16\\POWERPNT.EXE");
        Assumptions.assumeTrue(Files.isRegularFile(Path.of(powerpoint)), "PowerPoint is not installed");
        Files.createDirectories(directory.resolve("home/.quelea"));
        previousProperties = QueleaProperties.get();
        QueleaProperties.init(directory.resolve("home").toString());
        propertiesChanged = true;
        QueleaProperties.get().setPPPath(powerpoint);
        Path deck = directory.resolve("test presentation with spaces.pptx");
        Path other = directory.resolve("unrelated unsaved presentation.pptx");
        writeDeck(deck);
        writeDeck(other);
        WindowsPowerPointBackend backend = new WindowsPowerPointBackend();
        try {
            Assertions.assertEquals(1, backend.open(deck.toFile(), null).getSlide());
            Assertions.assertEquals(2, backend.next().getSlide());
            Assertions.assertEquals(3, backend.gotoSlide(3).getSlide());
            Assertions.assertEquals(2, backend.previous().getSlide());
            Assertions.assertTrue(backend.setBlack(true).isBlack());
            Assertions.assertFalse(backend.setBlack(false).isBlack());
            Assertions.assertTrue(backend.setLoop(true));
            Assertions.assertTrue(backend.setLoop(false));

            String opened = runScript("open unrelated.vbs", "On Error Resume Next\r\n"
                    + "Set app = GetObject(, \"PowerPoint.Application\")\r\n"
                    + "Set pres = app.Presentations.Open(WScript.Arguments(0))\r\n"
                    + "If Err.Number <> 0 Then WScript.Quit 1\r\n"
                    + "pres.Saved = 0\r\n"
                    + "WScript.Echo pres.FullName\r\n", other);
            Assertions.assertTrue(opened.contains(other.toString()), opened);

            PowerPointException unsaved = Assertions.assertThrows(PowerPointException.class, backend::close);
            Assertions.assertEquals(PowerPointException.Kind.UNSAVED_CHANGES, unsaved.getKind());
            Assertions.assertTrue(presentationState(deck, other).contains("owned=0"));
            Assertions.assertTrue(presentationState(deck, other).contains("other=0"));

            // Simulate the user choosing to discard changes to our generated
            // presentation. Never change the unrelated presentation's state.
            runScript("discard generated changes.vbs", "On Error Resume Next\r\n"
                    + "Set app = GetObject(, \"PowerPoint.Application\")\r\n"
                    + "For Each pres In app.Presentations\r\n"
                    + " If LCase(pres.FullName) = LCase(WScript.Arguments(0)) Then pres.Saved = -1\r\n"
                    + "Next\r\n", deck);
            backend.close();
            String state = presentationState(deck, other);
            Assertions.assertTrue(state.contains("other=0"), state); // 0 means unsaved
            Assertions.assertFalse(state.contains("owned="), state);

            PowerPointException busy = Assertions.assertThrows(PowerPointException.class,
                    () -> backend.open(deck.toFile(), null));
            Assertions.assertEquals(PowerPointException.Kind.ALREADY_RUNNING, busy.getKind());
            Assertions.assertTrue(presentationState(deck, other).contains("other=0"));
            closeGeneratedPresentations(other);
            Assertions.assertEquals(1, backend.open(deck.toFile(), null).getSlide(),
                    "An empty PowerPoint instance can be reused for another show");
            Assertions.assertEquals(1, backend.open(other.toFile(), null).getSlide(),
                    "Switching presentations closes only Quelea's previous show");
            backend.close();
        } finally {
            // Only close the generated documents. Never quit an application
            // that might have acquired unrelated user presentations.
            closeGeneratedPresentations(deck, other);
        }
    }

    private static void writeDeck(Path deck) throws Exception {
        try (XMLSlideShow show = new XMLSlideShow()) {
            show.setPageSize(new Dimension(960, 540));
            show.createSlide();
            show.createSlide();
            show.createSlide();
            try (var output = Files.newOutputStream(deck)) {
                show.write(output);
            }
        }
    }

    private String presentationState(Path deck, Path other) throws Exception {
        return runScript("inspect test presentations.vbs", "On Error Resume Next\r\n"
                + "Set app = GetObject(, \"PowerPoint.Application\")\r\n"
                + "If Err.Number <> 0 Then WScript.Quit 1\r\n"
                + "For Each pres In app.Presentations\r\n"
                + " If LCase(pres.FullName) = LCase(WScript.Arguments(0)) Then WScript.Echo \"owned=\" & pres.Saved\r\n"
                + " If LCase(pres.FullName) = LCase(WScript.Arguments(1)) Then WScript.Echo \"other=\" & pres.Saved\r\n"
                + "Next\r\n", deck, other);
    }

    private void closeGeneratedPresentations(Path... files) throws Exception {
        runScript("close test presentations.vbs", "On Error Resume Next\r\n"
                + "Set app = GetObject(, \"PowerPoint.Application\")\r\n"
                + "If Err.Number <> 0 Then WScript.Quit 0\r\n"
                + "For i = app.Presentations.Count To 1 Step -1\r\n"
                + " Set pres = app.Presentations(i)\r\n"
                + " For Each path In WScript.Arguments\r\n"
                + "  If LCase(pres.FullName) = LCase(path) Then\r\n"
                + "   pres.SlideShowWindow.View.Exit\r\n"
                + "   pres.Saved = -1\r\n"
                + "   pres.Close\r\n"
                + "  End If\r\n"
                + " Next\r\n"
                + "Next\r\n", files);
    }

    private String runScript(String name, String source, Path... arguments) throws Exception {
        Path script = directory.resolve(name);
        Files.writeString(script, source);
        var command = new java.util.ArrayList<String>(List.of("cscript", "//Nologo", script.toString()));
        for (Path argument : arguments) {
            command.add(argument.toString());
        }
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        Assertions.assertTrue(process.waitFor(15, TimeUnit.SECONDS), "PowerPoint test helper timed out");
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.Charset.defaultCharset());
        Assertions.assertEquals(0, process.exitValue(), output);
        return output;
    }
}
