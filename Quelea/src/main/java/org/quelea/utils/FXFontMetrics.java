package org.quelea.utils;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A replacement for the deprecated / removed Fontmetrics class in JavaFX.
 * @author Michael
 */
public class FXFontMetrics {

    private final Text uiText;
    private final double lineHeight;

    public FXFontMetrics(Font fnt) {
        uiText = new Text();
        uiText.setFont(fnt);
        lineHeight = uiText.getLayoutBounds().getHeight();
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public double computeStringWidth(String txt) {
        uiText.setText(txt);
        return uiText.getLayoutBounds().getWidth();
    }

}
