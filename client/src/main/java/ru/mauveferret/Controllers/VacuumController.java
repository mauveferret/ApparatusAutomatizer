package ru.mauveferret.Controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
        //pressureChartXAxis.setLabel("Time, ms");
        pressureChartXAxis.setAnimated(false); // axis animations are removed
        pressureChartXAxis.setForceZeroInRange(false);
        pressureChartYAxis.setLabel("Pressure,torr");
        pressureChartYAxis.setAnimated(false); // axis animations are removed

        //FIXME how to make several axis/ Now only frequency is
        //TMP1 axis
        //tmp1ChartXAxis.setLabel("Time, ms");
        tmp1ChartXAxis.setAnimated(false);
        tmp1ChartXAxis.setAutoRanging(true);
        tmp1ChartXAxis.setForceZeroInRange(false);
        tmp1ChartYAxis.setLabel("frequency, Hz");
        tmp1ChartYAxis.setAnimated(false);
        tmp1ChartYAxis.setAutoRanging(true);

        pressureColumn1Series = new XYChart.Series();
        //pressureColumn1Series.setName("First column pressure, torr");
        pressureColumn2Series = new XYChart.Series();
        //pressureColumn2Series.setName("Second column pressure, torr");
        pressureColumn3Series = new XYChart.Series();
        freq1Series = new XYChart.Series();
        //freq1Series.setName("frequency, Hz");
        pressureScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        pressureChart.getData().add(pressureColumn1Series);
        pressureChart.getData().add(pressureColumn2Series);
        pressureChart.getData().add(pressureColumn3Series);

        tmp1Chart.getData().add(freq1Series);

        line1ButtonControls = new Button[]{auto1, angel1,gauge1, pump1,bypass1,valve1,gate1,
                tmp1,temperat1,press1};

        auto1.setTooltip(auto1Tooltip);
        angel1.setTooltip(angel1Tooltip);
        gauge1.setTooltip(gauge1Tooltip);
        pump1.setTooltip(pump1Tooltip);
        bypass1.setTooltip(bypass1Tooltip);
        valve1.setTooltip(valve1Tooltip);
        gate1.setTooltip(gate1Tooltip);
        tmp1.setTooltip(tmp1Tooltip);
        temperat1.setTooltip(temperat1Tooltip);
        press1.setTooltip(press1Tooltip);

        for (int i=0; i<10; i++)  line1ButtonControls[i].getTooltip().getStyleClass().add("tooltip");

        //set buttons positions and colors

        initializeButtons();


        enableDataUpdating();
    }

    private void initializeButtons(){
        Platform.runLater(() -> {
            String mess = (System.currentTimeMillis()+"").substring(7)+" but ";
            String[] message;
            message = communicator.makeRequest(mess, true).split(" ");
            for (int i=0; i<message[2].length();i++)
            {
                buttons1[i] = (message[2].charAt(i)+"").equals( (i<7) ? "2" : "1");
                if (i<7) line1ButtonControls[i].setText((buttons1[i]) ? ">" : "<");
                gaugeControl.setText("GAUGES "+(buttons1[2] ? "ON" : "OFF"));
                tmp1Cooling.setText("CONTROL "+(buttons1[7] ? "ON" : "OFF"));
                tmp1Cooling.setText("COOL "+(buttons1[9] ? "ON" : "OFF"));
                tmp1Cooling.setText("RUN "+(buttons1[8] ? "ON" : "OFF"));
                tmp1Standby.setText("STANDBY "+(buttons1[10] ? "ON" : "OFF"));
            }
        });
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

    //ButtonPanel
    @FXML
    private Button auto1;
    @FXML
    private Tooltip auto1Tooltip = new Tooltip();
    @FXML
    private Button angel1;
    @FXML
    private Tooltip angel1Tooltip = new Tooltip();
    @FXML
    private Button gauge1;
    @FXML
    private Tooltip gauge1Tooltip = new Tooltip();
    @FXML
    private Button pump1;
    @FXML
    private Tooltip pump1Tooltip = new Tooltip();
    @FXML
    private Button bypass1;
    @FXML
    private Tooltip bypass1Tooltip = new Tooltip();
    @FXML
    private Button valve1;
    @FXML
    private Tooltip valve1Tooltip = new Tooltip();
    @FXML
    private Button gate1;
    @FXML
    private Tooltip gate1Tooltip = new Tooltip();
    @FXML
    private Button tmp1;
    @FXML
    private Tooltip tmp1Tooltip = new Tooltip();
    @FXML
    private Button temperat1;
    @FXML
    private Tooltip temperat1Tooltip = new Tooltip();
    @FXML
    private Button press1;
    @FXML
    private Tooltip press1Tooltip = new Tooltip();


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
    private boolean[] buttons1 = new boolean[11];
    private boolean[] buttons2 = new boolean[11];
    private int[] response =  new int[30];
    private int[] request =  new int[30];
    private Button[] line1ButtonControls;
    private Button[] line2ButtonControls;

    // Array
    private XYChart.Series pressureColumn1Series;
    private XYChart.Series pressureColumn2Series;
    private XYChart.Series pressureColumn3Series;

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
    //TMP

    // FROM THE SERVER:  0 -> auto, angel, gauge,pump, bypass,valve,6 ->gate, 7 -> tmp control,
    // tmp run, 9 -> tmp cool, 10 -> tmp standby   == 11 buttons

    @FXML
    private void tmp1ControlPressed()
    {
        buttons1[7] = !buttons1[7];
        tmp1Control.setText("CONTROL "+(buttons1[7] ? "ON" : "OFF"));
    }

    @FXML
    private void tmp1RunPressed()
    {
        buttons1[8] = !buttons1[8];
        tmp1Run.setText("RUN "+(buttons1[8] ? "ON" : "OFF"));
    }

    @FXML
    private void tmp1CoolingPressed()
    {
        buttons1[9] = !buttons1[9];
        line1ButtonControls[8].setText((buttons1[9]) ? ">" : "<");
        tmp1Cooling.setText("COOL "+(buttons1[9] ? "ON" : "OFF"));
    }

    @FXML
    private void tmp1StandbyPressed()
    {
        buttons1[10] = !buttons1[10];
        tmp1Standby.setText("STANDBY "+(buttons1[10] ? "ON" : "OFF"));
    }



    //GAUGES

    @FXML
    private void gaugeControlPressed()
    {
        buttons1[2] = !buttons1[2];
        gaugeControl.setText("GAUGES "+(buttons1[2] ? "ON" : "OFF"));
        line1ButtonControls[2].setText((buttons1[2]) ? ">" : "<");
    }

    @FXML
    private void gaugeCalibratePressed()
    {
        //FIXME
    }

    //Button Panel

    @FXML
    private void auto1Pressed()
    {
        buttons1[0] = !buttons1[0];
        line1ButtonControls[0].setText((buttons1[0]) ? ">" : "<");
    }

    @FXML
    private void automation1Pressed()
    {
        buttons1[1] = !buttons1[1];
        line1ButtonControls[1].setText((buttons1[1]) ? ">" : "<");
    }

    @FXML
    private void pump1Pressed()
    {
        buttons1[3] = !buttons1[3];
        line1ButtonControls[3].setText((buttons1[3]) ? ">" : "<");
    }

    @FXML
    private void bypass1Pressed()
    {
        buttons1[4] = !buttons1[4];
        line1ButtonControls[4].setText((buttons1[4]) ? ">" : "<");
    }

    @FXML
    private void valve1Pressed()
    {
        buttons1[5] = !buttons1[5];
        line1ButtonControls[5].setText((buttons1[5]) ? ">" : "<");
    }

    @FXML
    private void gate1Pressed()
    {
        buttons1[6] = !buttons1[6];
        line1ButtonControls[6].setText((buttons1[6]) ? ">" : "<");
    }

    @FXML
    private void tmp1Pressed()
    {
        buttons1[7] = false;
        buttons1[8] = buttons1[8];
        //magic~~~
        tmp1ControlPressed();
        tmp1RunPressed();
        line1ButtonControls[7].setText((buttons1[8]) ? ">" : "<");
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

                    String first7TimeDigits =(System.currentTimeMillis()+"").substring(0,7);
                   long ltime = Long.parseLong((first7TimeDigits+""+message[0]));

                    //FIXME ypu indicate not the propper TMP: indicate  control, but need run.
                    // So change their positions in protocol

                    for (int i=0;i<10;i++) {
                        if (response[i] != Integer.parseInt(message[2].charAt(i) + "")) {
                            setIndicatorColor(line1ButtonControls[i], message[2].charAt(i) + "");
                        }
                    }

                    refreshResponse(message);

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
                    time.setText((ltime+"").substring(6));

                    freq1.setText(ifreq1+"");
                    temp1.setText(itemp1+"");
                    volt1.setText(decFormat.format(dvolt1));
                    curr1.setText(decFormat.format(dcurr1));
                    tmp1Time.setText((ltime+"").substring(6));

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
        //FIXME is it correct for auto, automation and gauges?
        for (int i=0; i< 7;i++) {
            request[i] = (buttons1[i]) ? 2 : 1;
            request[i+8] = (buttons2[i]) ? 2 : 1;
        }
        //for tmp control, enable, standby, cooling of both lines
        for (int i=7; i<11;i++){
            request[i] = (buttons1[i]) ? 1 : 0;
            request[i+8] = (buttons2[i]) ? 1 : 0;
        }

        String sRequest = (System.currentTimeMillis()+"").substring(7)+" vac ";
        for (int i=0;i<11;i++) sRequest+=request[i]; //first line
        sRequest+=" ";
        for (int i=11;i<18;i++) sRequest+=request[i]; //second line
        return  sRequest;
    }

    private void refreshResponse(String[] message)
    {
        String mes = message[2]+""+message[3];
        for (int i=0; i<mes.length();i++) response[i] = Integer.parseInt(mes.charAt(i)+"");
    }

    private void setIndicatorColor(Button button, String flag)
    {
            switch (Integer.parseInt(flag)) {
                case 0:
                {
                    button.setStyle("-fx-background-color: #000000");
                    button.getTooltip().setText("device switched off");
                }
                    break;
                case 1: {
                    button.setStyle("-fx-background-color: #ffffff");
                    button.getTooltip().setText("device switched on and disabled");
                }
                    break;
                case 2:{
                    button.setStyle("-fx-background-color: #99FF33");
                    button.getTooltip().setText("device switched on and enabled");
                }
                break;
                case 3:
                case 4:
                case 5:
                {
                    button.setStyle("-fx-background-color: #FF0000");
                    button.getTooltip().setText("device switched on, low pneumo line pressure");
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

