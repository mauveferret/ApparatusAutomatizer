package ru.mauveferret;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;

public class SocketCryptedCommunicator {

    private int port;
    private String host;
    private Socket socket;
    private String deviceType;  //either "server" or "client"
    private RSA rsa = new RSA(); //asymmetric encryption protocol
    private KeyPair deviceKeyPair;
    private PublicKey otherDevicePublicKey;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean stopCommunication = false;
    private boolean isConnected=false;
    //account parameters for the current session
    private int timeout = 100000;

    private int accessLevel = 0;
    private Date expireDate;
    private String Login;



    //for clients
    SocketCryptedCommunicator(String host, int port) {
        deviceType = "client";
        this.port = port;
        this.host = host;

    }

    //for servers
    SocketCryptedCommunicator(Socket socket) {
        this.socket = socket;
        deviceType = "server";
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            isConnected = true;
        }
        catch (IOException e)
        {
            isConnected = false;
        }
    }

    //Getters and Setters


    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public boolean isStopCommunication() {
        return stopCommunication;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getLogin() {
        return Login;
    }

    public void setLogin(String login) {
        Login = login;
    }

    //Socket methods


    boolean connectToServer()
    {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(timeout);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            isConnected = true;
            return  true;
        }
        catch (IOException e)
        {
            isConnected = false;
            return  false;
        }
    }

    void stopCommunication()
    {
        try {
            writeEncryption("stop");
            stopCommunication = true;
            in.close();
            out.close();
            socket.close();
            isConnected = false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean createSecureCommunicationLine()
    {
        /* take as a rule:
           firstly client sends its public key, server takes it and sends its own punlic key
         */
        try {
            long t1 = System.nanoTime();
            deviceKeyPair = rsa.generateKeyPair();
            long t2 = System.nanoTime();
            System.out.println("TEST pair generation, ns " + (t2 - t1));
            if (deviceType.equals("server"))
            {
                otherDevicePublicKey = rsa.stringToPublicKey(in.readLine());
                sendMessage("Public key was got");
                out.write(rsa.publicKeyToString(deviceKeyPair.getPublic()) + "\n");
                out.flush();
                sendMessage("Public key is sent");
            }
            else if (deviceType.equals("client"))
            {
                out.write(rsa.publicKeyToString(deviceKeyPair.getPublic()) + "\n");
                out.flush();
                sendMessage("Public key is sent");
                otherDevicePublicKey = rsa.stringToPublicKey(in.readLine());
                sendMessage("Public key was got");
            }
            return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            sendMessage("line connection is unstable. Closing...");
            stopCommunication();
            return false;
        }
    }

    //only for clients, server shouldn't speak without permission!
    public synchronized String makeRequest(String request, boolean useCryptedChannel)
    {

         /*
    TODO make the possibility to either crypt message or send in opened channel
    communicator will  guess the type of the message thorugh first word like
    "crpt" or "open"
     */

        writeEncryption(request);
        return readEncryption();
    }

    //TODO add some HAsh to messages in order to check that the data package is sent correctly

    void writeEncryption(String message)
    {
        try {
            long t1 = System.currentTimeMillis();
            out.write(rsa.encrypt(message, otherDevicePublicKey)+"\n");
            out.flush();
            long t2 = System.currentTimeMillis();
            //System.out.println(message+" time:"+(t2-t1));
        }
        catch (Exception e)
        {
            stopCommunication = true;
            sendMessage("strange things happened");
        }

    }

     String readEncryption()
    {
        try
        {
            long t1 = System.currentTimeMillis();
            String message  = rsa.decrypt(in.readLine(), deviceKeyPair.getPrivate());
            long t2 = System.currentTimeMillis();
            //System.out.println(message+" time:"+(t2-t1));
            return message;
        }
        catch (IOException ex)
        {
            stopCommunication = true;
            sendMessage("connection unstable");
            return "";
        }
    }


    private void sendMessage(String message)
    {
        System.out.println(message);
    }
}
