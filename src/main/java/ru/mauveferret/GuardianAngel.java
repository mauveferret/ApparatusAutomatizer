package ru.mauveferret;

import java.util.HashMap;
import java.util.TreeMap;

public class GuardianAngel extends Device {

    /*
    used to check if the temperature and pressure conditions are comfortable
    for devices.
    In case of emergency it tries to save the devices: closes gates and valves
    works as thread
     */

    private boolean continueChecking = true;


    //TODO Command interpretator


    //Getters and Setters

    private void setContinueChecking(boolean continueChecking) {
        this.continueChecking = continueChecking;
    }


    //command related methods

    @Override
    public void run() {
        while (true) {

            // FIXME so...?
        }

    }

    public GuardianAngel(String path) {
        super(path);
    }

    @Override
    void log() {

    }

    private void startCheckingPressure(Device device) {
        while (continueChecking) {
            //TODO very dangerous, what if device is not a termonal?
            Terminal terminal = (Terminal) device;
            ThyracontGauge gauge = (ThyracontGauge) ((Terminal) device).getDevice("gauge");
            double pressureColumn1 = gauge.getPressureColumn1();
            double pressureColumn2 = gauge.getPressureColumn2();
            double pressureVessel = gauge.getPressureVessel();
            Arduino arduino = (Arduino) ((Terminal) device).getDevice("arduino");
            double gate1 = arduino.getAnalogPinsRead()[3];

            //if pressure difference is too much or pressureVessel to much -> close gate
            if ((Math.abs(pressureColumn1 - pressureVessel) > 10 || pressureVessel > 10) && gate1 > 2.5) {
                terminal.runCommand(terminal, "arduino dwrite 8 0");
            }

        }
    }

    //for commandline

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("start", "");
        commands.put("pause", "");

        return commands;
    }

    @Override
    void chooseCommand(String[] command) {

        super.chooseCommand(command);
        switch (command[1]) {
            case ("start"): {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startCheckingPressure(getReceivedDevice());
                    }
                }
                ).start(); //FIXME received device?!
            }
            break;
            case ("pause"): {
                setContinueChecking(false);
            }
        }
    }
}
