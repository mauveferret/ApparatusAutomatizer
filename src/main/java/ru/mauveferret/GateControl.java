package ru.mauveferret;

import java.util.TreeMap;

//an example of the  virtual device which would be made by some user to configure Automizer for his personal needs
public class GateControl extends Device{

    private int columnNumber;
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

    private boolean pumpEnabled = false;
    private boolean valveOpened = false;
    private boolean gateOpened = false;

    private Arduino arduino;
    private ThyracontGauge gauge;
    private GateControl gateControl = this;

    public GateControl(String path)
    {
        super(path);
        measureAndLog();
    }


    public boolean isPumpEnabled() {
        return pumpEnabled;
    }

    public boolean isValveOpened() {
        return valveOpened;
    }

    public boolean isGateOpened() {
        return gateOpened;
    }

    public int getColumnNumber() {
        return columnNumber;
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
            arduino.runCommand(gateControl, "deviceCommand dwrite "+ forlinePumpDigitalPin+digitalOutput);
            pumpEnabled = enablePump;
        }

    }

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
            double columnPressure = gauge.getPressure(columnNumber);
            double vesselPressure = gauge.getPressure(3);
            double pressureDifference = Math.abs(columnPressure-vesselPressure);
            int pin = (type.equals("gate")) ? gateDigitalPin : valveDigitalPin;
            final boolean isOpened = arduino.getDigitalPinsWritten()[pin];
            if (openValve ^ isOpened) {
                //FIXME ?!
                if (openValve) {   //control
                    boolean isforlinePumpOn = arduino.getDigitalPinsWritten()[forlinePumpDigitalPin];
                    if (type.equals("valve")) { //valve
                        if (isforlinePumpOn || (columnPressure > 700))
                            arduino.runCommand(gateControl, "deviceCommand dwrite " + valveDigitalPin + " 1");
                        else
                            sendMessage("pressure difference is too high.");
                    }
                    else
                        {   //gate
                        if (pressureDifference < maxPresDifference && vesselPressure < maxPresDifference)
                            arduino.runCommand(gateControl,"deviceCommand dwrite "+gateDigitalPin+" 1");
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
                    arduino.runCommand(gateControl, "deviceCommand dwrite " + pin + " 0");
                }
            }

            //FIXME sleep is very very bad!
            final  boolean triedToOpen = openValve;
            final boolean wasOpened = isOpened(type);
            Thread checkGate = new Thread(new Runnable() {
                @Override
                public void run() {
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
                    }
                    catch (InterruptedException ignored){}
                }
            });

        }
    }


    private boolean isOpened(String gate)
    {
        boolean closed,opened;
        int closedPin = (gate.equals("gate")) ? gateAnalogClosedPin : valveAnalogClosedPin;
        int openedPin = (gate.equals("gate")) ? gateAnalogOpenedPin : valveAnalogOpenedPin;
        arduino.runCommand(this, "deviceCommand aread "+openedPin);
        arduino.runCommand(this, "deviceCommand aread "+closedPin);
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
    TreeMap<String, String> getCommands() {
        commands.put("forline","");
        commands.put("valve","open or close valve in form: valve $control$");
        commands.put("gate","open or close gate in form: gate $control$");
        commands.put("isOpened","shows the isOpened of the gates in form: isOpened $gate type(valve, gate)$");
        return super.getCommands();
    }

    @Override
    void chooseImportCommand(String line) {
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
                   case "gauge": gauge= (ThyracontGauge) (terminalSample.getDevice(command[1]));
                   break;
                   case "arduino": arduino = (Arduino) (terminalSample.getDevice(command[1]));
                   break;
                   case "columnnumber": columnNumber =  Integer.parseInt(command[1]);
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
    void chooseTerminalCommand(String[] command) {
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
    void measureAndLog() {
        dataLog.createFile(config.dataPath, "forlineStatus  valveStatus   gateStatus");
        log = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stop = false;
                while (!stop)
                {
                    try {
                        String pump = (pumpEnabled) ? "enabled" : "disabled";
                        String valve = (valveOpened) ? "opened" : "closed";
                        String gate = (gateOpened) ? "opened" : "closed";
                        dataLog.write("time "+pump+" "+valve+" "+gate);
                        stop = Thread.currentThread().isInterrupted();
                        //FIXME sleep is very bad decision!
                        Thread.sleep(100);
                    }
                    catch (Exception  e)
                    {
                        sendMessage("ERROR while log: "+e.getMessage());
                    }
                }

            }
        });
        log.setName(config.deviceName);
        log.start();
    }

}
