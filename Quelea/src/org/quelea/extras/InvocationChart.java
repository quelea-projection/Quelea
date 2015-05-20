/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.extras;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.TimeStringConverter;
import org.javafx.dialog.InputDialog;

/**
 * Displays a graph of Quelea invocations. Not tied to Quelea itself, just used
 * independently to visualise some phonehome data.
 * <p/>
 * @author Michael
 */
public class InvocationChart extends Application {

    private static final String RAW_DUMP_URL = "http://quelea.org/phonehome/dumpraw.php";
    private static final boolean JUST_SUNDAY = true;
    private final TreeMap<Date, Integer> times = new TreeMap<>();

    /**
     * Fire it off.
     * <p/>
     * @param stage
     */
    @Override
    public void start(Stage stage) {
        try {
            if (JUST_SUNDAY) {
                stage.setTitle("Unique invocations per Sunday");
            } else {
                stage.setTitle("Unique invocations per day");
            }
            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Time");
            yAxis.setLabel("Invocations");
            xAxis.setForceZeroInRange(false);
            xAxis.setTickLabelFormatter(new DateStringConverter());
            final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setLegendVisible(false);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            String password = InputDialog.getUserInput("Data password?", "");
            URL url = new URL(RAW_DUMP_URL + "?pass=" + password);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            Date minDate = new Date(Long.MAX_VALUE);
            Date maxDate = new Date(0);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            while ((line = reader.readLine()) != null) {
                if (line.equalsIgnoreCase("Unable to select database")) {
                    System.err.println("Auth error.");
                    System.exit(0);
                }
                String[] parts = line.split(",");
                Date date = formatter.parse(parts[0]);
                if (date.after(maxDate)) {
                    maxDate = date;
                }
                if (date.before(minDate)) {
                    minDate = date;
                }
                String num = parts[1];
                times.put(date, Integer.parseInt(num));
            }

            for (Entry<Date, Integer> entry : times.entrySet()) {
                Date date = entry.getKey();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if(JUST_SUNDAY && cal.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY) {
                    continue;
                }
                Integer num = entry.getValue();
                
                XYChart.Data<Number, Number> data = new XYChart.Data<>(date.getTime(), num);
                series.getData().add(data);
                data.setNode(new HoveredThresholdNode(formatter.format(date.getTime()) + "\n" + num + " invocations"));

            }

            Scene scene = new Scene(lineChart);
            lineChart.getData().add(series);

            stage.setScene(scene);
            stage.show();
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * Used to convert a timestamp (epoch millis) to a human readable date.
 * <p/>
 * @author Michael
 */
class DateStringConverter extends StringConverter<Number> {

    TimeStringConverter tsc = new TimeStringConverter("dd.MM.yy");

    @Override
    public String toString(Number t) {
        return tsc.toString(new Date(t.longValue()));
    }

    @Override
    public Number fromString(String string) {
        return 1;
    }
}

/**
 * Used to display accurate data when a particular chart point is hovered over.
 * <p/>
 * @author Michael
 */
class HoveredThresholdNode extends StackPane {

    HoveredThresholdNode(String text) {
        final Label label = createDataThresholdLabel(text);

        setOnMouseEntered(mouseEvent -> {
            getChildren().setAll(label);
            toFront();
        });
        setOnMouseExited(mouseEvent -> {
            getChildren().clear();
        });
    }

    private Label createDataThresholdLabel(String val) {
        final Label label = new Label(val);
        label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
        label.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        label.setTextFill(Color.FIREBRICK);
        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }
}
