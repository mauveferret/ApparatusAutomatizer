package ru.mauveferret.Vacuum;

import ru.mauveferret.Logger;
import ru.mauveferret.SerialDevice;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

public abstract class Gauge extends SerialDevice {

    protected Gauge(String fileName) {
        super(fileName);
        deviceAccessLevel = 2;
    }

    protected abstract double measure(int gauge);
    protected abstract void calibrate(String gaugeType);

    @Override
    protected void initialize() {
        String newPath =(new File(config.dataPath)).getParent();
        for (int number: config.elements)
        {
            loggerMap.put(number, new Logger(false));
            String pressurePath = newPath+File.separator+"pressure"+File.separator+config.name +number+".txt";
            loggerMap.get(number).createFile(pressurePath,"");
        }
        super.initialize();
    }


    //made to realize call method
    protected boolean deviceIsOn=false;
    //keeps pressures current value. pressure[0] - is always null!
    protected double[] pressure = new double[]{-1,0.0001,0.1,700};
    //used to write single pressure from every gauge/ Can be used by third party software
    private HashMap<Integer, Logger> loggerMap = new HashMap<>();

    @Override
    protected void measureAndLog() {
        dataLog.createFile(config.dataPath, "time  column1,torr column2,torr vessel torr  ");
        log = new Thread(() -> {
            boolean stop = false;
            while (!stop)
            {
                if (!isReconnectActive())
                {
                    try {
                        String logPressures = "time ";
                        for (int deviceNumber : config.elements)
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
               else
                {
                    //FIXME very bad!!
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception ignored){}
                }
            }

        });
        log.setName(config.name);
        log.start();
    }

    //for commandline

    @Override
    protected void chooseTerminalCommand(String[] command) {
        switch (command[1])
        {
            case "measure":
            {
                //check if the user entered gauge number and if yes - checking if its valid
                if ((command.length>2) && (command[2].matches("[123]")))
                {
                    measure(Integer.parseInt(command[2]));
                    sendMessage("pressure in gauge "+command[2]+" is "+pressure[Integer.parseInt(command[2])]);
                }
                else
                    sendMessage("enter gauge number (1-3) as an option");
            }
            break;
            case "calibrate":
            {
                calibrate(command[2]);
            }
            break;
            case "turn":
            {
                //TODO
            }
        }
        super.chooseTerminalCommand(command);
    }

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge number$ ");
        commands.put("calibrate", "makes a calibration of the pirani or cold cathode in form: calibrate $type$");
        commands.put("turn","enable or disable the gauge in form: turn $on/off$ $gauge_number$ ");
        return super.getCommands();
    }

    @Override
    protected void type() {}

    @Override
    protected boolean callDevice() {
        try {
            measure(1);
            return  deviceIsOn;
        }
        catch (Exception e)
        {
            return  false;
        }
    }

}
