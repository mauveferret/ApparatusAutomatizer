package ru.mauveferret;

import jssc.SerialPort;

import java.util.ArrayList;
import java.util.HashMap;

public class ThyracontGauge extends Device {

    private SerialPort serialPort;

    public ThyracontGauge(SerialPort serialPort, String deviceName, String deviceCommand) {
        this.serialPort = serialPort;
        this.messageList = messageList;
        setDeviceName(deviceName);
        setDeviceCommand(deviceCommand);
    }

    @Override
    String runCommand(Device device, String someCommand) {
        return null;
    }

    @Override
    HashMap<String, String> getCommands() {
        commands.put("pressure", "measures pressure in mBar by some gauge in form: pressure $gauge number$ ");
        return commands;
    }

    private ArrayList<String> messageList = new ArrayList<>();
    @Override
    public void sendMessage(String message) {
        messageList.add(message);
    }
}
