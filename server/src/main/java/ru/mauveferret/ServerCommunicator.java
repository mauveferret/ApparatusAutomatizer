package ru.mauveferret;

import java.net.Socket;
import java.util.Date;

class ServerCommunicator extends ControlDevice{

    ServerCommunicator(String fileName, Socket socket) {
        super(fileName);
        communicator = new SocketCryptedCommunicator(socket);
    }

    //TODO divide commands handler into separete pieces in Terminal style
    //Here you'll have several 'devices' like vacuum, discharge, console (all that is in Chooser.fxml in client)
    //First word will be a 'device' command
    //TODO make packages

    private Date expireDate;
    private int accessLevel;
    private String login;
    private SocketCryptedCommunicator communicator;

    @Override
    public synchronized void start() {
        if (communicator.createSecureCommunicationLine()) {
            if (canAccess()) {
                communicator.writeEncryption("granted");
                sendMessage("Access granted to " + login);
                int accessLevel = terminalSample.passwords.getAccessLevel(login);
                while (expireDate.after(new Date()) && !communicator.isStopCommunication()) {
                    try {
                        //FIXME it dont wait for read enough long
                        String someCommand = communicator.readEncryption();
                        communicator.writeEncryption(createResponse(someCommand));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        sendMessage("client doesn't write me");
                    }
                }
                sendMessage(login + " was disconnected");
            }
            else
            {
                communicator.writeEncryption("denied");
                sendMessage("Access denied!");
            }
        }
    }

    private boolean canAccess()
    {
        String[] loginAndPassword = communicator.readEncryption().split(" ");
        boolean passworIsValid = terminalSample.passwords.IsPasswordValid(loginAndPassword[0],loginAndPassword[1]);
        boolean pairIssNotExpired = terminalSample.passwords.loginHasNotExpired(loginAndPassword[0]);
        login = loginAndPassword[0];
        expireDate = terminalSample.passwords.getExpireDate(loginAndPassword[0]);
        accessLevel = 10;
        return (passworIsValid && pairIssNotExpired);
    }

    //TODO move servercommunicator to server
    //Fixme vacuumResponse. make methods for all?

    private String createResponse(String request)
    {
        String response = "";
        if (request.startsWith("vac"))
        {
            if (request.contains("nocom"))
           {
               response = System.currentTimeMillis()+" ";
               String columnData = booleanToString(bypass.isOpened)+booleanToString(gateControl1.isPumpEnabled());
               columnData+=booleanToString(gateControl1.isValveOpened())+booleanToString(gateControl1.isGateOpened());
               columnData+=booleanToString(tmp1.isEnabled())+" ";
               //add "auto pumping block"
              // response+=columnData;
              // columnData = booleanToString(bypass.isOpened)+booleanToString(gateControl2.isPumpEnabled());
               //columnData+=booleanToString(gateControl2.isValveOpened())+booleanToString(gateControl2.isGateOpened());
               //columnData+=booleanToString(tmp2.isEnabled())+" ";
               response+=columnData+gauge.pressure[1]+" "+gauge.pressure[2]+" "+gauge.pressure[3]+" ";
               response+=tmp1.getTemperature()+" "+tmp1.getFrequency()+" "+tmp1.getVoltage()+" ";
               response+=tmp1.getCurrent()+" ";
           }
           else
            {//TODO create separate vacuum terminal
                terminalSample.launchCommand(request.substring(3), true, accessLevel);
            }

        }
       // createResponse+=""+gateControl.isPumpEnabled()+gateControl.isValveOpened()+gateControl.isGateOpened();
       // createResponse+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        return response;
    }
}
