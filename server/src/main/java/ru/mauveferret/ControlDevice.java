package ru.mauveferret;

abstract class ControlDevice extends Device{

    ControlDevice(String fileName) {
        super(fileName);
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
    void initialize() {

        super.initialize();
       // System.out.println("OIWEHJFIOHQWE*(#URF*(  "+config.deviceName);
       try {
           bypass = (Bypass) (terminalSample.getDevice(bypassName));
           arduino = (Arduino) (terminalSample.getDevice(arduinoName));
           gauge = (Gauge) (terminalSample.getDevice(gaugeName));
           tmp1 = (TMP) (terminalSample.getDevice(tmp1Name));
           tmp2 = (TMP) (terminalSample.getDevice(tmp2Name));
           gateControl1 = (GateControl) (terminalSample.getDevice(gateControl1Name));
           gateControl2 = (GateControl) (terminalSample.getDevice(gateControl2Name));

       }
       catch (Exception ignored) {}

    }

    @Override
    void chooseImportCommand(String line) {
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
    void measureAndLog() {

    }
}
