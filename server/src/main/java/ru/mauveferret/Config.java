package ru.mauveferret;

import java.util.ArrayList;
import java.util.TreeMap;

public class Config {

    //like ID or columnNumber
    String unitNumber ="ID isn't set";
    public String name = "name isn't set";
    public String unitCommand = "name isn't set";
    String unitPort = "port isn't set";
    String unitType = "type isn't set";
    int baudRate = 9600;
    String configPath = "";
    String logPath = "";
    public String dataPath = "";
    String units = "";
    //used to add several elements of one device like gauge amount or arduino pins
    public ArrayList<String> devices = new ArrayList<>();
    TreeMap<String,Object> parameters = new TreeMap<>();
    //TODO перенеси все параметры сюда! заодно решишь проблемы с инициализацией!

   // private String logPath;


    //public void setLogPath(String logPath) {
       // this.logPath = logPath;
  //  }

    //public String getLogPath() {
     //   return logPath;
   // }


    public TreeMap<String, Object> getParameters() {
        return parameters;
    }

    public void addParameter(String key, Object value) {
        parameters.put(key, value);
    }

    String info()
    {
        String info = "\n";
        String line = "\n--------------------\n";
        info+="device: "+ name +line+"deviceID: "+ unitNumber +line+"command: "+ unitCommand +line;
        info+="device COM port: "+ unitPort +line;
        for (String str: parameters.keySet())
            info+=str+" "+parameters.get(str)+line;
        return info;
    }


}
