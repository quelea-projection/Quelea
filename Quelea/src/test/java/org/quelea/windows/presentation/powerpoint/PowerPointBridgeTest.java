package org.quelea.windows.presentation.powerpoint;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The bridge protocol, timeouts and restarts, against {@link FakeBridge}; and
 * on macOS with PowerPoint installed, one round trip through the real
 * pptbridge.js (the "ping" command does not talk to PowerPoint).
 */
@DisplayName("PowerPointBridge")
public class PowerPointBridgeTest {

    private PowerPointBridge bridge;

    @AfterEach
    public void tearDown() {
        if (bridge != null) {
            bridge.close();
        }
    }

    private static List<String> fake(String... extra) {
        List<String> cmd = new ArrayList<>();
        String java = ProcessHandle.current().info().command().orElse("java");
        cmd.add(java);
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add(FakeBridge.class.getName());
        cmd.addAll(Arrays.asList(extra));
        return cmd;
    }

    @Test
    @DisplayName("Answers are matched to their requests, arguments arrive unchanged")
    public void roundTrip() throws Exception {
        bridge = new PowerPointBridge(fake());
        assertEquals("a b", bridge.run(5000, "echo", "a", "b"));
        String path = "/Users/me/Dir with spaces/\"quoted\" & å \\ .pptx";
        assertEquals(path, bridge.run(5000, "echo", path));
        for (int i = 0; i < 20; i++) {
            assertEquals(Integer.toString(i), bridge.run(5000, "echo", Integer.toString(i)));
        }
    }

    @Test
    @DisplayName("AppleScript errors keep their number and are classified")
    public void errors() {
        bridge = new PowerPointBridge(fake());
        PowerPointException ex = assertThrows(PowerPointException.class, () -> bridge.run(5000, "fail", "-1743", "Not authorised"));
        assertEquals(PowerPointException.Kind.NOT_AUTHORIZED, ex.getKind());
        assertEquals(-1743, ex.getCode());
        assertFalse(ex instanceof PowerPointBridge.BridgeException);
        ex = assertThrows(PowerPointException.class, () -> bridge.run(5000, "fail", "1002", "not allowed to send keystrokes"));
        assertEquals(PowerPointException.Kind.NOT_ACCESSIBLE, ex.getKind());
    }

    @Test
    @DisplayName("A command that hangs times out, and the next command gets a fresh bridge")
    public void timeoutRestarts() throws Exception {
        bridge = new PowerPointBridge(fake());
        long start = System.nanoTime();
        PowerPointException ex = assertThrows(PowerPointException.class, () -> bridge.run(300, "hang"));
        assertEquals(PowerPointException.Kind.TIMEOUT, ex.getKind());
        assertTrue((System.nanoTime() - start) / 1_000_000 < 3000, "should give up after the timeout");
        assertEquals("ok", bridge.run(5000, "echo", "ok"));
    }

    @Test
    @DisplayName("If the bridge exits, the command fails as a bridge problem and the next one restarts it")
    public void exitRestarts() throws Exception {
        bridge = new PowerPointBridge(fake());
        assertThrows(PowerPointBridge.BridgeException.class, () -> bridge.run(5000, "exit"));
        assertEquals("back", bridge.run(5000, "echo", "back"));
    }

    @Test
    @DisplayName("A bridge that cannot start gives up after a few tries, and the fallback takes over")
    public void fatalStart() throws Exception {
        bridge = new PowerPointBridge(fake("fatal"));
        for (int i = 0; i < PowerPointBridge.MAX_STARTS; i++) {
            assertThrows(PowerPointBridge.BridgeException.class, () -> bridge.run(5000, "echo", "x"));
        }
        assertFalse(bridge.isUsable());
        AppleScriptRunner fallback = new AppleScriptRunner() {
            @Override
            public String run(long timeoutMillis, String... args) {
                return "fallback:" + args[0];
            }

            @Override
            public void close() {
            }
        };
        FallbackRunner runner = new FallbackRunner(bridge, fallback);
        assertFalse(runner.isFast());
        assertEquals("fallback:state", runner.run(1000, "state"));
    }

    @Test
    @DisplayName("The real pptbridge.js starts and answers (macOS with PowerPoint only)")
    public void realBridge() throws Exception {
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("mac"), "macOS only");
        assumeTrue(MacPowerPointBackend.isInstalled(), "needs Microsoft PowerPoint");
        bridge = PowerPointBridge.create();
        assertEquals("pong", bridge.run(10_000, "ping"));
        PowerPointException ex = assertThrows(PowerPointException.class, () -> bridge.run(10_000, "noSuchCommand"));
        assertEquals(-50, ex.getCode());
        assertEquals("pong", bridge.run(10_000, "ping"));
    }

    @Test
    @DisplayName("The real osascript fallback reports errors with their number (macOS with PowerPoint only)")
    public void realOsascript() throws Exception {
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("mac"), "macOS only");
        assumeTrue(MacPowerPointBackend.isInstalled(), "needs Microsoft PowerPoint");
        OsascriptRunner runner = new OsascriptRunner("/usr/bin/osascript", ScriptFiles.get(ScriptFiles.APPLESCRIPT));
        assertEquals("pong", runner.run(10_000, "ping"));
        PowerPointException ex = assertThrows(PowerPointException.class, () -> runner.run(10_000, "noSuchCommand"));
        assertEquals(-50, ex.getCode());
        assertTrue(new File(ScriptFiles.get(ScriptFiles.BRIDGE).getPath()).isFile());
    }
}
