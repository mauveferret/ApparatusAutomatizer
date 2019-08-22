package ru.mauveferret;

import jssc.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

abstract  class Device extends Thread{

    Device(String path)
    {
        importConfigurationFile(path);
    }

    Device() {
    }

    // some config data
    abstract void info();

    //device's serial port
    SerialPort serialPort;
    //ComPortName
    private String port="";
    //used in openComPort to prevent constant error messages printing
    private boolean isReconnectActive = false;
    //used in reconnect method to rerun command which cause reconnect
    private String receivedCommand = "";
    private Device receivedDevice;
    //it is used in help
    private String deviceName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
    //some String from which your appeal in Terminal starts with
    private String deviceCommand = deviceName.substring(0,3).toLowerCase();
    //some devices like arduino needs to set some ID
     private String deviceID = "001";
    //key == command, value == its desciption for help
    HashMap<String, String> commands = new HashMap<>();
    //key == alias, value == command which is represented by the alias
    private HashMap<String,String> aliases = new HashMap<>();
    //key == alias, value == options.

    //Setters and Getters

     private void setId(String id) {
        this.deviceID = id;
    }

     String getDeviceID() {
        return deviceID;
    }

    boolean isReconnectActive() {
        return isReconnectActive;
    }

     void setReceivedDevice(Device receivedDevice) {
        this.receivedDevice = receivedDevice;
    }

     void setReceivedCommand(String receivedCommand) {
        this.receivedCommand = receivedCommand;
    }

     private void setDeviceCommand(String deviceCommand) {
        this.deviceCommand = deviceCommand;
    }

    void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    HashMap<String,String> getAliases()
    {
        return aliases;
    }

    // terminal related

