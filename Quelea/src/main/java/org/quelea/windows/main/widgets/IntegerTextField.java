/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.main.widgets;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * @author Shivaji.Barge
 * <p/>
 */
public class IntegerTextField extends TextField {

    private String numericLastKey;

    public IntegerTextField() {
        super();

        this.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            char[] ar = event.getCharacter().toCharArray();
            char ch = ar[event.getCharacter().toCharArray().length - 1];
            /*populating lastkey if it is numeric*/
            if ((ch >= '0' && ch <= '9')) {
                numericLastKey = String.valueOf(ch);
            }

            if (isValid()) {
                /* Disable other charater than numeric character. */
                if (!(ch >= '0' && ch <= '9')) {
                    event.consume();
                }
            } else {
                event.consume();
            }
        });

        /*Disabling 'invalid sting' past functionality if not numeric */
        this.textProperty().addListener((arg0, oldValue, newValue) -> {
            if (!isNumeric(newValue)) {
                clear();
            } else if (!isValid()) {
                clear();
            }
        });

        /**
         * check for numeric value.
         * <p/>
         * @param text
         * @return boolean
         */

    }
    private boolean isNumeric(String text) {
        return text.matches("-?\\d+(.\\d+)?");
    }

    /**
     * Check for valid text or not.
     * <p/>
     * @return boolean if not valid then return false else true.
     */
    private boolean isValid() {
        if(getText().length() == 0) {
            return true;
        }
        try {
            String testText = getText();
            testText = (numericLastKey != null && !"".equals(numericLastKey)) ? testText + numericLastKey : testText;
            numericLastKey = "";
            Integer.parseInt(testText);
        }
        catch(NumberFormatException ex) {
            return false;
        }
        return true;
    }
}
