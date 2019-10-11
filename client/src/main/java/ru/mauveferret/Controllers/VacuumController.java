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

import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VacuumController {

    void initialize(SocketCryptedCommunicator communicator)
    {

        //Gauges axis

        this.communicator = communicator;
        pressureChartXAxis.setLabel("Time, ms");
        pressureChartXAxis.setAnimated(true); // axis animations are removed
        pressureChartXAxis.setForceZeroInRange(false);
        pressureChartYAxis.setLabel("Pressure,torr");
        pressureChartYAxis.setAnimated(true); // axis animations are removed
        //pressureChartYAxis.setForceZeroInRange(false);

        //FIXME how to make several axis/ Now only frequency is
        //TMP1 axis
        tmp1ChartXAxis.setLabel("Time, ms");
        tmp1ChartXAxis.setAnimated(true);
        tmp1ChartXAxis.setAutoRanging(true);
        tmp1ChartXAxis.setForceZeroInRange(false);
        tmp1ChartYAxis.setLabel("temperature, celsium");
        tmp1ChartYAxis.setAnimated(true);
        tmp1ChartYAxis.setAutoRanging(true);

        pressureColumn1Series = new XYChart.Series();
        pressureColumn1Series.setName("First column pressure, torr");
        pressureColumn2Series = new XYChart.Series();
        pressureColumn2Series.setName("Second column pressure, torr");
        freq1Series = new XYChart.Series();
        freq1Series.setName("frequency, Hz");
        pressureScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        pressureChart.getData().add(pressureColumn1Series);
        pressureChart.getData().add(pressureColumn2Series);

        tmp1Chart.getData().add(freq1Series);

        enableDataUpdating();
    }

    private boolean isMenuVisible = false;
    private boolean isFullscreen = false;
    private SocketCryptedCommunicator communicator;

    // menu buttons

    @FXML
    private Button tmp1Run;
    @FXML
    private Button tmp1Control;

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
    private Button auto2;

    //Charts

    @FXML
    private LogarithmicNumberAxis pressureChartYAxis = new LogarithmicNumberAxis(0.000000001,1000);
    @FXML
    private  NumberAxis pressureChartXAxis = new NumberAxis();
    @FXML
    private LineChart<Number,Number> pressureChart = new LineChart<>(pressureChartXAxis, pressureChartYAxis);
    @FXML
    private LineChart<Number,Number> tmp1Chart = new LineChart<>(pressureChartXAxis, pressureChartYAxis);
    @FXML
    private  NumberAxis tmp1ChartXAxis = new NumberAxis();
    @FXML
    private  NumberAxis tmp1ChartYAxis = new NumberAxis();

    DecimalFormat decFormat = new DecimalFormat("#0.00");
    //used to sent commands from the buttons to the server

    //FIXME when program is opened, it turns off all devices. Probably you nedd "enable connection button" ?!
    //bypass, pump, valve, gate, tmpCOntrol,tmpEnable
    boolean[] buttons = new boolean[]{false,false,false,false,false,false};

    // Array
    private XYChart.Series pressureColumn1Series;
    private XYChart.Series pressureColumn2Series;

    private XYChart.Series temp1Series;
    private XYChart.Series freq1Series;
    private XYChart.Series vol1Series;
    private XYChart.Series curr1Series;

    //used to delete ol data
    final int WINDOW_SIZE = 100000;
    private ScheduledExecutorService pressureScheduledExecutorService;

    //Gauges Data Bar

    @FXML
    private TextField pressure1;
    @FXML
    private TextField pressure2;
    @FXML
    private TextField pressure3;
    @FXML
    private TextField time;

    //TMP1 Data Bar
    @FXML
    private TextField temp1;
    @FXML
    private TextField freq1;
    @FXML
    private TextField volt1;
    @FXML
    private TextField curr1;
    @FXML
    private TextField tmp1Time;


    //Buttons methods

    @FXML
    private void tmp1ControlPressed()
    {
        buttons[4] = !buttons[4];
        if (buttons[4])
            tmp1Control.setText("CONTROL ON");
        else
            tmp1Control.setText("CONTROL OFF");
    }

    @FXML
    private void tmp1RunPressed()
    {
        buttons[5] = !buttons[5];
        if (buttons[5])
            tmp1Run.setText("RUN ON");
        else
            tmp1Run.setText("RUN OFF");
    }

    //data updating

    //TODO preferences with choosing units and plot updating regime

    private void enableDataUpdating() {

        // setup a scheduled executor to periodically put data into the chart
        pressureScheduledExecutorService.scheduleAtFixedRate(() -> {
            // Update the chart
            Platform.runLater(() -> {
                //pumpIndicator.setFill(Paint.valueOf("green"));
                if (!disableUpdating) {

                    String command =  "";
                    for (int i=0; i< buttons.length;i++) command+= (buttons[i]) ? "1" : "0";
                    String[] message = communicator.makeRequest("vac "+command, true).split(" ");
                    long ltime = Long.parseLong(message[0]);
                    setIndicatorColor(bypass1, message[1].charAt(0) + "");
                    setIndicatorColor(pump1, message[1].charAt(1) + "");
                    setIndicatorColor(auto2, message[1].charAt(2) + "");
                    setIndicatorColor(gate1, message[1].charAt(3) + "");
                    setIndicatorColor(tmp1, message[1].charAt(4) + "");

                    //setIndicatorColor();

                    double dpressure1 = Double.parseDouble(message[2]);
                    double dpressure2 = Double.parseDouble(message[3]);
                    double dpressure3 = Double.parseDouble(message[4]);
                    int ifreq1 = Integer.parseInt(message[5]);
                    int itemp1 = Integer.parseInt(message[6]);
                    double dvolt1 = Double.parseDouble(message[7]);
                    double dcurr1 = Double.parseDouble(message[8]);
                    pressure1.setText(String.format("%6.2E", dpressure1));
                    pressure2.setText(String.format("%6.2E", dpressure2));
                    pressure3.setText(String.format("%6.2E", dpressure3));
                    time.setText(""+message[0].substring(6));

                    freq1.setText(ifreq1+"");
                    temp1.setText(itemp1+"");
                    volt1.setText(decFormat.format(dvolt1));
                    curr1.setText(decFormat.format(dcurr1));
                    tmp1Time.setText(""+message[0].substring(6));

                    pressureColumn1Series.getData().add(new XYChart.Data<>(ltime, dpressure1));
                    pressureColumn2Series.getData().add(new XYChart.Data<>(ltime, dpressure2));
                    freq1Series.getData().add(new XYChart.Data<>(ltime, ifreq1));
                //show only part of the chart (left part is gragually deleting)
                /*if ( pressureColumn1Series.getData().size() > WINDOW_SIZE)
                    pressureColumn1Series.getData().remove(0);
                    if ( pressureColumn2Series.getData().size() > WINDOW_SIZE)
                        pressureColumn2Series.getData().remove(0);

                 */

                }
            });
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void setIndicatorColor(Button button, String flag)
    {
            switch (Integer.parseInt(flag)) {
                case 0:
                    button.setStyle(".IndicatorDeviceIsOff");
                    break;
                case 1:
                {
                    button.setStyle(".IndicatorDeviceIsOn");
                }
                    break;
                case 2:
                case 3:
                case 4: {
                    button.setStyle(".IndicatorDeviceError");
                }
                break;
                case 5:
                    button.setStyle(".IndicatorDeviceIsDisconnected");
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

