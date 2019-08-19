package ru.mauveferret;

import jssc.*;

import java.util.Arrays;
import java.util.HashMap;

public abstract  class Device extends Thread{

    //TODO добавлен Thread. МОжно использовать для перманентного измерения давления!!!
   /*
   run ()
   {
  this.getpressure1(this.runcommand(pressure));
   }
    */

    //associates a command with a method the command dedicated to
    abstract void runCommand (Device device, String someCommand);
    //actually not only returns a commands Map, but also forms it
    abstract  HashMap<String, String> getCommands();


    //device's serial port
    SerialPort serialPort;
    //used in openComPort to prevent constant error messages printing
    private boolean isReconnectActive = false;
    //used in reconnectmethod to rerun command which cause reconnect
    private String receivedCommand = "";
    private Device receivedDevice;
    //it is used in help
    private String deviceName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
    //some String from which your appeal in Terminal starts with
    private String deviceCommand = deviceName.substring(0,3).toLowerCase();
    //key == command, value == its desciption for help
    HashMap<String, String> commands = new HashMap<>();
    //key == alias, value == command which is represented by the alias
    private HashMap<String,String> aliases = new HashMap<>();

    //Setters and Getters

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

    boolean addAlias(String alias, String command)
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

    synchronized String[] showAvailableCOMPorts()
    {
        return SerialPortList.getPortNames();
    }

    synchronized boolean openPort(String portName)
    {
        try {
            serialPort.closePort();
        } catch (Exception ignored) {
            //if port is opened - we close it, otherwise, Exception happened (ignored)
        }
        //System.out.println(Thread.currentThread().getName());
        serialPort = new SerialPort(portName);
        String[] portList = SerialPortList.getPortNames();
        boolean comPortExist = false;
        //check if the portName exist
        for (String s : portList)
            if (portName.equals(s)) {
                comPortExist = true;
                break;
            }

        if (comPortExist)
        {
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                //!--it shouldn't exist in extands method!
                //don't know why, but arduino  need these 2000
                //Thread.sleep(2000);
            } catch (Exception ex) {
                sendMessage("Port is busy");
                return false;
            }
        }
        else {
            if (!isReconnectActive)
                sendMessage("This COM port doesn't exist!");
            return false;
        }
        sendMessage(serialPort.getPortName() + " is opened");
        return true;
    }

     synchronized boolean closePort()
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

    public void sendMessage(String message) {
        System.out.println(message);

        //TODO logging to file with time labels
    }

}
