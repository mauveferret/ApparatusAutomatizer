package ru.mauveferret;

import jssc.SerialPort;

import java.util.ArrayList;
import java.util.HashMap;

public class ThyracontGauge extends Device {

    private double pressureColumn1;
    private double pressureColumn2;
    private double pressureVessel;


    public ThyracontGauge(SerialPort serialPort, String deviceName, String deviceCommand) {
        this.serialPort = serialPort;
        setDeviceName(deviceName);
        setDeviceCommand(deviceCommand);
    }

    //Getters

    public double getPressureColumn1() {
        return pressureColumn1;
    }

    public double getPressureColumn2() {
        return pressureColumn2;
    }

    public double getPressureVessel() {
        return pressureVessel;
    }

    //Com port related commands



    //for commandline
    @Override
    void runCommand(Device device, String someCommand) {
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
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

}

