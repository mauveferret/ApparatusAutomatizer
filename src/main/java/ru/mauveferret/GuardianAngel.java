package ru.mauveferret;

public class GuardianAngel extends Thread{

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
}
