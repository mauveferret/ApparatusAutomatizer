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
        columnGauge = (config.unitNumber == 1) ? LoadedUnits.gauge.get("column1") : LoadedUnits.gauge.get("column2");
        vesselGauge = LoadedUnits.gauge.get("vessel");
        gateControl = (config.unitNumber == 1) ? LoadedUnits.column1.gateControl : LoadedUnits.column2.gateControl;
        tmp = (config.unitNumber == 1) ? LoadedUnits.column1.tmp : LoadedUnits.column2.tmp;
        //FIXME we have to wate while Gauge will load all its parameters
        try {
            Thread.sleep(3000);
        }
        catch (Exception ignored){}
        enabled = 0;
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
    private Gauge columnGauge;
    private Gauge vesselGauge;
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
                try {

                    boolean order1=false;
                    boolean order2=false;
                    boolean order3=false;
                    boolean order4=false;


                    while (continueChecking) {

                        double pressureColumn = columnGauge.pressure.get(firstColumnGaugeName);
                        double pressureVessel = vesselGauge.pressure.get(vesselGaugeName);
                        int gate = gateControl.getGateStatus();
                        int valve = gateControl.getValveStatus();
                        int bypass = gateControl.getBypassStatus();
                        int pump = gateControl.getPumpStatus();
                        boolean turboPump = tmp.isEnabled();
                        boolean isOK = true;
                        int temperature = tmp.getTemperature();

                        /*
                        fixme if there is some problem woth gerkons, it runs commands always
                        We will make  a booolean parameter which represents, that some agels't instruction
                        was launched in order not to send it several times
                         */


                        //if pressure difference is too much or pressureVessel to much
                        boolean highPressure = Math.abs(pressureColumn - pressureVessel) > 10;


                        //ORDER1: To prevent high pressure ?!
                        if ((highPressure && (gate==2 || valve ==2)) && !order1 )  //|| (gate>2 || valve >2)
                        {
                            sendMessage("ORDER1: high pressure. Closing valve and gate of the column "+gateControl.config.unitNumber+".");
                            enabled = 7;
                            isOK = false;
                            order1 = true;
                            gateControl.runTerminalCommand("bla gate close", 10);
                            gateControl.runTerminalCommand("bla valve close", 10);

                        }
                        else {
                            if (!(highPressure && (gate==2 || valve ==2)) && order1) {
                                order1 = false;
                                sendMessage("ORDER1 was completed");
                            }
                        }

                        //ORDER2: to prevent TMP overheating
                        if (( temperature>42 && pressureColumn>10 && turboPump) && !order2)
                        {
                            sendMessage("ORDER2: TMP in danger. I'm closing the gates and stopping the TMP!");
                            enabled = 7;
                            isOK = false;
                            order2 = true;
                            tmp.runTerminalCommand( "bla stop",10);
                            gateControl.runTerminalCommand("bla gate close", 10);
                            gateControl.runTerminalCommand("bla valve close", 10);
                        }
                        else {
                            if (!( temperature>42 && pressureColumn>10 && turboPump) && order2) {
                                order2 = false;
                                sendMessage("ORDER2 was completed");
                            }
                        }

                        //ORDER3: it is not right them to be opened at the same time
                        if ((bypass>1 && valve>1) && !order3)
                        {
                            sendMessage("ORDER3: bypass and valve opened. I'm closing the bypass!");
                            enabled = 7;
                            isOK = false;
                            order3 = true;
                            gateControl.runTerminalCommand("bla bypass close", 10);
                        }
                        else {
                            if (!(bypass>1 && valve>1) && order3) {
                                order3 = false;
                                sendMessage("ORDER3 was completed");
                            }
                        }

                        //ORDER4:
                        if ((pump<2 && turboPump) && !order4)
                        {
                            sendMessage("ORDER4: tmp running wothout pump. I'm  stopping the TMP!");
                            enabled = 7;
                            isOK = false;
                            order4 = true;
                            tmp.runTerminalCommand( "bla stop",10);
                        }
                        else {
                            if (!(pump<2 && turboPump) && order4) {
                                order4 = false;
                                sendMessage("ORDER4 was completed");
                            }
                        }

                        if (isOK) enabled = 2;

                        //FIXME very bad
                        Thread.sleep(50);
                    }
                }
                catch (Exception ignored){ignored.printStackTrace(); continueChecking = false; }

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
                case "firstcolumngaugename" : firstColumnGaugeName = command[1];
                break;
                case "vesselgaugename" : vesselGaugeName = command[1];
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
