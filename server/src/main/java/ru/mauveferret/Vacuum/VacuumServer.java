package ru.mauveferret.Vacuum;

import ru.mauveferret.Server;
import ru.mauveferret.SocketCryptedCommunicator;
import ru.mauveferret.Terminal;

import java.text.DecimalFormat;
import java.util.HashMap;

class VacuumServer extends Server {

    VacuumServer(String fileName) {
        super(fileName);
    }

    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String, String> initializeData) {
        for (String someDevice: config.devices)
        {
            String[] digitalpinsfromTHeFile = initializeData.get(someDevice).split(" ");
            try {
                for (int i=1; i<digitalpinsfromTHeFile.length;i++)
                {
                    commandsFromClient[i-1] = Integer.parseInt(digitalpinsfromTHeFile[i]);
                }
            }
            catch (Exception e)
            {
                sendMessage("12123 "+e.getMessage());
            }
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        try {  gauge= (Gauge) (terminalSample.getDevice(gaugeName));} catch (Exception ignored) {}
        try {tmp = (TMP) (terminalSample.getDevice(tmpName));} catch (Exception ignored) {ignored.printStackTrace();}
        try {  gateControl = (GateControl) (terminalSample.getDevice(gateControlName));} catch (Exception ignored) {}
        try {  angel = (GuardianAngel) (terminalSample.getDevice(angelName));} catch (Exception ignored) {}
        try {  autoPumping = (AutoPumping) (terminalSample.getDevice(autoName));} catch (Exception ignored) {}
    }


    Gauge gauge;
    private TMP tmp;
    AutoPumping autoPumping;
    GuardianAngel angel;
    GateControl gateControl;

    String gaugeName;
    private String tmpName ;
    String gateControlName;
    String autoName;
    String angelName;

    private DecimalFormat decFormat = new DecimalFormat("#0.0");
    DecimalFormat sciFormat = new DecimalFormat("%6.3e");
    // 0 -> auto, angel, gauge,pump, bypass,valve,6 ->gate, 7 -> tmp control,
    // tmp run, 9 -> tmp cool, 10 -> tmp standby   == 11 buttons
    private int[] commandsFromClient = new int[11];

    @Override
    public void communicate(SocketCryptedCommunicator communicator)
    {
        String request = communicator.readEncryption();
        String[] commandsArray = request.split(" ");
        switch (commandsArray[1]) {
            case ("vac"): {
                try {
                    //first column
                    fullfillOrders(request.split(" ")[2], communicator);
                    //second column
                    //fullfillOrders(request.split(" ")[3], communicator);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    sendMessage(ignored.getMessage());
                }
                communicator.writeEncryption(createResponse());
            }
            break;
            case ("but"): //to set button position in order not to change units by opening the client
            {
                String buttonPositions = (System.currentTimeMillis()+"").substring(7)+" but ";
                for (int i=0;i < commandsFromClient.length; i++) buttonPositions+=commandsFromClient[i];
                communicator.writeEncryption(buttonPositions);
            }
            break;
        }
    }

    private String createResponse()
    {
        //its useless to send first 7 digits, the ping of our system is little so client can calculate first digits
        //You can use vac to send error messages!!
        String response =(System.currentTimeMillis()+"").substring(7)+" xyz ";
        response+= columnMainParameters(1)+" "+"00000000000 ";
        //FIXME do you need replace?!
        response+=String.format("%6.2e",gauge.pressure.get("column1")).replace(",",".")+" ";
        //Gauges TODO change it to column 2
        response+=String.format("%6.2e",gauge.pressure.get("column1")).replace(",",".")+" ";
        response+=String.format("%6.2e",gauge.pressure.get("vessel")).replace(",",".")+" ";
        //tmp TODO  change  the first column to the second
        response+=tmpMainParameters(1)+" "+tmpMainParameters(1);

        return  response;
    }

    private String columnMainParameters(int columnNumber){
        //PumpingColumn local = (columnNumber==1) ? column1 :column2;
        //FIXME
        //String columnData = autoPumping.getEnabled()+""+angel.getEnabled()+""+gauge.getEnabled();
        String columnData = "002";
        columnData+=gateControl.getPumpStatus()+""+gateControl.getBypassStatus();
        columnData+=gateControl.getValveStatus()+""+gateControl.getGateStatus();
        columnData+=booleanToString(tmp.isControlOn())+""+booleanToString(tmp.isEnabled());
        columnData+=booleanToString(tmp.isCoolingOn())+""+booleanToString(tmp.isStandbyOn());
        return columnData;
    }

    private String tmpMainParameters(int columnNumber)
    {
        //PumpingColumn local = (columnNumber==1) ? column1 :column2;
        String columnData =tmp.getFrequency()+" "+tmp.getTemperature()+" ";
        columnData+=decFormat.format(tmp.getVoltage()).replace(",",".")+" ";
        columnData+=decFormat.format(tmp.getCurrent()).replace(",",".");
        return columnData;
    }


    private void fullfillOrders(String commands, SocketCryptedCommunicator communicator)
    {
        //boolean[] newCommands = stringToBooleanArray(commands);
        for (int i=0;i<commands.length();i++)
        {
            int commandValue = Integer.parseInt(commands.charAt(i)+"");
            if (commandValue != commandsFromClient[i])
            {
                commandsFromClient[i] =commandValue ;
                executeCommand(i, communicator);
            }
        }
    }

    private boolean[] stringToBooleanArray(String line)
    {
        boolean[] b = new boolean[line.length()];
        for (int i=0;i<line.length();i++) b[i] = (line.charAt(i)+"").equals("1");
        return b;
    }

    private void executeCommand(int commandIndex, SocketCryptedCommunicator communicator)
    {

        String stmp1 =tmp.config.unitCommand;
        String gate1 = gateControl.config.unitCommand;
        int accessLevel = communicator.getAccessLevel();
        String userName = communicator.getLogin();
        boolean comIs1 = commandsFromClient[commandIndex]==1;
        boolean comIs2 = commandsFromClient[commandIndex]==2;
        switch (commandIndex){
            case 8: {
                terminalSample.launchCommand(stmp1+(comIs1 ? " run" : " stop"),
                        true, accessLevel);
            }
            break;
            case 7: {
                terminalSample.launchCommand(stmp1+" control "+(comIs1 ? "on" : "off"),
                        true, accessLevel);
            }
            break;
            case 6: {
                terminalSample.launchCommand(gate1+" gate "+(comIs2? "open" : "close"),
                        true, accessLevel);
            }
            break;
            case 5: {
               terminalSample.launchCommand(gate1+" valve "+(comIs2? "open" : "close"),
                       true, accessLevel);
            }
            break;
            case 4: {
                terminalSample.launchCommand(gate1+" bypass "+(comIs2? "open" : "close"),
                        true, accessLevel);
            }
            break;
            case 3: {
                terminalSample.launchCommand(gate1+" pump "+(comIs2? "on" : "off"),
                        true, accessLevel);
            }
            break;
        }
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);


        String[] command = line.split(" ");
        try {
            switch (command[0]) {
                case "gauge": gaugeName = command[1];
                break;
                case  "tmp1" : { tmpName = command[1]; }
                break;
                case  "gatecontrol1" : gateControlName= command[1];
                break;
                case "angel1" : angelName = command[1];
                break;
                case "auto1" : autoName = command[1];
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
        log = new Thread(() -> {
            boolean stop = false;
            //FIXME you don't have to use while
            while (!stop)
            {
                try {
                    String buttonsValues = "";
                    for (int i: commandsFromClient) buttonsValues+=i+" ";

                    for (String someDevice: config.devices)
                    {
                        loggerMap.get(someDevice).write("time " + buttonsValues);
                    }

                    stop = Thread.currentThread().isInterrupted();
                    //TODO very bad
                    try {
                        Thread.sleep(500);
                    }
                    catch (Exception ignored){}
                }
                catch (Exception  e)
                {
                    sendMessage("ERROR while log: "+e.getMessage());
                }
            }

        });
        log.setName(config.name);
        log.start();
    }
}
