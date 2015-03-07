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

import java.text.NumberFormat;
import java.text.ParseException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;

/**
 * Textfield implementation that accepts formatted number and stores them in a
 * BigDecimal property The user input is formatted when the focus is lost or the
 * user hits RETURN.
 * <p/>
 * @author Thomas Bolz
 */
public class NumberTextField extends TextField {

    private final NumberFormat nf;
    private ObjectProperty<Integer> number = new SimpleObjectProperty<>();

    public final Integer getNumber() {
        return number.get();
    }

    public final void setNumber(Integer value) {
        number.set(value);
    }

    public ObjectProperty<Integer> numberProperty() {
        return number;
    }

    public NumberTextField() {
        this(0);
    }

    public NumberTextField(Integer value) {
        this(value, NumberFormat.getInstance());
        initHandlers();
    }

    public NumberTextField(Integer value, NumberFormat nf) {
        this.nf = nf;
        initHandlers();
        setNumber(value);
        setPrefWidth(45);
    }

    private void initHandlers() {

        // try to parse when focus is lost or RETURN is hit
        setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                parseAndFormatInput();
            }
        });

        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue.booleanValue()) {
                    parseAndFormatInput();
                }
            }
        });

        // Set text in field if Integer property is changed from outside.
        numberProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> obserable, Integer oldValue, Integer newValue) {
                setText(nf.format(newValue));
            }
        });
    }

    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
        try {
            String input = getText();
            if(input == null || input.length() == 0) {
                return;
            }
            Number parsedNumber = nf.parse(input);
            Integer newValue = new Integer(parsedNumber.toString());
            setNumber(newValue);
            selectAll();
        }
        catch(ParseException ex) {
            // If parsing fails keep old number
            setText(nf.format(number.get()));
        }
    }
}
