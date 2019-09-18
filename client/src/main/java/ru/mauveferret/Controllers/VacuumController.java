package ru.mauveferret.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import ru.mauveferret.SocketCryptedCommunicator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VacuumController {

    void initialize(SocketCryptedCommunicator communicator)
    {
        this.communicator = communicator;
        pressureChartXAxis.setLabel("Time, ms");
        pressureChartXAxis.setAnimated(true); // axis animations are removed
        pressureChartXAxis.setForceZeroInRange(false);
        pressureChartYAxis.setLabel("Pressure,torr");
        pressureChartYAxis.setAnimated(true); // axis animations are removed
        pressureChartYAxis.setForceZeroInRange(false);
        pressureColumn1Series = new XYChart.Series();
        pressureColumn1Series.setName("First column pressure, torr");
        pressureColumn2Series = new XYChart.Series();
        pressureColumn2Series.setName("Second column pressure, torr");
        pressureScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        pressureChart.getData().add(pressureColumn1Series);
        pressureChart.getData().add(pressureColumn2Series);
        enableDataUpdating();
    }

    private SocketCryptedCommunicator communicator;
    @FXML
    private Button pump1;
    @FXML
    private Button bypass1;
    @FXML
    private Button valve1;
    @FXML
    private Button gate1;
    @FXML
    private Button tmp1;
    @FXML
    private NumberAxis pressureChartYAxis = new NumberAxis();
    //private LogarithmicNumberAxis pressureChartYAxis = new LogarithmicNumberAxis(0.0000000001, 1000);
    @FXML
    private  NumberAxis pressureChartXAxis = new NumberAxis();
    @FXML
    private LineChart<Number,Number> pressureChart = new LineChart<>(pressureChartXAxis, pressureChartYAxis);
    // Array
    private XYChart.Series pressureColumn1Series;
    private XYChart.Series pressureColumn2Series;
    //used to delete ol data
    final int WINDOW_SIZE = 10;
    private ScheduledExecutorService pressureScheduledExecutorService;



    private void enableDataUpdating() {

        // setup a scheduled executor to periodically put data into the chart
        pressureScheduledExecutorService.scheduleAtFixedRate(() -> {
            // Update the chart
            Platform.runLater(() -> {
                //pumpIndicator.setFill(Paint.valueOf("green"));
                String[] message = communicator.makeRequest("vac nocom", true).split(" ");
                long time = Long.parseLong(message[0]);
                setIndicatorColor(bypass1,message[1].charAt(0)+"");
                setIndicatorColor(pump1,message[1].charAt(1)+"");
                setIndicatorColor(valve1,message[1].charAt(2)+"");
                setIndicatorColor(gate1,message[1].charAt(3)+"");
                setIndicatorColor(tmp1,message[1].charAt(4)+"");
                double pressure1 = Double.parseDouble(message[3]);
                double pressure2 = Double.parseDouble(message[4]);
                pressureColumn1Series.getData().add(new XYChart.Data<>(time, pressure1));
                pressureColumn2Series.getData().add(new XYChart.Data<>(time, pressure2));
                /*show only part of the chart (left part is gragually deleting)
                if (pressureSeries.getData().size() > WINDOW_SIZE)
                    pressureSeries.getData().remove(0);
                 */
            });
        }, 0, 1, TimeUnit.SECONDS);
    }


    private void setIndicatorColor(Button button, String flag)
    {

        switch (Integer.parseInt(flag))
        {

            case 0: button.setStyle("-fx-background-color: #0000FF; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #ffffff; -fx-border-width: 1;");
            break;
            case 1: button.setStyle("-fx-background-color: #66CC33; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #ffffff; -fx-border-width: 1;");
            break;
            case 2: button.setStyle("-fx-background-color: #990000; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #ffffff; -fx-border-width: 1;");
            break;
        }
    }

    private void disablePressurePlotting(){}
}

