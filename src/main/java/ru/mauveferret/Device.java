package ru.mauveferret;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

abstract  class Device extends Logger{

    Device(String path)
    {
        getCommands();
        importConfigurationFile(path);
        measureAndLog();
    }

    Device(){}


    // some config data

    private Config config = new Config();

    //used in reconnect method to rerun command which cause reconnect
    private String receivedCommand = "";
    private Device receivedDevice;
    //it is used in help
    //key == command, value == its desciption for help
    TreeMap<String, String> commands = new TreeMap<>();
    //key == alias, value == command + options which is represented by the alias
    private HashMap<String,String[]> aliases = new HashMap<>();
    //key == alias, value == options.

    //Setters and Getters


    public String getReceivedCommand() {
        return receivedCommand;
    }

    public Device getReceivedDevice() {
        return receivedDevice;
    }

    void setReceivedDevice(Device receivedDevice) {
        this.receivedDevice = receivedDevice;
    }

     void setReceivedCommand(String receivedCommand) {
        this.receivedCommand = receivedCommand;
    }

    Config getConfig() {
        return config;
    }


    HashMap<String,String[]> getAliases()
    {
        return aliases;
    }

    // terminal related

    //associates a command with a method the command dedicated to
    void runCommand(Device device, String someCommand)
    {
        try {
            long t1 = System.currentTimeMillis();
            String[] command = commandToStringArray(someCommand);
            command[1] = command[1].toLowerCase();
            command = replaceAliasByCommand(command);
            if (commands.containsKey(command[1])) {
                setReceivedCommand(someCommand);
                setReceivedDevice(device);
                long t2 = System.currentTimeMillis();
                chooseCommand(command);
                long t3 = System.currentTimeMillis();
                System.out.println(" logic time "+(t2-t1)+" command time: "+(t3-t2));

            } else {
                sendMessage("command \"" + command[1] + "\" doesn't exist");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sendMessage("Unknown error during command "+someCommand);
        }
    }

    void chooseCommand(String[] command)
    {
        switch (command[1]) {

            case "info": sendMessage(config.info());
            break;
            case "alias": sendMessage(addAlias(command) ? command[2]+" added" : command[2]+" isn't added");
            break;
            case  "import": importConfigurationFile(command[2]);
            break;
        }
    }

    //actually not only returns a commands Map, but also forms it
      TreeMap<String, String> getCommands()
      {

          commands.put("alias", "adds alias to the specific command in form: alias $alias$  $command$ $options$");
          commands.put("import", "import parameters from the file if form: import $path$");
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



     private void importConfigurationFile(String path) {
        try {
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
                if (!isComment && !line.equals("")) {
                    String[] command = line.split(" ");
                    switch (command[0].toLowerCase())
                    {
                        case "id": config.setDeviceID(command[1]);
                        break;
                        case "name": config.setDeviceName(command[1]);
                        break;
                        case "command": config.setDeviceCommand(command[1]);
                        break;
                        case "port": config.setDevicePort(command[1]);
                        break;
                        case "logpath": createLogFile(command[1]);
                        break;
                        case "datapath" : createDataFile(command[1]);
                        break;
                        case "type" : config.setDeviceType(command[1]);
                        break;
                        default:
                        {
                            //FIXME
                            //Bug is need to create some options as port Name or message
                            runCommand(receivedDevice, "somecommand"+" "+line+" bug bug");
                        }
                        break;
                    }
                }
            }
            scanner.close();
            sendMessage("~"+path.substring(path.lastIndexOf("\\")) + " imported correctly.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sendMessage(path + " FileNotFound or some commands are incorrect");
            sendMessage(e.getLocalizedMessage());
        }
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

}

