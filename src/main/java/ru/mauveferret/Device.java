package ru.mauveferret;

import java.util.ArrayList;
import java.util.HashMap;

public abstract  class Device {

    abstract String RunCommand (String someCommand);

    abstract  HashMap<String, String> getCommands();

    abstract String GetDeviceName();

    abstract public void SendMessage(String message);

    public HashMap<String, String> commands = new HashMap<>();
    //key == alias, value == command
    public HashMap<String,String> aliases = new HashMap<>();

    public boolean AddAlias(String alias, String command)
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

    public String replaceAliasByCommand(String alias)
    {
        if (aliases.containsKey(alias))
            return aliases.get(alias);
        else
            return alias;
    }

    public HashMap<String,String> getAliases()
    {
        return aliases;
    }

    public String[] CommandToStringArray(String command)
    {
        command = command.replaceAll("\\s+"," ");
        command = (command.charAt(0)==' ') ? command.substring(1) : command;
        String[] commandArray = new String[10];
        for (int i=0; i<commandArray.length;i++) commandArray[i]="";
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
