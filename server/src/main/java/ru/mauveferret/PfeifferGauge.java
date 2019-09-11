package ru.mauveferret;


//driver for TPG 256a controller Unit

import java.io.File;

public class PfeifferGauge extends Gauge {


    PfeifferGauge(String fileName) {
        super(fileName);
    }

    @Override
    double measure(int gauge) {
        try
        {
            Thread.sleep(30);
        }
        catch (Exception ignored){}

        String command = "PR"+gauge+"\n";
        writeMessage(command);
        if (('\6'+"").equals(readMessage()))
        {
            writeMessage(""+'\5');
        }
        String message = readMessage();
        //FIXME
        return 0;
    }

    @Override
    void calibrate(String gaugeType) {

    }


    //Getters

    synchronized double getPressure(int gaugeNumber) {
        return pressure[gaugeNumber];
    }


    @Override
    void type() {

    }

    @Override
    boolean callDevice() {
        return false;
    }

    @Override
    void measureAndLog() {

    }
}
