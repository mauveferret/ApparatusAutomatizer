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
                response = System.currentTimeMillis()+" ";
                System.out.println(devices.bypass.isOpened);
                System.out.println(devices.gateControl1.isGateOpened());
                String columnData = booleanToString(devices.bypass.isOpened)+booleanToString(devices.gateControl1.isPumpEnabled());
                columnData+=booleanToString(devices.gateControl1.isValveOpened())+booleanToString(devices.gateControl1.isGateOpened());
                columnData+=booleanToString(devices.tmp1.isEnabled())+" ";
                // response+=columnData;
                // columnData = booleanToString(bypass.isOpened)+booleanToString(gateControl2.isPumpEnabled());
                //columnData+=booleanToString(gateControl2.isValveOpened())+booleanToString(gateControl2.isGateOpened());
                //columnData+=booleanToString(tmp2.isEnabled())+" ";
                response+=columnData+devices.gauge.pressure[1]+" "+devices.gauge.pressure[2]+" "+devices.gauge.pressure[3]+" ";
                response+=devices.tmp1.getTemperature()+" "+devices.tmp1.getFrequency()+" "+devices.tmp1.getVoltage()+" ";
                response+=devices.tmp1.getCurrent()+" ";
            }
            else
            {//TODO create separate vacuum terminal
                terminalSample.launchCommand(request.substring(3), true, 10);
            }

        }

        // createResponse+=""+gateControl.isPumpEnabled()+gateControl.isValveOpened()+gateControl.isGateOpened();
        // createResponse+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        return response;
    }


}
