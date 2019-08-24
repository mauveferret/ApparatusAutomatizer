package ru.mauveferret;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

abstract  class Device extends Thread{

    Device(String path)
    {
        getCommands();
        importConfigurationFile(path);
    }

    Device(){}


    // some config data

    private Config config = new Config();
     FileWriter logger;
    //used in reconnect method to rerun command which cause reconnect
    private String receivedCommand = "";
    private Device receivedDevice;
    //it is used in help
    //key == command, value == its desciption for help
    HashMap<String, String> commands = new HashMap<>();
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
        someCommand = someCommand.toLowerCase();
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            setReceivedCommand(someCommand);
            setReceivedDevice(device);
            command[1] = replaceAliasByCommand(command[1]);
            chooseCommand(command);
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist");
        }
    }

    void chooseCommand(String[] command)
    {
        switch (command[1]) {

            case "info":
            {
                sendMessage(config.info());
            }
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
      HashMap<String, String> getCommands()
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

    boolean commandExists(String command)
    {
        return (commands.containsKey(command)||aliases.containsKey(command));
    }

     private void importConfigurationFile(String path) {
        try {
            boolean isComment = false;
            String line;
            String[] confArray;
            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
                if (line.contains("/*")) isComment = true;
                if (line.contains("*/")) {
                    isComment = false;
                    line = line.substring(line.indexOf("*") + 2);
                }
                if (!isComment && !line.equals("")) {
                    confArray = line.split(" ");
                    switch (confArray[0].toLowerCase()) {
                        case "id": config.setDeviceID(confArray[1]);
                        break;
                        case "name": config.setDeviceName(confArray[1]);
                        break;
                        case "command": config.setDeviceCommand(confArray[1]);
                        break;
                        case "port": config.setDevicePort(confArray[1]);
                        break;
                        case "log":
                        {
                            File file = new File(confArray[1]);
                            file.createNewFile();
                            logger = new FileWriter(file, true);
                        }
                        break;
                        default: runCommand(receivedDevice, config.getDeviceCommand()+" "+line);
                        break;
                    }

                }
            }
            scanner.close();
            sendMessage("~"+path.substring(path.lastIndexOf("\\")) + " imported correctly.");
        }
        catch (Exception e)
        {
            sendMessage(path + " encountered some problem.");
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

    void sendMessage(String message) {
        try {
            message = System.currentTimeMillis() +  " : " + message;
            System.out.println(message);
            logger.write(message+"\n");
            logger.flush();
        }
        catch (IOException ex)
        {
            System.out.println(ex.getLocalizedMessage());
        }
    }

}
