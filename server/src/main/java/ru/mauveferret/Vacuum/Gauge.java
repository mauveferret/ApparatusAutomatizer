package ru.mauveferret.Vacuum;

import ru.mauveferret.Logger;
import ru.mauveferret.SerialDevice;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

public abstract class Gauge extends SerialDevice {

    protected Gauge(String fileName) {
        super(fileName);
        unitAccessLevel = 2;
    }

    protected abstract double measure(int gauge);
    protected abstract void calibrate(String gaugeType);


    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String,String> initializeData)
    {

        for (String someDevice: config.devices)
        {
            try {
                String pres = initializeData.get(someDevice).split(" ")[1].replaceAll(",",".");
                pressure.put(someDevice, Double.parseDouble(pres));
            }
            catch (Exception e)
            {
                sendMessage("Pressure input failed: "+e.getMessage());
            }
        }
    }


    //key - device name (column1, vessel), value - double pressure
    protected HashMap<String, Double> pressure = new HashMap<>();

    //made to realize call method
    protected boolean deviceIsOn=false;

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
                        for (String someDevice : config.devices)
                        {
                            measure(config.devices.indexOf(someDevice)+1);
                            String pr = String.format("%6.3e",pressure.get(someDevice));
                            loggerMap.get(someDevice).write("time " + pr);
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
                if ((command.length>2) && (config.devices.contains(command[2])))
                {
                    //FIXME +1
                    measure(config.devices.indexOf(command[2])+1);
                    sendMessage("pressure in gauge "+command[2]+" is "+pressure.get(command[2]));
                }
                else
                    sendMessage("enter correct gauge name as an option");
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
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge name$ ");
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
