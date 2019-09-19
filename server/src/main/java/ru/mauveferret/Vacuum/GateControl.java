package ru.mauveferret.Vacuum;

import ru.mauveferret.Vacuum.ControlDevice;

import java.util.TreeMap;

/*
an example of the  virtual device which would be made by some user to configure Automizer for his personal needs
Generally, its is a simulation of the vacuum column number with forline pump, bypass (separate class),
pneumatic valve controlled by the arduino, some TMP with temperature sensor, pneumatic gate after the TMP
also controlled by the arduino. Gate control allows to control forlinePump, valve and gate and dosen't allow some
potentially crucial actions like "please open turn off forline pump while TMp is on"
or "please open valve when pressure before is much another tha pressure after"
 */
class GateControl extends ControlDevice {


    GateControl(String fileName) {
        super(fileName);
        deviceAccessLevel = 6;
        dataLog.createFile(config.dataPath, "forlineStatus  valveStatus   gateStatus");
    }

    //in torr
    private int maxPresDifference = 10;

    //for Arduino
    private int forlinePumpDigitalPin;
    private int valveDigitalPin;
    private int gateDigitalPin;

    private int valveAnalogOpenedPin;
    private int valveAnalogClosedPin;
    private int gateAnalogOpenedPin;
    private int gateAnalogClosedPin;

    private boolean pumpEnabled;
    private boolean valveOpened;
    private boolean gateOpened;

    private int pumpStatus = 5;
    private int valveStatus = 5;
    private int gateStatus = 5;

    public int getPumpStatus() {
        return pumpStatus;
    }

    public int getValveStatus() {
        return valveStatus;
    }

    public int getGateStatus() {
        return gateStatus;
    }

    //GateControl Methods

    private void forlinePump(String enable)
    {
        boolean isCorrectControl = false;
        boolean enablePump=false;

        if (enable.equals("on") || enable.equals("off"))
        {
            isCorrectControl = true;
            enablePump = enable.equals("on");
        }
        else
            sendMessage("No $control$ options or it's incorrect (should use \"on\" or \"off\")");

        String digitalOutput = (enablePump) ? " 1" : " 0";
        if (pumpEnabled ^ enablePump && isCorrectControl)
        {
            String someCommand = "deviceCommand dwrite "+ forlinePumpDigitalPin+digitalOutput;
            terminalSample.runTerminalCommand(someCommand, 10);
            pumpEnabled = enablePump;
            measureAndLog();
        }
    }

    //FIXME it looks too large and seems not working
    private void control(final String type, String control)
    {
        boolean isCorrectControl = false;
        boolean openValve=false;

        if (control.equals("open") || control.equals("close"))
        {
            isCorrectControl = true;
            openValve = control.equals("open");
        }
        else
            sendMessage("No $control$ options or it's incorrect (should use \"open\" or \"close\")");
        if (!(type.equals("valve") || type.equals("gate")))
        {
            isCorrectControl = false;
            sendMessage("no $type$ option or its incorrect");
        }
        if (isCorrectControl) {
            double columnPressure = gauge.pressure[columnNumber];
            double vesselPressure = gauge.pressure[columnNumber];
            double pressureDifference = Math.abs(columnPressure-vesselPressure);
            int pin = (type.equals("gate")) ? gateDigitalPin : valveDigitalPin;
            final boolean isOpened = arduino.getDigitalPinsWritten()[pin];
            if (openValve ^ isOpened) {
                if (openValve) {   //control
                    boolean isforlinePumpOn = arduino.getDigitalPinsWritten()[forlinePumpDigitalPin];
                    if (type.equals("valve")) { //valve
                        if (isforlinePumpOn || (columnPressure > 700))
                        {
                            String someCommand = "deviceCommand dwrite " + valveDigitalPin + " 1";
                            arduino.runTerminalCommand(someCommand, 10);
                            valveOpened=true;
                            measureAndLog();
                        }
                        else
                            sendMessage("pressure difference is too high.");
                    }
                    else
                        {   //gate
                        if (pressureDifference < maxPresDifference && vesselPressure < maxPresDifference)
                        {
                            String someCommand = "deviceCommand dwrite "+gateDigitalPin+" 1";
                            arduino.runTerminalCommand(someCommand, 10);
                            gateOpened = true;
                            measureAndLog();
                        }
                        else
                        {
                            if (pressureDifference>maxPresDifference)
                                sendMessage("pressure difference is too high.");
                            if (vesselPressure > maxPresDifference)
                                sendMessage("pressure in vessel is too high.");
                        }
                    }
                }
                else //close
                {
                    String someCommand = "deviceCommand dwrite " + pin + " 0";
                    arduino.runTerminalCommand(someCommand, 10);
                    if (type.equals("gate"))
                        gateOpened=false;
                    else
                        valveOpened = false;
                    measureAndLog();
                }
            }

            //FIXME sleep is very very bad!
            final  boolean triedToOpen = openValve;
            final boolean wasOpened = isOpened(type);
            Thread checkGate = new Thread(() -> {
                try {
                    Thread.sleep(1000);

                    String status = ((wasOpened) ? "opened" : "closed");
                    if (triedToOpen ^ wasOpened)
                    {
                        //TODO add closing of the gates!
                        sendMessage("!!!!!!!ERROR!!!!"+ type+columnNumber+"is not "+status);
                        if (type.equals("gate"))
                            gateOpened = false;
                        else
                            valveOpened = false;
                    }
                        else
                    {
                        sendMessage(type+columnNumber+" is "+status);
                        if (type.equals("gate"))
                            gateOpened = wasOpened;
                        else
                            valveOpened = wasOpened;
                    }
                        measureAndLog();
                }
                catch (InterruptedException ignored){}
            });

        }
    }


