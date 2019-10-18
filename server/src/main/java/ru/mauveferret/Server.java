package ru.mauveferret;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

public abstract class Server extends Unit {

    public Server(String fileName) {
        super(fileName);
    }

    //TODO
    protected ArrayList<Socket> socketList = new ArrayList<>();
    protected boolean stopServer = false;
    protected int port = 4004;


    protected abstract void communicate(SocketCryptedCommunicator communicator);


    //waits for clientSocket's answer and launch in separate thread "meetClient"
    private void launchServer()
    {
        try {
            ServerSocket server = new ServerSocket(port);
            sendMessage(config.name +" launched at port "+port+". Its IP is "+ server.getInetAddress().getHostName());
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
                        communicate(communicator);
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


    //terminal related commands

    @Override
    protected void initialize() {
        //FIXME is the order correct?
        super.initialize();
        new Thread(() -> launchServer()).start();
    }

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("launch","");
        return super.getCommands();
    }

    @Override
    protected void chooseTerminalCommand(String[] command) {
        if (command[1].equals("launch"))
            launchServer();
        super.chooseTerminalCommand(command);
    }

    @Override
    protected void measureAndLog() {

    }

}
