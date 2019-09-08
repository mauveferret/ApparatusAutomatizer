package ru.mauveferret;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;

class Communicator extends Device{

    public Communicator(String fileName, Socket socket) {
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
                sendMessage("Access granted to " + login);
                while (expireDate.after(new Date()) && !stopCommunication) {
                    terminalSample.launchCommand(secureReadFromClient(), false);
                }
                sendMessage(login + " was disconnected");
            }
            else
            {
                sendMessage("Access denied!");
            }
        }
        else
        {
            sendMessage("WTF");
        }
    }

    private boolean createSecureCommunicationLine()
    {
        try {
            long t1 = System.nanoTime();
            serverKeyPair = rsa.generateKeyPair();
            long t2 = System.nanoTime();
            System.out.println("pair generation, ns " + (t2 - t1));
            out.write(rsa.publicKeyToString(serverKeyPair.getPublic()) + "\n");
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
        PasswordManager passwordManager = new PasswordManager();
        String[] loginAndPassword = secureReadFromClient().split(" ");
        boolean passworIsValid = passwordManager.IsPasswordValid(loginAndPassword[0],loginAndPassword[1]);
        boolean pairIssNotExpired = passwordManager.userHasAccess(loginAndPassword[0]);
        login = loginAndPassword[0];
        expireDate = passwordManager.getExpireDate(loginAndPassword[0]);
        accessType = 10;
        return (passworIsValid && pairIssNotExpired);
    }


    private void secureWriteToClient(String message)
    {
        try {
            long t1 = System.nanoTime();
            out.write(rsa.encrypt(message,clientPublicKey)+"\n");
            long t2 = System.nanoTime();
            System.out.println(message+" time:"+(t2-t1));
        }
        catch (Exception e)
        {
            stopCommunication = true;
            sendMessage("strange things happened");
        }

    }

    private String secureReadFromClient()
    {
        try
        {
            long t1 = System.nanoTime();
            String message  = rsa.decrypt(in.readLine(), serverKeyPair.getPrivate());
            long t2 = System.nanoTime();
            System.out.println(message+" time:"+(t2-t1));
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
