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
    //key == alias, value == command which is represented by the alias
    private HashMap<String,String> aliases = new HashMap<>();
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


    HashMap<String,String> getAliases()
    {
        return aliases;
    }

    // terminal related

    //associates a command with a method the command dedicated to
    void runCommand(Device device, String someCommand)
    {
        try {

            String[] command = commandToStringArray(someCommand);
            command[1] = command[1].toLowerCase();
            if (commandExists(command[1])) {
                setReceivedCommand(someCommand);
                setReceivedDevice(device);
                command[1] = replaceAliasByCommand(command[1]);
                chooseCommand(command);
            } else {
                sendMessage("command \"" + command[1] + "\" doesn't exist");
            }
        }
        catch (Exception e)
        {
            sendMessage("Unknown error during command "+someCommand);
        }
    }

    void chooseCommand(String[] command)
    {
        switch (command[1]) {

            case "info": sendMessage(config.info());
            break;
            case "alias":
            {
                String options="";
                for (int i=4;i<command.length;i++) options+=command[i];
                sendMessage((addAlias(command[2], command[3], options)) ?
                        command[2]+" added" : command[2]+" isn't added");
            }
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

      //TODO check how does the alias works
    private boolean addAlias(String alias, String command, String options)
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
        if (canBeAdded) aliases.put(alias,command+" "+options);
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
                            runCommand(receivedDevice, "somecommand"+" "+line);
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
            sendMessage(path + " FileNotFound or some commands ard incorrect");
            sendMessage(e.getLocalizedMessage());
        }
    }
    /*
    used in run method to replace alias by its command
    if the alias exists. If not, returns as it was
     */
     String replaceAliasByCommand(String alias)
    {
        if (aliases.containsKey(alias))
        {
            return aliases.get(alias);
        }
        else
            return alias; //which is a command really
    }

    String[] commandToStringArray(String command)
    {
        command = command.replaceAll("\\s+"," ").trim();
        //command = (command.charAt(0)==' ') ? command.substring(1) : command;
        String[] commandArray = new String[20];
        Arrays.fill(commandArray, "");
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

    boolean commandExists(String command)
    {
        return (commands.containsKey(command)||aliases.containsKey(command));
    }
}

