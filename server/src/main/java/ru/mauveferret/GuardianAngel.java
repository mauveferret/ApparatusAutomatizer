package ru.mauveferret;

import java.util.TreeMap;

public class GuardianAngel extends Device {

    /*
    used to check if the temperature and pressure conditions are comfortable
    for devices.
    In case of emergency it tries to save the devices: closes gates and valves
    works as thread
     */

    private boolean continueChecking = true;
    private ThyracontGauge gauge;
    private GateControl gateControl;
    private LeyboldTMP tmp;
    private  GuardianAngel angel = this;

    //Getters and Setters

    private void setContinueChecking(boolean continueChecking) {
        this.continueChecking = continueChecking;
    }


    //command related methods

    public GuardianAngel(String path) {
        super(path);
        measureAndLog();
    }

    private void startCheckingPressure() {
        Thread checkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stop = false;
                while (!stop)
                {
                    while (continueChecking) {
                        double pressureColumn = gauge.getPressure(gateControl.getColumnNumber());
                        double pressureVessel = gauge.getPressure(3);
                        boolean gate = gateControl.isGateOpened();
                        //FIXME checking valve
                        boolean valve = gateControl.isValveOpened();
                        boolean pump = gateControl.isPumpEnabled();
                        boolean turboPump = tmp.isEnabled();
                        int temperature = tmp.getTemperature();

                        //if pressure difference is too much or pressureVessel to much -> close gate
                        if ((Math.abs(pressureColumn - pressureVessel) > 10 || pressureColumn>10) && gate)
                        {
                            sendMessage("i'm closing the gates!");
                            gateControl.runCommand(angel,"bla gate close");
                        }
                        if ((pressureColumn>10 || (temperature>42)) && turboPump)
                        {
                            sendMessage("i'm closing the gates and stop the TMP!");
                            tmp.runCommand(angel, "bla stop");
                            gateControl.runCommand(angel,"bla gate close");
                            //FIXME find better indication of  valve closing need
                            gateControl.runCommand(angel,"bla valve close");
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
        checkerThread.setName(config.deviceName);
        checkerThread.start();
    }

    //for commandline

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("start", "");
        commands.put("pause", "");
        commands.put("resume","");

        return commands;
    }

    @Override
    void chooseTerminalCommand(String[] command) {

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
    void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        try {
            switch (command[0]) {
                case "gauge": gauge =(ThyracontGauge) terminalSample.getDevice(command[1]);
                    break;
                case "gatecontrol": gateControl = (GateControl) terminalSample.getDevice(command[1]);
                    break;
                case "tmp" : tmp = (LeyboldTMP)  terminalSample.getDevice(command[1]);
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
    void measureAndLog() {}
}
