package org.quelea.services.notice;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.services.utils.SerializableColor;
import org.quelea.services.utils.SerializableFont;

@DisplayName("Notice persistence")
public class NoticeTest {
    private Notice notice(String text, int times) {
        return new Notice(text, times, new SerializableColor(Color.WHITE), new SerializableFont(Font.font("System", 24)));
    }

    private Notice roundTrip(Notice notice) throws Exception {
        var input = new ByteArrayInputStream(notice.getXML().getBytes(StandardCharsets.UTF_8));
        return Notice.parseXML(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input).getDocumentElement());
    }

    @Test
    public void xmlRoundTripPreservesTextFormattingAndOriginalDuration() throws Exception {
        Notice original = notice("Tea & coffee <afterwards> \u263a", 3);
        original.decrementTimes();
        Notice saved = roundTrip(original);
        Assertions.assertEquals(original.getText(), saved.getText());
        Assertions.assertEquals(3, saved.getTimes());
        Assertions.assertEquals(original.getColor().getColor(), saved.getColor().getColor());
        Assertions.assertEquals(original.getFont().getFont().getName(), saved.getFont().getFont().getName());
        Assertions.assertEquals(original.getFont().getFont().getSize(), saved.getFont().getFont().getSize());
    }

    @Test
    public void unlimitedNoticeRoundTrips() throws Exception {
        Assertions.assertEquals(Integer.MAX_VALUE, roundTrip(notice("Always", Integer.MAX_VALUE)).getTimes());
    }

    @Test
    public void copiedNoticePersistsTheCopiedDuration() throws Exception {
        Notice original = notice("Old", 2);
        original.copyAttributes(notice("New", 7));
        Assertions.assertEquals(7, original.getTimes());
        Assertions.assertEquals(7, roundTrip(original).getTimes());
    }
}
