package org.quelea.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DesktopApiTest {
    private final URI uri = URI.create("https://quelea.org/help");

    @Test
    void snapUsesDesktopPortalBeforeAwt() {
        List<String> calls = new ArrayList<>();
        Assertions.assertTrue(DesktopApi.browse(uri, true, record(calls, "snap", true),
                record(calls, "awt", true), record(calls, "system", true)));
        Assertions.assertEquals(List.of("snap"), calls);
    }

    @Test
    void failedSnapOpenerFallsBackToDesktopAndSystemOpeners() {
        List<String> calls = new ArrayList<>();
        Assertions.assertTrue(DesktopApi.browse(uri, true, record(calls, "snap", false),
                record(calls, "awt", false), record(calls, "system", true)));
        Assertions.assertEquals(List.of("snap", "awt", "system"), calls);
    }

    @Test
    void nonSnapSkipsSnapOpenerAndReportsFailure() {
        List<String> calls = new ArrayList<>();
        Assertions.assertFalse(DesktopApi.browse(uri, false, record(calls, "snap", true),
                record(calls, "awt", false), record(calls, "system", false)));
        Assertions.assertEquals(List.of("awt", "system"), calls);
    }

    private Predicate<URI> record(List<String> calls, String name, boolean result) {
        return value -> {
            Assertions.assertEquals(uri, value);
            calls.add(name);
            return result;
        };
    }
}
