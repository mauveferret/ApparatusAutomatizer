package ru.mauveferret;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/*
 must be accompanied by special Arduino driver which is made
 by Ivan Sorokin and can be got at plasma physics department of
 the National research Nuclear University "Mephi"
 */

//TODO проверка корректного ввода пинов

public class Arduino extends SerialUnit {

    public Arduino(String fileName) {
        super(fileName);
        unitAccessLevel = 9;
        analogPinsRead = new double[14];
    }




    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String, String> initializeData) {
        for (String someDevice: config.devices)
        {
            String[] digitalpinsfromTHeFile = initializeData.get(someDevice).split(" ");
            try {
                //FIXME write loaded values on the ardino?! or it may cause relay rebooting?
                for (int i=1; i<digitalpinsfromTHeFile.length;i++)
                {
                    digitalPinsWritten[i-1] = digitalpinsfromTHeFile[i].equals("1");
                }
            }
            catch (Exception e)
            {
                sendMessage("12123 "+e.getMessage());
            }
        }
    }

    //arrays with pins status (only for dwrite, aread)
    private boolean[] digitalPinsWritten;
    private double[] analogPinsRead = new double[14];

    //TODO create list with active pin numbers for log in order to save freespace
    private ArrayList<Integer> actualDigitalPins;
    private ArrayList<Integer> actualAnalogPins;

    //Getters and Setters

    public boolean[] getDigitalPinsWritten() {
        return digitalPinsWritten;
    }

    public double[] getAnalogPinsRead() {
        return analogPinsRead;
    }

    //Arduino operations

    synchronized private boolean digitalWrite(String pin, boolean value)
    {
           String message=fillStringByZeros(config.unitNumber+"",3);
           message+="DO"+fillStringByZeros(pin,2)+(value ? 1 : 0);
           writeString(message+fillStringByZeros(""+checkSum(message),3)+"\n");
           String answer = readString("\n");
           if (answer.contains("SETTED"))
           {
               sendMessage(((value) ? "HIGH" : "LOW")+" on pin "+pin+" is set");
               digitalPinsWritten[Integer.parseInt(pin)] = value;
           }
           else
           {
               //if (!isReconnectActive()) sendMessage("ERROR: pin number is incorrect.");
               sendMessage(((value) ? "HIGH" : "LOW")+" on pin "+pin+" isn't set");
           }
           return (answer.contains("SETTED"));
    }

    synchronized private boolean digitalRead(String pin)
    {
            String message = fillStringByZeros(config.unitNumber+"",3);
            message +="DI" + fillStringByZeros(pin, 2);
            writeString(message + fillStringByZeros(""+checkSum(message), 3) + "\n");
            String answer = readString("\n");
            int signal = checkSum(answer.substring(0, answer.length() - 4));
            int checksum = Integer.parseInt(answer.substring(answer.length() - 4, answer.length() - 1));
            if ((signal == checksum) && (!answer.contains("ERROR"))) {
                return answer.substring(7, 8).equals("1");
            } else {
                if (answer.contains("ERROR"))
                    sendMessage("pin " + pin + " doesn't exist");
                else
                    sendMessage("CheckSum is not correct. Check the wires!");
            }
            return false;
    }

    synchronized private boolean analogWrite(String pin, int value)
    {
            String message=fillStringByZeros(config.unitNumber+"",3);
            message+="AO"+fillStringByZeros(pin,2)+fillStringByZeros(""+value*51, 4);
            writeString(message+fillStringByZeros(""+checkSum(message),3)+"\n");
            String answer = readString("\n");
            if (answer.contains("SETTED")) sendMessage(value+" on pin "+pin+" is set");;
            return (answer.contains("SETTED"));
    }

    synchronized private double analogRead(String pin)
    {
            String message = fillStringByZeros(config.unitNumber+"",3);
            message += "AI" + fillStringByZeros(pin, 2);
            writeString(message + fillStringByZeros(""+checkSum(message), 3) + "\n");
            String answer = readString("\n");
           try {
               int signal = checkSum(answer.substring(0, answer.length() - 4));
               int checksum = Integer.parseInt(answer.substring(answer.length() - 4, answer.length() - 1));
               if ((signal == checksum) && (!answer.contains("ERROR"))) {
                   {
                       double value = Integer.parseInt(answer.substring(7, 11)) / 204.6;
                       analogPinsRead[Integer.parseInt(pin)] = value;
                       return value;
                   }
               } else {
                   if (answer.contains("ERROR")) sendMessage("pin " + pin + " doesn't exist");
                   else
                       sendMessage("CheckSum is not correct. Check the wires!");
               }
               return -1;
           }
           catch (Exception e)
           {
               sendMessage(config.name+": "+e.getMessage());
               return  -1;
           }
    }

    //internal Arduino methods (are needed for the driver support)

    private String fillStringByZeros(String value, int stringLength)
    {

        if (value.length()>stringLength) return value;
        StringBuilder returnString = new StringBuilder();
        for (int i=0; i<(stringLength-value.length()); i++) returnString.append("0");
        returnString.append(value);
        return returnString.toString();
    }

    private int checkSum(String message)
    {
        int checkSum=0;
        byte[] b = message.getBytes(StandardCharsets.US_ASCII);
        for (byte value : b) checkSum += value;
        return (checkSum % 256);
    }

    //for commandline

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("dwrite", "Write on Arduino digital port in form: DO $pin number$ $value(0,1)$");
        commands.put("dread", "Read from Arduino digital  port in form: DI $pin number$");
        commands.put("awrite", "Write on Arduino analog port(PWM~) in form: AO $pin number$ $value(0-5)$");
        commands.put("aread", "Read from Arduino analog  port in form: AI $pin number$");
        return super.getCommands();
    }

    @Override
    protected void chooseTerminalCommand(String[] command) {
        super.chooseTerminalCommand(command);
        switch (command[1]) {
            case "dwrite": {
                if (command[2].equals("") || command[3].equals(" "))
                {
                    sendMessage("Enter pin number and value (0,1) as an option");
                }
                else
                {
                    if (command[3].equals("1") || command[3].equals("0"))
                        digitalWrite(command[2], command[3].equals("1"));
                    else
                        sendMessage("Value is incorrect (should be 0 or 1).");
                }
            }
            break;
            case "dread":
                if (command[2].equals(""))
                    sendMessage("Enter pin number as an option");
                else
                    sendMessage("" + digitalRead(command[2]));
            break;
            case "awrite":
                if (command[2].equals("") || command[3].equals(""))
                    sendMessage("Enter pin number and value (0-5) as an option");
                else
                    analogWrite((command[2]), Integer.parseInt(command[3]));
                break;
            case "aread":
                if (command[2].equals(""))
                    sendMessage("Enter pin number as an option");
                else
                    sendMessage("Analog signal on pin "+command[2]+" is  "+ analogRead(command[2]));
                break;
        }

    }

    @Override
    protected void measureAndLog() {
        dataLog.createFile(config.dataPath, "time  digitalPinsStatus analogPinsStatus");
        log = new Thread(() -> {
            boolean stop = false;
            //FIXME you don't have to use while
            while (!stop)
            {
                try {
                    String time = "time";
                    String dataToLog =time+" digital: ";
                    String digitalPins = "";
                    for (boolean b: digitalPinsWritten) digitalPins+=(b) ? "1 " : "0 ";

                    for (String someDevice: config.devices)
                    {
                        loggerMap.get(someDevice).write("time " + digitalPins);
                    }

                    dataToLog+=digitalPins+" analog: ";
                    for (double d: analogPinsRead) dataToLog+=d+" ";
                    dataLog.write(dataToLog);



                    stop = Thread.currentThread().isInterrupted();
                    //TODO very bad
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception ignored){}
                }
                catch (Exception  e)
                {
                    sendMessage("ERROR while log: "+e.getMessage());
                }
            }

        });
        log.setName(config.name);
        log.start();
    }

    //FIXME
    @Override
    public void type() {
        String type = config.unitType;
        if (type.toLowerCase().equals("nano"))
        {
            digitalPinsWritten = new boolean[14];
            analogPinsRead = new double[8];
        }
        if (type.toLowerCase().equals("uno"))
        {
            digitalPinsWritten = new boolean[14];
            analogPinsRead = new double[6];
        }

    }

    @Override
    protected boolean callDevice() {
        try {
            analogRead(""+1);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}
