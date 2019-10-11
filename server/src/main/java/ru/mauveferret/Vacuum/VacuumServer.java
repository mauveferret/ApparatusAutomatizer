package ru.mauveferret.Vacuum;

import ru.mauveferret.Server;

import java.text.DecimalFormat;

class VacuumServer extends Server {

    VacuumServer(String fileName) {
        super(fileName);
    }

    @Override
    protected void initialize() {
        super.initialize();
        devices = new ControlDevice(config.fileName);
        devices.setTerminal(terminalSample);
        devices.initialize();
    }

    private ControlDevice devices;
    private DecimalFormat decFormat = new DecimalFormat("#0.00");
    DecimalFormat sciFormat = new DecimalFormat("%6.3e");
    // FIXME why 6?
    private boolean[] previousCommands = new boolean[6];
    @Override
    public String createResponse(String request)
    {

        try {
            fullfillOrders(request.split(" ")[1]);
        }
        catch (ArrayIndexOutOfBoundsException ignored){}

        //TODO since you have separate SOckets for different parts, you don't have to use "vac"
        String response = "";
        if (request.startsWith("vac"))
        {


                String columnData =System.currentTimeMillis()+" ";
                columnData+=booleanToString(devices.bypass.isOpened)+""+devices.gateControl1.getPumpStatus();
                columnData+=devices.gateControl1.getValveStatus()+""+devices.gateControl1.getGateStatus()+"";
                columnData+=booleanToString(devices.tmp1.isEnabled())+" "+booleanToString(devices.tmp1.isControlOn);
                // response+=columnData;
                // columnData = booleanToString(bypass.isOpened)+booleanToString(gateControl2.pumpStatus());
                //columnData+=booleanToString(gateControl2.isValveOpened())+booleanToString(gateControl2.isGateOpened());
                //columnData+=booleanToString(tmp2.isEnabled())+" ";
                response+=columnData;
                //FIXME do you need replace?!
                response+=String.format("%6.3e",devices.gauge.pressure[1]).replace(",",".")+" ";
                response+=String.format("%6.3e",devices.gauge.pressure[2]).replace(",",".")+" ";
                response+=String.format("%6.3e",devices.gauge.pressure[3]).replace(",",".")+" ";
                response+=devices.tmp1.getFrequency()+" "+devices.tmp1.getTemperature()+" ";
                response+=decFormat.format(devices.tmp1.getVoltage()).replace(",",".")+" ";
                response+=decFormat.format(devices.tmp1.getCurrent()).replace(",",".");
        }

        // createResponse+=""+gateControl.pumpStatus()+gateControl.isValveOpened()+gateControl.isGateOpened();
        // createResponse+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        return response;
    }

    private void fullfillOrders(String commands)
    {
        boolean[] newCommands = stringToBooleanArray(commands);
        for (int i=0;i<newCommands.length;i++)
        {
            if (newCommands[i]^previousCommands[i])
            {
                previousCommands[i] = newCommands[i];
                executeCommand(i);
            }
        }

    }

    private boolean[] stringToBooleanArray(String line)
    {
        boolean[] b = new boolean[line.length()];
        for (int i=0;i<line.length();i++) b[i] = (line.charAt(i)+"").equals("1");
        return b;
    }

    private void executeCommand(int commandIndex)
    {

        String tmp1 = devices.tmp1.config.deviceCommand;
        //FIXME accessLevel
        switch (commandIndex)
        {
            case 5:
            {
                if (previousCommands[commandIndex])
                    terminalSample.launchCommand(tmp1+" run", true, 10);
                else
                    terminalSample.launchCommand(tmp1+" stop", true, 10);
            }
            break;
            case 4:
            {
                if (previousCommands[commandIndex])
                    terminalSample.launchCommand(tmp1+" control on", true, 10);
                else
                    terminalSample.launchCommand(tmp1+" control off", true, 10);
            }
            break;
        }
    }

}
