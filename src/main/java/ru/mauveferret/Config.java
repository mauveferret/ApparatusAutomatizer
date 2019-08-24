package ru.mauveferret;

import java.util.TreeMap;

public class Config {




    private String deviceID;
    private String deviceName;
    private String devicePort;
    private String deviceCommand;
    private String logPath;

    private TreeMap<String,String> parameters = new TreeMap<>();

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getLogPath() {
        return logPath;
    }

    String getDeviceID() {
        return deviceID;
    }

    void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    String getDeviceName() {
        return deviceName;
    }

    void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    String getDevicePort() {
        return devicePort;
    }

    void setDevicePort(String devicePort) {
        this.devicePort = devicePort;
    }

    String getDeviceCommand() {
        return deviceCommand;
    }

    void setDeviceCommand(String deviceCommand) {
        this.deviceCommand = deviceCommand;
    }

    public TreeMap<String, String> getParameters() {
        return parameters;
    }

    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }

    String info()
    {
        String info = "";
        String line = "\n--------------------\n";
        info+="device: "+deviceName+line+"deviceID: "+deviceID+line+"command: "+deviceCommand+line;
        info+="device COM port: "+devicePort+line;
        for (String str: parameters.keySet())
            info+=str+" "+parameters.get(str)+line;
        return info;
    }


}
