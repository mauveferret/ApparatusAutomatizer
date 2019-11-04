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
        tmp1 = LoadedUnits.column1.tmp;
        tmp2 = LoadedUnits.column2.tmp;
        autoPumping1 = LoadedUnits.column1.autoPumping;
        autoPumping2 = LoadedUnits.column2.autoPumping;
        angel1 = LoadedUnits.column1.angel;
        angel2 = LoadedUnits.column2.angel;
        gateControl1 = LoadedUnits.column1.gateControl;
        gateControl2 = LoadedUnits.column2.gateControl;
        columnGauge1 = LoadedUnits.gauge.get("column1");
        columnGauge2 = LoadedUnits.gauge.get("column2");
        vesselGauge = LoadedUnits.gauge.get("vessel");
    }


    private Gauge columnGauge1;
    private Gauge columnGauge2;
    private Gauge vesselGauge;
    private TMP tmp1;
    private TMP tmp2;
    private AutoPumping autoPumping1;
    private AutoPumping autoPumping2;
    private GuardianAngel angel1;
    private GuardianAngel angel2;
    private GateControl gateControl1;
    private GateControl gateControl2;



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
        try {
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
                    String buttonPositions = (System.currentTimeMillis() + "").substring(7) + " but ";
                    for (int i = 0; i < commandsFromClient.length; i++) buttonPositions += commandsFromClient[i];
                    communicator.writeEncryption(buttonPositions);
                }
                break;
            }
        }
        catch (Exception ignored){}
    }

    private String createResponse()
    {
        //its useless to send first 7 digits, the ping of our system is little so client can calculate first digits
        //You can use vac to send error messages!!
        String response =(System.currentTimeMillis()+"").substring(7)+" xyz ";
        response+= columnMainParameters(1)+" "+"00000000000 ";
        //FIXME do you need replace?!
        response+=String.format("%6.2e",columnGauge1.pressure.get("column1")).replace(",",".")+" ";
        response+=String.format("%6.2e",columnGauge2.pressure.get("column2")).replace(",",".")+" ";
        response+=String.format("%6.2e",vesselGauge.pressure.get("vessel")).replace(",",".")+" ";
        //tmp TODO  change  the first column to the second
        response+=tmpMainParameters(1)+" "+tmpMainParameters(1);

        return  response;
    }

    private String columnMainParameters(int columnNumber){
        GateControl localCOntrol;
        TMP localTMP;
        GuardianAngel localAngel;

        if (columnNumber == 1)
        {
            localCOntrol = gateControl1;
            localTMP = tmp1;
            localAngel = angel1;
        }
        else {
            localCOntrol = gateControl2;
            localTMP = tmp2;
            localAngel = angel2;
        }
        String columnData = "0"; //auto
        columnData+=""+localAngel.getEnabled()+"2"; //angel + gauges
        columnData+=localCOntrol.getPumpStatus()+""+localCOntrol.getBypassStatus();
        columnData+=localCOntrol.getValveStatus()+""+localCOntrol.getGateStatus();
        columnData+=booleanTOProtocol(localTMP.isControlOn())+""+booleanTOProtocol(localTMP.isEnabled());
        columnData+=booleanTOProtocol(localTMP.isCoolingOn())+""+booleanTOProtocol(localTMP.isStandbyOn());
        return columnData;
    }

    private String booleanTOProtocol(boolean b) { return (b ? "2" : "1");}

    private String tmpMainParameters(int columnNumber)
    {
        TMP localTMP = (columnNumber==1) ? tmp1 : tmp2;
        String columnData =localTMP.getFrequency()+" "+localTMP.getTemperature()+" ";
        columnData+=decFormat.format(localTMP.getVoltage()).replace(",",".")+" ";
        columnData+=decFormat.format(localTMP.getCurrent()).replace(",",".");
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
                //FIXME columnNumber
                executeCommand(i, communicator,1);
            }
        }
    }

    private boolean[] stringToBooleanArray(String line)
    {
        boolean[] b = new boolean[line.length()];
        for (int i=0;i<line.length();i++) b[i] = (line.charAt(i)+"").equals("1");
        return b;
    }

    private void executeCommand(int commandIndex, SocketCryptedCommunicator communicator, int columnNumber)
    {
        GateControl localCOntrol;
        TMP localTMP;
        GuardianAngel localAngel;
        if (columnNumber == 1)
        {
            localCOntrol = gateControl1;
            localTMP = tmp1;
            localAngel = angel1;
        }
        else {
            localCOntrol = gateControl2;
            localTMP = tmp2;
            localAngel = angel2;
        }
        String stmp1 =localTMP.config.unitCommand;
        String gate1 =  localCOntrol.config.unitCommand;
        int accessLevel = communicator.getAccessLevel();
        String userName = communicator.getLogin();
        boolean comIs2 = commandsFromClient[commandIndex]==2;
        switch (commandIndex){
            case 8: {
                terminalSample.launchCommand(stmp1+(comIs2 ? " run" : " stop"),
                        true, accessLevel);
            }
            break;
            case 7: {
                terminalSample.launchCommand(stmp1+" control "+(comIs2 ? "on" : "off"),
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
            case 1: {
                terminalSample.launchCommand(localAngel.config.unitCommand+" "+(comIs2 ? "start" : "stop"),
                        true, accessLevel);
            }
            break;
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
