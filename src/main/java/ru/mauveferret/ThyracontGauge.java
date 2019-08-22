package ru.mauveferret;


import java.util.HashMap;

public class ThyracontGauge extends Device {

    private double pressureColumn1;
    private double pressureColumn2;
    private double pressureVessel;

    ThyracontGauge(String path) {
        super(path);
    }

    //Getters

    double getPressureColumn1() {
        return pressureColumn1;
    }

    double getPressureColumn2() {
        return pressureColumn2;
    }

    double getPressureVessel() {
        return pressureVessel;
    }

    //gauge related commands


    synchronized double measure()
    {

        return 78;
    }

    //for commandline
    @Override
    void runCommand(Device device, String someCommand) {
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
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge number$ ");
        commands.put("calibrate", "makes a calibration of the pirani or cold cathode in form: calibrate $type$");
        return commands;
    }

    @Override
    void info() {


    }

}

