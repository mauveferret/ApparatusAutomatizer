package ru.mauveferret.Vacuum;

import ru.mauveferret.SerialUnit;

import java.util.HashMap;
import java.util.TreeMap;

public abstract class TMP extends SerialUnit {

    protected TMP(String fileName) {
        super(fileName);
        isEnabled = false;
        isControlOn = false;
    }


    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String,String> initializeData)
    {

        for (String someDevice: config.devices)
        {
            try {
                try {
                    for (String dataType: initializeData.keySet())
                    {
                        switch (dataType)
                        {
                            case "temperature" : {
                               String temp =  initializeData.get(dataType).split(" ")[1];
                                temperature = Integer.parseInt(temp.replaceAll(",","."));
                            }
                            break;
                            case "frequency" : {
                                String freq =  initializeData.get(dataType).split(" ")[1];
                                frequency = Integer.parseInt(freq.replaceAll(",","."));
                            }
                            break;
                            case "voltage" : {
                                String volt =  initializeData.get(dataType).split(" ")[1];
                                voltage = Double.parseDouble(volt.replaceAll(",","."));
                            }
                            break;
                            case "current" : {
                                String curr =  initializeData.get(dataType).split(" ")[1];
                                current = Double.parseDouble(curr.replaceAll(",","."));
                            }
                            break;
                            case "status" : {
                                String buttonStatus = initializeData.get(dataType).split(" ")[1];
                               isEnabled = (buttonStatus.charAt(0)+"").equals("1");
                               isCoolingOn = (buttonStatus.charAt(1)+"").equals("1");
                               isControlOn = (buttonStatus.charAt(2)+"").equals("1");
                               isStandbyOn = (buttonStatus.charAt(3)+"").equals("1");
                            }
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    sendMessage("12123 "+e.getMessage());
                }

            }
            catch (Exception e)
            {
                sendMessage("Pressure input failed: "+e.getMessage());
            }
        }
    }


    public abstract void measure();


    protected int  temperature = 0;
    protected int frequency = 0;
    protected double voltage = 0;
    protected double current = 0;

    //for logging
    protected String status;
    //shows if the device answer on the requests
    protected boolean deviceIsOn = false;

    protected boolean isEnabled;
    protected boolean isControlOn;
    protected boolean isCoolingOn;
    protected boolean isStandbyOn;


    public int getTemperature() {
        return temperature;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }




    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isControlOn() {
        return isControlOn;
    }

    public boolean isCoolingOn() {
        return isCoolingOn;
    }

    public boolean isStandbyOn() {
        return isStandbyOn;
    }

    @Override
    protected void type() {

    }

    @Override
    protected boolean callDevice() {
        try {
            measure();
            return  deviceIsOn;
        }
        catch (Exception e){
            return  false;
        }
    }



    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("run", "launches the TMP in form $run$");
        commands.put("stop","stops the TMP in form: $stop$");
        commands.put("data","");
        commands.put("measure","");
        commands.put("temperature", "returnes the temperature of the TMP in celsium in form: $temperature$");
        commands.put("frequency", "...");
        commands.put("control","enables or disables control of the TMP  in form control $on/off$");
        return super.getCommands();
    }


    @Override
    protected void measureAndLog() {
        dataLog.createFile(config.dataPath, "time  temperature,C  frequency, Hz   voltage, 0.1V   current,A   status (see manual) ");
        Thread log = new Thread(() -> {
            boolean stop = true;

            while (stop)
            {
                //FIXME in case of loss of the message it stops measuring! Check it
                measure();
                dataLog.write("time "+temperature+" "+frequency+" "+voltage+" "+current+" "+status);
                for (String someDevice: config.devices)
                {
                    switch (someDevice)
                    {
                        case "temperature" : loggerMap.get(someDevice).write("time "+temperature);
                            break;
                        case "frequency" : loggerMap.get(someDevice).write("time "+frequency);
                            break;
                        case "voltage" : loggerMap.get(someDevice).write("time "+voltage);
                            break;
                        case "current" : loggerMap.get(someDevice).write("time "+current);
                            break;
                        case "status" : {
                            String buttonStatus = booleanToString(isEnabled)+""+ booleanToString(isCoolingOn);
                            buttonStatus+=booleanToString(isControlOn)+""+booleanToString(isStandbyOn);
                            loggerMap.get(someDevice).write("time "+buttonStatus);
                        }
                        break;
                    }
                }
                stop = !Thread.currentThread().isInterrupted();
            }

        });
        log.setName(config.name);
        log.start();
    }


}
