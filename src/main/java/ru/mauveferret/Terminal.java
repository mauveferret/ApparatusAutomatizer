package ru.mauveferret;

import java.util.Scanner;
import java.util.TreeMap;

/*
needs to send some command to its owner through LaunchCommand
also realises help command
 */
public class Terminal extends Device {

    //key == deviceCommand, value == device object
    private TreeMap<String, Device> commandMap = new TreeMap<>();
    //key == device name, value == device object
    private TreeMap<String, Device> deviceMap = new TreeMap<>();


    public Terminal(String path) {
        super(path);
        measureAndLog();
    }

    //Getters

    TreeMap<String, Device> getCommandMap() {
        return commandMap;
    }

    public TreeMap<String, Device> getDeviceMap() {
        return deviceMap;
    }

    Device getDevice(String deviceName)
    {
        return deviceMap.get(deviceName);
    }

    //commands


    void addDevice(Device someDevice) {

        String deviceName = someDevice.config.deviceName;
        String deviceCommand = someDevice.config.deviceCommand;

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
            someDevice.terminalSample = this;
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
            TreeMap<String,String> description =deviceMap.get(s).getCommands();

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

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        while (!command.equals("exit"))
        {
            launchCommand(command,false);
            command = scanner.nextLine();
        }
        sendMessage("Goodbye, my Lord.");
        for (Thread thr: Thread.getAllStackTraces().keySet())
        {
            thr.interrupt();
        }
    }


    //for commandline

    void launchCommand(String command, final boolean silentMode)  {

        final String[] commandArray = commandToStringArray(command);
        final String internalCommand = command;
        if (commandMap.containsKey(commandArray[0]))
        {
            Thread commandThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    commandMap.get(commandArray[0]).runCommand(Terminal.this, internalCommand);
                   // TODO silentmode
                }
            }
            );
            commandThread.setName(command);
            commandThread.start();
        }
        else
        {
            sendMessage("Command \""+commandArray[0]+"\" not found.");
        }
    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("help", "this page");
        // commands.put("exit", "stops program");
        commands.put("threads","");
        return super.getCommands();
    }

    @Override
    void chooseTerminalCommand(String[] command) {
        switch (command[1]) {
            case "help":
                showHelp();
                break;

            case "threads":
            {
                for (Thread thread: Thread.getAllStackTraces().keySet()) System.out.println(thread.getName()+" "+thread.getPriority()+" "+thread.isAlive());
            }
            break;
        }
        super.chooseTerminalCommand(command);
    }

    @Override
    void measureAndLog() {

    }
}
