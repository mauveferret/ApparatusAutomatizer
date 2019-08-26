package ru.mauveferret;


import java.util.HashMap;
import java.util.TreeMap;

class ThyracontGauge extends SerialDevice {

    private double pressureColumn1;
    private double pressureColumn2;
    private double pressureVessel;

    ThyracontGauge(String path) {
        super(path);
    }

    @Override
    void log() {

    }

//Getters

    double getPressureColumn1() {
        return pressureColumn1;
    }

    double getPressureColumn2() {
        return pressureColumn2;
    }

    double getPressureVessel() {
        return pressureVessel;
    }

    //gauge related commands


    synchronized double measure()
    {

        return 78;
    }

    //for commandline


    @Override
    void chooseCommand(String[] command) {
        super.chooseCommand(command);
    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge number$ ");
        commands.put("calibrate", "makes a calibration of the pirani or cold cathode in form: calibrate $type$");
        return super.getCommands();
    }
}

