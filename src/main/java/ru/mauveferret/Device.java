package ru.mauveferret;

import jssc.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public abstract  class Device extends Thread{

    public  Device(String path)
    {
        importConfigurationFile(path);
    }

    protected Device() {
    }

    //TODO добавлен Thread. МОжно использовать для перманентного измерения давления!!!
   /*
   run ()
   {
  this.getpressure1(this.runcommand(pressure));
   }
    */






    //device's serial port
    SerialPort serialPort;
    //ComPortName
    String port="";
    //used in openComPort to prevent constant error messages printing
    private boolean isReconnectActive = false;
    //used in reconnectmethod to rerun command which cause reconnect
    private String receivedCommand = "";
    private Device receivedDevice;
    //it is used in help
    private String deviceName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
    //some String from which your appeal in Terminal starts with
    private String deviceCommand = deviceName.substring(0,3).toLowerCase();
    //some devices like arduino needs to set some ID
     String id = "001";
    //key == command, value == its desciption for help
    HashMap<String, String> commands = new HashMap<>();
    //key == alias, value == command which is represented by the alias
    private HashMap<String,String> aliases = new HashMap<>();

    //Setters and Getters

    public void setId(String id) {
        this.id = id;
    }

    public boolean isReconnectActive() {
        return isReconnectActive;
    }

    public void setReceivedDevice(Device receivedDevice) {
        this.receivedDevice = receivedDevice;
    }

    public void setReceivedCommand(String receivedCommand) {
        this.receivedCommand = receivedCommand;
    }

    public void setDeviceCommand(String deviceCommand) {
        this.deviceCommand = deviceCommand;
    }

    public String getDeviceCommand() {
        return deviceCommand;
    }

    void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

     String getDeviceName() {
        return deviceName;
    }

    HashMap<String,String> getAliases()
    {
        return aliases;
    }

    boolean commandExists(String command)
    {
        return (commands.containsKey(command)||aliases.containsKey(command));
    }

    // terminal related

    //associates a command with a method the command dedicated to
    void runCommand (Device device, String someCommand)
    {
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
                    sendMessage((addAlias(command[3], command[2])) ? command[2]+" added" : command[2]+" isn't added");
                }
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
          commands.put("alias", "adds alias to the specific command in form: alias $alias$  $command$");
          return commands;
      }

    private boolean addAlias(String alias, String command)
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
        if (canBeAdded) aliases.put(alias,command);
        return canBeAdded;
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
                    switch (confArray[0]) {
                        case "id":
                        {
                            setId(confArray[1]);
                        }
                        break;
                        case "port": {
                            try {
                                serialPort.closePort();
                            } catch (Exception ignored) {
                            }
                            if (doesPortExist(confArray[1])) {
                                port = confArray[1];
                            } else {
                                sendMessage(confArray[1] + " doesn't exist.");
                            }
                            //we use incorrect "", but openPort will chose our "port" as a correct portName.
                            openPort("");
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
                        case "alias": {
                            addAlias(confArray[2], confArray[1]);
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
     String replaceAliasByCommand(String command)
    {
        if (aliases.containsKey(command))
        {
            return aliases.get(command);
        }
        else
            return command;
    }

    String[] commandToStringArray(String command)
    {
        command = command.replaceAll("\\s+"," ").trim();
        //command = (command.charAt(0)==' ') ? command.substring(1) : command;
        String[] commandArray = new String[10];
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

     synchronized void reconnect()
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
