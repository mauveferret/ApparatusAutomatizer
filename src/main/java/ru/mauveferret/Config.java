package ru.mauveferret;

import java.util.ArrayList;
import java.util.TreeMap;

public class Config {




    private String deviceID="ID isn't set";
    private String deviceName = "name isn't set";
    private String devicePort = "port isn't set";
    private String deviceType = "type isn't set";

//TODO перенеси все параметры сюда! заодно решишь проблемы с инициализацией!


    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;

    }

    private String deviceCommand;
   // private String logPath;


    private TreeMap<String,String> parameters = new TreeMap<>();

    //public void setLogPath(String logPath) {
       // this.logPath = logPath;
  //  }

    //public String getLogPath() {
     //   return logPath;
   // }

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
        String info = "\n";
        String line = "\n--------------------\n";
        info+="device: "+deviceName+line+"deviceID: "+deviceID+line+"command: "+deviceCommand+line;
        info+="device COM port: "+devicePort+line;
        for (String str: parameters.keySet())
            info+=str+" "+parameters.get(str)+line;
        return info;
    }


}
