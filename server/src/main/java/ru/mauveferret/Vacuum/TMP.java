package ru.mauveferret.Vacuum;

import ru.mauveferret.SerialDevice;

public abstract class TMP extends SerialDevice {

    protected TMP(String fileName) {
        super(fileName);
        temperature = 24;
        frequency = 0;
        voltage = 0;
        current = 0;
        status = "";
        isEnabled = false;
        isControlOn = false;
    }


    public abstract void measure();


    protected int  temperature = 0;
    protected int frequency = 0;
    protected double voltage = 0;
    protected double current = 0;
    //for logging
    protected String status;
    //shows if the device answer on the requests
    protected boolean deviceIsOn = false;

    protected boolean isEnabled;
    protected boolean isControlOn;
    protected boolean isCoolingOn;
    protected boolean isStandbyOn;



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

    public boolean isCoolingOn() {
        return isCoolingOn;
    }

    public boolean isStandbyOn() {
        return isStandbyOn;
    }

    @Override
    protected void type() {

    }

    @Override
    protected boolean callDevice() {
        try {
            measure();
            return  deviceIsOn;
        }
        catch (Exception e){
            return  false;
        }
    }

    @Override
    protected void measureAndLog() {

    }
}
