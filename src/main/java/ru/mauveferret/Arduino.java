package ru.mauveferret;
import jssc.SerialPort;;
import jssc.SerialPortList;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/*
 must be accompanied by special Arduino driver which is made
 by Ivan Sorokin and can be got at plasma physics department of
 the National research Nuclear University "Mephi"
 */

public class Arduino extends Device {


    private String arduinoID="001";
    private SerialPort serialPort;

    public Arduino(){}

    public Arduino(int arduinoNumber)
    {
       arduinoID=fillStringByZeros(arduinoNumber,3);
    }

    public Arduino(int arduinoNumber, String comPortName, String deviceName, String deviceCommand) throws Exception
    {
        arduinoID = fillStringByZeros(arduinoNumber, 3);
        OpenPort(comPortName);
        setDeviceName(deviceName);
        setDeviceCommand(deviceCommand);
    }

    //Arduino operations

    private String[] ShowAvailableCOMPorts()
    {
        return SerialPortList.getPortNames();
    }

    private boolean OpenPort(String portName) throws Exception
    {
        serialPort = new SerialPort(portName);
        try
        {
            serialPort.closePort();
        }
        catch (Exception ignored) {}
        String[] portList = SerialPortList.getPortNames();
        boolean comPortExist = false;
        //check if the portName exist
        for (String s: portList)
            if (portName.equals(s))
            {
                comPortExist = true;
                break;
            }

        if (comPortExist)
        {
            try
            {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                //don't know why, but arduino  need these 2000
                Thread.sleep(2000);
            }
            catch (Exception ex)
            {
               sendMessage("Wrong Port number or port is busy");
                return false;
            }
        }
        else
        {
            sendMessage("This COM port doesn't exist!");
            return false;
        }
        if (comPortExist)  sendMessage(serialPort.getPortName()+": opened");
        return true;
    }

    private boolean ClosePort ()
    {
        try
        {
            serialPort.closePort();
            sendMessage(serialPort.getPortName()+": closed");
            return true;
        }
        catch (Exception ex)
        {
            sendMessage(ex.getLocalizedMessage());
            return false;
        }
    }

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

    private boolean DigitalRead(int pin) throws Exception
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

    private boolean AnalogWrite(int pin, int value)
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

    private double AnalogRead(int pin) throws Exception
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
                        String[] s = ShowAvailableCOMPorts();
                        for (String value : s) message += (value + " ");
                        sendMessage(message);
                    }
                    break;
                    case "open": {
                        if (command[2].equals("")) sendMessage("Enter COM port name as an option");
                        else OpenPort(command[2]);
                    }
                    break;
                    case "dwrite": {
                        if (command[2].equals("") || command[3].equals(" "))
                            sendMessage("Enter pin number and value (0,1) as an option");
                        else {
                            DigitalWrite(Integer.parseInt(command[2]), command[3].equals("1"));
                        }
                    }
                    break;
                    case "dread": {

                        if (command[2].equals(""))
                            sendMessage("Enter pin number as an option");
                        else sendMessage("" + DigitalRead(Integer.parseInt(command[2])));
                    }
                    break;
                    case "awrite":
                        if (command[2].equals("") || command[3].equals(""))
                            sendMessage("Enter pin number and value (0-5) as an option");
                        else AnalogWrite(Integer.parseInt(command[2]), Integer.parseInt(command[3]));
                        break;
                    case "aread":
                        if (command[2].equals("")) sendMessage("Enter pin number as an option");
                        else sendMessage("" + AnalogRead(Integer.parseInt(command[2])));
                        break;
                    case "close":
                        ClosePort();
                    case "alias":
                        sendMessage((addAlias(command[2], command[3])) ? "added" : "not added");
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
