package ru.mauveferret.Vacuum;

import ru.mauveferret.RecordingUnit;

import java.util.HashMap;

public class AutoPumping extends RecordingUnit {

    public AutoPumping(String fileName) {

        super(fileName);
        enabled=0;
    }


    @Override
    protected void initialize() {
        super.initialize();
        columnGauge = (config.unitNumber == 1) ? LoadedUnits.gauge.get("column1") : LoadedUnits.gauge.get("column2");
        vesselGauge = LoadedUnits.gauge.get("vessel");
        gateControl = (config.unitNumber == 1) ? LoadedUnits.column1.gateControl : LoadedUnits.column2.gateControl;
        tmp = (config.unitNumber == 1) ? LoadedUnits.column1.tmp : LoadedUnits.column2.tmp;
    }

    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String, String> initializeData) {

    }



    private Gauge columnGauge;
    private Gauge vesselGauge;
    private GateControl gateControl;
    private TMP tmp;
    private int enabled;

    private String firstColumnGaugeName;
    private String vesselGaugeName;

    public int getEnabled() {
        return enabled;
    }

    @Override
    protected void measureAndLog() {

    }

    private void pump()
    {
        Thread autoPump = new Thread(new Runnable() {
            @Override
            public void run() {
                if (gateControl.getPumpStatus()!=2)
                {
                    gateControl.runTerminalCommand("bla pump on", 10);
                }

            }
        });
        autoPump.setName("autopump"+gateControl.config.unitNumber);
        autoPump.start();
    }

    private void dePump()
    {

    }


    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        try {
            switch (command[0]) {
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


}
