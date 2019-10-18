package ru.mauveferret;

import java.util.Scanner;
import java.util.TreeMap;

/*
needs to send some command to its owner through LaunchCommand
also realises help command
 */
public class Terminal extends Unit {

    //key == deviceCommand, value == device object
    private TreeMap<String, Unit> commandMap = new TreeMap<>();
    //key == device name, value == device object
    private TreeMap<String, Unit> deviceMap = new TreeMap<>();
    //FIXME passwords
    PasswordManager passwords = new PasswordManager("passwords");


    //TODO VERY BIG improvement: make device class which keeps all devices, keeos their help and initialize them
    //Terminal would be used only  for commandline, aliases. Password manager has to become separate class
    // entering of the key for PasswordManager must be made from the password manager (separate screen?)

    Terminal(String path) {
        super(path);
        passwords.setKey("12345");
        passwords.initialize();
        unitAccessLevel = 9;
    }

    //Getters

    TreeMap<String, Unit> getCommandMap() {
        return commandMap;
    }

    public Unit getDevice(String deviceName)
    {
        return deviceMap.get(deviceName);
    }

    //commands


    public void addDevice(Unit someUnit) {
        someUnit.terminalSample = Terminal.this;
        new Thread(someUnit::initialize).start();
        String deviceName = someUnit.config.name;
        String deviceCommand = someUnit.config.unitCommand;

        // get commands should not be launched several times!

        if (commandMap.containsKey(deviceCommand))
        {
            sendMessage("Unit command \""+deviceCommand+"\" repeats.");
        }
        else
        {
            commandMap.put(deviceCommand, someUnit);
        }
        if (deviceMap.containsKey(deviceName ))
        {
            sendMessage("Unit name \""+deviceName+"\" repeats.");
        }
        else
        {
            deviceMap.put(deviceName, someUnit);
            someUnit.terminalSample = this;
        }

       // deviceCommandMap.put(someUnit.getDeviceName(), someUnit);
       /* for (String str : someUnit.getCommands().keySet())
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


    //FIXME: it can stop the program?!
    void startNewSession() {
        Scanner scanner = new Scanner(System.in);
        sendMessage("Enter login and password, please.");
        String command = scanner.nextLine();
        String login = "";
        String password = "";
        try {
            login = command.split(" ")[0];
            password = command.split(" ")[1];
        }
        catch (Exception e)
        {
            sendMessage("Enter correct login and password, please.");
        }
        if (passwords.loginExists(login)) {
            if (passwords.IsPasswordValid(login, password))
                if (passwords.loginHasNotExpired(login)) {
                    sendMessage("Access granted. Your level of access is "+passwords.getAccessLevel(login));
                    command = scanner.nextLine();
                    while (!command.equals("exit")) {
                        launchCommand(command, false,passwords.getAccessLevel(login));
                        command = scanner.nextLine();

                    }
                    sendMessage("Goodbye, "+login);
                } else
                {
                    sendMessage("your account has no access to the system!");
                }
            else
            {
                sendMessage("Password is not valid. Try again.");
            }
        }
        else
        {
            sendMessage("login "+login+" doesn't exist.");
        }
        scanner.close();
        if (!stopUnit)
            startNewSession();
    }


    //for commandline

    public String launchCommand(String command, final boolean silentMode, int accessLevel)  {

        final String[] commandArray = commandToStringArray(command);
        final String internalCommand = command;
        if (commandMap.containsKey(commandArray[0]))
        {
            //Thread commandThread = new Thread(() -> {
                commandMap.get(commandArray[0]).runTerminalCommand( internalCommand, accessLevel);
               // TODO silentmode
           // });
           // commandThread.setName(command);
            //commandThread.start();
        }
        else
        {
            sendMessage("Command \""+commandArray[0]+"\" not found.");
        }
        //FIXME
        return "";
    }

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("help", "this page");
        commands.put("threads","");
        commands.put("exit", "stop current terminal session");
        commands.put("terminate", "stop the program");
        commands.put("create","");
        return super.getCommands();
    }

    @Override
    protected void chooseTerminalCommand(String[] command) {
        switch (command[1]) {
            case "help":
                showHelp();
                break;

            case "threads":
            {
                for (Thread thread: Thread.getAllStackTraces().keySet()) System.out.println(thread.getName()+" "+thread.getPriority()+" "+thread.isAlive());
            }
            break;
            case "terminate": terminate();
            break;
        }
        super.chooseTerminalCommand(command);
    }

    private void terminate()
    {
        for (Thread thr: Thread.getAllStackTraces().keySet())
        {
            thr.interrupt();
        }
        stopUnit = true;
        sendMessage("Goodbye, my Lord.");
        try {
            Thread.sleep(500);
        }
        catch (Exception ignored){}
        System.exit(0);
    }

    @Override
    protected void measureAndLog() {

    }
}
