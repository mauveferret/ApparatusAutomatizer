package ru.mauveferret;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public abstract  class Device extends Thread{

    public Device(String fileName)
    {
        getCommands();
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

        //FIXME "Apparatus..." can be used only during development

        String separator = File.separator ;
        path = path.substring(0,path.indexOf("ApparatusAutomatizer")+"ApparatusAutomatizer".length());
        /*
        You need double separator because replacement is a regex and "\" is interpreted incorrect
        So to get real \ thr replacement should look like \\
        FIXME this might cause troubles in systems where line separator is /
         */
        path = path.replaceAll("/", separator+separator);
        path+=separator+"resources"+separator;
        config.name = fileName;
        config.configPath = path+separator+fileName+".txt";
        config.logPath = path+"logs"+separator+fileName+"Log.txt";
        config.dataPath = path+"data"+separator+fileName+"Data.txt";
        messageLog.createFile(config.logPath,"time device message");
        importConfigurationFile();
    }

    /*TODO class with huge Map, which connects some number with error type.
    It will help to send errors to client
     */

    Device(){}

    protected abstract void measureAndLog();
    protected void  initialize()  { measureAndLog(); }

    //disable all proccesses, threads in order to exit program correctly
    boolean stopDevice = false;
    //TODO move to config?!
    //limits access to device
    protected float deviceAccessLevel = 100;
    //keeps terminal object
    protected Terminal terminalSample;
    // some config dat
    public Config config = new Config();
    private Logger messageLog = new Logger(true);
    protected Logger dataLog = new Logger(true);
    protected Thread log;
    //used in reconnect method to rerun command which cause reconnect
     String receivedCommand = "";
     int receivedAccessLevel = 0;
    //it is used in help
    //key == command, value == its desciption for help
    protected TreeMap<String, String> commands = new TreeMap<>();
    //key == alias, value == command + options which is represented by the alias
    private HashMap<String,String[]> aliases = new HashMap<>();
    //key == alias, value == options.

    //Setters and Getters

    String getReceivedCommand() {
        return receivedCommand;
    }

    HashMap<String,String[]> getAliases()
    {
        return aliases;
    }

    // terminal related

    //associates a command with a method the command dedicated to
    public void runTerminalCommand(String someCommand, int accessLevel)
    {
        if (accessLevel>=deviceAccessLevel) {
            try {
                long t1 = System.currentTimeMillis();
                String[] command = commandToStringArray(someCommand);
                command[1] = command[1].toLowerCase();
                command = replaceAliasByCommand(command);
                if (commands.containsKey(command[1])) {
                    receivedCommand = someCommand;
                    receivedAccessLevel = accessLevel;
                    long t2 = System.currentTimeMillis();
                    chooseTerminalCommand(command);
                    long t3 = System.currentTimeMillis();
                    sendMessage("DEV logic time " + (t2 - t1) + " command time: " + (t3 - t2));

                } else {
                    sendMessage("command \"" + command[1] + "\" doesn't exist");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage("some internal error happened" + someCommand);
            }
        }
        else
            sendMessage("Access denied.");
    }

    protected void chooseTerminalCommand(String[] command)
    {
        switch (command[1]) {

            case "info": sendMessage(config.info());
            break;
            case "alias": sendMessage(addAlias(command) ? command[2]+" added" : command[2]+" isn't added");
            break;
            case  "import": importConfigurationFile();
            break;
        }
    }

    private void importConfigurationFile() {
        String path = config.configPath;
        try {
            new File(new File(path).getParent()).mkdirs();
            File configFile = new File(path);
            if (!configFile.exists())
            {
                configFile.createNewFile();
                sendMessage("Config file wasn't created. Please fill it and run \"import\" command");
            }
            boolean isComment = false;
            String line;
            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
                if (line.contains("/*")) isComment = true;
                if (line.contains("*/")) {
                    isComment = false;
                    line = line.substring(line.indexOf("*") + 2);
                }
                if (!(isComment || line.equals("") || line.contains("//"))) {
                   try {
                       chooseImportCommand (line);
                   }
                   catch (Exception e)
                   {
                       sendMessage("Probably, import command options are incorrect "+e.getMessage());
                   }
                }
            }
            scanner.close();
            sendMessage("~"+path.substring(path.lastIndexOf("\\")) + " imported correctly.");
        }
        catch (IOException e)
        {
            sendMessage("File "+path.substring(path.lastIndexOf(File.separator))+" doesn't exist and can't be created.");
        }
    }

    protected void chooseImportCommand(String line)
    {
        String[] command = line.split(" ");
        switch (command[0].toLowerCase())
        {
            case "id": config.deviceID = command[1];
                break;
            case "name": config.name = command[1];
                break;
            case "command": config.deviceCommand = command[1];
                break;
            case "run" : runTerminalCommand( "somecommand"+" "+line+" bug bug", 10);
            break;
            case "devices":
            {
                for (int i=1; i<command.length;i++)
                {
                    try {
                        config.elements.add(Integer.parseInt(command[i]));
                    }
                    catch (Exception e)
                    {
                        sendMessage("Incorrect device number "+command[i]);
                    }
                }
            }
            break;
        }
    }

    //actually not only returns a commands Map, but also forms it
    protected TreeMap<String, String> getCommands()
    {

        commands.put("alias", "adds alias to the specific command in form: alias $alias$  $command$ $options$");
        commands.put("import", "import parameters from the file if form: import");
        commands.put("info", "info");
        return commands;
    }

    private boolean addAlias(String[] command)
    {
        String alias = command[2];
        String someCommand = command[3];
        boolean canBeAdded=true;
        for (String str: aliases.keySet())
        {
            if (alias.equals(str))
            {
                canBeAdded=false;
                sendMessage("alias "+alias+" already exist.");
                break;
            }
        }
        for (String str: commands.keySet())
        {
            if (alias.equals(str))
            {
                canBeAdded=false;
                sendMessage("alias "+alias+" already exist as a command.");
                break;
            }
        }
        if (!commands.containsKey(someCommand))
        {
            canBeAdded=false;
            sendMessage("command "+someCommand+" doesn't exist");
        }

        String[] commandToReplace = new String[command.length-2];
        for (int i = 3; i<command.length; i++)
            commandToReplace[i-2] = command[i];
        if (canBeAdded) aliases.put(alias,commandToReplace);
        return canBeAdded;
    }

    /*
    used in run method to replace alias by its command
    if the alias exists. If not, returns as it was
     */

    private String[] replaceAliasByCommand(String[] command)
    {
        if (aliases.containsKey(command[1]))
        {
            if (command.length>aliases.get(command[1]).length)
            {
                command[1] = aliases.get(command[1])[1];
                return  command;
            }
            else return aliases.get(command[1]);
        }
        else
            return command;
    }

    String[] commandToStringArray(String command)
    {
        command = command.replaceAll(" {2}"," ").trim();
        return  command.split(" ");
    }

    protected void sendMessage(String message) {
        message = System.currentTimeMillis() + " " + message;
        System.out.println(message);
        messageLog.write(message + "\n");
    }

    protected String booleanToString(boolean b) {return (b) ? "1" : "0";}

}

