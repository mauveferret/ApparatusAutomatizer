package ru.mauveferret;

import java.util.HashMap;

//an example of the  virtual device which would be made by some user to configure Automizer for his personal needs
public class GateControl extends Device{

    private int columnNumber;
    private String arduinoName;
    private String gaugeName;
    //in torr
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




    //Setters

    public void setMaxPresDifference(int maxPresDifference) {
        this.maxPresDifference = maxPresDifference;
    }

    public void setForlinePumpDigitalPin(int forlinePumpDigitalPin) {
        this.forlinePumpDigitalPin = forlinePumpDigitalPin;
    }

    public void setValveDigitalPin(int valveDigitalPin) {
        this.valveDigitalPin = valveDigitalPin;
    }

    public void setValveAnalogOpenedPin(int valveAnalogOpenedPin) {
        this.valveAnalogOpenedPin = valveAnalogOpenedPin;
    }

    public void setValveAnalogClosedPin(int valveAnalogClosedPin) {
        this.valveAnalogClosedPin = valveAnalogClosedPin;
    }

    public void setGateDigitalPin(int gateDigitalPin) {
        this.gateDigitalPin = gateDigitalPin;
    }

    public void setGateAnalogOpenedPin(int gateAnalogOpenedPin) {
        this.gateAnalogOpenedPin = gateAnalogOpenedPin;
    }

    public void setGateAnalogClosedPin(int gateAnalogClosedPin) {
        this.gateAnalogClosedPin = gateAnalogClosedPin;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public void setArduinoName(String arduinoName) {
        this.arduinoName = arduinoName;
    }

    public void setGaugeName(String gaugeName) {
        this.gaugeName = gaugeName;
    }

    //GateControl Methods

    private void forlinePump(String enable)
    {
        boolean isCorrectControl = false;

        boolean enablePump=false;
        if (enable.equals("on"))
        {
            isCorrectControl = true;
            enablePump = true;
        }
        else
        if (enable.equals("off"))
        {
            isCorrectControl = true;
            enablePump = false;
        }
        else
        {
            isCorrectControl = false;
            sendMessage("No $open$ options or it's incorrect (should use \"on\" or \"off\")");
        }

        String digitalOutput = (enablePump) ? " 1" : " 0";
        arduino.runCommand(terminal, "deviceCommand dwrite "+ forlinePumpDigitalPin+digitalOutput);
    }

    private void valve( String control)
    {
        boolean isCorrectControl = false;

        boolean openValve=false;
        if (control.equals("open"))
        {
            isCorrectControl = true;
            openValve = true;
        }
        else
        if (control.equals("close"))
        {
            isCorrectControl = true;
            openValve = false;
        }
        else
        {
            isCorrectControl = false;
            sendMessage("No $open$ options or it's incorrect (should use \"open\" or \"close\")");
        }



        if ((openValve ^ arduino.getDigitalPins()[valveDigitalPin]) && isCorrectControl)
        {
            boolean isforlinePumpOn = arduino.getDigitalPins()[forlinePumpDigitalPin];
            double columnPressure = (columnNumber == 1) ? gauge.getPressureColumn1() : gauge.getPressureColumn2();
            //FIXME ?!
            if ((isforlinePumpOn|| (!isforlinePumpOn && columnPressure > 700)) && openValve)
            {
                arduino.runCommand(terminal,"deviceCommand dwrite "+valveDigitalPin+" 1");
            }
            else
            {
                sendMessage("pressure difference is too high.");
            }
            if (!openValve && isCorrectControl)
            {
                arduino.runCommand(terminal,"deviceCommand dwrite "+valveDigitalPin+" 0");
            }
        }
    }

    private void gate(String open)
    {
        boolean isCorrectControl = false;

        boolean openGate=false;
        if (open.equals("open"))
        {
            isCorrectControl = true;
            openGate = true;
        }
        else
        if (open.equals("close"))
        {
            isCorrectControl = true;
            openGate = false;
        }
        else
        {
            isCorrectControl = false;
            sendMessage("No $open$ options or it's incorrect (should use \"open\" or \"close\")");
        }

        if ((openGate ^ arduino.getDigitalPins()[valveDigitalPin]) && isCorrectControl)
        {
            double columnPressure = (columnNumber == 1) ? gauge.getPressureColumn1() : gauge.getPressureColumn2();
            double vesselPressure = gauge.getPressureVessel();
            double pressureDifference = Math.abs(columnPressure-vesselPressure);
            if (pressureDifference < maxPresDifference && openGate && vesselPressure<maxPresDifference)
            {
                arduino.runCommand(terminal,"deviceCommand dwrite "+gateDigitalPin+" 1");
            }
            else
            {
                if (pressureDifference>maxPresDifference)
                {
                    sendMessage("pressure difference is too high.");
                }
                if (vesselPressure > maxPresDifference)
                {
                    sendMessage("pressure in vessel is too high.");
                }
            }
            if (!openGate)
            {
                arduino.runCommand(terminal,"deviceCommand dwrite "+gateDigitalPin+" 0");
            }
        }
    }

    //terminal related methods

    @Override
    HashMap<String, String> getCommands() {
        return super.getCommands();
    }

    @Override
    void info() {

    }

    @Override
    void runCommand(Device device, String someCommand) {
        terminal = (Terminal) device;
        arduino = (Arduino) terminal.getDevice(arduinoName);
        gauge = (ThyracontGauge) terminal.getDevice(gaugeName);

        someCommand = someCommand.toLowerCase();
        super.runCommand(device, someCommand);
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            command[1] = replaceAliasByCommand(command[1]);
            switch (command[1]) {
                case "valve": valve( command[2]);
                break;
                case "gate": gate(command[2]);
                break;
                case "forline": forlinePump(command[2]);
                break;
                case "forlinepin": setForlinePumpDigitalPin(Integer.parseInt(command[2]));
                break;
                case "valvepin": setValveDigitalPin(Integer.parseInt(command[2]));
                break;
                case "gatepin": setGateDigitalPin(Integer.parseInt(command[2]));
                break;
                case "gauge" : setGaugeName(command[2]);
                break;
                case "arduino" : setArduinoName(command[2]);
                break;
                case "columnnumber" : setColumnNumber(Integer.parseInt(command[2]));
                break;
            }
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }
    }

}
