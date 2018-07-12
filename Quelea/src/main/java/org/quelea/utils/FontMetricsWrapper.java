package org.quelea.utils;

import com.sun.javafx.tk.FontMetrics;
import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author Michael
 */
public class FontMetricsWrapper {

    private FontMetricsIntl metrics;

    public FontMetricsWrapper(FontMetrics metrics) {
        this.metrics = new FontMetricsIntl(metrics.getFont());
    }

    public double getLineHeight() {
        return metrics.lineHeight;
    }

    public double computeStringWidth(String line) {
        return metrics.computeStringWidth(line);
    }
//    
//    public Object invoke(Method m, Object o, Object... args) {
//        try {
//            return m.invoke(o, args);
//        }
//        catch(Exception ex) {
//            return null;
//        }
//    }
//
//    public Method getCharWidthMethod() {
//        try {
//            return FontMetrics.class.getMethod("getCharWidth", char.class);
//        } catch (Exception ex) {
//            return null;
//        }
//    }
//
//    public Method getComputeStringWidthMethod() {
//        try {
//            return FontMetrics.class.getMethod("computeStringWidth", String.class);
//        } catch (Exception ex) {
//            return null;
//        }
//    }
    
    class FontMetricsIntl {

        final private Text internal;
        public float ascent, descent, lineHeight;

        public FontMetricsIntl(Font fnt) {
            internal = new Text();
            internal.setFont(fnt);
            Bounds b = internal.getLayoutBounds();
            lineHeight = (float) b.getHeight();
            ascent = (float) -b.getMinY();
            descent = (float) b.getMaxY();
        }

        public float computeStringWidth(String txt) {
            internal.setText(txt);
            return (float) internal.getLayoutBounds().getWidth();
        }
    }

}
