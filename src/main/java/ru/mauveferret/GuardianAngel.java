package ru.mauveferret;

import java.util.HashMap;

public class GuardianAngel extends Device{

    /*
    used to check if the temperature and pressure conditions are comfortable
    for devices.
    In case of emergency it tries to save the devices: closes gates and valves
    works as thread
     */


    GuardianAngel(String angelCommand) {
        setDeviceCommand(angelCommand);
        setDeviceName("angel");
    }


    //command related methods

    @Override
    public void run() {
        while (true)
        {
            // FIXME so...?
        }

    }

    //for commandline

    @Override
    HashMap<String, String> getCommands() {
        return null;
    }

    @Override
    void runCommand(Device device, String someCommand) {
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            command[1] = replaceAliasByCommand(command[1]);
            //switch
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }
    }
}
