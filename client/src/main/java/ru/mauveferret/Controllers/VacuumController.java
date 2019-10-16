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

    //TMP
    @FXML
    private Button tmp1Run;
    @FXML
    private Button tmp1Control;
    @FXML
    private Button tmp1Standby;
    @FXML
    private Button tmp1Cooling;

    //Gauges
    @FXML
    private Button gaugeControl;
    @FXML
    private Button gaugeCalibrate;

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
    private  NumberAxis tmp1ChartXAxis = new NumberAxis();
    @FXML
    private  NumberAxis tmp1ChartYAxis = new NumberAxis();
    @FXML
    private LineChart<Number,Number> tmp1Chart = new LineChart<>(tmp1ChartXAxis, tmp1ChartYAxis);

    DecimalFormat decFormat = new DecimalFormat("#0.00");
    //used to sent commands from the buttons to the server

    //FIXME when program is opened, it turns off all devices. Probably you nedd "enable connection button" ?!
    // pump, bypass valve, gate, tmpCOntrol,tmpEnable, tmpStandBy, tmpCooling
    private boolean[] buttons1 = new boolean[]{false,false,false,false,false,false,false,false,false};
    private boolean[] buttons2 = new boolean[]{false, false,false,false,false,false,false,false,false};
    private boolean[] gauges = new boolean[]{false,false};
    private int[] response =  new int[20];
    private int[] request =  new int[20];

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
        buttons1[5] = !buttons1[5];
        if (buttons1[5])
            tmp1Control.setText("CONTROL ON");
        else
            tmp1Control.setText("CONTROL OFF");
    }

    @FXML
    private void tmp1RunPressed()
    {
        buttons1[6] = !buttons1[6];
        if (buttons1[6])
            tmp1Run.setText("RUN ON");
        else
            tmp1Run.setText("RUN OFF");
    }

    @FXML
    private void tmp1StandbyPressed()
    {
        buttons1[7] = !buttons1[6];
        if (buttons1[7])
            tmp1Standby.setText("STANDBY ON");
        else
            tmp1Standby.setText("STANDBY OFF");
    }

    @FXML
    private void tmp1CoolingPressed()
    {
        buttons1[8] = !buttons1[6];
        if (buttons1[8])
            tmp1Cooling.setText("COOL ON");
        else
            tmp1Cooling.setText("COOL OFF");
    }

    @FXML
    private void gaugeControlPressed()
    {
        gauges[0] = !gauges[0];
        if (gauges[0])
            gaugeControl.setText("GAUGES ON");
        else
            gaugeControl.setText("GAUGES OFF");
    }

    @FXML
    private void gaugeCalibratePressed()
    {
        gauges[1] = !gauges[1];
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

                    String[] message;
                    message = communicator.makeRequest(refreshRequest(), true).split(" ");

                    long ltime = Long.parseLong(message[0]);
                    setIndicatorColor(pump1, message[1].charAt(0) + "");
                    setIndicatorColor(bypass1, message[1].charAt(1) + "");
                    setIndicatorColor(auto2, message[1].charAt(2) + "");
                    setIndicatorColor(gate1, message[1].charAt(3) + "");
                    setIndicatorColor(tmp1, message[1].charAt(4) + "");


                    double dpressure1 = Double.parseDouble(message[4]);
                    double dpressure2 = Double.parseDouble(message[5]);
                    double dpressure3 = Double.parseDouble(message[6]);
                    int ifreq1 = Integer.parseInt(message[7]);
                    int itemp1 = Integer.parseInt(message[8]);
                    double dvolt1 = Double.parseDouble(message[9]);
                    double dcurr1 = Double.parseDouble(message[10]);
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

    private String refreshRequest()
    {

        //for pump, bypass, valve and gate of both lines
        for (int i=1; i< 5;i++) {
            request[i] = (buttons1[i]) ? 2 : 1;
            request[i+8] = (buttons2[i]) ? 2 : 1;
        }
        //for tmp control, enable, standby, cooling of both lines
        for (int i=5; i<9;i++){
            request[i] = (buttons1[i]) ? 1 : 0;
            request[i+8] = (buttons2[i]) ? 1 : 0;
        }
        //for gauges
        request[17] = (gauges[0]) ? 1 : 0;
        request[18] = (gauges[1]) ? 1 : 0;
        String sRequest = (System.currentTimeMillis()+"").substring(7)+" ";
        for (int s: request) sRequest+=s;
        return  sRequest;
    }

    private void setIndicatorColor(Button button, String flag)
    {
            switch (Integer.parseInt(flag)) {
                case 0:
                    button.setStyle(".IndicatorDeviceIsDisconnected");
                    break;
                case 1: {
                    button.setStyle(".IndicatorDeviceIsOff");
                }
                    break;
                case 2:{
                    button.setStyle(".IndicatorDeviceIsOn");
                }
                break;
                case 3:
                case 4:
                case 5:
                {
                    button.setStyle(".IndicatorDeviceError");
                }
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

