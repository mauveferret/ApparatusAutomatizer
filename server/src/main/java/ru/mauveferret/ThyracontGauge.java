package ru.mauveferret;

//driver for VD10 controller Unit

class ThyracontGauge extends Gauge {

    ThyracontGauge(String path) {
        super(path);
    }

    //gauge related commands

    synchronized double measure(int gaugeNumber)
    {
        //FIXME thread sleep is very bad!
        try
        {
            Thread.sleep(30);
        }
        catch (Exception ignored){}
        String message = "00"+gaugeNumber+"M";
        writeMessage(message+checkSum(message)+"\r");
        try {
            Thread.sleep(30);
        }
        catch (Exception ignored){}
        message = readMessage("\r");

        if (message.length()>10) {
            if ((message.charAt(10) + "").equals(checkSum(message.substring(0, 10))))
            {
                double mantissa = Double.parseDouble(message.substring(4, 8)) / 1000;
                int order = Integer.parseInt(message.substring(8, 10)) - 20;
                double value = mantissa * 0.75 * Math.pow(10, order);
                pressure[gaugeNumber] = value;
                return value;
            }
            else
                {
                if (!isReconnectActive())
                    sendMessage("Error during measuring pressure by " + gaugeNumber + " gauge. Wrong checksum.");
                //return previous
                return getPressure(gaugeNumber);
                }
        }
        else
        {
            if (!isReconnectActive())
                sendMessage("Error during measuring pressure by " + gaugeNumber + " gauge.Message too short");
            //return previous
            return getPressure(gaugeNumber);
        }
    }

    synchronized void calibrate(String sensorType){}

    private String checkSum(String message)
    {
        int checkSum=0;
        for (char c: message.toCharArray())
            checkSum+=c;
        return ((char) (checkSum % 64 +64))+"";
    }

}

