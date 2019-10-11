package ru.mauveferret.Vacuum.Gauges;

//driver for Pfeiffer MaxiGauge TPG 256A controller Unit

import ru.mauveferret.Vacuum.Gauge;

public class PfeifferGauge extends Gauge {

    private int[] status = {4,4,4,4,4,4,4};
    private String[] statuses = {
            "Sensor is on. Measurement data okey",
            "Underrange",
            "Overrange",
            "Sensor error",
            "Sensor off",
            "No sensor",
            "Identification error" };

    public PfeifferGauge(String fileName) {
        super(fileName);
    }

    @Override
    protected synchronized double  measure(int gauge) {
        String command = "PR"+gauge+"\n";
        writeString(command);
        readString("\r\n");
        writeString(""+'\5');
        String message = readString("\r\n");
        try {

            int somestatus = Integer.parseInt(message.charAt(0)+"");
            if (somestatus!=status[gauge])
            {
                sendMessage("Gauge "+gauge+":"+statuses[somestatus]);
                status[gauge] = somestatus;
            }
            double measurement = Double.parseDouble(message.substring(2,10))*0.75;
            pressure[gauge] = measurement;
            deviceIsOn = true;
            return measurement;
        }
        catch (Exception e)
        {
            deviceIsOn = false;
            return pressure[gauge];
        }
    }

    @Override
    protected void calibrate(String gaugeType) {}
}
