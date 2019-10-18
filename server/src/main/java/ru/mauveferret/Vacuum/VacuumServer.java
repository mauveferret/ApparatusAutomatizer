package ru.mauveferret.Vacuum;

import ru.mauveferret.Server;
import ru.mauveferret.SocketCryptedCommunicator;

import java.text.DecimalFormat;

class VacuumServer extends Server {

    VacuumServer(String fileName) {
        super(fileName);
    }

    @Override
    protected void initialize() {
        super.initialize();
        try {  gauge = (Gauge) (terminalSample.getDevice(gaugeName));} catch (Exception ignored) {}
        try {  tmp1 = (TMP) (terminalSample.getDevice(tmp1Name));} catch (Exception ignored) {}
        try { tmp2 = (TMP) (terminalSample.getDevice(tmp2Name));} catch (Exception ignored) {}
        try { gateControl1 = (GateControl) (terminalSample.getDevice(gateControl1Name));} catch (Exception ignored) {}
        try {  gateControl2 = (GateControl) (terminalSample.getDevice(gateControl2Name));} catch (Exception ignored) {}
    }

    Gauge gauge;
    TMP tmp1;
    TMP tmp2;
    GateControl gateControl1;
    GateControl gateControl2;

    private String gaugeName;
    private String tmp1Name ;
    private String tmp2Name = "tmp2";
    private String gateControl1Name  = "gate";
    private String gateControl2Name = "gate2" ;


    private DecimalFormat decFormat = new DecimalFormat("#0.0");
    DecimalFormat sciFormat = new DecimalFormat("%6.3e");
    // FIXME why 6?
    private int[] previousCommands = new int[20];

    @Override
    public void communicate(SocketCryptedCommunicator communicator)
    {
        String request = communicator.readEncryption();
        try {
            fullfillOrders(request.split(" ")[1], communicator);
        }
        catch (ArrayIndexOutOfBoundsException ignored){sendMessage(ignored.getMessage());}

        //TODO since you have separate SOckets for different parts, you don't have to use "vac"
        if (request.startsWith("vac"))
        {

        }
       communicator.writeEncryption(createResponse());
    }

    private String createResponse()
    {
        //its useless to send first 7 digits, the ping of our system is little so client can calculate first digits
        String response =(System.currentTimeMillis()+" ").substring(7);
        //gatecontrol2 and gauges are not controlled yet
        response+=columnData(gateControl1, tmp1)+" "+"00000000 "+"00 ";
        //FIXME do you need replace?!
        response+=String.format("%6.2e",gauge.pressure.get("column1")).replace(",",".")+" ";
        response+=String.format("%6.2e",gauge.pressure.get("column1")).replace(",",".")+" ";
        response+=String.format("%6.2e",gauge.pressure.get("vessel")).replace(",",".")+" ";
        response+=tmp1.getFrequency()+" "+tmp1.getTemperature()+" ";
        response+=decFormat.format(tmp1.getVoltage()).replace(",",".")+" ";
        response+=decFormat.format(tmp1.getCurrent()).replace(",",".");
        return  response;
    }

    private String columnData(GateControl gateControl, TMP tmp){
        String columnData=gateControl.getPumpStatus()+""+gateControl.getBypassStatus();
        columnData+=gateControl.getValveStatus()+""+gateControl.getGateStatus();
        columnData+=booleanToString(tmp.isControlOn())+""+booleanToString(tmp.isEnabled());
        columnData+=booleanToString(tmp.isStandbyOn())+""+booleanToString(tmp.isCoolingOn());
        return columnData;
    }
    private void fullfillOrders(String commands, SocketCryptedCommunicator communicator)
    {
        //FIXME new communication protocol
        //boolean[] newCommands = stringToBooleanArray(commands);
        for (int i=0;i<commands.length();i++)
        {
            int commandValue = Integer.parseInt(commands.charAt(i)+"");
            if (commandValue !=previousCommands[i+1])
            {
                previousCommands[i+1] =commandValue ;
                executeCommand(i+1, communicator);
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

        String stmp1 = tmp1.config.unitCommand;
        String gate1 = gateControl1.config.unitCommand;
        int accessLevel = communicator.getAccessLevel();
        String userName = communicator.getLogin();
        //FIXME accessLevel
        //FIXME change two commands to one by adding a String which either "on" or "stop"
        switch (commandIndex)
        {
            case 6:
            {
                if (previousCommands[commandIndex]==1)
                    terminalSample.launchCommand(stmp1+" run", true, accessLevel);
                else
                    terminalSample.launchCommand(stmp1+" stop", true, accessLevel);
            }
            break;
            case 5:
            {
                if (previousCommands[commandIndex]==1)
                    terminalSample.launchCommand(stmp1+" control on", true, accessLevel);
                else
                    terminalSample.launchCommand(stmp1+" control off", true, accessLevel);
            }
            break;
            case 4:
            {
                if (previousCommands[commandIndex]==2)
                    terminalSample.launchCommand(gate1+" gate open", true, accessLevel);
                else
                    terminalSample.launchCommand(gate1+" gate close", true, accessLevel);
            }
            break;
            case 3:
            {
                if (previousCommands[commandIndex]==2)
                    terminalSample.launchCommand(gate1+" valve open", true, accessLevel);
                else
                    terminalSample.launchCommand(gate1+" valve close", true, accessLevel);
            }
            break;
            case 2:
            {
                if (previousCommands[commandIndex]==2)
                    terminalSample.launchCommand(gate1+" bypass open", true, accessLevel);
                else
                    terminalSample.launchCommand(gate1+" bypass close", true, accessLevel);
            }
            break;
            case 1:
            {
                if (previousCommands[commandIndex]==2)
                    terminalSample.launchCommand(gate1+" pump on", true, accessLevel);
                else
                    terminalSample.launchCommand(gate1+" pump off", true, accessLevel);
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
                case  "tmp1" : tmp1Name = command[1];
                    break;
                case  "gatecontrol1" : gateControl1Name = command[1];
                    break;
                case  "tmp2" : tmp2Name = command[1];
                    break;
                case  "gatecontrol2" : gateControl2Name = command[1];
                    break;
            }
        }
        catch (Exception e)
        {
            sendMessage("incorrect option.");
            sendMessage(e.getMessage());
        }
    }

}
