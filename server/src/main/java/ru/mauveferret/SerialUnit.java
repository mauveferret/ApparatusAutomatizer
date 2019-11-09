package ru.mauveferret;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.TreeMap;

public abstract class SerialUnit extends RecordingUnit {

    public SerialUnit(String fileName) {
        super(fileName);
    }



    protected abstract void type();
    //just to check if the Unit works properly (for finding the port and checking if the reconnection was successful)
    protected abstract boolean callDevice();
    private SerialPort serialPort;
    //in this thread reconnection to the device happening
    private Thread reconnectionThread;
    //indicates if the reconnection is active
    private boolean isReconnectActive = false;

    //SerialUnit(){}

    //Getters AND SETTERS


    synchronized  private void setReconnectActive(boolean reconnectActive) {
        isReconnectActive = reconnectActive;
    }

    synchronized protected boolean isReconnectActive() {
        return isReconnectActive;
    }

    //terminal related methods

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("ports", "shows available COM port's names");
        commands.put("open", "Open Arduino Port in form: OP $arduino number$ $COM port name$");
        commands.put("close", "close Arduino port");
        commands.put("break", "stops the reconnection to the Unit");
        commands.put("connect", "trying to reconnect to the Unit in case of the failure");
        commands.put("write", "write command on the port");
        commands.put("read", "read message from the port");
        commands.put("enablelog","");
        return super.getCommands();
    }

    @Override
    protected void chooseTerminalCommand(String[] command) {
        super.chooseTerminalCommand(command);
        switch (command[1]) {
            case "ports":
                showAvailableCOMPorts();
                break;
            case "open":
                openPort(command[2]);
                break;
            case "connect":
                reconnect();
                break;
            case "break":
                stopReconnection();
                break;
            case "close":
                closePort();
                break;
            case "write":
                writeString(command[2]);
                break;
            case "read":
                sendMessage(readString(command[2]));
                break;
            case  "enablelog" :  measureAndLog();
            break;
        }
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        switch (command[0].toLowerCase()) {
            case "type": {
                config.unitType = command[1];
                type();
            }
            break;
            case "port":
                config.unitPort = command[1];
                break;
            case "open":
                openPort("");
                break;
            case "baudrate":
                config.baudRate = Integer.parseInt(command[1]);
            break;
        }
    }

    //Com port related commands

    synchronized String[] showAvailableCOMPorts() {
        String message = "Available ports:";
        String[] s = SerialPortList.getPortNames();
        for (String value : s) message += (value + " ");
        sendMessage(message);
        return s;
    }

    private boolean doesPortExist(String portName) {
        String[] portList = SerialPortList.getPortNames();
        boolean comPortExist = false;
        //check if the portName exist
        for (String s : portList)
            if (portName.equals(s)) {
                comPortExist = true;
                break;
            }
        return comPortExist;
    }

    private synchronized boolean openPort(String portName) {
        //if it wasn'r set, trying to load from the config file
        String port = config.unitPort;
        try {
            serialPort.closePort();
        }
        catch (Exception ignored) {}
        if (!(doesPortExist(portName) || doesPortExist(port))) {
            if (!isReconnectActive) sendMessage("Port name doesn't exist");
            return false;
        }
        else
            {
            if (doesPortExist(portName) || doesPortExist(port)) {
                if (!doesPortExist(portName)) portName = port;
                try
                {
                    serialPort = new SerialPort(portName);
                    serialPort.openPort();
                    serialPort.setParams(config.baudRate,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    //don't know why, but arduino  need these 2000
                    // Thread.sleep(2000);
                    //Turn on hardware flow control
                    //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                          //  SerialPort.FLOWCONTROL_RTSCTS_OUT);
                }
                catch (Exception ex)
                {
                    //FIXME is busy wouldnt ever appear is seems
                    if (!isReconnectActive)
                        sendMessage("Port " + portName + " is busy");
                    return false;
                }
            }
            else {
                if (!isReconnectActive)
                    sendMessage(portName + " doesn't exist!");
                return false;
            }
            if (!isReconnectActive)
                sendMessage(serialPort.getPortName() + " is opened");
            return true;
        }
    }

    private synchronized boolean closePort() {
        try
        {
            serialPort.closePort();
            if (!isReconnectActive) sendMessage(serialPort.getPortName() + " is closed");
            return true;
        }
        catch (SerialPortException ex) {
            //if (!isReconnectActive) sendMessage("Cant't close port: "+ex.getLocalizedMessage());
            return false;
        }
    }

    protected synchronized String sendCommandToDevice(String command, int delay, String endOfLine)
    {
        writeString(command);
        try {
            Thread.sleep(delay);
        }
        catch (Exception ignored){}
        if (!isReconnectActive)
            return readString(endOfLine);
        else
            return "ConnectionLost";
    }

    protected synchronized String readString(String endOfLine) {
        try {
            String answer = "";
            long startTime = System.currentTimeMillis();
            while (!(answer.contains(endOfLine))) {
                //FIXME зависает в случае, если порт не отвечает, addlistener. А правда ли?
                if (serialPort.getInputBufferBytesCount() > 0) {
                    answer += (new String(serialPort.readBytes(1)));
                }
                if (System.currentTimeMillis() - startTime > 3000) {
                    if (!isReconnectActive) {
                        sendMessage(config.name + " message wasn't got.");
                        //FIXME is it really necessary?
                        reconnect();
                    }
                    break;
                }
            }
            return answer;
        }
        catch (SerialPortException|NullPointerException e) {
            if (!isReconnectActive){
                //sendMessage(""e.getLocalizedMessage());
                 reconnect();
            }
            return "error";
        }
    }

    protected synchronized byte[] readBytes(int bytesCount)
    {
       try {
           //FIXME wait while bytes wouldn't come
          if (serialPort.getInputBufferBytesCount() > bytesCount-1) {
               return serialPort.readBytes(bytesCount);
           }
           else {
              if (!isReconnectActive) sendMessage("No response found");
              return null;
          }
       }
       catch (SerialPortException e)
       {
           if (!isReconnectActive) {
               //sendMessage(e.getMessage());
               reconnect();
           }
           return  null;
       }
    }

    protected synchronized boolean writeBytes(byte[] message)
    {
        try
        {
            try {
                if (!serialPort.isOpened())
                {
                    if (!isReconnectActive) sendMessage("port is closed!!");
                    return false;
                }
            }
            catch (Exception e)
            {
                if (!isReconnectActive) {
                    sendMessage(config.unitPort + " port wasn't created: "+e.getLocalizedMessage());
                    reconnect();
                }
            }
            return serialPort.writeBytes(message);
        }
        catch (SerialPortException e) {
            checkWhyMessageWasntSent(e);
            return false;
        }
    }

    protected synchronized boolean writeString(String message) {
        try
        {
          try {
              if (!serialPort.isOpened())
              {
                  if (!isReconnectActive) sendMessage("port is closed!!");
                  return false;
              }
          }
          catch (Exception e) {
              if (!isReconnectActive) {
                  sendMessage("port wasn't created!");
                  reconnect();
              }
          }
            return serialPort.writeString(message);
        }
        catch (Exception e) {
            checkWhyMessageWasntSent(e);
            return false;
        }
    }

    private void checkWhyMessageWasntSent(Exception e)
    {
        if (!isReconnectActive)
            sendMessage("message wasn't written on"+config.name);
        try {
            serialPort.purgePort(SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXCLEAR);
        }
        catch (SerialPortException ex)
        {
            sendMessage(ex.getPortName()+" не прокатило!!");
        }
        catch (NullPointerException ignored){}
        if (!isReconnectActive) {
            //sendMessage(e.getMessage());
            reconnect();
        }
    }

    protected synchronized void reconnect() {
        reconnectionThread = new Thread(() -> {
            try {
                try {closePort();}
                catch (Exception ignored){}
                String comPortName = config.unitPort;
                serialPort = new SerialPort(comPortName);
                if (!isReconnectActive) {
                    setReconnectActive(true);
                    sendMessage(serialPort.getPortName() + " is lost. Reconnecting...");
                }
                long t1 = System.currentTimeMillis();
                while (!(serialPort.isOpened() && callDevice())) {
                    openPort(comPortName);
                    /*if (System.currentTimeMillis() - t1 > 20000) {
                        //probably, device changed port
                        sendMessage("probably, port has changed...looking");
                        findPort();
                        break;}*/
                    //FIXME very bad!!!
                    try {
                        Thread.sleep(3000);
                    }
                    catch (Exception ignored){}
                }
                sendMessage("Reconnected.");
                setReconnectActive(false);

                try {
                    Thread.sleep(3000);
                }
                catch (Exception ignored){}
                //rerunning command which caused the reconnection
                //FIXME бесконечный цикл в случае неправильной команды
                //TODO create reconnection disabling
                //FIXME very bad!!!
                if (!"".equals(getReceivedCommand()))
                terminalSample.launchCommand(receivedCommand, true, receivedAccessLevel);
            } catch (Exception e) {
                setReconnectActive(false);
                sendMessage("port wasn't found.");
            }
        });
        reconnectionThread.setName("reconnection");
        if (!isReconnectActive) reconnectionThread.start();

        try {
            Thread.sleep(1000);
        }
        catch (Exception ignored){}
    }

    private void findPort()
    {
        /*for (String port: showAvailableCOMPorts())
        {
            try {
                openPort(port);
                if (callDevice())
                {
                    config.devicePort = port;
                    sendMessage("Unit "+config.deviceName+" changed its port to "+port);
                    break;
                }
            }
            catch (Exception ignored) {}
        }

         */
    }

    private boolean stopReconnection()
    {
        if (reconnectionThread.isAlive())
            reconnectionThread.interrupt();
        else
            sendMessage("Can't stop. Reconnection wasn't launched.");
        return true;
    }

}
