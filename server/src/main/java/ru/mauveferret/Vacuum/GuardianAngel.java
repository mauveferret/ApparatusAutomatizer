package ru.mauveferret.Vacuum;



import ru.mauveferret.RecordingDevice;

import java.util.HashMap;
import java.util.TreeMap;

class GuardianAngel extends RecordingDevice {

    //FIXME WTF?!
    GuardianAngel(String fileName) {
        super(fileName);
        unitAccessLevel = 2;
    }

    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String, String> initializeData) {

    }

    /*
    used to check if the temperature and pressure conditions are comfortable
    for devices.
    In case of emergency it tries to save the devices: closes gates and valves
    works as thread
     */

    private boolean continueChecking = true;
    private Gauge gauge;
    private GateControl gateControl;
    private TMP tmp;
    private  GuardianAngel angel = this;

    //Getters and Setters

    private void setContinueChecking(boolean continueChecking) {
        this.continueChecking = continueChecking;
    }


    //command related methods


    private void startCheckingPressure() {
        Thread checkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stop = false;
                while (!stop)
                {
                    while (continueChecking) {
                        double pressureColumn = gauge.pressure.get("column");
                        double pressureVessel = gauge.pressure.get("vessel");
                        //boolean gate = gateControl.isGateOpened();
                        //FIXME checking valve
                        //boolean valve = gateControl.isValveOpened();
                        //boolean pump = gateControl.pumpStatus();
                        boolean turboPump = tmp.isEnabled();
                        int temperature = tmp.getTemperature();

                        //if pressure difference is too much or pressureVessel to much -> close gate
                       // if ((Math.abs(pressureColumn - pressureVessel) > 10 || pressureColumn>10) && gate)
                        {
                            sendMessage("i'm closing the gates!");
                            gateControl.runTerminalCommand("bla gate close", 10);
                        }
                        if ((pressureColumn>10 || (temperature>42)) && turboPump)
                        {
                            sendMessage("i'm closing the gates and stop the TMP!");
                            tmp.runTerminalCommand( "bla stop",10);
                            gateControl.runTerminalCommand("bla gate close", 10);
                            //FIXME find better indication of  valve closing need
                            gateControl.runTerminalCommand("bla valve close", 10);
                        }

                    }

                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception ignored){}

                    stop = Thread.currentThread().isInterrupted();
                }

            }
        });
        checkerThread.setName(config.name);
        checkerThread.start();
    }

    //for commandline

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("start", "");
        commands.put("pause", "");
        commands.put("resume","");

        return commands;
    }

    @Override
    protected void chooseTerminalCommand(String[] command) {

        super.chooseTerminalCommand(command);
        switch (command[1]) {
            case ("start"): startCheckingPressure();
            break;
            case ("pause"): setContinueChecking(false);
            break;
            case ("resume") : setContinueChecking(true);
            break;
        }
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        try {
            switch (command[0]) {
                case "gauge": gauge =(Gauge) terminalSample.getDevice(command[1]);
                    break;
                case "gatecontrol": gateControl = (GateControl) terminalSample.getDevice(command[1]);
                    break;
                case "tmp" : tmp = (TMP)  terminalSample.getDevice(command[1]);
                break;
            }
        }
        catch (Exception e)
        {
            sendMessage("incorrect option.");
            sendMessage(e.getMessage());
        }
    }

    @Override
    protected void measureAndLog() {}
}
