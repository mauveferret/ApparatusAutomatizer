package ru.mauveferret;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Terminal extends Device {
    public HashMap<String, String> commands = new HashMap<>();
    //key == alias, value == command
    public HashMap<String,String> aliases = new HashMap<>();
    //needs to send some command to its owner through LaunchCommand
    private HashMap<Device, HashMap<String, String>> commandMap = new HashMap<>();

    //key - device name, value - its object
    private HashMap<String, Device> deviceMap = new HashMap<>();
    private  ArrayList<String> help = new ArrayList<>();
    private SortedSet<String> commandSet = new TreeSet<>();


    public Terminal(String loadFromFile, String path)
    {
        //load scrypt
        if (loadFromFile.equals("l")) LoadCommandsFromFile(path);
    }

    public void LoadCommandsFromFile(String path)
    {
        try {

            path = Terminal.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"commands.txt";
            System.out.println(path);
            BufferedReader reader = new BufferedReader(new FileReader(path));
            while (reader.ready()) LaunchCommand(reader.readLine(), true);
            reader.close();
        }
        catch (Exception e)
        {
            SendMessage("File not Found");
        }
    }

    public void AddDevice(Device someDevice) {
        commandMap.put(someDevice, someDevice.getCommands());
        for (String str : someDevice.getCommands().keySet())
        {
            if (!commandSet.add(str))
                SendMessage("WARNING: Command "+str +" is used by several devices");
        }
        deviceMap.put(someDevice.GetDeviceName(),someDevice);

        help.add("_____________"+someDevice.GetDeviceName()+"_________");
        for (String s : someDevice.getCommands().keySet())
        {
            help.add(s+"   "+someDevice.getAliases().get(s)+" "+someDevice.getCommands().get(s));
        }
    }



    public Device GetDevice(String deviceName)
    {
        return deviceMap.get(deviceName);
    }

    public void LaunchCommand(String command, boolean silentMode)  {


            for (Device device : commandMap.keySet())
                for (String deviceCommand : commandMap.get(device).keySet())
                    if (CommandToStringArray(command)[0].equals(deviceCommand))
                        if (silentMode)
                        {
                            device.RunCommand(command);
                            break;
                        }
                        else
                            SendMessage(device.RunCommand(command));
    }


    //doesn't return SendMessage and it is printing!!!
    @Override
    String RunCommand(String someCommand) {
        String[] command = CommandToStringArray(someCommand);
        try {
            switch (command[0])
            {
                case "help":
                    ShowHelp();
                    break;
                case "load":
                    LoadCommandsFromFile(command[1]);
                    break;
                case "alias":
                    AddAlias(command[1], command[2]);
                    break;
                case "wait":
                    Wait(Integer.parseInt(command[1]));
                    break;
            }
        }
        catch (Exception e)
        {
            SendMessage(e.getLocalizedMessage());
        }
        return "";
    }

    @Override
    public void SendMessage(String message) {
        System.out.println(message);
    }

    @Override
    HashMap<String, String> getCommands() {
        commands.put("help", "this page");
        commands.put("load", "load command scrypt from the file in form: load $file_path$");
        commands.put("alias", "creates alias for some command in form:  alias  &alias name& $command name$");
        commands.put("exit", "stops program");
        commands.put("wait","wait for some ms in form: wait $ms$");
        return commands;
    }

    @Override
    String GetDeviceName() {
        return "Terminal";
    }


    private void ShowHelp()
    {
        for (String s : help) SendMessage(s);
    }

    //doesn't work !!!!!!!!!!!
    private void Wait(int ms)
    {
        try
        {
            Thread.currentThread().wait(ms);
        }
        catch (Exception e)
        {
            SendMessage(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean AddAlias(String alias, String command) {
        return super.AddAlias(alias, command);
    }

    @Override
    public String replaceAliasByCommand(String alias) {
        return super.replaceAliasByCommand(alias);
    }

    @Override
    public HashMap<String, String> getAliases() {
        return super.getAliases();
    }

    @Override
    public String[] CommandToStringArray(String command) {
        return super.CommandToStringArray(command);
    }
}
