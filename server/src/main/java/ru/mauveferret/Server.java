package ru.mauveferret;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

class Server  extends  ControlDevice{

    Server(String fileName) {
        super(fileName);
    }

    private ArrayList<Socket> socketList = new ArrayList<>();
    private boolean stopServer = false;
    private int port = 4004;


    //waits for clientSocket's answer and launch in separate thread "meetClient"
    private void launchServer()
    {
        try {
            ServerSocket server = new ServerSocket(port);
            sendMessage("server launched at port "+port+". Its IP is "+server.getInetAddress().getHostAddress());
            while (!stopServer)
            {
                Socket socket = server.accept();
                new Thread(() -> {meetClient(socket);}).start();
                socketList.add(socket);
            }
        }
        catch (IOException ex)
        {
            sendMessage("server met some error "+ex.getMessage());
            ex.printStackTrace();
        }
    }


    private  void meetClient(Socket socket)
    {
        SocketCryptedCommunicator communicator = new SocketCryptedCommunicator(socket);
        if (communicator.createSecureCommunicationLine()) {
            if (canAccess(communicator)) {
                communicator.writeEncryption("granted");
                sendMessage("Access granted to " + communicator.getLogin());
                Date currentDate = new Date();
                while (communicator.getExpireDate().after(currentDate) && !communicator.isStopCommunication()) {
                    try {
                        String someCommand = communicator.readEncryption();
                        communicator.writeEncryption(createResponse(someCommand));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        sendMessage("client doesn't write me");
                    }
                }
                sendMessage(communicator.getLogin() + " disconnected");
            }
            else
            {
                communicator.writeEncryption("denied");
                sendMessage("Access denied!");
            }
        }
    }

    private boolean canAccess(SocketCryptedCommunicator communicator)
    {
        String[] loginAndPassword = communicator.readEncryption().split(" ");
        boolean passwordValid = terminalSample.passwords.IsPasswordValid(loginAndPassword[0],loginAndPassword[1]);
        boolean pairIssNotExpired = terminalSample.passwords.loginHasNotExpired(loginAndPassword[0]);
        communicator.setLogin(loginAndPassword[0]);
        communicator.setExpireDate(terminalSample.passwords.getExpireDate(loginAndPassword[0]));
        communicator.setAccessLevel(terminalSample.passwords.getAccessLevel(loginAndPassword[0]));
        return (passwordValid && pairIssNotExpired);
    }

    //TODO divide commands handler into separete pieces in Terminal style
    //Here you'll have several 'devices' like vacuum, discharge, console (all that is in Chooser.fxml in client)
    //First word will be a 'device' command
    //TODO make packages

    //Fixme vacuumResponse. make methods for all?
    private String createResponse(String request)
    {
        String response = "";
        if (request.startsWith("vac"))
        {

            if (request.contains("nocom"))
            {
                response = System.currentTimeMillis()+" ";
                System.out.println(bypass.isOpened);
                System.out.println(gateControl1.isGateOpened());
                String columnData = booleanToString(bypass.isOpened)+booleanToString(gateControl1.isPumpEnabled());
                columnData+=booleanToString(gateControl1.isValveOpened())+booleanToString(gateControl1.isGateOpened());
                columnData+=booleanToString(tmp1.isEnabled())+" ";
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
                terminalSample.launchCommand(request.substring(3), true, 10);
            }

        }

        // createResponse+=""+gateControl.isPumpEnabled()+gateControl.isValveOpened()+gateControl.isGateOpened();
        // createResponse+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        return response;
    }

    //terminal related commands

    @Override
    void initialize() {
       new Thread(new Runnable() {
           @Override
           public void run() {
               launchServer();
           }
       }).start();
        super.initialize();
    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("launch","");
        return super.getCommands();
    }

    @Override
    void chooseTerminalCommand(String[] command) {
        if (command[1].equals("launch"))
            launchServer();
        super.chooseTerminalCommand(command);
    }

    @Override
    void measureAndLog() {

    }

}
