package ru.mauveferret;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

class SocketCryptedCommunicator {

    /*
    TODO make the possibility to either crypt message or send in opened channel
    communicator will  guess the type of the message thorugh first word like
    "crpt" or "open"
     */

    private int port;
    private String host;
    private Socket clientSocket;
    private RSA rsa = new RSA();
    private KeyPair clientKeyPair;
    private PublicKey serverPublicKey;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean stopCommunication = false;
    int accessLevel;
    String Login;


    public SocketCryptedCommunicator( String host, int port) {
        this.port = port;
        this.host = host;
    }

    boolean connectToServer()
    {
        try {
            clientSocket = new Socket(host, port);
            clientSocket.setSoTimeout(100000);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            return  true;
        }
        catch (IOException e)
        {
            return  false;
        }
    }

    void stopCommunication()
    {
        try {
            writeEncryptionToServer("stop");
            stopCommunication = true;
            in.close();
            out.close();
            clientSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void sendMessage(String message)
    {
        System.out.println(message);
    }

    boolean createSecureCommunicationLine()
    {
        try {
            long t1 = System.nanoTime();
            clientKeyPair = rsa.generateKeyPair();
            long t2 = System.nanoTime();
            System.out.println("pair generation, ns " + (t2 - t1));
            serverPublicKey = rsa.stringToPublicKey(in.readLine());
            sendMessage("Открытый ключ сервера получен");
            out.write(rsa.publicKeyToString(clientKeyPair.getPublic()) + "\n");
            out.flush();
            sendMessage("Открытый ключ пользователя отправлен сервера");
            return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            sendMessage("line connection is unstable. Closing...");
            return false;
        }
    }


    synchronized String makeRequest(String request, boolean cryptedchannel)
    {
        writeEncryptionToServer(request);
        return readEncryptionFromServer();
    }

    void writeEncryptionToServer(String message)
    {
        try {
            long t1 = System.nanoTime();
            out.write(rsa.encrypt(message, serverPublicKey)+"\n");
            out.flush();
            long t2 = System.nanoTime();
            System.out.println(message+" time:"+(t2-t1));
        }
        catch (Exception e)
        {
            stopCommunication = true;
            sendMessage("strange things happened");
        }

    }

     String readEncryptionFromServer()
    {
        try
        {
            long t1 = System.nanoTime();
            String message  = rsa.decrypt(in.readLine(), clientKeyPair.getPrivate());
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

}
