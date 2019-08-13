package ru.mauveferret;
import jssc.SerialPort;;
import jssc.SerialPortList;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Arduino extends Device {

    private String arduinoID="001";
    private SerialPort serialPort;

    public Arduino(){}

    public Arduino(int arduinoNumber)
    {
       arduinoID=FillStringByZeros(arduinoNumber,3);
    }

    public Arduino(int arduinoNumber, String comPortName) throws Exception
    {
        arduinoID = FillStringByZeros(arduinoNumber, 3);
        OpenPort(comPortName);
    }

    //Arduino operations

    public String[] ShowAvailableCOMPorts()
    {
        return SerialPortList.getPortNames();
    }

    public boolean OpenPort(String portName) throws Exception
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
                SendMessage("Wrong Port number or port is busy");
                return false;
            }
        }
        else
        {
            SendMessage("This COM port doesn't exist!");
            return false;
        }
        if (comPortExist)  SendMessage(serialPort.getPortName()+": opened");
        return true;
    }

    public boolean ClosePort ()
    {
        try
        {
            serialPort.closePort();
            SendMessage(serialPort.getPortName()+": closed");
            return true;
        }
        catch (Exception ex)
        {
            SendMessage(ex.getLocalizedMessage());
            return false;
        }
    }

    public boolean DigitalWrite(int pin, boolean value)
    {
       try {
           String message=arduinoID+"DO"+FillStringByZeros(pin,2)+(value ? 1 : 0);
           serialPort.writeBytes((message+FillStringByZeros(CheckSum(message),3)+"\n").getBytes());
           String answer = "";
           while (!answer.contains("\n")) answer+=(new String(serialPort.readBytes(1)));
           if (answer.contains("SETTED")) SendMessage((value) ? "HIGH" : "LOW"+" on pin "+pin+" is set");
           return (answer.contains("SETTED"));
       }
       catch (Exception e)
       {
           SendMessage(value+"on pin "+pin+" is not set");
           return false;
       }
    }

    public boolean DigitalRead(int pin) throws Exception
    {
            String message=arduinoID+"DI"+FillStringByZeros(pin,2);
            serialPort.writeBytes((message+FillStringByZeros(CheckSum(message),3)+"\n").getBytes());
            String answer ="";
            while (!answer.contains("\n"))
            {
                answer+=(new String(serialPort.readBytes(1)));
            }
            int signal = CheckSum(answer.substring(0,answer.length()-4));
            int checksum = Integer.parseInt(answer.substring(answer.length()-4,answer.length()-1));
            if ((signal == checksum)&&(!answer.contains("ERROR")))
            {
                return answer.substring(7,8).equals("1");
            }
            else
            {
                if (answer.contains("ERROR"))
                    SendMessage("pin "+pin+" doesn't exist");
                else
                    SendMessage("CheckSum is not correct. Check the wires!");
            }
            return false;
    }

    public boolean AnalogWrite(int pin, int value)
    {
        try {
            String message=arduinoID+"AO"+FillStringByZeros(pin,2)+FillStringByZeros(value*51, 4);
            serialPort.writeBytes((message+FillStringByZeros(CheckSum(message),3)+"\n").getBytes());
            String answer = "";
            while (!answer.contains("\n"))
            {
                answer+=(new String(serialPort.readBytes(1)));
            }
            if (answer.contains("SETTED")) SendMessage(value+" on pin "+pin+" is set");;
            return (answer.contains("SETTED"));
        }
        catch (Exception e)
        {
            SendMessage(value+"on pin "+pin+" is not set");
            return false;
        }

    }

    public double AnalogRead(int pin) throws Exception
    {
            String message=arduinoID+"AI"+FillStringByZeros(pin,2);
            serialPort.writeBytes((message+FillStringByZeros(CheckSum(message),3)+"\n").getBytes());
            String answer ="";
            while (!answer.contains("\n"))
            {
                answer+=(new String(serialPort.readBytes(1)));
            }
            int signal = CheckSum(answer.substring(0,answer.length()-4));
            int checksum = Integer.parseInt(answer.substring(answer.length()-4,answer.length()-1));
            if ((signal == checksum)&&(!answer.contains("ERROR")))
            {
                return Integer.parseInt(answer.substring(7,11)) /204.6;
            }
            else
            {
                if (answer.contains("ERROR")) SendMessage("pin "+pin+" doesn't exist");
                else
                    SendMessage("CheckSum is not correct. Check the wires!");
            }
            return -1;
    }


    //internal methods

    private String FillStringByZeros(int value, int stringLength)
    {
        StringBuilder returnString = new StringBuilder();
        for (int i=0; i<(stringLength-String.valueOf(value).length()); i++) returnString.append("0");
        returnString.append(value);
        return returnString.toString();
    }

    private int CheckSum(String message)
    {
        int checkSum=0;
        byte[] b = message.getBytes(StandardCharsets.US_ASCII);
        for (byte value : b) checkSum += value;
        return (checkSum % 256);
    }

    //for commandline

    @Override

    public String GetDeviceName() {
        return "Arduino";
    }

    @Override
    public HashMap<String, String> getCommands() {
        HashMap<String, String> commands = new HashMap<>();
        commands.put("AP", "shows available COM port's names");
        commands.put("OP", "Open Arduino Port in form: OP $arduino number$ $COM port name$");
        commands.put("DO", "Write on Arduino digital port in form: DO $pin number$ $value(0,1)$");
        commands.put("DI", "Read from Arduino digital  port in form: DI $pin number$");
        commands.put("AO", "Write on Arduino analog port(PWM~) in form: AO $pin number$ $value(0-5)$");
        commands.put("AI", "Read from Arduino analog  port in form: AI $pin number$");
        commands.put("CP", "close Arduino port");
        return commands;
    }

    @Override
    public String[] CommandToStringArray(String command) {
        return super.CommandToStringArray(command);
    }

  private ArrayList<String> messageList = new ArrayList<>();
    @Override
    public void SendMessage(String message)
    {
        messageList.add(message);
    }

    @Override
    public String RunCommand(String someCommand) {
        String[] command = CommandToStringArray(someCommand);
       try {
           switch (command[0])
           {
               case "AP" :
               {
                   String message="Available ports:";
                   String[] s = ShowAvailableCOMPorts();
                   for (String value : s) message+=(value + " ");
                   //message+="\n";
                   SendMessage(message);
               }
               break;
               case "OP" :
               {
                   if (command[1].equals("")) SendMessage("Enter COM port name as an option");
                   else OpenPort(command[1]);
               }
               break;
               case "DO" :
               {
                   if (command[1].equals("") || command[2].equals(" "))
                       SendMessage("Enter pin number and value (0,1) as an option");
                   else
                   {
                       DigitalWrite(Integer.parseInt(command[1]), command[2].equals("1"));
                   }
               }
               break;
               case "DI" :
               {

                   if (command[1].equals(""))
                       SendMessage("Enter pin number as an option");
                   else SendMessage(""+DigitalRead(Integer.parseInt(command[1])));
               }
               break;
               case "AO" : if (command[1].equals("")||command[2].equals("")) SendMessage("Enter pin number and value (0-5) as an option");
               else AnalogWrite(Integer.parseInt(command[1]),Integer.parseInt(command[2]));
                   break;
               case "AI" :  if (command[1].equals("")) SendMessage("Enter pin number as an option");
               else SendMessage(""+AnalogRead(Integer.parseInt(command[1])));
                   break;
               case "CP" : ClosePort();
                   break;
           }

       }
       catch (Exception e)
       {
           SendMessage(e.getLocalizedMessage());
       }

        String returnMessage = "";
       for (String str: messageList) returnMessage+=str+"\n";
       return returnMessage;
    }
}
