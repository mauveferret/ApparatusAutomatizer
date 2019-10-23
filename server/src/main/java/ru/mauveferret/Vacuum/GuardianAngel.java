package ru.mauveferret.Vacuum;

import ru.mauveferret.RecordingUnit;
import java.util.HashMap;
import java.util.TreeMap;

class GuardianAngel extends RecordingUnit {

    GuardianAngel(String fileName) {
        super(fileName);
        unitAccessLevel = 2;
        enabled = 0;
    }

    @Override
    protected void initialize() {
        super.initialize();
        gauge =(Gauge) terminalSample.getDevice(gaugeName);
        gateControl = (GateControl) terminalSample.getDevice(gateName);
        tmp = (TMP) terminalSample.getDevice(tmpName);
        startCheckingPressure();
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

    private boolean continueChecking;
    private Gauge gauge;
    private GateControl gateControl;
    private TMP tmp;
    private int enabled = 0;

    private  String gaugeName;
    private String gateName;
    private String tmpName;


    private String firstColumnGaugeName;
    private String vesselGaugeName;

    public int getEnabled() {
        return enabled;
    }
    //Getters and Setters

    private void setContinueChecking(boolean continueChecking) {
        enabled = (continueChecking) ? 2 : 1;
        this.continueChecking = continueChecking;
    }


    //command related methods


    private void startCheckingPressure() {
        continueChecking = true;
        Thread checkerThread = new Thread(() -> {
            boolean stop = false;
            while (!stop)
            {
                while (continueChecking) {

                    double pressureColumn = gauge.pressure.get(firstColumnGaugeName);
                    double pressureVessel = gauge.pressure.get(vesselGaugeName);
                    int gate = gateControl.getGateStatus();
                    int valve = gateControl.getValveStatus();
                    int bypass = gateControl.getBypassStatus();
                    int pump = gateControl.getPumpStatus();
                    boolean turboPump = tmp.isEnabled();
                    boolean isOK = true;
                    int temperature = tmp.getTemperature();

                    //if pressure difference is too much or pressureVessel to much
                    boolean highPressure = Math.abs(pressureColumn - pressureVessel) > 10;

                    //To prevent high pressure ?!
                    if ((highPressure && (gate==2 || valve ==2)))  //|| (gate>2 || valve >2)
                    {
                        sendMessage("High pressure. Closing valve and gate of the column "+gateControl.config.unitNumber+".");
                        enabled = 7;
                        isOK = false;
                        gateControl.runTerminalCommand("bla gate close", 10);
                        gateControl.runTerminalCommand("bla valve close", 10);
                    }

                    //to prevent TMP overheating
                    if ( temperature>42 && pressureColumn>10 && turboPump)
                    {
                        sendMessage("i'm closing the gates and stopping the TMP!");
                        enabled = 7;
                        isOK = false;
                        tmp.runTerminalCommand( "bla stop",10);
                        gateControl.runTerminalCommand("bla gate close", 10);
                        gateControl.runTerminalCommand("bla valve close", 10);
                    }

                    //it is not right them to be opened at the same time
                    if (bypass>1 && valve>1)
                    {
                        sendMessage("i'm closing the bypass!");
                        enabled = 7;
                        isOK = false;
                        gateControl.runTerminalCommand("bla bypass close", 10);
                    }

                    if (pump<2 && turboPump)
                    {
                        sendMessage("i'm  stopping the TMP!");
                        enabled = 7;
                        isOK = false;
                        tmp.runTerminalCommand( "bla stop",10);
                    }

                    if (isOK) enabled = 2;

                    try {
                        Thread.sleep(50);
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
        commands.put("stop", "");
        return commands;
    }
    @Override
    protected void chooseTerminalCommand(String[] command) {

        super.chooseTerminalCommand(command);
        switch (command[1]) {
            case ("start"): setContinueChecking(true);
            break;
            case ("stop"): setContinueChecking(false);
            break;
        }
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        try {
            switch (command[0]) {
                case "gauge": gaugeName = command[1];
                    break;
                case "gatecontrol": gateName = command[1];
                    break;
                case "tmp" :  tmpName = command[1];
                break;
                case "firstcolumngaugename" : firstColumnGaugeName = command[1];
                break;
                case "vesselGaugeName" : vesselGaugeName = command[1];
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