    private boolean isOpened(String gate)
    {
        boolean closed,opened;
        int closedPin = (gate.equals("gate")) ? gateAnalogClosedPin : valveAnalogClosedPin;
        int openedPin = (gate.equals("gate")) ? gateAnalogOpenedPin : valveAnalogOpenedPin;
        terminalSample.runTerminalCommand( "deviceCommand aread "+openedPin, 10);
        terminalSample.runTerminalCommand( "deviceCommand aread "+closedPin, 10);
        closed = (arduino.getAnalogPinsRead()[closedPin]>2.5);
        opened = (arduino.getAnalogPinsRead()[openedPin]>2.5);
        if (opened ^ closed) return opened;
        else
        {
            sendMessage("ERROR: low pressure in pneumo line");
            return false;
        }
    }

    //terminal related methods

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("forline","");
        commands.put("valve","open or close valve in form: valve $control$");
        commands.put("gate","open or close gate in form: gate $control$");
        commands.put("isOpened","shows the isOpened of the gates in form: isOpened $gate type(valve, gate)$");
        return super.getCommands();
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
               String[] command = line.split(" ");
               try {
               switch (command[0]) {
                   case "forlinepin": forlinePumpDigitalPin =  Integer.parseInt(command[1]);
                   break;
                   case "valvecontrolpin": valveDigitalPin =  Integer.parseInt(command[1]);
                   break;
                   case "valveanalogpins" :
                   {
                       valveAnalogClosedPin =  Integer.parseInt(command[1]);
                       valveAnalogOpenedPin =  Integer.parseInt(command[2]);
                   }
                   break;
                   case "gatecontrolpin": gateDigitalPin =  Integer.parseInt(command[1]);
                   break;
                   case "gateanalogpins" :
                   {
                       gateAnalogClosedPin =  Integer.parseInt(command[1]);
                       gateAnalogOpenedPin =  Integer.parseInt(command[2]);
                   }
                   break;
                   case "maxpressuredifference" : maxPresDifference = Integer.parseInt(command[1]);
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
    protected void chooseTerminalCommand(String[] command) {
        super.chooseTerminalCommand(command);
        switch (command[1]) {
            case "valve": control("valve", command[2]);
                break;
            case "gate": control("gate", command[2]);
                break;
            case "forline": forlinePump(command[2]);
                break;
            case "isOpened":
                break;
        }
    }

    @Override
    protected void measureAndLog() {
        try {
            String pump = (pumpEnabled) ? "enabled" : "disabled";
            String valve = (valveOpened) ? "opened" : "closed";
            String gate = (gateOpened) ? "opened" : "closed";
            dataLog.write("time "+pump+" "+valve+" "+gate);
        }
        catch (Exception  e)
        {
            sendMessage("ERROR while log: "+e.getMessage());
        }
    }
}
