package ru.mauveferret;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;

class ServerComunicator extends Device{


    public ServerComunicator(String fileName, Socket socket) {
        super(fileName);
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private RSA rsa = new RSA();
    private Socket socket;
    private KeyPair serverKeyPair;
    private PublicKey clientPublicKey;
    private BufferedReader in;
    private BufferedWriter out;
    private Date expireDate;
    private int accessType;
    private boolean stopCommunication = false;
    private String login;
    @Override
    public synchronized void start() {
        if (createSecureCommunicationLine()) {
            if (canAccess()) {
                writeEncryptionToClient("granted");
                sendMessage("Access granted to " + login);
                int accessLevel = terminalSample.passwords.getAccessLevel(login);
                while (expireDate.after(new Date()) && !stopCommunication) {
                    try {
                        //FIXME it dont wait for read
                        String someCommand = readEncryptionFromClient();
                        terminalSample.launchCommand(someCommand, false, accessLevel);
                    }
                    catch (Exception e)
                    {
                        sendMessage("client doesn't write me");
                    }
                }
                sendMessage(login + " was disconnected");
            }
            else
            {
                writeEncryptionToClient("denied");
                sendMessage("Access denied!");
            }
        }
        else
        {
            sendMessage("WTF");
        }
        try {
            out.close();
            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean createSecureCommunicationLine()
    {
        try {
            long t1 = System.currentTimeMillis();
            serverKeyPair = rsa.generateKeyPair();
            long t2 = System.currentTimeMillis();
            System.out.println("pair generation, ms " + (t2 - t1));
            out.write(rsa.publicKeyToString(serverKeyPair.getPublic()) + "\n");
            out.flush();

            sendMessage("Открытый ключ сервера отправлен пользователю");
            clientPublicKey = rsa.stringToPublicKey(in.readLine());
            sendMessage("Открытый ключ пользователя получен");
            return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            sendMessage("line connection is unstable. Closing...");
            return false;
        }
    }

    private boolean canAccess()
    {
        String[] loginAndPassword = readEncryptionFromClient().split(" ");
        boolean passworIsValid = terminalSample.passwords.IsPasswordValid(loginAndPassword[0],loginAndPassword[1]);
        boolean pairIssNotExpired = terminalSample.passwords.loginHasNotExpired(loginAndPassword[0]);
        login = loginAndPassword[0];
        expireDate = terminalSample.passwords.getExpireDate(loginAndPassword[0]);
        accessType = 10;
        return (passworIsValid && pairIssNotExpired);
    }


    private void writeEncryptionToClient(String message)
    {
        try {
            long t1 = System.currentTimeMillis();
            out.write(rsa.encrypt(message,clientPublicKey)+"\n");
            out.flush();
            long t2 = System.currentTimeMillis();
            System.out.println(message+" time:"+(t2-t1));
        }
        catch (Exception e)
        {
            stopCommunication = true;
            sendMessage("strange things happened");
        }

    }

    private String readEncryptionFromClient()
    {
        try
        {
            String line = in.readLine();
            long t1 = System.currentTimeMillis();
            String message  = rsa.decrypt(line, serverKeyPair.getPrivate());
            long t2 = System.currentTimeMillis();


             System.out.println(message+" readTime, ms :"+(t2-t1));
            return message;
        }
        catch (IOException ex)
        {
            stopCommunication = true;
            sendMessage("connection unstable");
            return "";
        }
    }

    @Override
    void measureAndLog() {

    }
}