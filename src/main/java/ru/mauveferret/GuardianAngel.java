package ru.mauveferret;

import java.util.HashMap;

public class GuardianAngel extends Device{

    /*
    used to check if the temperature and pressure conditions are comfortable
    for devices.
    In case of emergency it tries to save the devices: closes gates and valves
    works as thread
     */

    Device device;

    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public void run() {
        while (true)
        {
            //?
        }

    }

    @Override
    String runCommand(Device device, String someCommand) {
        return null;
    }

    @Override
    HashMap<String, String> getCommands() {
        return null;
    }

    @Override
    public void sendMessage(String message) {

    }
}
