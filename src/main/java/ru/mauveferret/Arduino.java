package ru.mauveferret;
;
import jssc.SerialPortException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/*
 must be accompanied by special Arduino driver which is made
 by Ivan Sorokin and can be got at plasma physics department of
 the National research Nuclear University "Mephi"
 */

public class Arduino extends Device {


    //TODO list of pins status
    private boolean[] digitalPins = new boolean[50];
    private int[] analogPins = new int[50];
    //FIXME: does Jssc controls simultenous writingon arduino?

    public Arduino(String path) {
        super(path);
    }


    //Getters and Setters

    //TODO добавить в методы ардуины добавление информации в эти массив
    public boolean[] getDigitalPins() {
        return digitalPins;
    }

    public int[] getAnalogPins() {
        return analogPins;
    }

    //Arduino operations

    synchronized private boolean digitalWrite(String pin, boolean value)
    {
       try {
           String message=fillStringByZeros(getDeviceID(),3);
           message+="DO"+fillStringByZeros(pin,2)+(value ? 1 : 0);
           serialPort.writeBytes((message+fillStringByZeros(""+checkSum(message),3)+"\n").getBytes());
           String answer = readMessage();
           if (answer.contains("SETTED"))
               sendMessage(((value) ? "HIGH" : "LOW")+" on pin "+pin+" is set");
           else
               if (!isReconnectActive()) sendMessage("ERROR: pin number is incorrect.");
           return (answer.contains("SETTED"));
       }
       catch (Exception e)
       {
           sendMessage(((value) ? "HIGH" : "LOW")+" on pin "+pin+" isn't set");
           launchReconnectInThread();
           return false;
       }
    }

    synchronized private boolean digitalRead(String pin)
    {
        try {

            String message = fillStringByZeros(getDeviceID(),3);
            message +="DI" + fillStringByZeros(pin, 2);
            serialPort.writeBytes((message + fillStringByZeros(""+checkSum(message), 3) + "\n").getBytes());
            String answer = readMessage();
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
        catch (SerialPortException ex)
        {
            launchReconnectInThread();
            return false;
        }
    }

    synchronized private boolean analogWrite(String pin, int value)
    {
        try {
            String message=fillStringByZeros(getDeviceID(),3);
            message+="AO"+fillStringByZeros(pin,2)+fillStringByZeros(""+value*51, 4);
            serialPort.writeBytes((message+fillStringByZeros(""+checkSum(message),3)+"\n").getBytes());
            String answer = readMessage();
            if (answer.contains("SETTED")) sendMessage(value+" on pin "+pin+" is set");;
            return (answer.contains("SETTED"));
        }
        catch (SerialPortException ex)
        {
            sendMessage(value+" on pin "+pin+" isn't set");
            launchReconnectInThread();
            return false;
        }

    }

    synchronized private double analogRead(String pin)
    {
            try {
                String message = fillStringByZeros(getDeviceID(),3);
                message += "AI" + fillStringByZeros(pin, 2);
                serialPort.writeBytes((message + fillStringByZeros(""+checkSum(message), 3) + "\n").getBytes());
                String answer = readMessage();
                int signal = checkSum(answer.substring(0, answer.length() - 4));
                int checksum = Integer.parseInt(answer.substring(answer.length() - 4, answer.length() - 1));
                if ((signal == checksum) && (!answer.contains("ERROR"))) {
                    return Integer.parseInt(answer.substring(7, 11)) / 204.6;
                } else {
                    if (answer.contains("ERROR")) sendMessage("pin " + pin + " doesn't exist");
                    else
                        sendMessage("CheckSum is not correct. Check the wires!");
                }
                return -1;
            }
            catch (SerialPortException ex)
            {
                launchReconnectInThread();
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
    HashMap<String, String> getCommands() {
        commands.put("dwrite", "Write on Arduino digital port in form: DO $pin number$ $value(0,1)$");
        commands.put("dread", "Read from Arduino digital  port in form: DI $pin number$");
        commands.put("awrite", "Write on Arduino analog port(PWM~) in form: AO $pin number$ $value(0-5)$");
        commands.put("aread", "Read from Arduino analog  port in form: AI $pin number$");
        return super.getCommands();
    }

    @Override
    void runCommand(Device device, String someCommand) {
        someCommand = someCommand.toLowerCase();
        super.runCommand(device, someCommand);
        String[] command = commandToStringArray(someCommand);
        if (commandExists(command[1]))
        {
            command[1] = replaceAliasByCommand(command[1]);
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
                {
                    if (command[2].equals(""))
                    {
                        sendMessage("Enter pin number as an option");
                    }
                    else
                    {
                        sendMessage("" + digitalRead(command[2]));
                    }
                }
                break;
                case "awrite":
                    if (command[2].equals("") || command[3].equals(""))
                    {
                        sendMessage("Enter pin number and value (0-5) as an option");
                    }
                    else
                    {
                        analogWrite((command[2]), Integer.parseInt(command[3]));
                    }
                    break;
                case "aread":
                    if (command[2].equals(""))
                    {
                        sendMessage("Enter pin number as an option");
                    }
                    else
                    {
                        sendMessage("" + analogRead(command[2]));
                    }
                    break;
            }
        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }
    }

    @Override
    void info() {

    }
}
