package ru.mauveferret.Vacuum;

import ru.mauveferret.Server;

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

    @Override
    public String createResponse(String request)
    {
        String response = "";
        if (request.startsWith("vac"))
        {

            if (request.contains("nocom"))
            {
                String columnData =System.currentTimeMillis()+" ";
                columnData+=booleanToString(devices.bypass.isOpened)+""+devices.gateControl1.getPumpStatus();
                columnData+=devices.gateControl1.getValveStatus()+""+devices.gateControl1.getGateStatus();
                columnData+=booleanToString(devices.tmp1.isEnabled())+" ";
                // response+=columnData;
                // columnData = booleanToString(bypass.isOpened)+booleanToString(gateControl2.pumpStatus());
                //columnData+=booleanToString(gateControl2.isValveOpened())+booleanToString(gateControl2.isGateOpened());
                //columnData+=booleanToString(tmp2.isEnabled())+" ";
                response+=columnData+devices.gauge.pressure[1]+" "+devices.gauge.pressure[2]+" "+devices.gauge.pressure[3]+" ";
                response+=devices.tmp1.getTemperature()+" "+devices.tmp1.getFrequency()+" "+devices.tmp1.getVoltage()+" ";
                response+=devices.tmp1.getCurrent()+" ";
            }
            else
            {
                terminalSample.launchCommand(request.substring(3), true, 10);
            }

        }

        // createResponse+=""+gateControl.pumpStatus()+gateControl.isValveOpened()+gateControl.isGateOpened();
        // createResponse+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        return response;
    }


}
