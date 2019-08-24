package ru.mauveferret;

import java.util.HashMap;


public class LeyboldTMP extends Device {

    private int  temperature;
    private int frequency;
    private double voltage;
    private double current;

    public LeyboldTMP(String path) {
        super(path);
    }

    @Override
    void info() {


    }


//Getters

    public int getTemperature() {
        return temperature;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }


    //Com port related Commands


    //Device related commands

    @Override
    void analyzeCommand(Device device, String someCommand) {
        someCommand = someCommand.toLowerCase();
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            setReceivedCommand(someCommand);
            setReceivedDevice(device);
            command[1] = replaceAliasByCommand(command[1]);
            //switch
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }
    }

    @Override
    HashMap<String, String> getCommands() {
        commands.put("run", "launches the TMP in form $run$");
        commands.put("stop","stops the TMP in form: $stop$");
        commands.put("temperature", "returnes the temperature of the TMP in celsium in form: $temperature$");
        commands.put("frequency", "...");

        return commands;
    }
}
