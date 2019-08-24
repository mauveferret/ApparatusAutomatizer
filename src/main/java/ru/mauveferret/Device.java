package ru.mauveferret;

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

    private void log()
    {

    }

    //device's serial port

    //ComPortName

    //used in openComPort to prevent constant error messages printing

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

    public String getReceivedCommand() {
        return receivedCommand;
    }

    public Device getReceivedDevice() {
        return receivedDevice;
    }

    void setReceivedDevice(Device receivedDevice) {
        this.receivedDevice = receivedDevice;
    }

     void setReceivedCommand(String receivedCommand) {
        this.receivedCommand = receivedCommand;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceCommand() {
        return deviceCommand;
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
    void analyzeCommand(Device device, String someCommand)
    {
        someCommand = someCommand.toLowerCase();
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            setReceivedCommand(someCommand);
            setReceivedDevice(device);
            command[1] = replaceAliasByCommand(command[1]);
            runCommand(command);
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }
    }

    void runCommand(String[] command)
    {
        switch (command[1]) {

            case "setID":
            {
                if (command[2].equals(""))
                    sendMessage("Enter ID number.");
                else
                    setId(command[2]);
            }


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
                            analyzeCommand(receivedDevice, deviceCommand+" "+line);
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

    void sendMessage(String message) {
        System.out.println(message);

        //TODO logging to file with time labels
    }

}
