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
        this.messageList = messageList;
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


    //device commands

    @Override
    String runCommand(Device device, String someCommand) {
        return null;
    }

    @Override
    HashMap<String, String> getCommands() {
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge number$ ");
        commands.put("calibrate", "makes a calibration of the pirani or cold cathode in form: calibrate $type$");
        return commands;
    }

    private ArrayList<String> messageList = new ArrayList<>();
    @Override
    public void sendMessage(String message) {
        messageList.add(message);
    }
}

