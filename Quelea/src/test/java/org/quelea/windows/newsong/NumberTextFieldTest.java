package org.quelea.windows.newsong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;

@DisplayName("Theme numeric input")
public class NumberTextFieldTest {
    @Test
    public void acceptsDigitsAndDeletionButRejectsLetters() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            NumberTextField field = new NumberTextField();
            field.replaceText(0, 0, "1");
            field.replaceText(1, 1, "2");
            field.replaceText(2, 2, "x");
            Assertions.assertEquals("12", field.getText());
            field.replaceText(1, 2, "");
            Assertions.assertEquals("1", field.getText());
            return null;
        });
    }

    @Test
    public void acceptsPastedMultiDigitNumbers() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            NumberTextField field = new NumberTextField();
            field.replaceSelection("123");
            Assertions.assertEquals("123", field.getText());
            field.selectAll();
            field.replaceSelection("45x");
            Assertions.assertEquals("123", field.getText());
            return null;
        });
    }
}
