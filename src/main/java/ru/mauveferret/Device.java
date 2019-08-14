package ru.mauveferret;

import java.util.Arrays;
import java.util.HashMap;

public abstract  class Device {

    //associates a command with a method the command dedicated to
    abstract String runCommand (Device device, String someCommand);

    //actually not only returns a commands Map, but also forms it
    abstract  HashMap<String, String> getCommands();

    //....???
    abstract public void sendMessage(String message);

    //it is used in help
    private  String deviceName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
    //some String from which your appeal in Terminal starts with
    private String deviceCommand = deviceName.substring(0,3);
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
}
