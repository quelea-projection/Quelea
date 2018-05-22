package utils;

import com.sun.javafx.tk.FontMetrics;
import java.lang.reflect.Method;

/**
 *
 * @author Michael
 */
public class FontMetricsWrapper {

    private FontMetrics metrics;

    public FontMetricsWrapper(FontMetrics metrics) {
        this.metrics = metrics;
    }

    public double getLineHeight() {
        return metrics.getLineHeight();
    }

    public double computeStringWidth(String line) {
        if (getCharWidthMethod() != null) {
            if (line == null) {
                return 0;
            }
            double ret = 0;
            Method m = getCharWidthMethod();
            for (char c : line.toCharArray()) {
                ret += (float)invoke(m, metrics, c);
            }
            return ret;
        }
        else {
            return (float)invoke(getComputeStringWidthMethod(), metrics, line);
        }
    }
    
    public Object invoke(Method m, Object o, Object... args) {
        try {
            return m.invoke(o, args);
        }
        catch(Exception ex) {
            return null;
        }
    }

    public Method getCharWidthMethod() {
        try {
            return FontMetrics.class.getMethod("getCharWidth", char.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public Method getComputeStringWidthMethod() {
        try {
            return FontMetrics.class.getMethod("computeStringWidth", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

}
