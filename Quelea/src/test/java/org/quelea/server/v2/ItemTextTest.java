package org.quelea.server.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ItemTextTest {

    @Test
    void verseNumbersBecomePlainNumbers() {
        assertEquals("16 For God so loved the world. 17 For God sent not his Son.",
                ItemText.plain("<sup>16</sup>For God so loved the world. <sup>17</sup>For God sent not his Son."));
    }

    @Test
    void otherTagsAndEntitiesAreRemoved() {
        assertEquals("Grace & peace <to you>", ItemText.plain("<i>Grace</i> &amp; peace &lt;to you&gt;"));
    }

    @Test
    void plainTextIsUnchanged() {
        String line = "Amazing grace, how sweet the sound";
        assertEquals(line, ItemText.plain(line));
        assertNull(ItemText.plain(null));
    }

    @Test
    void multiLineTitleIsJoined() {
        assertEquals("John 3:16-18 · King James Version", ItemText.title("John 3:16-18\nKing James Version"));
        assertEquals("Amazing Grace", ItemText.title("Amazing Grace"));
        assertEquals("A · B", ItemText.title("  A \r\n\n B  "));
        assertNull(ItemText.title((String) null));
    }
}
