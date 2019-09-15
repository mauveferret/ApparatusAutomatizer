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
    private LineChart<Integer,Double> chart;
    private ObservableList<XYChart.Series<Integer, Double>> datas = FXCollections.observableArrayList();
    XYChart.Series<Integer, Double> series = new XYChart.Series<Integer, Double>();
    @FXML
    protected NumberAxis yAxis;
    @FXML
    protected NumberAxis xAxis;

    void addData(Integer xValue, double yValue)
    {
        //series = new XYChart.Series();
        series.getData().add(new XYChart.Data(xValue,yValue));
        //series.setData(datas);
        //chart.getData().add(series);
        datas.addAll(series);
        chart.setData(datas);
    }

}
