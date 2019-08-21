package ru.mauveferret;

import java.util.HashMap;

public class GateControl extends Device{
    public GateControl(String path) {
        super(path);
    }

    @Override
    void runCommand(Device device, String someCommand) {

    }

    @Override
    HashMap<String, String> getCommands() {
        return null;
    }


    // FIXME а нафига оно нужно, когда есть ардуино? ПРопиши в конфиге и будет тебе счастье!


}
