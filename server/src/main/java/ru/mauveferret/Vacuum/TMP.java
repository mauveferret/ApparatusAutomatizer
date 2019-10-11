package ru.mauveferret.Vacuum;

import ru.mauveferret.SerialDevice;

public class TMP extends SerialDevice {

    protected TMP(String fileName) {
        super(fileName);
        temperature = 0;
        frequency = 0;
        voltage = 0;
        current = 0;
        status = "";
        isEnabled = false;
        isControlOn = false;
    }


    protected int  temperature = 0;
    protected int frequency = 0;
    protected double voltage = 0;
    protected double current = 0;
    //for logging
    protected String status;
    protected boolean isEnabled;
    protected boolean isControlOn;



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


    public String getStatus() {
        return status;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isControlOn() {
        return isControlOn;
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
