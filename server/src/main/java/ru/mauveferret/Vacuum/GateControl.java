package ru.mauveferret.Vacuum;

import ru.mauveferret.Arduino;
import ru.mauveferret.RecordingUnit;

import java.util.HashMap;
import java.util.TreeMap;

/*
an example of the  virtual device which would be made by some user to configure Automizer for his personal needs
Generally, its is a simulation of the vacuum column number with forline pump, bypass (separate class),
pneumatic valve controlled by the arduino, some TMP with temperature sensor, pneumatic gate after the TMP
also controlled by the arduino. Gate control allows to control forlinePump, valve and gate and dosen't allow some
potentially crucial actions like "please open turn off forline pump while TMp is on"
or "please open valve when pressure before is much another tha pressure after"
 */
class GateControl extends RecordingUnit {

    //FIXME remove Control device

    GateControl(String fileName) {
        super(fileName);
        unitAccessLevel = 6;
        dataLog.createFile(config.dataPath, "forlineStatus  bypassStatus valveStatus   gateStatus");
    }

    @Override
    protected void initialize() {
        super.initialize();
        arduino = (config.unitNumber == 1) ? LoadedUnits.column1.arduino : LoadedUnits.column2.arduino;
        // in case of gauges it could be different devices
        columnGauge =  LoadedUnits.gauge.get(columnGaugeName);
        vesselGauge =  LoadedUnits.gauge.get(vesselGaugeName);
    }


    @Override
    protected  void convertDataFromInitializeToLocalType(HashMap<String,String> initializeData)
    {

        for (String someDevice: config.devices)
        {
            try {
                status.put(someDevice, Integer.parseInt(initializeData.get(someDevice).split(" ")[1]));
                opened.put(someDevice, Integer.parseInt(initializeData.get(someDevice).split(" ")[2])==1);
            }
            catch (Exception e)
            {
                sendMessage("12123 "+e.getMessage());
            }
        }
    }

    //key - device name (pump, bypass, valve, gate), value - ststus int number or boolean number
    private HashMap<String, Integer> status = new HashMap<>();
    private HashMap<String, Boolean> opened = new HashMap<>();

    private String arduinoName;
    private String gaugeName;
    private Arduino arduino;
    private Gauge columnGauge;
    private Gauge vesselGauge;



    public final String[] statuses = new String[]{
            "device switched off",                                //0
            "device switched on and disabled",                    //1
            "device switched on and enabled",                     //2
            "device doesn't response",                            //3
            "device switched on, low pneumo line pressure",        //4
            "device switched on, but there is an unknown error",  //5
            "Cant't open. Pressure difference is too high",        //6
            "pressure in vessel is too high",                        //7
            "Hardware error. Unit doesn't response. Check the cable.", //8
            "close valve for bypass pumping!"                           //9
    };


    private double columnPressure;
    private double vesselPressure;
    private double pressureDifference;

    //for valve and bypass opening
    private boolean isPumpOn;

    //in torr
    private int maxPresDifference = 10;

    //for Gauge
    private String columnGaugeName;
    private String vesselGaugeName;

    //for Arduino
    private int pumpDigitalPin;
    private int valveDigitalPin;
    private int gateDigitalPin;
    private int bypassDigitalPin;

    private int valveAnalogOpenedPin;
    private int valveAnalogClosedPin;
    private int gateAnalogOpenedPin;
    private int gateAnalogClosedPin;
    private int bypassAnalogOpenedPin;
    private int bypassAnalogClosedPin;





    public int getPumpStatus() {
        return status.get("pump");
    }

    public int getBypassStatus() { return status.get("bypass");}

    public int getValveStatus() {
        return status.get("valve");
    }

    public int getGateStatus() { return status.get("gate");}

    //GateControl Methods

    //forline pump (for creating low vacuum)

    synchronized private void pump(String enable)
    {
        boolean isCorrectControl = false;
        boolean enablePump=false;

        if (enable.equals("on") || enable.equals("off"))
        {
            isCorrectControl = true;
            enablePump = enable.equals("on");
        }
        else
        {
            sendMessage("No $control$ options or it's incorrect (should use \"on\" or \"off\")");
        }

        String digitalOutput = (enablePump) ? " 1" : " 0";
        if ((opened.get("pump") ^ enablePump) && isCorrectControl)
        {
            String someCommand = "deviceCommand dwrite "+ pumpDigitalPin +digitalOutput;
            arduino.runTerminalCommand(someCommand, 10);
            new Thread(() -> {
                opened.put("pump",arduino.getDigitalPinsWritten()[pumpDigitalPin]);
                status.put ("pump", (opened.get("pump")) ? 2 : 1);
            }).start();

            measureAndLog();
        }
    }

