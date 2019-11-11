package ru.mauveferret.Vacuum.Gauges;

//driver for VD10 controller Unit

import ru.mauveferret.Vacuum.Gauge;

public class ThyracontGauge extends Gauge {

    public ThyracontGauge(String path) {
        super(path);
    }

    //gauge related commands

    protected synchronized double measure(int gaugeNumber)
    {
        //FIXME very bad!!
        try {
            Thread.sleep(50);
        }
        catch (Exception ignored){}
        String message = "00"+gaugeNumber+"M";
        message = sendCommandToDevice(message+checkSum(message)+"\r",1,"\r");

        if (message.length()>10) {
            if ((message.charAt(10) + "").equals(checkSum(message.substring(0, 10))))
            {
                double mantissa = Double.parseDouble(message.substring(4, 8)) / 1000;
                int order = Integer.parseInt(message.substring(8, 10)) - 20;
                double value = mantissa * 0.75 * Math.pow(10, order);
                pressure.put(config.devices.get(gaugeNumber-1),value);
                deviceIsOn = true;
                return value;
            }
            else
                {
                if (!isReconnectActive())
                    sendMessage("Error during measuring pressure by " + gaugeNumber + " gauge. Wrong checksum.");
                    deviceIsOn=false;
                //return previous
                return pressure.get(config.devices.get(gaugeNumber-1));
                }
        }
        else
        {
            if (!isReconnectActive())
                sendMessage("Error during measuring pressure by " + gaugeNumber + " gauge.Message too short");
            deviceIsOn=false;
            //return previous
            return pressure.get(config.devices.get(gaugeNumber-1));
        }
    }

    protected synchronized void calibrate(String sensorType){}

    private String checkSum(String message)
    {
        int checkSum=0;
        for (char c: message.toCharArray())
            checkSum+=c;
        return ((char) (checkSum % 64 +64))+"";
    }

}

