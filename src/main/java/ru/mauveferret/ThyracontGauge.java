package ru.mauveferret;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TreeMap;

class ThyracontGauge extends SerialDevice {

    ThyracontGauge(String path) {
        super(path);
        String newPath = config.dataPath.substring(0,config.dataPath.lastIndexOf("\\"))+"pr1.txt";
        logPressure1.createFile( newPath, "");
        newPath = config.dataPath.substring(0,config.dataPath.lastIndexOf("\\"))+"pr2.txt";
        logPressure2.createFile( newPath,"");
        newPath = config.dataPath.substring(0,config.dataPath.lastIndexOf("\\"))+"pr3.txt";
        logPressure3.createFile( newPath,"");
        sendMessage(logPressure1.getPath());
        measureAndLog();
    }

    private double[] pressure = new double[4];
    private Logger logPressure1 = new Logger();
    private Logger logPressure2 = new Logger();
    private Logger logPressure3 = new Logger();
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
        //if ((message.charAt(10)+"").equals(checkSum(message.substring(0,10))))
        if (true)
        {
            double mantissa  = Double.parseDouble(message.substring(4, 8)) / 1000;
            int order = Integer.parseInt(message.substring(8, 10)) - 20;
            double value = mantissa * 0.75 * Math.pow(10, order);
            pressure[gaugeNumber] = value;
            //System.out.println(value);
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
        log = new Thread(new Runnable() {
            @Override
            public void run() {
                Locale locale;
                NumberFormat numFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                boolean stop = false;
                while (!stop)
                {
                    try {
                        long t1 = System.currentTimeMillis();
                        measure(1);
                        measure(2);
                        //measure(3);
                        long t2 = System.currentTimeMillis();
                        String pr1 = numFormat.format(getPressure(1));
                        String pr2 = numFormat.format(getPressure(2));
                        String pr3 = numFormat.format(getPressure(3));
                        dataLog.write("time " + pr1 + " " + pr2+" "+pr3);
                        logPressure1.write("time " + pr1);
                        logPressure2.write("time " + pr2);
                        logPressure3.write("time " + pr3);
                        stop = Thread.currentThread().isInterrupted();
                        Thread.currentThread().wait(30);
                        stop = true;
                    }
                    catch (Exception  e)
                    {
                        e.printStackTrace();
                        stop = true;
                    }
                }

            }
        });
        log.setName(config.deviceName);
        log.start();
    }

    @Override
    void chooseTerminalCommand(String[] command) {
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
        super.chooseTerminalCommand(command);
    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge number$ ");
        commands.put("calibrate", "makes a calibration of the pirani or cold cathode in form: calibrate $type$");
        return super.getCommands();
    }

    @Override
    void type() {}
}

