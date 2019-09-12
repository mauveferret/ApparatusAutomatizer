package ru.mauveferret;

//driver for Pfeiffer MaxiGauge TPG 256A controller Unit

class PfeifferGauge extends Gauge {


    PfeifferGauge(String fileName) {
        super(fileName);
    }

    @Override
    synchronized double  measure(int gauge) {
        String command = "PR"+gauge+"\n";
        writeMessage(command);
       readMessage("\r\n");
        writeMessage(""+'\5');
        String message = readMessage("\r\n");
        //TODO Status
        try {
            double measurement = Double.parseDouble(message.substring(2,10))*0.75;
            pressure[gauge] = measurement;
            deviceWorks = true;
            return measurement;
        }
        catch (Exception e)
        {
            deviceWorks = false;
            return pressure[gauge];
        }
    }

    @Override
    void calibrate(String gaugeType) {

    }

}
