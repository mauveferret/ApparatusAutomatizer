package ru.mauveferret;

import java.util.HashMap;

//an example of the  virtual device which would be made by some user to configure Automizer for his personal needs
public class GateControl extends Device{

    private int columnNumber;
    private String arduinoName;
    private String gaugeName;
    private int maxPresDifference;

    //for Arduino
    private int forlinePumpDigitalPin;
    private int valveDigitalPin;
    private int valveAnalogOpenedPin;
    private int valveAnalogClosedPin;
    private int gateDigitalPin;
    private int gateAnalogOpenedPin;
    private int gateAnalogClosedPin;

    private Arduino arduino;
    private ThyracontGauge gauge;
    private Terminal terminal;

    public GateControl(String path) {
        super(path);
    }


    //GateControl Methods
    private boolean forlinePump(boolean open)
    {
        return  true;
    }
    private boolean Valve( boolean open)
    {
        if (open ^ arduino.getDigitalPins()[valveDigitalPin])
        {
            boolean isforlinePumpOn = arduino.getDigitalPins()[forlinePumpDigitalPin];
            double columnPressure = (columnNumber == 1) ? gauge.getPressureColumn1() : gauge.getPressureColumn2();
            //FIXME ?!
            if ((isforlinePumpOn|| (!isforlinePumpOn && columnPressure > 700)) && open)
            {
                arduino.runCommand(terminal,"deviceCommand dwrite "+valveDigitalPin+" 1");
            }
            if (!open)
            {
                arduino.runCommand(terminal,"deviceCommand dwrite "+valveDigitalPin+" 0");
            }
        }

        return true;
    }

    private boolean Gate(boolean open)
    {

        return true;
    }

    @Override
    void runCommand(Device device, String someCommand) {
        terminal = (Terminal) device;
        arduino = (Arduino) terminal.getDevice(arduinoName);
        gauge = (ThyracontGauge) terminal.getDevice(gaugeName);

        super.runCommand(device, someCommand);
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            command[1] = replaceAliasByCommand(command[1]);
            switch (command[1]) {
                case "valve" :

            }
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }
    }

    // FIXME а нафига оно нужно, когда есть ардуино? ПРопиши в конфиге и будет тебе счастье!


}
