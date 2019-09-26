package ru.mauveferret.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.mauveferret.LogarithmicNumberAxis;
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
        //pressureChartYAxis.setForceZeroInRange(false);
        pressureColumn1Series = new XYChart.Series();
        pressureColumn1Series.setName("First column pressure, torr");
        pressureColumn2Series = new XYChart.Series();
        pressureColumn2Series.setName("Second column pressure, torr");
        pressureScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        pressureChart.getData().add(pressureColumn1Series);
        pressureChart.getData().add(pressureColumn2Series);
        enableDataUpdating();
    }

    private boolean isMenuVisible = false;
    private boolean isFullscreen = false;
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
    private LogarithmicNumberAxis pressureChartYAxis = new LogarithmicNumberAxis(0.000000001,1000);
    @FXML
    private  NumberAxis pressureChartXAxis = new NumberAxis();
    @FXML
    private LineChart<Number,Number> pressureChart = new LineChart<>(pressureChartXAxis, pressureChartYAxis);
    // Array
    private XYChart.Series pressureColumn1Series;
    private XYChart.Series pressureColumn2Series;
    //used to delete ol data
    final int WINDOW_SIZE = 100000;
    private ScheduledExecutorService pressureScheduledExecutorService;
    @FXML
    private TextField pressure1;
    @FXML
    private TextField pressure2;
    @FXML
    private TextField pressure3;
    @FXML
    private TextField time;

    //TODO preferences with choosing units and plot updating regime

    private void enableDataUpdating() {

        // setup a scheduled executor to periodically put data into the chart
        pressureScheduledExecutorService.scheduleAtFixedRate(() -> {
            // Update the chart
            Platform.runLater(() -> {
                //pumpIndicator.setFill(Paint.valueOf("green"));
                if (!disableUpdating) {
                    String[] message = communicator.makeRequest("vac nocom", true).split(" ");
                    long ltime = Long.parseLong(message[0]);
                    //setIndicatorColor(bypass1, message[1].charAt(0) + "");
                   // setIndicatorColor(pump1, message[1].charAt(1) + "");
                   // setIndicatorColor(valve1, message[1].charAt(2) + "");
                   // setIndicatorColor(gate1, message[1].charAt(3) + "");
                   // setIndicatorColor(tmp1, message[1].charAt(4) + "");
                    double dpressure1 = Double.parseDouble(message[2]);
                    double dpressure2 = Double.parseDouble(message[3]);
                   // pressure1.setText(dpressure1+", torr");
                    //pressure2.setText(dpressure2+", torr");
                   // time.setText(".."+message[0].substring(4));
                    pressureColumn1Series.getData().add(new XYChart.Data<>(ltime, dpressure1));
                    pressureColumn2Series.getData().add(new XYChart.Data<>(ltime, dpressure2));
                //show only part of the chart (left part is gragually deleting)
                /*if ( pressureColumn1Series.getData().size() > WINDOW_SIZE)
                    pressureColumn1Series.getData().remove(0);
                    if ( pressureColumn2Series.getData().size() > WINDOW_SIZE)
                        pressureColumn2Series.getData().remove(0);

                 */

                }
            });
        }, 0, 1000, TimeUnit.MILLISECONDS);
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

    @FXML
    private void closeLoginWindows()
    {
        // get a handle to the stage
        Stage stage = (Stage) pressureChart.getScene().getWindow();
        stage.close();
        disableUpdating();
        //System.exit(0);
    }

    @FXML
    private void fullscreen()
    {
        isFullscreen = !isFullscreen;
        Stage stage = (Stage) pressureChart.getScene().getWindow();
        stage.setMaximized(isFullscreen);

    }

    @FXML
    private void menu()
    {
        isMenuVisible=!isMenuVisible;
        //setup.setVisible(isMenuVisible);
    }

    private boolean disableUpdating = false;
    private void disableUpdating(){disableUpdating = true;}
}