    synchronized private void control(final String type, String control)
    {
        boolean isCorrectControl;
        boolean openControl=false;

        if (control.equals("open") || control.equals("close"))
        {
            isCorrectControl = true;
            openControl = control.equals("open");
        }
        else
        {
            isCorrectControl = false;
            sendMessage("No $control$ options or it's incorrect (should use \"open\" or \"close\")");
        }

        if (!(type.equals("valve") || type.equals("gate") || type.equals("bypass")) && isCorrectControl)
        {
            isCorrectControl = false;
            sendMessage("no $type$ option or its incorrect");
        }

        if (isCorrectControl) {
            columnPressure = columnGauge.pressure.get(columnGaugeName);
            vesselPressure = vesselGauge.pressure.get(vesselGaugeName);
            pressureDifference = Math.abs(columnPressure-vesselPressure);
            isPumpOn = arduino.getDigitalPinsWritten()[pumpDigitalPin];

            switch (type)
            {
                case "valve" : {
                    valve(openControl);
                }
                break;
                case "gate" : {
                    gate(openControl);
                }
                break;
                case "bypass" : {
                    bypass(openControl);
                }
                break;
            }

            isOpened(type, openControl);
        }
    }


    private void valve(boolean open)
    {
        if (open ^ opened.get("valve")) {
            if (open) {
                if (isPumpOn || (columnPressure > 700)) {
                    String someCommand = "deviceCommand dwrite " + valveDigitalPin + " 1";
                    arduino.runTerminalCommand(someCommand, 10);

                } else
                    sendMessage("pressure difference is too high.");
            } else {
                String someCommand = "deviceCommand dwrite " + valveDigitalPin + " 0";
                arduino.runTerminalCommand(someCommand, 10);
            }
        }else {
            sendMessage("valve"+config.unitNumber+" already "+((open) ? "opened" : "closed"));
        }
    }

    private void gate(boolean open)
    {
        if (open ^ opened.get("gate"))
        {
            if (open) {
                if (pressureDifference < maxPresDifference && vesselPressure < maxPresDifference) {
                    String someCommand = "deviceCommand dwrite " + gateDigitalPin + " 1";
                    arduino.runTerminalCommand(someCommand, 10);
                    measureAndLog();
                } else {
                    if (pressureDifference > maxPresDifference)
                        sendMessage("pressure difference is too high.");
                    if (vesselPressure > maxPresDifference)
                        sendMessage("pressure in vessel is too high.");
                }
            } else {
                String someCommand = "deviceCommand dwrite " + gateDigitalPin + " 0";
                arduino.runTerminalCommand(someCommand, 10);
            }
        }
        else {
            sendMessage("gate"+config.unitNumber+" already "+((open) ? "opened" : "closed"));
        }
    }

    private void bypass(boolean open)
    {
        if (open ^ opened.get("bypass")) {
            if (open) {
                if ((isPumpOn || (columnPressure > 700)) && (!opened.get("valve"))) {
                    String someCommand = "deviceCommand dwrite " + bypassDigitalPin + " 1";
                    arduino.runTerminalCommand(someCommand, 10);

                } else{
                    if (!opened.get("valve")) sendMessage("close valve"+config.unitNumber+" for bypass pumping!");
                    else sendMessage("pressure difference is too high.");
                }
            } else {
                String someCommand = "deviceCommand dwrite " + bypassDigitalPin + " 0";
                arduino.runTerminalCommand(someCommand, 10);
            }
        }
        else {
            sendMessage("bypass"+config.unitNumber+" already "+((open) ? "opened" : "closed"));
        }
    }

