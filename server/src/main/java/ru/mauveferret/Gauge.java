package ru.mauveferret;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

abstract class Gauge extends SerialDevice {

    Gauge(String fileName) {
        super(fileName);
        deviceAccessLevel = 1;
    }

    abstract double measure(int gauge);
    abstract void calibrate(String gaugeType);

    @Override
    void initialize() {
        String newPath =(new File(config.dataPath)).getParent();
        for (int number: config.devices)
        {
            //FIXME files are not updated, values ar added to the end.
            loggerMap.put(number, new Logger(false));
            loggerMap.get(number).createFile(newPath+File.separator+config.deviceName+number+".txt","");
        }
        super.initialize();
    }

    //keeps pressures current value
    double[] pressure = new double[4];
    //used to write single pressure from every gauge/ Can be used by third party software
    private HashMap<Integer, Logger> loggerMap = new HashMap<>();

    @Override
    void measureAndLog() {
        dataLog.createFile(config.dataPath, "time  column1,torr column2,torr vessel torr  ");
        log = new Thread(() -> {
            boolean stop = false;
            while (!stop)
            {
                if (!isReconnectActive())
                {
                    try {
                        String logPressures = "time ";
                        for (int deviceNumber : config.devices)
                        {
                            measure(deviceNumber);
                            String pr = String.format("%6.3e",pressure[deviceNumber]);
                            loggerMap.get(deviceNumber).write("time " + pr);
                            logPressures+=pr+" ";
                        }
                        dataLog.write(logPressures);
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
                    Thread.sleep(100);
                }
                catch (Exception ignored){}
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
