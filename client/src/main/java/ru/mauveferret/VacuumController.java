package ru.mauveferret;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class VacuumController {

    private SocketCryptedCommunicator communicator;
    void setCommunicator(SocketCryptedCommunicator communicator)
    {
        this.communicator = communicator;
        System.out.println("получилось1!");
        new Thread(() -> {
            new Vacuum(this, communicator).start();
        }).start();
    }

    @FXML
    private LineChart<Number,Number> chart;
    private ObservableList<XYChart.Series<Number, Number>> datas = FXCollections.observableArrayList();
    XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
    @FXML
    protected NumberAxis yAxis;
    @FXML
    protected NumberAxis xAxis;

    void addData(Number xValue, Number yValue)
    {
        System.out.println(xValue+" "+yValue);
        //series = new XYChart.Series();
        //series.getData().add(new XYChart.Data<Number,Number>(xValue,yValue));
        //series.setData(datas);
        //chart.getData().add(series);
        //datas.addAll(series);
        //chart.getData().add(series);
    }

}
