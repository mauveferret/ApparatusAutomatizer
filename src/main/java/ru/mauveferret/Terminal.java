package ru.mauveferret;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/*
needs to send some command to its owner through LaunchCommand
also realises help command
 */
public class Terminal extends Device {


    //key == deviceCommand, value == device object
    private HashMap<String, Device> commandMap = new HashMap<>();
    //key == device name, value == device object
    private HashMap<String, Device> deviceMap = new HashMap<>();

    ArrayList<Thread> threads = new ArrayList<>();

    public Terminal(String loadFromFile, String path)
    {
        //load scrypt
        if (loadFromFile.equals("l")) LoadCommandsFromFile(path);
    }

    //Getters

    public HashMap<String, Device> getCommandMap() {
        return commandMap;
    }

    public HashMap<String, Device> getDeviceMap() {
        return deviceMap;
    }

    public Device getDevice(String deviceName)
    {
        return deviceMap.get(deviceName);
    }

    //commands

    private void LoadCommandsFromFile(String path)
    {
        //TODO needs improvement. not bufferet but scanner?
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

        someDevice.getCommands();

        // get commands should not be launched several times!

        if (commandMap.containsKey(someDevice.getDeviceCommand()))
        {
            sendMessage("Device command \""+someDevice.getDeviceCommand()+"\" repeats.");
        }
        else
        {
            commandMap.put(someDevice.getDeviceCommand(), someDevice);
        }
        if (deviceMap.containsKey(someDevice.getDeviceName()))
        {
            sendMessage("Device name \""+someDevice.getDeviceName()+"\" repeats.");
        }
        else
        {
            deviceMap.put(someDevice.getDeviceName(),someDevice);
        }

       // deviceCommandMap.put(someDevice.getDeviceName(), someDevice);
       /* for (String str : someDevice.getCommands().keySet())
        {
            if (!commandSet.add(str))
                sendMessage("WARNING: Command "+str +" is used by several devices");
        }
        */
    }

    private void showHelp()
    {
        int length = 20;
        String help="";
        for (String s: deviceMap.keySet())
        {
            String star="";
            for (int i=0;i<(length-s.length())/2;i++)
            {
                star+="*";
            }
            help+=star+" "+s+" "+star+"\n";
            HashMap<String,String> description =deviceMap.get(s).getCommands();
            for(String command: description.keySet())
            {
                String str="";
                str+=command+fillStringByZeros(command, 10);
                if (deviceMap.get(s).getAliases().containsValue(command))
                {
                    String alias="";
                    for (String keys: deviceMap.get(s).getAliases().keySet())
                    {
                        if (command.equals(deviceMap.get(s).getAliases().get(keys)))
                        {
                            alias=keys;
                            break;
                        }
                    }
                    str+=alias+fillStringByZeros(alias,10);
                }
                else
                {
                    str+=fillStringByZeros("",10);

                }
                str+=deviceMap.get(s).commands.get(command);
                str+="\n";
                help+=str;
            }
        }
        sendMessage(help);
    }


    private String fillStringByZeros(String value, int stringLength)
    {
        String returnString = "";
        for (int i=0; i<(stringLength-String.valueOf(value).length()); i++) returnString+=" ";
        return  returnString;
    }


    //for commandline

    void launchCommand(String command, final boolean silentMode)  {

        //TODO probably while with Scanner is better here than in main?

        final String[] commandArray = commandToStringArray(command);
        final String internalCommand = command;
        if (commandMap.containsKey(commandArray[0]))
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    threads.add(Thread.currentThread());
                    System.out.println(Thread.currentThread().getName());
                    commandMap.get(commandArray[0]).runCommand(Terminal.this, internalCommand);
                   // TODO silentmode
                }

            }
            ).start();
        }
        else
        {
            sendMessage("Command \""+commandArray[0]+"\" not found.");
        }
    }

    @Override
    HashMap<String, String> getCommands() {
        commands.put("help", "this page");
        commands.put("load", "load command scrypt from the file in form: load $file_path$");
        commands.put("alias", "creates alias for some command in form:  alias  &alias name& $command name$");
       // commands.put("exit", "stops program");
        commands.put("threads","");
        return commands;
    }

    @Override
    void runCommand(Device device, String someCommand) {
        String[] command = commandToStringArray(someCommand);
        try {
            switch (command[1])
            {
                case "help":
                    showHelp();
                    break;
                case "load":
                    LoadCommandsFromFile(command[2]);
                    break;

                case "threads":
                {
                    for (Thread thread: threads) System.out.println(thread.getName()+" "+thread.getPriority()+" "+thread.isAlive());
                }
                break;
            }
        }
        catch (Exception e)
        {
            sendMessage(e.getLocalizedMessage());
        }
    }
}
