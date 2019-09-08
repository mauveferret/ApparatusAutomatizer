package ru.mauveferret;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeMap;

public class Server  extends  Device{

    Server(String fileName) {
        super(fileName);
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
                new ServerCommunicator("clients", socket).start();
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
