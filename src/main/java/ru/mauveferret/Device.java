package ru.mauveferret;

import jssc.*;

import javax.sql.rowset.serial.SerialException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;

public abstract  class Device {

    //associates a command with a method the command dedicated to
    abstract String runCommand (Device device, String someCommand);

    //actually not only returns a commands Map, but also forms it
    abstract  HashMap<String, String> getCommands();

    //....???
    abstract public void sendMessage(String message);


    //device serial port
    SerialPort serialPort;
    //used to check it before sending any COM related command
    boolean isPortOpened = false;
    //it is used in help
    private String deviceName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
    //some String from which your appeal in Terminal starts with
    private String deviceCommand = deviceName.substring(0,3).toLowerCase();
    //key == command, value == its desciption for help
    HashMap<String, String> commands = new HashMap<>();
    //key == alias, value == command which is represented by the alias
    private HashMap<String,String> aliases = new HashMap<>();

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

    boolean exist(String command)
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
        command = command.replaceAll("\\s+"," ");
        command = (command.charAt(0)==' ') ? command.substring(1) : command;
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

    String[] showAvailableCOMPorts()
    {
        return SerialPortList.getPortNames();
    }

    boolean openPort(String portName)
    {
        serialPort = new SerialPort(portName);
         try {
                serialPort.closePort();
            } catch (Exception ignored) {
                //if port is opened - we close it, otherwise, Exception happened (ignored)
            }

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
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    sendMessage("Wrong Port number or port is busy");
                    return false;
                }
            }
            else {
                sendMessage("This COM port doesn't exist!");
                return false;
            }
            sendMessage(serialPort.getPortName() + ": opened");
            isPortOpened=true;
            return true;
    }

     boolean ClosePort ()
    {
        try
        {
            serialPort.closePort();
            sendMessage(serialPort.getPortName()+": closed");
            isPortOpened=false;
            return true;
        }
        catch (Exception ex)
        {
            sendMessage(ex.getLocalizedMessage());
            return false;
        }
    }

}
