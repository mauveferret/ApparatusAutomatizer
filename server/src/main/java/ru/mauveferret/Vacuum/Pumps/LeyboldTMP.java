package ru.mauveferret.Vacuum.Pumps;

import ru.mauveferret.Vacuum.TMP;

import java.util.TreeMap;


public class LeyboldTMP extends TMP {



    private byte[] request = new byte[]{0x02,0x16,0x00,0x00, 0x00,0x00,0x00, 0x00,0x00,0x00, 0x00,0x04, 0x01,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x12};
    private byte[] answer = new byte[24];

    private int  temperature = 0;
    private int frequency = 0;
    private double voltage = 0;
    private double current = 0;
    private boolean enabled = false;


    public LeyboldTMP(String fileName) {
        super(fileName);
        deviceAccessLevel = 6;
    }

    @Override
    protected void type() {

    }

    @Override
    protected boolean callDevice() {
        return false;
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

    public boolean isEnabled() {
        return enabled;
    }

    //Device related commands


    @Override
    protected void chooseTerminalCommand(String[] command) {
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
    protected void measureAndLog() {
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
    protected TreeMap<String, String> getCommands() {
        commands.put("run", "launches the TMP in form $run$");
        commands.put("stop","stops the TMP in form: $stop$");
        commands.put("measure","");
        commands.put("temperature", "returnes the temperature of the TMP in celsium in form: $temperature$");
        commands.put("frequency", "...");
        return super.getCommands();
    }
}
