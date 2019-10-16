package ru.mauveferret.Vacuum;

import ru.mauveferret.Device;
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
        devices = new ControlDevice(config.name);
        devices.setTerminal(terminalSample);
        devices.initialize();
    }

    private ControlDevice devices;
    private DecimalFormat decFormat = new DecimalFormat("#0.00");
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
        catch (ArrayIndexOutOfBoundsException ignored){ignored.printStackTrace();}

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
        response+=columnData(devices.gateControl1, devices.tmp1)+" "+"00000000 "+"00 ";
        //FIXME do you need replace?!
        response+=String.format("%6.3e",devices.gauge.pressure[1]).replace(",",".")+" ";
        response+=String.format("%6.3e",devices.gauge.pressure[2]).replace(",",".")+" ";
        response+=String.format("%6.3e",devices.gauge.pressure[3]).replace(",",".")+" ";
        response+=devices.tmp1.getFrequency()+" "+devices.tmp1.getTemperature()+" ";
        response+=decFormat.format(devices.tmp1.getVoltage()).replace(",",".")+" ";
        response+=decFormat.format(devices.tmp1.getCurrent()).replace(",",".");
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
            if (commands.charAt(i)!=previousCommands[i])
            {
                previousCommands[i] = commands.charAt(i);
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

        String tmp1 = devices.tmp1.config.deviceCommand;
        String gate1 = devices.gateControl1.config.deviceCommand;
        int accessLevel = communicator.getAccessLevel();
        String userName = communicator.getLogin();
        //FIXME accessLevel
        //FIXME change two commands to one by adding a String which either "on" or "stop"
        switch (commandIndex)
        {
            case 6:
            {
                if (previousCommands[commandIndex]==1)
                    terminalSample.launchCommand(tmp1+" run", true, accessLevel);
                else
                    terminalSample.launchCommand(tmp1+" stop", true, accessLevel);
            }
            break;
            case 5:
            {
                if (previousCommands[commandIndex]==1)
                    terminalSample.launchCommand(tmp1+" control on", true, accessLevel);
                else
                    terminalSample.launchCommand(tmp1+" control off", true, accessLevel);
            }
            break;
            case 4:
            {
                if (previousCommands[commandIndex]==2)
                    terminalSample.launchCommand(gate1+" gate open", true, accessLevel);
                else
                    terminalSample.launchCommand(gate1+" gate close", true, accessLevel);
            }
        }
    }

}