    //compares the user request with the sensors measurements and sets real status of the valve
    synchronized private void isOpened(String type, boolean wasOpened)
    {

        Thread checkGate = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    //FIXME
                    Thread.sleep(1000);

                    boolean closedSignal;
                    boolean openedSignal;
                    int closedPin = 0;
                    int openedPin = 1;

                    switch (type) {
                        case "valve": {
                            closedPin = valveAnalogClosedPin;
                            openedPin = valveAnalogOpenedPin;
                        }
                        break;
                        case "gate": {
                            closedPin = gateAnalogClosedPin;
                            openedPin = gateAnalogOpenedPin;
                        }
                        break;
                        case "bypass": {
                            closedPin = bypassAnalogClosedPin;
                            openedPin = bypassAnalogOpenedPin;
                        }
                        break;
                    }

                    arduino.runTerminalCommand("deviceCommand aread " + openedPin, 10);
                    arduino.runTerminalCommand("deviceCommand aread " + closedPin, 10);
                    closedSignal = (arduino.getAnalogPinsRead()[closedPin] > 2.5);
                    openedSignal = (arduino.getAnalogPinsRead()[openedPin] > 2.5);
                    String action = ((wasOpened) ? "opened" : "closed");

                    if (openedSignal ^ closedSignal) {  //means sensors don't contradict
                        if (openedSignal ^ wasOpened)  //means that the real position of the valve is not you wanted
                        {
                            //TODO add closing of the gates in case of errors!
                            status.put(type,5);
                            opened.put(type,openedSignal);
                            sendMessage("ERROR: " + type + config.unitNumber + " wasn't " + action );
                        } else  //everything is good!
                        {
                            opened.put(type,openedSignal);
                            status.put(type,(openedSignal) ? 2 : 1);
                            sendMessage(type + config.unitNumber + " was " + action);
                        }
                    } else //sensors contradict. They are either broken or the pressure in pneumo line is low
                    {
                        //FIXME ist it a rubbish?
                        opened.put(type,true);
                        status.put(type,4);
                        sendMessage("ERROR: " + type + config.unitNumber + " wasn't " + action +
                                ". Low pressure in pneumo line");
                    }

                }
                catch (InterruptedException ignored){}
                measureAndLog();
            }
        });
        checkGate.setName("Defining the status of the device "+type);
        checkGate.start();
    }

    //terminal related methods

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("pump","");
        commands.put("valve","open or close valve in form: valve $control$");
        commands.put("gate","open or close gate in form: gate $control$");
        commands.put("isOpened","shows the isOpened of the gates in form: isOpened $gate type(valve, gate)$");
        commands.put("bypass","shows the isOpened of the gates in form: isOpened $gate type(valve, gate)$");
        return super.getCommands();
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
               String[] command = line.split(" ");
               try {
               switch (command[0].toLowerCase()) {
                   case "forlinepin": pumpDigitalPin =  Integer.parseInt(command[1]);
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
                   case "bypasscontrolpin" : bypassDigitalPin = Integer.parseInt(command[1]);
                   break;
                   case "gateanalogpins" :
                   {
                       gateAnalogClosedPin =  Integer.parseInt(command[1]);
                       gateAnalogOpenedPin =  Integer.parseInt(command[2]);
                   }
                   case "bypassanalogpins" :
                   {
                       bypassAnalogClosedPin =  Integer.parseInt(command[1]);
                       bypassAnalogOpenedPin =  Integer.parseInt(command[2]);
                   }
                   break;
                   case "maxpressuredifference" : maxPresDifference = Integer.parseInt(command[1]);
                   break;
                   case "columngauge" : columnGaugeName = command[1];
                   break;
                   case "vesselgauge" : vesselGaugeName = command[1];
                   break;
               }
           }
           catch (Exception e)
           {
               sendMessage("incorrect option: "+ command[1]+". "+e.getMessage());
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
            case "bypass": control("bypass", command[2]);
                break;
            case "pump": pump(command[2]);
                break;
            case "isOpened":
                break;
        }
    }

    @Override
    protected void measureAndLog() {
        try {
            String mes = "time ";
            for (int someValue: status.values()) mes+=someValue+" ";
            dataLog.write(mes);
            for (String someDevice: config.devices)
            {
                loggerMap.get(someDevice).write("time " + status.get(someDevice)+" "+((opened.get(someDevice)) ? "1" : "0"));
            }
        }
        catch (Exception  e)
        {
            sendMessage("ERROR while log: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
