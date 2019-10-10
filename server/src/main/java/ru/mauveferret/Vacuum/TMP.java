package ru.mauveferret.Vacuum;

import ru.mauveferret.SerialDevice;

public class TMP extends SerialDevice {

    protected TMP(String fileName) {
        super(fileName);
    }


    private boolean enabled = false;
    protected int  temperature = 0;
    protected int frequency = 0;
    protected double voltage = 0;
    protected double current = 0;

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



    @Override
    protected void type() {

    }

    @Override
    protected boolean callDevice() {
        return false;
    }

    @Override
    protected void measureAndLog() {

    }
}
