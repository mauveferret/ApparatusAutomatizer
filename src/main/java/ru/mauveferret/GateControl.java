package ru.mauveferret;

import java.util.TreeMap;

//an example of the  virtual device which would be made by some user to configure Automizer for his personal needs
public class GateControl extends Device{

    private int columnNumber;
    private String arduinoName;
    private String gaugeName;
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


    //TODO
    // заменить булины на значения из ардуины (как уже сделано в методе клапана)
    // записывать всё время в булины значения из ардуины для удобства
    // скорее всего класс ардуины сам по себе использоваться не будет
    // так что здесь надо сделать полноценный лог
    boolean pumpOpened = false;
    boolean valveOpened = false;
    boolean gateOpened = false;

    private Arduino arduino;
    private ThyracontGauge gauge;
    private Terminal terminal;

    public GateControl(String path)
    {
        super(path);
        measureAndLog();
    }

    @Override
    void measureAndLog() {

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
        if (pumpOpened ^ enablePump && isCorrectControl)
        {
            arduino.runCommand(terminal, "deviceCommand dwrite "+ forlinePumpDigitalPin+digitalOutput);
            pumpOpened = enablePump;
        }

    }

    private void open(final String type, String control)
    {
        boolean isCorrectControl = false;

        boolean openValve=false;
        if (control.equals("open") || control.equals("close"))
        {
            isCorrectControl = true;
            openValve = control.equals("open");
        }
        else
        {
            isCorrectControl = false;
            sendMessage("No $open$ options or it's incorrect (should use \"open\" or \"close\")");
        }
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
                boolean isforlinePumpOn = arduino.getDigitalPinsWritten()[forlinePumpDigitalPin];
                //FIXME ?!
                if (openValve) {   //open
                    if (type.equals("valve")) { //valve
                        if (isforlinePumpOn || (!isforlinePumpOn && columnPressure > 700)) {
                            arduino.runCommand(terminal, "deviceCommand dwrite " + valveDigitalPin + " 1");
                        } else {
                            sendMessage("pressure difference is too high.");
                        }
                    }
                    else {   //gate
                        if (pressureDifference < maxPresDifference && vesselPressure < maxPresDifference)
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
                    }
                }
                else //close
                    arduino.runCommand(terminal, "deviceCommand dwrite " + pin + " 0");
            }

            //FIXME sleep is very very bad!
            final  boolean triedToOpen = openValve;
            Thread checkGate = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        String status = ((isOpened) ? "opened" : "closed");
                        if (triedToOpen ^ isOpened)
                        {
                            //TODO add closing of the gates!!!!!!!!!!
                            sendMessage("ERROR"+ type+columnNumber+"is not "+status);
                        }
                            else
                                sendMessage(type+columnNumber+" is "+status);
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
        commands.put("forlinepump","");
        commands.put("open","open or close gates in form: open $type$ $open$");
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
                   case "gauge": gaugeName = command[1];
                   break;
                   case "arduino": arduinoName = command[1];
                   break;
                   case "columnnumber": columnNumber =  Integer.parseInt(command[1]);
                   break;
               }
           }
           catch (Exception e)
           {
               sendMessage("incorrect option "+command[1]+" "+command[2]);
               sendMessage(e.getMessage());
           }

    }

    @Override
    void chooseTerminalCommand(String[] command) {
        terminal = (Terminal) getReceivedDevice();
        arduino = (Arduino) terminal.getDevice(arduinoName);
        gauge = (ThyracontGauge) terminal.getDevice(gaugeName);
        super.chooseTerminalCommand(command);
        switch (command[1]) {
            case "valve": open("valve", command[2]);
                break;
            case "gate": open("gate", command[2]);
                break;
            case "forline": forlinePump(command[2]);
                break;
            case "isOpened":
                break;
        }
    }
}
