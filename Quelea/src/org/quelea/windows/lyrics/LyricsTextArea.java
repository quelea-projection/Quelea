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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.InlineCssTextArea;
import org.quelea.services.utils.LineTypeChecker;
import org.quelea.services.utils.LineTypeChecker.Type;

/**
 *
 * @author Michael
 */
public class LyricsTextArea extends InlineCssTextArea {
    
    public LyricsTextArea() {
        textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        refreshStyle();
                    }
                });
            }
        });
    }
    
    public void refreshStyle() {
        clearStyle(0, getLength());
        setStyles(getText());
    }
    
    private void setStyles(String text) {
        String[] lines = text.split("\n");
        int charPos = 0;
        for(int i=0 ; i<lines.length ; i++) {
            String line = lines[i];
            if(new LineTypeChecker(line).getLineType()==Type.TITLE) {
                setStyle(charPos, charPos+line.length(), "-fx-fill: blue; -fx-font-weight: bold;");
            }
            else if(new LineTypeChecker(line).getLineType()==Type.CHORDS) {
                setStyle(charPos, charPos+line.length(), "-fx-fill: grey; -fx-font-style: italic;");
            }
            else if(new LineTypeChecker(line).getLineType()==Type.NONBREAK) {
                setStyle(charPos, charPos+line.length(), "-fx-fill: red; -fx-font-weight: bold;");
            }
            charPos += line.length()+1;
        }
    }
    
}
