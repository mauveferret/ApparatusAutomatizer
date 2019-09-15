package ru.mauveferret;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.Random;

class ServerComunicator extends Device{


    ServerComunicator(String fileName, Socket socket) {
        super(fileName);
        this.socket = socket;

        try {
            socket.setSoTimeout(100000);
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

    private Gauge gauge;
    private String gaugeName;
    private GateControl gateControl;
    private  String gateControlName;
    private  LeyboldTMP tmp;
    private  String tmpName;

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
                        if (!someCommand.contains("nocommand"))
                        {
                            System.out.println("comsecommand launched "+someCommand);
                            terminalSample.launchCommand(someCommand, true, accessLevel);
                        }
                        if (someCommand.equals("stop"))
                        {
                            stopCommunication = true;
                            in.close();
                            out.close();
                            socket.close();
                        }
                        else
                            writeEncryptionToClient(response(1));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
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
            e.printStackTrace();
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
            ex.printStackTrace();
            return "";
        }
    }

    String response(int columnNumber)
    {
        String response =""+System.currentTimeMillis();
       // response+=""+gateControl.isPumpEnabled()+gateControl.isValveOpened()+gateControl.isGateOpened();
       // response+=""+tmp.isEnabled()+gauge.pressure[1]+""+gauge.pressure[2];
        response= ((int) (( System.currentTimeMillis())/1000))+" "+(new Random()).nextDouble();
        return response;
    }

    @Override
    void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        try {
            switch (command[0]) {

                case "gauge": gaugeName = command[1];
                break;
                case "gatecontrol": gateControlName = command[1];
                break;
                case "tmp" : tmpName = command[1];
                break;
            }
        }
        catch (Exception e)
        {
            sendMessage("incorrect option.");
            sendMessage(e.getMessage());
        }

    }

    @Override
    void initialize() {
        super.initialize();
        gauge= (ThyracontGauge) (terminalSample.getDevice(gaugeName));
        gateControl = (GateControl) (terminalSample.getDevice(gateControlName));
        tmp = (LeyboldTMP) (terminalSample.getDevice(tmpName));
    }

    @Override
    void measureAndLog() {

    }
}
