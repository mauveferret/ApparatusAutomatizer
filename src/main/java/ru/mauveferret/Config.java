package ru.mauveferret;

import java.util.TreeMap;

class Config {



    String deviceID="ID isn't set";
    String deviceName = "name isn't set";
    String deviceCommand = "name isn't set";
    String devicePort = "port isn't set";
    String deviceType = "type isn't set";
    String logPath;
    String dataPath;
    String units;
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
        info+="device: "+deviceName+line+"deviceID: "+deviceID+line+"command: "+deviceCommand+line;
        info+="device COM port: "+devicePort+line;
        for (String str: parameters.keySet())
            info+=str+" "+parameters.get(str)+line;
        return info;
    }


}
