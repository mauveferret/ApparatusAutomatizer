package ru.mauveferret;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VacuumController {

    void initialize(SocketCryptedCommunicator communicator)
    {
        this.communicator = communicator;
        pressureChartXAxis.setLabel("Time, ms");
        pressureChartXAxis.setAnimated(true); // axis animations are removed
        pressureChartYAxis.setLabel("Pressure,torr");
        pressureChartYAxis.setAnimated(true); // axis animations are removed
        pressureChartYAxis.setForceZeroInRange(false);
        pressureChartXAxis.setForceZeroInRange(false);
        pressureColumn1Series = new XYChart.Series();
        pressureColumn1Series.setName("First column pressure, torr");
        pressureColumn2Series = new XYChart.Series();
        pressureColumn2Series.setName("Second column pressure, torr");
        pressureScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        pressureChart.getData().add(pressureColumn1Series);
        pressureChart.getData().add(pressureColumn2Series);
        enablePressurePlotting();
    }

    private SocketCryptedCommunicator communicator;
    @FXML
    private  NumberAxis pressureChartYAxis = new NumberAxis();
    @FXML
    private  NumberAxis pressureChartXAxis = new NumberAxis();
    @FXML
    private LineChart<Number,Number> pressureChart = new LineChart<>(pressureChartXAxis, pressureChartYAxis);;
    // Array
    private XYChart.Series pressureColumn1Series;
    private XYChart.Series pressureColumn2Series;
    //used to delete ol data
    final int WINDOW_SIZE = 10;
    private ScheduledExecutorService pressureScheduledExecutorService;



    private void enablePressurePlotting() {
        // setup a scheduled executor to periodically put data into the chart
        pressureScheduledExecutorService.scheduleAtFixedRate(() -> {
            // Update the chart
            Platform.runLater(() -> {
                String message = communicator.makeRequest("nocommand", true);
                long time = Long.parseLong(message.split(" ")[0]);
                double pressure1 = Double.parseDouble(message.split(" ")[1]);
                double pressure2 = Double.parseDouble(message.split(" ")[2]);
                pressureColumn1Series.getData().add(new XYChart.Data<>(time, pressure1));
                pressureColumn2Series.getData().add(new XYChart.Data<>(time, pressure2));
                /*show only part of the chart (left part is gragually deleting)
                if (pressureSeries.getData().size() > WINDOW_SIZE)
                    pressureSeries.getData().remove(0);
                 */
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void disablePressurePlotting(){}
}

