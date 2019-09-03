package ru.mauveferret;

import java.util.TreeMap;


public class LeyboldTMP extends Device {

    private int  temperature;
    private int frequency;
    private double voltage;
    private double current;

    public LeyboldTMP(String path) {
        super(path);
        measureAndLog();
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


    //Device related commands


    @Override
    void chooseTerminalCommand(String[] command) {
        switch (command[1])
        {
            case "enable": enable();
            case "measure": measure();
            break;
        }
        super.chooseTerminalCommand(command);
    }

    private void enable() {

        //TODO TMP

    }

    private void measure()
    {

    }

    //terminal related

    @Override
    void measureAndLog() {
        Thread log = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stop = true;
                while (stop)
                {
                    dataLog.write("time "+temperature+" "+frequency+" "+voltage+" "+current);
                    stop = Thread.currentThread().isInterrupted();
                }
            }
        });
        log.setName(config.deviceName);
        log.start();

    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("enable", "launches the TMP in form $run$");
        commands.put("stop","stops the TMP in form: $stop$");
        commands.put("measure","");
        commands.put("temperature", "returnes the temperature of the TMP in celsium in form: $temperature$");
        commands.put("frequency", "...");
        return super.getCommands();
    }
}
