package ru.mauveferret;

import java.util.TreeMap;


public class LeyboldTMP extends SerialDevice {

    private int  temperature = 0;
    private int frequency = 0;
    private double voltage = 0;
    private double current = 0;
    private boolean enabled = false;

    LeyboldTMP(String fileName) {
        super(fileName);
        deviceAccessLevel = 6;
    }

    @Override
    void type() {

    }

    @Override
    boolean callDevice() {
        return false;
    }


//Getters

    int getTemperature() {
        return temperature;
    }

    int getFrequency() {
        return frequency;
    }

    double getVoltage() {
        return voltage;
    }

    double getCurrent() {
        return current;
    }

    boolean isEnabled() {
        return enabled;
    }

    //Device related commands


    @Override
    void chooseTerminalCommand(String[] command) {
        switch (command[1])
        {
            case "run": activate(true);
            break;
            case "stop": activate(false);
            break;
            case "measure": measure();
            break;
        }
        super.chooseTerminalCommand(command);
    }

    private void activate(boolean enable) {

        //TODO TMP

    }

    private void measure()
    {

        System.out.println(readMessage("\n"));
    }

    //terminal related

    @Override
    void measureAndLog() {
        dataLog.createFile(config.dataPath, "time  temperature,C  frequency, Hz   voltage, 0.1V   current,A ");
        Thread log = new Thread(() -> {
            boolean stop = true;
            while (stop)
            {
                //measure();
                dataLog.write("time "+temperature+" "+frequency+" "+voltage+" "+current);
                stop = Thread.currentThread().isInterrupted();
            }
        });
        log.setName(config.deviceName);
        log.start();

    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("run", "launches the TMP in form $run$");
        commands.put("stop","stops the TMP in form: $stop$");
        commands.put("measure","");
        commands.put("temperature", "returnes the temperature of the TMP in celsium in form: $temperature$");
        commands.put("frequency", "...");
        return super.getCommands();
    }
}
