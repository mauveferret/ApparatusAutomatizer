package ru.mauveferret;

import java.util.HashMap;

public class LeyboldTMP extends Device {

    private int  temperature;
    private int frequency;
    private double voltage;
    private double current;

    public LeyboldTMP(){}
    public LeyboldTMP(String comPortName) {
        openPort(comPortName);
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
    String runCommand(Device device, String someCommand) {
        return null;
    }

    @Override
    HashMap<String, String> getCommands() {
        commands.put("run", "launches the TMP in form $run$");
        commands.put("stop","stops the TMP in form: $stop$");
        commands.put("temperature", "returnes the temperature of the TMP in celsium in form: $temperature$");
        commands.put("frequency", "...");

        return commands;
    }

    @Override
    public void sendMessage(String message) {

    }
}