    //associates a command with a method the command dedicated to
    void runCommand (Device device, String someCommand)
    {
        someCommand = someCommand.toLowerCase();
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            setReceivedCommand(someCommand);
            setReceivedDevice(device);
            command[1] = replaceAliasByCommand(command[1]);
            switch (command[1]) {
                case "ports": {
                    String message = "Available ports:";
                    String[] s = showAvailableCOMPorts();
                    for (String value : s) message += (value + " ");
                    sendMessage(message);
                }
                break;
                case "setID":
                {
                    if (command[2].equals(""))
                        sendMessage("Enter ID number.");
                    else
                        setId(command[2]);
                }
                case "open": {
                    if (command[2].equals("")) sendMessage("Enter COM port name as an option");
                    //else sendMessage((openPort(command[2])) ? command[2]+" is opened" : command[2]+" isn't opened");
                    else openPort(command[2]);
                }
                break;
                case "close":
                {
                    closePort();
                }
                break;
                case "alias":
                {
                    String options="";
                    for (int i=4;i<command.length;i++) options+=command[i];
                    sendMessage((addAlias(command[2], command[3], options)) ?
                            command[2]+" added" : command[2]+" isn't added");
                }
                break;
                case "info": info();
                break;
            }
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }
    }

    //actually not only returns a commands Map, but also forms it
      HashMap<String, String> getCommands()
      {
          commands.put("ports", "shows available COM port's names");
          commands.put("open", "Open Arduino Port in form: OP $arduino number$ $COM port name$");
          commands.put("close", "close Arduino port");
          commands.put("alias", "adds alias to the specific command in form: alias $alias$  $command$ $options$");
          commands.put("setID","sets ID of the device in form: setID $ID number$");
          commands.put("info", "gives info about the device");
          return commands;
      }

      //TODO check how does the alias works
    private boolean addAlias(String alias, String command, String options)
    {
        boolean canBeAdded=true;
        for (String str: aliases.keySet())
        {
            if (alias.equals(str))
            {
                canBeAdded=false;
                break;
            }
        }
        for (String str: commands.keySet())
        {
            if (alias.equals(str))
            {
                canBeAdded=false;
                break;
            }
        }
        if (!commands.containsKey(command))
        {
            canBeAdded=false;
        }
        if (canBeAdded) aliases.put(alias,command+" "+options);
        return canBeAdded;
    }

    boolean commandExists(String command)
    {
        return (commands.containsKey(command)||aliases.containsKey(command));
    }

    private void importConfigurationFile(String path) {
        try {
            boolean isComment = false;
            String line;
            String[] confArray;
            Scanner scanner = new Scanner(path);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
                if (line.contains("/*")) isComment = true;
                if (line.contains("*/")) {
                    isComment = false;
                    line = line.substring(line.indexOf("*") + 1);
                }
                if (!isComment) {
                    confArray = line.split(" ");
                    switch (confArray[0].toLowerCase()) {
                        case "id":
                        {
                            setId(confArray[1]);
                        }
                        break;
                        case "name": {
                            setDeviceName(confArray[1]);
                        }
                        break;
                        case "command": {
                            setDeviceCommand(confArray[1]);
                        }
                        break;
                        default:
                        {
                            runCommand(receivedDevice, deviceCommand+" "+line);
                        }
                        break;
                    }

                }
            }
            scanner.close();
            sendMessage(path.substring(path.lastIndexOf("\\")) + " imported correctly.");
        }
        catch (Exception e)
        {
            sendMessage(path.substring(path.lastIndexOf("\\")) + "encountered some problem.");
            sendMessage(e.getLocalizedMessage());
        }
    }
    /*
    used in run method to replace alias by its command
    if the alias exists. If not, returns as it was
     */
     String replaceAliasByCommand(String alias)
    {
        if (aliases.containsKey(alias))
        {
            return aliases.get(alias);
        }
        else
            return alias; //which is a command really
    }

    String[] commandToStringArray(String command)
    {
        command = command.replaceAll("\\s+"," ").trim();
        //command = (command.charAt(0)==' ') ? command.substring(1) : command;
        String[] commandArray = new String[20];
        Arrays.fill(commandArray, "");
        int i=0;
        for (char c: command.toCharArray())
        {
            if (c!=' ')
                commandArray[i]+=c;
            else
                i++;
        }
        return commandArray;
    }

    //COM port related commands

    private synchronized String[] showAvailableCOMPorts()
    {
        return SerialPortList.getPortNames();
    }

    private boolean doesPortExist(String portName)
    {
        String[] portList = SerialPortList.getPortNames();
        boolean comPortExist = false;
        //check if the portName exist
        for (String s : portList)
            if (portName.equals(s)) {
                comPortExist = true;
                break;
            }
        return comPortExist;
    }

    private synchronized boolean openPort(String portName)
    {
        try {
            serialPort.closePort();
        } catch (Exception ignored) {}

        if (doesPortExist(portName) || doesPortExist(port))
        {
            if (!doesPortExist(portName)) portName = port;
            try {
                serialPort = new SerialPort(portName);
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                //!--it shouldn't exist in extands method!
                //don't know why, but arduino  need these 2000
                //Thread.sleep(2000);
            } catch (Exception ex) {
                sendMessage("Port "+portName+" is busy");
                return false;
            }
        }
        else {
            if (!isReconnectActive)
                sendMessage(portName+" doesn't exist!");
            return false;
        }
        sendMessage(serialPort.getPortName() + " is opened");
        return true;
    }

     private synchronized boolean closePort()
    {
        try
        {
            serialPort.closePort();
            if (!isReconnectActive) sendMessage(serialPort.getPortName()+" is closed");
            return true;
        }
        catch (SerialPortException ex)
        {
            sendMessage(ex.getLocalizedMessage());
            return false;
        }
    }

    synchronized  String readMessage() throws SerialPortException
    {
        String answer="";
        long startTime = System.currentTimeMillis();
        while (!answer.contains("\n"))
        {
            answer+=(new String(serialPort.readBytes(1)));
            if (System.currentTimeMillis()-startTime>2000)
            {
                launchReconnectInThread();
                break;
            }

        }
        return answer;
    }

    void launchReconnectInThread()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }).start();
    }

    private synchronized void reconnect()
    {
        //TODO manual desactivation, threadlist

        try {
            String comPortName = serialPort.getPortName();
            sendMessage(serialPort.getPortName() + " is lost. Reconnecting...");
            isReconnectActive=true;
            closePort();
            while (!serialPort.isOpened()) {
                openPort(comPortName);
            }
            sendMessage("Reconnected.");
            isReconnectActive=false;
            //rerunning command which caused the reconnection
            runCommand(receivedDevice,receivedCommand);
        }
        catch (NullPointerException e)
        {
            isReconnectActive=false;
            sendMessage("COM port name isn't set.");
        }
    }

    void sendMessage(String message) {
        System.out.println(message);

        //TODO logging to file with time labels
    }

}
