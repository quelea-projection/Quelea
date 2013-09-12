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
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.TimeStringConverter;


/**
 * Displays a graph of Quelea invocations. Not tied to Quelea itself, just used
 * independently to visualise some phonehome data.
 * <p/>
 * @author Michael
 */
public class InvocationChart extends Application {

    private static final String RAW_DUMP_URL = "http://quelea.org/phonehome/dumpraw.php";
    private static final boolean JUST_SUNDAY = true;
    private static final boolean SHOW_ZERO = false;
    private TreeMap<Date, TreeMap<String, Integer>> times = new TreeMap<>();

    /**
     * Fire it off.
     * <p/>
     * @param stage
     */
    @Override
    public void start(Stage stage) {
        try {
            if(JUST_SUNDAY) {
                stage.setTitle("Unique invocations per Sunday");
            }
            else {
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
            URL url = new URL(RAW_DUMP_URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            Date minDate = new Date(Long.MAX_VALUE);
            Date maxDate = new Date(0);
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
            while((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                Date date = formatter.parse(parts[0]);
                if(date.after(maxDate)) {
                    maxDate = date;
                }
                if(date.before(minDate)) {
                    minDate = date;
                }
                String ip = parts[1];
                if(times.get(date) == null) {
                    times.put(date, new TreeMap<String, Integer>());
                }
                if(times.get(date).get(ip) == null) {
                    times.get(date).put(ip, 0);
                }
                times.get(date).put(ip, times.get(date).get(ip) + 1);
            }

            Calendar incDate = Calendar.getInstance();
            incDate.setTime(minDate);
            if(JUST_SUNDAY) {
                while(incDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    incDate.set(Calendar.DAY_OF_YEAR, incDate.get(Calendar.DAY_OF_YEAR) + 1);
                }
            }
            int addDays = 1;
            if(JUST_SUNDAY) {
                addDays = 7;
            }
            for(; incDate.getTimeInMillis() <= maxDate.getTime(); incDate.add(Calendar.DAY_OF_YEAR, addDays)) {
                TreeMap<String, Integer> map = times.get(incDate.getTime());
                int val;
                if(map == null) {
                    if(!SHOW_ZERO) {
                        continue;
                    }
                    val = 0;
                }
                else {
                    val = map.size();
                }
                if(JUST_SUNDAY && incDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    throw new AssertionError("Oops... Sunday's got out of sync!");
                }
                XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(incDate.getTimeInMillis(), val);
                series.getData().add(data);
                data.setNode(new HoveredThresholdNode(formatter.format(incDate.getTime()) + "\n" + val + " invocations"));

            }

            Scene scene = new Scene(lineChart);
            lineChart.getData().add(series);

            stage.setScene(scene);
            stage.show();
        }
        catch(Exception ex) {
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

        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                getChildren().setAll(label);
                toFront();
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                getChildren().clear();
            }
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