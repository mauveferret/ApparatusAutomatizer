package ru.mauveferret;

import java.io.*;
import java.net.Socket;
import java.util.Date;

class ServerCommunicator extends Device{


    ServerCommunicator(String fileName, Socket socket) {
        super(fileName);
        communicator = new SocketCryptedCommunicator(socket);
    }


    private Date expireDate;
    private int accessType;
    private String login;
    private SocketCryptedCommunicator communicator;
    private Gauge gauge;
    private String gaugeName;
    private GateControl gateControl;
    private  String gateControlName;
    private  LeyboldTMP tmp;
    private  String tmpName;

    @Override
    public synchronized void start() {
        if (communicator.createSecureCommunicationLine()) {
            if (canAccess()) {
                communicator.writeEncryption("granted");
                sendMessage("Access granted to " + login);
                int accessLevel = terminalSample.passwords.getAccessLevel(login);
                while (expireDate.after(new Date()) && !communicator.isStopCommunication()) {
                    try {
                        //FIXME it dont wait for read
                        String someCommand = communicator.readEncryption();
                        if (!someCommand.contains("nocommand"))
                        {
                            System.out.println("comsecommand launched "+someCommand);
                            terminalSample.launchCommand(someCommand, true, accessLevel);
                        }
                        if (someCommand.equals("stop"))
                        {
                            communicator.stopCommunication();
                        }
                        else
                            communicator.writeEncryption(response(1));
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
        accessType = 10;
        return (passworIsValid && pairIssNotExpired);
    }




    String response(int columnNumber)
    {
        String response =""+System.currentTimeMillis();
       // response+=""+gateControl.isPumpEnabled()+gateControl.isValveOpened()+gateControl.isGateOpened();
       // response+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        response= (System.currentTimeMillis()+" "+gauge.pressure[0]+" "+gauge.pressure[1]);
        return response;
    }

    @Override
    void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        try {
            switch (command[0]) {

                case "gauge": gaugeName = command[1];
                break;
                case "gatecontrol": gateControlName = command[1];
                break;
                case "tmp" : tmpName = command[1];
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
    void initialize() {
        super.initialize();
        gauge= (ThyracontGauge) (terminalSample.getDevice(gaugeName));
        gateControl = (GateControl) (terminalSample.getDevice(gateControlName));
        tmp = (LeyboldTMP) (terminalSample.getDevice(tmpName));
    }

    @Override
    void measureAndLog() {

    }
}
