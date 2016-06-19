/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.lyrics;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Basic formatted text that takes a (very) restricted subset of formatting tags
 * and applies them.
 * <p>
 * @author Michael
 */
public class FormattedText extends HBox {

    /**
     * Create new formatted text from a string.
     * <p>
     * @param line the string to create formatted text from.
     */
    public FormattedText(String line) {
        super(0);
        while(true) {
            int startSup = line.indexOf("<sup>");
            if(startSup == -1) {
                getChildren().add(new Text(line));
                break;
            }
            else {
                String normalStr = line.substring(0, startSup);
                if(!normalStr.isEmpty()) {
                    getChildren().add(new Text(normalStr));
                }
                line = line.substring(startSup + 5);
                int endSup = line.indexOf("</sup>");
                if(endSup == -1) {
                    endSup = 0;
                }
                String supStr = line.substring(0, endSup);
                if(!supStr.isEmpty()) {
                    Text supText = new Text(supStr);
                    line = line.substring(endSup + 6);
                    supText.setScaleX(0.5);
                    supText.setScaleY(0.5);
                    getChildren().add(supText);
                }
            }
        }
    }

    /**
     * Set the font of this formatted text.
     * <p>
     * @param font the font.
     */
    public void setFont(Font font) {
        for(Node node : getChildren()) {
            if(node instanceof Text) {
                ((Text) node).setFont(font);
            }
        }
    }

    /**
     * Set the fill of this formatted text.
     * <p>
     * @param paint the fill.
     */
    public void setFill(Paint paint) {
        for(Node node : getChildren()) {
            if(node instanceof Text) {
                ((Text) node).setFill(paint);
            }
        }
    }

    /**
     * Strip a string of all its formatting tags used to format text. This is
     * required when performed font metrics calculations.
     * <p>
     * @param text the text to strip tags from.
     * @return the string without formatting tags.
     */
    public static String stripFormatTags(String text) {
        if(text == null) {
            return null;
        }
        text = text.replace("<sup>", "");
        return text.replace("</sup>", "");
    }

}
