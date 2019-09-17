package ru.mauveferret;

class TMP extends SerialDevice {
    TMP(String fileName) {
        super(fileName);
    }

    private boolean enabled = false;
    private int  temperature = 0;
    private int frequency = 0;
    private double voltage = 0;
    private double current = 0;

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

    @Override
    void type() {

    }

    @Override
    boolean callDevice() {
        return false;
    }

    @Override
    void measureAndLog() {

    }
}
