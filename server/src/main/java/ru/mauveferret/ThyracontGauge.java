package ru.mauveferret;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TreeMap;

class ThyracontGauge extends SerialDevice {

    ThyracontGauge(String path) {
        super(path);
        String newPath =(new File(config.dataPath)).getParent();
        System.out.println(newPath);
        logPressure1.createFile( newPath+"\\pr1.txt", "");
        logPressure2.createFile( newPath+"\\pr2.txt","");
        logPressure3.createFile( newPath+"\\pr3.txt","");
        measureAndLog();
    }

    private double[] pressure = new double[4];
    private Logger logPressure1 = new Logger(false);
    private Logger logPressure2 = new Logger(false);
    private Logger logPressure3 = new Logger(false);

    //Getters

    synchronized double getPressure(int gaugeNumber) {
        return pressure[gaugeNumber];
    }

    //gauge related commands

    private synchronized double measure(int gaugeNumber)
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
        message = readMessage();

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

    @Override
    void measureAndLog() {
        dataLog.createFile(config.dataPath, "time  column1,torr column2,torr vessel torr  ");
        log = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stop = false;
                while (!stop)
                {
                    if (!isReconnectActive())
                    {
                        try {
                            measure(1);
                            //long t1 = System.currentTimeMillis();
                            //System.out.println(System.currentTimeMillis()-t1);
                            measure(2);
                            //measure(3);
                            String pr1 = String.format("%6.3e",getPressure(1));
                            String pr2 = String.format("%6.3e",getPressure(2));
                            String pr3 = String.format("%6.3e",getPressure(3));
                            dataLog.write("time " + pr1 + " " + pr2+" "+pr3);
                            logPressure1.write("time " + pr1);
                            logPressure2.write("time " + pr2);
                            logPressure3.write("time " + pr3);
                            stop = Thread.currentThread().isInterrupted();
                        }
                        catch (NullPointerException  e)
                        {
                            sendMessage("ERROR while log: port wasn't created\n ");
                            reconnect();
                            break;
                        }
                    }

                    //FIXME very bad!!
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception ignored){}

                }

            }
        });
        log.setName(config.deviceName);
        log.start();
    }

    //for commandline

    @Override
    void chooseTerminalCommand(String[] command) {
        switch (command[1])
        {
            case "measure":
            {
                //TODO check regex
                if (command[2].equals("\\[123]"))
                    measure(Integer.parseInt(command[2]));
                else
                    sendMessage("enter gauge number (1-3) as an option");
            }
            break;
            case "calibrate":
            {
                calibrate(command[2]);
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

    @Override
    boolean callDevice() {
        try {
            measure(1);
            return  true;
        }
        catch (Exception e)
        {
            return  false;
        }
    }


}

