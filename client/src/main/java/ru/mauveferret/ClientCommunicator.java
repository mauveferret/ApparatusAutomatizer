package ru.mauveferret;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

class ClientCommunicator extends Thread{

    public ClientCommunicator(String host, int port) {
       
        try {
            clientSocket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private RSA rsa = new RSA();
    private Socket clientSocket;
    private KeyPair clientKeyPair;
    private PublicKey serverPublicKey;
    private BufferedReader in;
    private BufferedWriter out;
    //TODO
    private int accessType;
    private boolean stopCommunication = false;
    private String login;
    
    //temp
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public synchronized void start() {
        if (createSecureCommunicationLine()) {
            try {
                sendMessage("enter login and password");
                writeEncryptionToServer(reader.readLine());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                sendMessage("suka");
            }
            if ("granted".equals(readEncryptionFromServer()))
            {
                while (!stopCommunication)
                {
                    try {
                        String message = reader.readLine();
                        if (message.equals("stop"))
                            stopCommunication = true;
                        else 
                        writeEncryptionToServer(message);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        stopCommunication = true;
                    }
                }
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

    

    private void writeEncryptionToServer(String message)
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

    private String readEncryptionFromServer()
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


    private void sendMessage(String message)
    {
        System.out.println(message);
    }

}
