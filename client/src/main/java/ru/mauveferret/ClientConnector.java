package ru.mauveferret;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

class ClientConnector extends Thread{


    public ClientConnector(String host, int port, LoginWindowController controller) {

        loginWindow = controller;
        try {
            clientSocket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            Platform.runLater((() -> loginWindow.setConnectionStatus("Server is ONLINE")));
        }
        catch (IOException e)
        {
            Platform.runLater((() -> loginWindow.setConnectionStatus("Server is OFFLINE")));
            e.printStackTrace();
        }
    }

    private RSA rsa = new RSA();
    private Socket clientSocket;
    private KeyPair clientKeyPair;
    private PublicKey serverPublicKey;
    private BufferedReader in;
    private BufferedWriter out;
    private LoginWindowController loginWindow;
    //TODO
    private int accessType;
    private boolean stopCommunication = false;
    
    //temp
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    @Override
    public synchronized void start() {

        if (createSecureCommunicationLine()) {
            Platform.runLater((() -> loginWindow.setConnectionStatus("data encryption enabled")));
            Platform.runLater((() ->
                    writeEncryptionToServer(loginWindow.getLogin()+" "+loginWindow.getPassword())));
            if ("granted".equals(readEncryptionFromServer()))
            {
                Platform.runLater((() -> loginWindow.setConnectionStatus("Access GRANTED.")));
                Platform.runLater((() -> loginWindow.closeWindow()));
                Platform.runLater((() ->
                {
                    try {
                        Stage windowsChooser = new Stage();
                        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Chooser.fxml"));
                        windowsChooser.initStyle(StageStyle.UNDECORATED);
                        windowsChooser.setScene(new Scene(root, 800, 430));
                        windowsChooser.show();
                    }
                    catch (Exception e)
                    {
                        System.out.println("shit shit");
                    }
                }));

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
                Platform.runLater((() -> loginWindow.setConnectionStatus("Access DENIED.")));
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


     void sendMessage(String message)
    {
        System.out.println(message);
    }

}
