package ru.mauveferret;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeMap;

public class Server  extends  Device{

    Server(String fileName) {
        super(fileName);
        //FIXME
    }

    public static ArrayList<Socket> socketList = new ArrayList<>();


    private boolean stopServer = false;
    private int port = 4004;

    private void launchServer()
    {
        try {
            ServerSocket server = new ServerSocket(port);
            sendMessage("server launched at port "+port);
            while (!stopServer)
            {
                Socket socket = server.accept();
                ServerCommunicator communicator = new ServerCommunicator("clients", socket);
                communicator.terminalSample = terminalSample;
                communicator.start();
                socketList.add(socket);
            }

        }
        catch (IOException ex)
        {
            System.out.println("very strange things");
            ex.printStackTrace();
        }
    }

    @Override
    void initialize() {
        launchServer();
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
