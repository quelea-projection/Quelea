package org.quelea.windows.presentation.powerpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The macOS backend against a scripted runner: the commands it sends, how
 * answers and their flags are read, and the projector screen check.
 */
@DisplayName("MacPowerPointBackend")
public class MacPowerPointBackendTest {

    private final List<List<String>> sent = new ArrayList<>();

    private MacPowerPointBackend backend(String answer, PowerPointException failure) {
        AppleScriptRunner runner = new AppleScriptRunner() {
            @Override
            public String run(long timeoutMillis, String... args) throws PowerPointException {
                sent.add(Arrays.asList(args));
                if (failure != null) {
                    throw failure;
                }
                return answer;
            }

            @Override
            public void close() {
            }
        };
        return new MacPowerPointBackend(runner, 1);
    }

    @Test
    @DisplayName("The projector area goes to the script as whole points, and the show's place is checked")
    public void screenArea() {
        Rectangle2D projector = new Rectangle2D(1512, -98.5, 1920, 1080);
        // A screen above/right of the main one; fractions are rounded half up.
        assertEquals("1512,-98,3432,982", MacPowerPointBackend.toRect(projector));
        assertEquals("", MacPowerPointBackend.toRect(null));
        Rectangle2D show = MacPowerPointBackend.parseRect("1512,-98,3432,982");
        assertTrue(MacPowerPointBackend.isOn(show, projector));
        assertFalse(MacPowerPointBackend.isOn(MacPowerPointBackend.parseRect("0,0,1512,982"), projector));
        // A custom projector area smaller than its screen, off the show's middle.
        assertTrue(MacPowerPointBackend.isOn(MacPowerPointBackend.parseRect("0,0,1512,982"), new Rectangle2D(50, 50, 300, 200)));
        assertNull(MacPowerPointBackend.parseRect(""));
        assertNull(MacPowerPointBackend.parseRect("1,2,3"));
    }

    @Test
    @DisplayName("A show that could not be moved to the projector screen becomes a warning, not a failure")
    public void offScreen() throws Exception {
        MacPowerPointBackend b = backend("1|5|RUNNING|OFFSCREEN", null);
        assertEquals(new SlideState(1, 5, SlideState.Mode.RUNNING), b.state());
        PowerPointException w = b.takeWarning();
        assertEquals(PowerPointException.Kind.NOT_ACCESSIBLE, w.getKind());
        assertEquals(PowerPointException.CODE_SHOW_OFF_SCREEN, w.getCode());
        assertNull(b.takeWarning());
    }

    @Test
    @DisplayName("Jumps and black screen go to the script with their arguments and answer with the state")
    public void commands() throws Exception {
        assertEquals(new SlideState(9, 15, SlideState.Mode.PAUSED), backend("9|15|PAUSED", null).gotoSlide(9));
        assertEquals(Arrays.asList("gotoSlide", "9", "1"), sent.get(0));
        assertEquals(new SlideState(9, 15, SlideState.Mode.BLACK), backend("9|15|BLACK", null).setBlack(true));
        assertEquals(Arrays.asList("setBlack", "true", "1"), sent.get(1));
    }

    @Test
    @DisplayName("Without Accessibility, black screen fails with a permission problem")
    public void refused() {
        PowerPointException ex = assertThrows(PowerPointException.class,
                () -> backend(null, PowerPointException.fromAppleScript(1002, "Quelea may not send keys to PowerPoint")).setBlack(true));
        assertEquals(PowerPointException.Kind.NOT_ACCESSIBLE, ex.getKind());
        assertTrue(ex.isPermissionProblem());
    }
}
