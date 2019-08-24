package ru.mauveferret;

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

    private ArrayList<Thread> threads = new ArrayList<>();

    public Terminal(String path) {

        super(path);
    }


    //Getters

    HashMap<String, Device> getCommandMap() {
        return commandMap;
    }

    public HashMap<String, Device> getDeviceMap() {
        return deviceMap;
    }

    Device getDevice(String deviceName)
    {
        return deviceMap.get(deviceName);
    }

    //commands


    void addDevice(Device someDevice) {

        someDevice.getCommands();
        String deviceName = someDevice.getConfig().getDeviceName();
        String deviceCommand = someDevice.getConfig().getDeviceCommand();

        // get commands should not be launched several times!

        if (commandMap.containsKey(deviceCommand))
        {
            sendMessage("Device command \""+deviceCommand+"\" repeats.");
        }
        else
        {
            commandMap.put(deviceCommand, someDevice);
        }
        if (deviceMap.containsKey(deviceName ))
        {
            sendMessage("Device name \""+deviceName+"\" repeats.");
        }
        else
        {
            deviceMap.put(deviceName,someDevice);
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
                    Thread.currentThread().setName(commandMap.get(commandArray[0]).getName()+" thread");
                    threads.add(Thread.currentThread());
                   // System.out.println(Thread.currentThread().getName());
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
        // commands.put("exit", "stops program");
        commands.put("threads","");
        return super.getCommands();
    }

    @Override
    void chooseCommand(String[] command) {
        switch (command[1]) {
            case "help":
                showHelp();
                break;

            case "threads":
            {
                for (Thread thread: threads) System.out.println(thread.getName()+" "+thread.getPriority()+" "+thread.isAlive());
            }
            break;
        }
        super.chooseCommand(command);
    }
}
