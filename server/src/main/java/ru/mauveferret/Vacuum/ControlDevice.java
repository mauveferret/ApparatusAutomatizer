package ru.mauveferret.Vacuum;

import ru.mauveferret.Arduino;
import ru.mauveferret.Device;
import ru.mauveferret.Terminal;

class ControlDevice extends Device {


     //TODO it

    ControlDevice(String fileName) {
        super(fileName);
    }

    //specially for VacuumServer
     void setTerminal(Terminal someTerminal) {
         terminalSample = someTerminal;
     }



     int columnNumber;
    Arduino arduino;
    Gauge gauge;
    TMP tmp1;
    TMP tmp2;
    GateControl gateControl1;
    GateControl gateControl2;
    Bypass bypass;

    //fixme
     static private String arduinoName;
     static private String gaugeName;
     static private String tmp1Name ;
    static private String tmp2Name = "tmp2";
   static private String gateControl1Name ;
     static private String gateControl2Name = "gate2" ;
  static private String bypassName;

    @Override
    protected void initialize() {

        super.initialize();

        //I'm sorry for this, i'l fix it!
        //FIXME exterminate this shit!!!

        try { bypass = (Bypass) (terminalSample.getDevice(bypassName));} catch (Exception ignored) {ignored.printStackTrace();}
        try { arduino = (Arduino) (terminalSample.getDevice(arduinoName));} catch (Exception ignored) {}
        try {  gauge = (Gauge) (terminalSample.getDevice(gaugeName));} catch (Exception ignored) {}
        try {  tmp1 = (TMP) (terminalSample.getDevice(tmp1Name));} catch (Exception ignored) {}
        try { tmp2 = (TMP) (terminalSample.getDevice(tmp2Name));} catch (Exception ignored) {}
        try { gateControl1 = (GateControl) (terminalSample.getDevice(gateControl1Name));} catch (Exception ignored) {}
        try {  gateControl2 = (GateControl) (terminalSample.getDevice(gateControl2Name));} catch (Exception ignored) {}
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        try {
            switch (command[0]) {
                case "columnnumber": columnNumber =  Integer.parseInt(command[1]);
                break;
                case "gauge": gaugeName = command[1];
                break;
                case "arduino": arduinoName = command[1];
                break;
                case  "tmp1" : tmp1Name = command[1];
                break;
                case  "gatecontrol1" : gateControl1Name = command[1];
                break;
                case  "tmp2" : tmp2Name = command[1];
                break;
                case  "gatecontrol2" : gateControl2Name = command[1];
                break;
                case  "bypass" : bypassName = command[1];
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
    protected void measureAndLog() {

    }
}
