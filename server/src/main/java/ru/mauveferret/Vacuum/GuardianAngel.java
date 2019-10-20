package ru.mauveferret.Vacuum;



import ru.mauveferret.RecordingUnit;

import java.util.HashMap;
import java.util.TreeMap;

class GuardianAngel extends RecordingUnit {

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
    In case of emergency it tries to defend the devices: closes gates and valves
    works as thread
     */

    private boolean continueChecking = true;
    private Gauge gauge;
    private GateControl gateControl;
    private TMP tmp;

    //Getters and Setters

    private void setContinueChecking(boolean continueChecking) {
        this.continueChecking = continueChecking;
    }


    //command related methods


    private void startCheckingPressure() {
        Thread checkerThread = new Thread(() -> {
            boolean stop = false;
            while (!stop)
            {
                while (continueChecking) {
                    //fixme gauge names
                    double pressureColumn = gauge.pressure.get("column1");
                    double pressureVessel = gauge.pressure.get("vessel");
                    int gate = gateControl.getGateStatus();
                    int valve = gateControl.getValveStatus();
                    int bypass = gateControl.getBypassStatus();
                    int pump = gateControl.getPumpStatus();
                    boolean turboPump = tmp.isEnabled();
                    int temperature = tmp.getTemperature();

                    //if pressure difference is too much or pressureVessel to much
                    boolean highPressure = (Math.abs(pressureColumn - pressureVessel) > 10 || pressureColumn>10);

                    if ((highPressure && (gate==2 || valve ==2)) || (gate>2 || valve >2))
                    {
                        if (gate>2 || valve >2)
                            sendMessage("Valve error occured. Closing valve and gate of the column "+gateControl.config.unitNumber+".");
                            else
                                sendMessage("High pressure. Closing valve and gate of the column "+gateControl.config.unitNumber+".");
                        gateControl.runTerminalCommand("bla gate close", 10);
                        gateControl.runTerminalCommand("bla valve close", 10);
                    }


                    if ((pressureColumn>10 || (temperature>42)) && turboPump)
                    {
                        sendMessage("i'm closing the gates and stop the TMP!");
                        tmp.runTerminalCommand( "bla stop",10);
                        gateControl.runTerminalCommand("bla gate close", 10);
                        //FIXME find better indication of  valve closing need
                        gateControl.runTerminalCommand("bla valve close", 10);
                    }

                    try {
                        Thread.sleep(100);
                    }
                    catch (Exception ignored){}

                }

                try {
                    Thread.sleep(1000);
                }
                catch (Exception ignored){}

                stop = Thread.currentThread().isInterrupted();
            }

            //FIXME very bad
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored){}

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
                //FIXME might be initialize will work only from the ...?
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
