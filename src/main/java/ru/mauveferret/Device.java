package ru.mauveferret;

import java.util.ArrayList;
import java.util.HashMap;

public abstract  class Device {


    abstract String RunCommand (String someCommand);

    abstract  HashMap<String, String> getCommands();

    abstract String GetDeviceName();

    abstract public void SendMessage(String message);

    public HashMap<String, String> commands = new HashMap<>();

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
