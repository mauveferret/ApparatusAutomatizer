package ru.mauveferret;

import java.io.File;
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
        logPressure1.createFile( newPath+"\\pr1.txt", "");
        logPressure2.createFile( newPath+"\\pr2.txt","");
        logPressure3.createFile( newPath+"\\pr3.txt","");
        super.initialize();
    }

    double[] pressure = new double[4];
    Logger logPressure1 = new Logger(false);
    Logger logPressure2 = new Logger(false);
    Logger logPressure3 = new Logger(false);

    //Getters

    synchronized double getPressure(int gaugeNumber) {
        return pressure[gaugeNumber];
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
