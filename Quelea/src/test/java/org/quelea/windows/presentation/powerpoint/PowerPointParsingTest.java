package org.quelea.windows.presentation.powerpoint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Turning script output into states and errors.
 */
@DisplayName("PowerPoint answers")
public class PowerPointParsingTest {

    @Test
    @DisplayName("Slide show states are parsed")
    public void slideState() {
        SlideState s = SlideState.parse("3|12|RUNNING\n");
        assertEquals(3, s.getSlide());
        assertEquals(12, s.getTotal());
        assertTrue(s.isShowing());
        assertFalse(s.isAtEnd());
        assertTrue(SlideState.parse("12|12|RUNNING").isAtEnd());
        assertTrue(SlideState.parse("0|12|DONE").isAtEnd());
        assertTrue(SlideState.parse("4|12|BLACK").isBlack());
        assertFalse(SlideState.parse("0|5|NOSHOW").isShowing());
        assertEquals(SlideState.NOT_RUNNING, SlideState.parse("0|0|NOTRUNNING"));
        assertThrows(IllegalArgumentException.class, () -> SlideState.parse("Not running"));
        assertThrows(IllegalArgumentException.class, () -> SlideState.parse("1|2|SIDEWAYS"));
        assertThrows(IllegalArgumentException.class, () -> SlideState.parse(""));
    }

    @Test
    @DisplayName("osascript error output keeps the error number, whatever the language")
    public void osascriptErrors() {
        PowerPointException ex = OsascriptRunner.errorFrom(
                "/tmp/powerpoint.applescript:4290:4344: execution error: Microsoft PowerPoint got an error: "
                + "Not authorised to send Apple events to Microsoft PowerPoint. (-1743)\n");
        assertEquals(PowerPointException.Kind.NOT_AUTHORIZED, ex.getKind());
        assertTrue(ex.isPermissionProblem());
        ex = OsascriptRunner.errorFrom("/tmp/x:1:2: execution error: Microsoft PowerPoint drabbades av ett fel: "
                + "The object you are trying to access does not exist (-1728)");
        assertEquals(PowerPointException.Kind.NO_SLIDESHOW, ex.getKind());
        assertTrue(ex.getMessage().startsWith("Microsoft PowerPoint drabbades"));
        assertEquals(PowerPointException.Kind.TIMEOUT,
                OsascriptRunner.errorFrom("x: execution error: AppleEvent timed out. (-1712)").getKind());
        assertEquals(PowerPointException.Kind.NOT_AUTHORIZED_SYSTEM_EVENTS,
                OsascriptRunner.errorFrom("x: execution error: Not authorised (1743)").getKind());
        assertEquals(PowerPointException.Kind.FAILED, OsascriptRunner.errorFrom("garbage").getKind());
    }

    @Test
    @DisplayName("Windows VBScript answers become states")
    public void windowsState() {
        assertEquals(new SlideState(4, 0, SlideState.Mode.RUNNING), WindowsPowerPointBackend.parseState("4", "2"));
        assertEquals(new SlideState(4, 0, SlideState.Mode.BLACK), WindowsPowerPointBackend.parseState("4", "3"));
        assertEquals(SlideState.NOT_RUNNING, WindowsPowerPointBackend.parseState("PowerPoint is not running", ""));
        assertEquals(SlideState.Mode.NOSHOW, WindowsPowerPointBackend.parseState("", "").getMode());
    }
}
