package ru.mauveferret;

import java.util.ArrayList;
import java.util.TreeMap;

public class Config {


    public Config(TreeMap<String, String> parameters) {
        this.parameters = parameters;
    }

    private String deviceID;
    private String deviceName;
    private String devicePort;
    private String deviceLaunchCommand;

    private TreeMap<String,String> parameters = new TreeMap<>();

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(String devicePort) {
        this.devicePort = devicePort;
    }

    public String getDeviceLaunchCommand() {
        return deviceLaunchCommand;
    }

    public void setDeviceLaunchCommand(String deviceLaunchCommand) {
        this.deviceLaunchCommand = deviceLaunchCommand;
    }

    public TreeMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(TreeMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
