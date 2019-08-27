package ru.mauveferret;

import java.util.TreeMap;

class ThyracontGauge extends SerialDevice {


    ThyracontGauge(String path) {
        super(path);
    }

    private double[] pressure = new double[4];


    //Getters

    double getPressure(int gaugeNumber) {
        return pressure[gaugeNumber];
    }

    //TODO calibrate

    //gauge related commands

    private synchronized double measure(int gaugeNumber)
    {
        //FIXME CHECK CHECKSuM
        String message = "00"+gaugeNumber+"M";
        writeMessage(message+checkSum(message)+"\r");
        message = readMessage();
        if (message.substring(message.length()-1).equals(checkSum(message))) {
            double mantissa  = Double.parseDouble(message.substring(4, 8)) / 1000;
            int order = Integer.parseInt(message.substring(8, 10)) - 20;
            double value = mantissa * 0.75 * Math.pow(10, order);
            pressure[gaugeNumber] = value;
            return value;
        }
        else
        {
            sendMessage("Error during measuring pressure by "+gaugeNumber+" gauge");
            reconnect();
            //return previous
            return getPressure(gaugeNumber);
        }
    }

    private synchronized double calibrate(String sensorType)
    {
        return  1;
    }

    private String checkSum(String message)
    {
        int checkSum=0;
        for (char c: message.toCharArray())
            checkSum+=c;
        return ((char) (checkSum % 64 +64))+"";
    }


    //for commandline

    @Override
    void measureAndLog() {
        Thread log = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stop = true;
                while (stop)
                {
                    measure(1);
                    measure(2);
                    //measure(3);
                    logData("time "+getPressure(1)+" "+getPressure(2)+"\n");
                    stop = Thread.currentThread().isInterrupted();
                }
            }
        });
        log.setName("GaugeLogger");
        log.start();
    }

    @Override
    void chooseCommand(String[] command) {
        switch (command[1])
        {
            case "measure":
            {
                try {
                    measure(Integer.parseInt(command[2]));
                }
                catch (Exception e)
                {
                    sendMessage("enter gauge number (1-3) as an option");
                }
            }
            break;
            case "calibrate":
            {

            }
            break;
        }
        super.chooseCommand(command);
    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge number$ ");
        commands.put("calibrate", "makes a calibration of the pirani or cold cathode in form: calibrate $type$");
        return super.getCommands();
    }
}

