package org.quelea.services.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("Numeric components with leading zeroes are compared by string length")
    public void leadingZerosDoNotChangeNumericOrdering() {
        // Update checks should not treat the same numeric release as newer merely
        // because it uses leading zeroes. Compare component values and decide how
        // equality and hashCode should normalize equivalent version strings.
        Assertions.assertEquals(0, version("2025.01").compareTo(version("2025.1")));
    }
}
