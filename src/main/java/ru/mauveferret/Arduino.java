package ru.mauveferret;
import jssc.SerialPort;;
import jssc.SerialPortList;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/*
 must be accompanied by special Arduino driver which is made
 by Ivan Sorokin and can be got at plasma physics department of
 the National research Nuclear University "Mephi"
 */

public class Arduino extends Device {


    private String arduinoID="001";


    public Arduino(){}

    public Arduino(int arduinoNumber)
    {
       arduinoID=fillStringByZeros(arduinoNumber,3);
    }

    public Arduino(int arduinoNumber, String comPortName, String deviceName, String deviceCommand) throws Exception
    {
        arduinoID = fillStringByZeros(arduinoNumber, 3);
        openPort(comPortName);
        setDeviceName(deviceName);
        setDeviceCommand(deviceCommand);
    }

    //Arduino operations

    private boolean DigitalWrite(int pin, boolean value)
    {
       try {
           String message=arduinoID+"DO"+fillStringByZeros(pin,2)+(value ? 1 : 0);
           serialPort.writeBytes((message+fillStringByZeros(checkSum(message),3)+"\n").getBytes());
           String answer = "";
           while (!answer.contains("\n")) answer+=(new String(serialPort.readBytes(1)));
           if (answer.contains("SETTED"))sendMessage((value) ? "HIGH" : "LOW"+" on pin "+pin+" is set");
           return (answer.contains("SETTED"));
       }
       catch (Exception e)
       {
           sendMessage(value+"on pin "+pin+" is not set");
           return false;
       }
    }

    private boolean digitalRead(int pin) throws Exception
    {
            String message=arduinoID+"DI"+fillStringByZeros(pin,2);
            serialPort.writeBytes((message+fillStringByZeros(checkSum(message),3)+"\n").getBytes());
            String answer ="";
            while (!answer.contains("\n"))
            {
                answer+=(new String(serialPort.readBytes(1)));
            }
            int signal = checkSum(answer.substring(0,answer.length()-4));
            int checksum = Integer.parseInt(answer.substring(answer.length()-4,answer.length()-1));
            if ((signal == checksum)&&(!answer.contains("ERROR")))
            {
                return answer.substring(7,8).equals("1");
            }
            else
            {
                if (answer.contains("ERROR"))
                    sendMessage("pin "+pin+" doesn't exist");
                else
                    sendMessage("CheckSum is not correct. Check the wires!");
            }
            return false;
    }

    private boolean analogWrite(int pin, int value)
    {
        try {
            String message=arduinoID+"AO"+fillStringByZeros(pin,2)+fillStringByZeros(value*51, 4);
            serialPort.writeBytes((message+fillStringByZeros(checkSum(message),3)+"\n").getBytes());
            String answer = "";
            while (!answer.contains("\n"))
            {
                answer+=(new String(serialPort.readBytes(1)));
            }
            if (answer.contains("SETTED")) sendMessage(value+" on pin "+pin+" is set");;
            return (answer.contains("SETTED"));
        }
        catch (Exception e)
        {
            sendMessage(value+"on pin "+pin+" is not set");
            return false;
        }

    }

    private double analogRead(int pin) throws Exception
    {
            String message=arduinoID+"AI"+fillStringByZeros(pin,2);
            serialPort.writeBytes((message+fillStringByZeros(checkSum(message),3)+"\n").getBytes());
            String answer ="";
            while (!answer.contains("\n"))
            {
                answer+=(new String(serialPort.readBytes(1)));
            }
            int signal = checkSum(answer.substring(0,answer.length()-4));
            int checksum = Integer.parseInt(answer.substring(answer.length()-4,answer.length()-1));
            if ((signal == checksum)&&(!answer.contains("ERROR")))
            {
                return Integer.parseInt(answer.substring(7,11)) /204.6;
            }
            else
            {
                if (answer.contains("ERROR")) sendMessage("pin "+pin+" doesn't exist");
                else
                    sendMessage("CheckSum is not correct. Check the wires!");
            }
            return -1;
    }


    //internal Arduino methods (are needed for the driver support)

    private String fillStringByZeros(int value, int stringLength)
    {
        StringBuilder returnString = new StringBuilder();
        for (int i=0; i<(stringLength-String.valueOf(value).length()); i++) returnString.append("0");
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
    public HashMap<String, String> getCommands() {
        commands.put("ports", "shows available COM port's names");
        commands.put("open", "Open Arduino Port in form: OP $arduino number$ $COM port name$");
        commands.put("dwrite", "Write on Arduino digital port in form: DO $pin number$ $value(0,1)$");
        commands.put("dread", "Read from Arduino digital  port in form: DI $pin number$");
        commands.put("awrite", "Write on Arduino analog port(PWM~) in form: AO $pin number$ $value(0-5)$");
        commands.put("aread", "Read from Arduino analog  port in form: AI $pin number$");
        commands.put("close", "close Arduino port");
        commands.put("alias", "adds alias to the specific command in form: alias $alias$  $command$");
        return commands;
    }

    @Override
    public String runCommand(Device device, String someCommand) {

        messageList.clear();
        String[] command = commandToStringArray(someCommand);
        if (exist(command[1]))
        {
            command[1] = replaceAliasByCommand(command[1]);
            try {
                switch (command[1]) {
                    case "ports": {
                        String message = "Available ports:";
                        String[] s = showAvailableCOMPorts();
                        for (String value : s) message += (value + " ");
                        sendMessage(message);
                    }
                    break;
                    case "open": {
                        if (command[2].equals("")) sendMessage("Enter COM port name as an option");
                        else sendMessage((openPort(command[2])) ? command[2]+" is opened" : command[2]+" isn't opened");
                    }
                    break;
                    case "dwrite": {
                        if (command[2].equals("") || command[3].equals(" "))
                        {
                            sendMessage("Enter pin number and value (0,1) as an option");
                        }
                        else
                            {
                                boolean isWritten=false;
                                isWritten = DigitalWrite(Integer.parseInt(command[2]), command[3].equals("1"));
                                sendMessage((isWritten) ? command[3]+"is setted" : command[3]+"isn't setted");
                            }
                    }
                    break;
                    case "dread": {

                        if (command[2].equals(""))
                        {
                            sendMessage("Enter pin number as an option");
                        }
                        else
                        {

                            sendMessage("" + digitalRead(Integer.parseInt(command[2])));
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
                            boolean isWritten=false;
                            isWritten = analogWrite(Integer.parseInt(command[2]), Integer.parseInt(command[3]));
                            sendMessage((isWritten) ? command[3]+"is setted": command[3]+" isn't setted");
                        }
                        break;
                    case "aread":
                        if (command[2].equals(""))
                        {
                            sendMessage("Enter pin number as an option");
                        }
                        else
                        {
                            sendMessage("" + analogRead(Integer.parseInt(command[2])));
                        }
                        break;
                    case "close":
                    {
                        ClosePort();
                    }
                    break;
                    case "alias":
                    {
                        sendMessage((addAlias(command[2], command[3])) ? command[2]+" added" : command[2]+" isn't added");
                    }
                        break;
                }

            } catch (Exception e) {
                sendMessage(e.getLocalizedMessage());
            }

        }
        else
        {
            sendMessage("command \""+command[1]+"\" doesn't exist ");
        }

            //if message list is empty it makes /n which is not good!!!
       String returnMessage = "";
       for (String str: messageList) returnMessage+=str+"\n";
       return returnMessage;
    }

    private ArrayList<String> messageList = new ArrayList<>();

    @Override
    public void sendMessage(String message)
    {
        messageList.add(message);
    }

}
