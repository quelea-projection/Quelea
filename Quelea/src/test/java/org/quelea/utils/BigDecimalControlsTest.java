package org.quelea.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.quelea.testutil.FxTestSupport;

@DisplayName("Decimal controls")
public class BigDecimalControlsTest {
    @Test
    public void textFieldParsesValidValuesAndRestoresInvalidInput() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            BigDecimalTextField field = new BigDecimalTextField(new BigDecimal("2.5"), NumberFormat.getInstance(Locale.US));
            field.setText("15.75");
            field.fireEvent(new ActionEvent());
            Assertions.assertEquals(0, new BigDecimal("15.75").compareTo(field.getNumber()));
            field.setText("not a number");
            field.fireEvent(new ActionEvent());
            Assertions.assertEquals(0, new BigDecimal("15.75").compareTo(field.getNumber()));
            Assertions.assertEquals("15.75", field.getText());
            return null;
        });
    }

    @Test
    public void numericSpinnerDoesNotStepPastItsBounds() throws Exception {
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            BigDecimalSpinner spinner = new BigDecimalSpinner(BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.6"), NumberFormat.getInstance(Locale.US));
            Button up = (Button) spinner.lookup("#" + BigDecimalSpinner.SPINNER_BUTTON_UP);
            Button down = (Button) spinner.lookup("#" + BigDecimalSpinner.SPINNER_BUTTON_DOWN);
            Assertions.assertNotNull(up);
            Assertions.assertNotNull(down);
            up.fire();
            up.fire();
            Assertions.assertEquals(0, BigDecimal.ONE.compareTo(spinner.getNumber()));
            down.fire();
            down.fire();
            Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(spinner.getNumber()));
            return null;
        });
    }

    @Test
    @Disabled("Decimal input accepts a numeric prefix followed by invalid text")
    public void textFieldRejectsTrailingNonNumericCharacters() throws Exception {
        // Silently changing '12oops' to 12 can apply an unintended setting.
        // Parse with a ParsePosition and require the full input to be consumed,
        // accounting for locale-specific whitespace and number formatting.
        FxTestSupport.assumeAvailable();
        FxTestSupport.onFx(() -> {
            BigDecimalTextField field = new BigDecimalTextField(new BigDecimal("2.5"), NumberFormat.getInstance(Locale.US));
            field.setText("12oops");
            field.fireEvent(new ActionEvent());
            Assertions.assertEquals(0, new BigDecimal("2.5").compareTo(field.getNumber()));
            return null;
        });
    }
}
