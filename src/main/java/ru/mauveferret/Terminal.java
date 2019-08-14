package ru.mauveferret;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Terminal extends Device {


    //needs to send some command to its owner through LaunchCommand
    private HashMap<Device, HashMap<String, String>> commandMap = new HashMap<>();
    //key - device name, value - its object
    private HashMap<String, Device> deviceMap = new HashMap<>();
    //key == deviceCommand, value == device
    private HashMap<String,Device> deviceCommandMap= new HashMap<>();

    private  ArrayList<String> help = new ArrayList<>();


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
            while (reader.ready()) launchCommand(reader.readLine(), true);
            reader.close();
        }
        catch (Exception e)
        {
            sendMessage("File not Found");
        }
    }

    void AddDevice(Device someDevice) {
        commandMap.put(someDevice, someDevice.getCommands());
        deviceMap.put(someDevice.getDeviceName(),someDevice);
        deviceCommandMap.put(someDevice.getDeviceName(), someDevice);
       /* for (String str : someDevice.getCommands().keySet())
        {
            if (!commandSet.add(str))
                sendMessage("WARNING: Command "+str +" is used by several devices");
        }
        */

        help.add("_____________"+someDevice.getDeviceName()+"_________");
        for (String s : someDevice.getCommands().keySet())
        {
            help.add(s+"   "+someDevice.getAliases().get(s)+" "+someDevice.getCommands().get(s));
        }
    }



    public Device getDevice(String deviceName)
    {
        return deviceMap.get(deviceName);
    }

    void launchCommand(String command, boolean silentMode)  {


            for (Device device : commandMap.keySet())
                for (String deviceCommand : commandMap.get(device).keySet())
                    //if (CommandToStringArray(command)[0].equals(deviceCommand))
                        if (silentMode)
                        {
                            device.runCommand(Terminal.this,command);
                            break;
                        }
                        else
                        {
                            sendMessage(device.runCommand(Terminal.this, command));
                            break;
                        }
    }


    //doesn't return SendMessage and it is printing!!!
    @Override
    String runCommand(Device device, String someCommand) {
        String[] command = commandToStringArray(someCommand);
        try {
            switch (command[0])
            {
                case "help":
                    ShowHelp();
                    break;
                case "load":
                    LoadCommandsFromFile(command[1]);
                    break;

                case "wait":
                    Wait(Integer.parseInt(command[1]));
                    break;
            }
        }
        catch (Exception e)
        {
            sendMessage(e.getLocalizedMessage());
        }
        return "";
    }

    @Override
    public void sendMessage(String message) {
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


    private void ShowHelp()
    {
        for (String s : help) sendMessage(s);
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
            sendMessage(e.getLocalizedMessage());
        }
    }
}
