package ru.mauveferret;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeMap;

public class Server  extends  Device{

    public Server(String fileName) {
        super(fileName);
    }

    public static LinkedList<Socket> socketList = new LinkedList<>(); // список всех нитей


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
                Thread someSocket = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkAuthentificatino(socket);
                        }
                        catch (Exception e)
                        {
                            System.out.println("stranger things");
                            e.printStackTrace();
                        }
                    }
                });
                someSocket.setName(socket.toString());
                someSocket.start();
            }

        }
        catch (IOException ex)
        {
            System.out.println("very strange things");
            ex.printStackTrace();
        }
    }

    private void checkAuthentificatino(Socket socket) throws IOException
    {
        String userName;
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        PasswordManager passwordManager = new PasswordManager();
        RSA rsa = new RSA();
        KeyPair keyPair =rsa.generateKeyPair();
        System.out.println("sefggsegfe32");
        userName = in.readLine();
        System.out.println(userName);
        out.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())+"\n");
        out.flush();
        System.out.println("Открытый ключ отправлен пользователю"+userName);
        PublicKey ClientPublicKey = rsa.bytesToPublicKey( Base64.getDecoder().decode(in.readLine()));
        System.out.println("Открытый ключ пользователя получен");

        String authentification1 = rsa.decrypt(in.readLine(), keyPair.getPrivate());
        System.out.println(authentification1);
        String[] authentification = authentification1.split(" ");
        boolean passworIsValid = passwordManager.IsPasswordValid(authentification[0],authentification[1]);
        boolean pairIssNotExpired = passwordManager.userHasAccess(authentification[0]);
        System.out.println(passworIsValid+" "+pairIssNotExpired);
        Date expireDate = passwordManager.getExpireDate(authentification[0]);
        if (passworIsValid && pairIssNotExpired)
        {
            out.write("passw is good! \n");
            out.flush();
            boolean disconnect = false;
            while (!disconnect && (expireDate.after(new Date())))
            {
                String tr = in.readLine();
               terminalSample.launchCommand(tr, false);
               out.write("");
            }
            sendMessage("disconnected");
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
