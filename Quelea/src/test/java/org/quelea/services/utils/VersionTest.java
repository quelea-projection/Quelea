package org.quelea.services.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Version")
public class VersionTest {
    private Version version(String value) {
        return new Version(value, VersionType.RELEASE);
    }

    @Test
    public void comparesNumericComponentsRatherThanAlphabeticalOrder() {
        Assertions.assertTrue(version("2025.10").compareTo(version("2025.2")) > 0);
        Assertions.assertTrue(version("2024.99").compareTo(version("2025.0")) < 0);
        Assertions.assertEquals(0, version("2025.1").compareTo(version("2025.1")));
    }

    @Test
    public void minorNamesAndMajorVersionAreAvailable() {
        Assertions.assertEquals("2025", version("2025.0").getMajorVersionNumber());
        Assertions.assertEquals("Genesis", version("2025.0").getMinorName());
        Assertions.assertEquals("Exodus", version("2025.1").getMinorName());
        Assertions.assertEquals("2", version("2025.2").getMinorName());
        Assertions.assertEquals("", version("2025").getMinorName());
    }

    @Test
    public void equalVersionsHaveEqualHashes() {
        Version first = version("2025.1");
        Version second = version("2025.1");
        Assertions.assertEquals(first, second);
        Assertions.assertEquals(first.hashCode(), second.hashCode());
        Assertions.assertNotEquals(first, version("2025.2"));
    }

    @Test
    public void leadingZerosDoNotChangeNumericOrdering() {
        Assertions.assertEquals(0, version("2025.01").compareTo(version("2025.1")));
        Assertions.assertNotEquals(version("2025.01"), version("2025.1"));
        Assertions.assertTrue(version("2025.1.0").compareTo(version("2025.01")) > 0);
        Assertions.assertTrue(version("2025.01").compareTo(version("2025.1.0")) < 0);
    }
}
