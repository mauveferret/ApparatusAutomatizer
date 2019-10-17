package ru.mauveferret.Vacuum;

import ru.mauveferret.Logger;

import java.io.File;
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
class GateControl extends ControlDevice {


    GateControl(String fileName) {
        super(fileName);
        deviceAccessLevel = 6;
        opened = new boolean[]{false,false,false,false,false,false,false};
        status = new int[]{0,0,0,0,0,0};
        dataLog.createFile(config.dataPath, "forlineStatus  bypassStatus valveStatus   gateStatus");
    }

    @Override
    protected void initialize() {
        super.initialize();
            String newPath = (new File(config.dataPath)).getParent();
            for (int number : config.elements) {
                loggerMap.put(number, new Logger(false));
                String sep = File.separator;
                String pressurePath = newPath + sep + "values" + sep + "valves" + sep + types[number] + columnNumber + ".txt";
                loggerMap.get(number).createFile(pressurePath, "");
            }
    }

    //used to write single pressure from every gauge/ Can be used by third party software
    private HashMap<Integer, Logger> loggerMap = new HashMap<>();

    public final String[] statuses = new String[]{
            "device switched off",                                //0
            "device switched on and disabled",                    //1
            "device switched on and enabled",                     //2
            "device doesn't response",                            //3
            "device switched on, low pneumo line pressure",        //4
            "device switched on, but there is an unknown error",  //5
    };

    public final String[] types =  new String[]{
            "some bug",
            "pump",
            "bypass",
            "valve",
            "gate"
    };

    private double columnPressure;
    private double vesselPressure;
    private double pressureDifference;

    //for valve and bypass opening
    private boolean isPumpOn;

    //in torr
    private int maxPresDifference = 10;

    //for Gauge
    private int columnGaugeNumber;
    private int vesselGaugeNumber;

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

    /*
    sound not normal for pump -> opened but this way is more compact

    0 -> some bug
    1 -> pump
    2 -> bypass
    3 -> valve
    4 -> gate
    5 -> nothing yet
     */
    private boolean[] opened;
    private int[] status;


    public int getPumpStatus() {
        return status[1];
    }

    public int getBypassStatus() { return status[2];}

    public int getValveStatus() {
        return status[3];
    }

    public int getGateStatus() { return status[4];}

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
        if ((opened[1] ^ enablePump) && isCorrectControl)
        {
            String someCommand = "deviceCommand dwrite "+ pumpDigitalPin +digitalOutput;
            arduino.runTerminalCommand(someCommand, 10);
            new Thread(() -> {
                opened[1] = arduino.getDigitalPinsWritten()[pumpDigitalPin];
                status[1] = (opened[1]) ? 2 : 1;
            }).start();

            measureAndLog();
        }
    }

    synchronized private void control(final String type, String control)
    {
        boolean isCorrectControl;
        boolean openControl=false;
        int typeNumber = 0;

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
            columnPressure = gauge.pressure[columnGaugeNumber];
            vesselPressure = gauge.pressure[vesselGaugeNumber];
            pressureDifference = Math.abs(columnPressure-vesselPressure);
            isPumpOn = arduino.getDigitalPinsWritten()[pumpDigitalPin];

            switch (type)
            {
                case "valve" : {
                    typeNumber = 3;
                    valve(openControl);
                }
                break;
                case "gate" : {
                    typeNumber = 4;
                    gate(openControl);
                }
                break;
                case "bypass" : {
                    typeNumber = 2;
                    bypass(openControl);
                }
                break;
            }

            isOpened(typeNumber, openControl);
        }
    }


    private void valve(boolean open)
    {
        if (open ^ opened[3]) {
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
            sendMessage("valve"+columnNumber+" already "+((open) ? "opened" : "closed"));
        }
    }

    private void gate(boolean open)
    {
        if (open ^ opened[4])
        {
            if (open) {
                if (pressureDifference < maxPresDifference && vesselPressure < maxPresDifference) {
                    String someCommand = "deviceCommand dwrite " + gateDigitalPin + " 1";
                    System.out.println(someCommand);
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
            sendMessage("gate"+columnNumber+" already "+((open) ? "opened" : "closed"));
        }
    }

    private void bypass(boolean open)
    {
        if (open ^ opened[2]) {
            if (open) {
                if ((isPumpOn || (columnPressure > 700)) && (!opened[3])) {
                    String someCommand = "deviceCommand dwrite " + bypassDigitalPin + " 1";
                    arduino.runTerminalCommand(someCommand, 10);

                } else{
                    if (!opened[3]) sendMessage("close valve"+columnNumber+" for bypass pumping!");
                    else sendMessage("pressure difference is too high.");
                }
            } else {
                String someCommand = "deviceCommand dwrite " + bypassDigitalPin + " 0";
                arduino.runTerminalCommand(someCommand, 10);
            }
        }    else {
            sendMessage("bypass"+columnNumber+" already "+((open) ? "opened" : "closed"));
        }
    }

    //compares the user request with the sensors measurements and sets real status of the valve
    synchronized private void isOpened(int type, boolean wasOpened)
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
                        case 3: {
                            closedPin = valveAnalogClosedPin;
                            openedPin = valveAnalogOpenedPin;
                        }
                        break;
                        case 4: {
                            closedPin = gateAnalogClosedPin;
                            openedPin = gateAnalogOpenedPin;
                        }
                        break;
                        case 2: {
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
                            status[type] = 5;
                            sendMessage("ERROR: " + types[type] + columnNumber + " wasn't " + action );
                        } else  //everything is good!
                        {
                            status[type] = (openedSignal) ? 2 : 1;
                            sendMessage(types[type] + columnNumber + " was " + action);
                        }
                    } else //sensors contradict. They are either broken or the pressure in pneumo line is low
                    {
                        status[type] = 4;
                        sendMessage("ERROR: " + types[type] + columnNumber + " wasn't " + action +
                                ". Low pressure in pneumo line");
                    }
                    opened[type] = openedSignal;
                }
                catch (InterruptedException ignored){}
                measureAndLog();
            }
        });
        checkGate.setName("Defining the status of the device "+types[type]);
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
                   case "gateanalogpins" :
                   {
                       gateAnalogClosedPin =  Integer.parseInt(command[1]);
                       gateAnalogOpenedPin =  Integer.parseInt(command[2]);
                   }
                   break;
                   case "maxpressuredifference" : maxPresDifference = Integer.parseInt(command[1]);
                   break;
                   case "columngauge" : columnGaugeNumber = Integer.parseInt(command[1]);
                   break;
                   case "vesselgauge" : vesselGaugeNumber = Integer.parseInt(command[1]);
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
            dataLog.write("time "+status[1]+" "+status[2]+" "+status[3]+" "+status[4]);
            for (int deviceNumber : config.elements)
            {
                loggerMap.get(deviceNumber).write("time " + status[deviceNumber]);
            }
        }
        catch (Exception  e)
        {
            sendMessage("ERROR while log: "+e.getMessage());
        }
    }
}
