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
    DecimalFormat decFormat = new DecimalFormat("#0.00");
    DecimalFormat sciFormat = new DecimalFormat("%6.3e");
    // FIXME why 6?
    private boolean[] previousCommands = new boolean[6];
    @Override
    public String createResponse(String request)
    {

        fullfillOrders(request.split(" ")[1]);

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

            /*
            {
                terminalSample.launchCommand(request.substring(3), true, 10);
            }

             */

        }

        // createResponse+=""+gateControl.pumpStatus()+gateControl.isValveOpened()+gateControl.isGateOpened();
        // createResponse+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        return response;
    }

    public void fullfillOrders(String commands)
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
        //FIXME accessLevel
        switch (commandIndex)
        {
            case 5:
            {
                if (previousCommands[commandIndex])
                    terminalSample.launchCommand("tmp run", true, 10);
                else
                    terminalSample.launchCommand("tmp stop", true, 10);
            }
            break;
        }
    }

}
