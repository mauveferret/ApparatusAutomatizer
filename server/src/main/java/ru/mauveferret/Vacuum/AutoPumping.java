package ru.mauveferret.Vacuum;

import ru.mauveferret.RecordingUnit;

import java.util.HashMap;

public class AutoPumping extends RecordingUnit {

    public AutoPumping(String fileName) {
        super(fileName);
    }

    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String, String> initializeData) {

    }


    private Gauge gauge;
    private GateControl gateControl;
    private TMP tmp;
    boolean enableAutoPumping;

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


}
