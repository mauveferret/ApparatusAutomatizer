package ru.mauveferret;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import java.util.HashMap;

class SerialDevice extends Device {

    private SerialPort serialPort;
    //in this thread reconnection to the device happening
    private Thread reconnectionThread;
    //indicates if the reconnection is active
    private boolean isReconnectActive = false;

    SerialDevice(String path) {
        super(path);
    }

    SerialDevice(){}

    //Getters

    boolean isReconnectActive() {
        return isReconnectActive;
    }

    //terminal related methods

    @Override
    HashMap<String, String> getCommands() {
        commands.put("ports", "shows available COM port's names");
        commands.put("open", "Open Arduino Port in form: OP $arduino number$ $COM port name$");
        commands.put("close", "close Arduino port");
        commands.put("break", "stops the reconnection to the Device");
        commands.put("connect", "trying to reconnect to the Device in case of the failure");
        commands.put("write","write command on the port");
        commands.put("read", "read message from the port");
        return super.getCommands();
    }

    @Override
    void chooseCommand(String[] command) {
        super.chooseCommand(command);
        switch (command[1]) {
            case "ports": showAvailableCOMPorts();
            break;
            case "open": openPort(command[2]);
            break;
            case "connect": reconnect();
            break;
            case "break": stopReconnection();
            break;
            case "close": closePort();
            break;
            case "write": writeMessage(command[2]);
            break;
            case "read": sendMessage(readMessage());
            break;
        }
    }

    //Com port related commands

    private synchronized String[] showAvailableCOMPorts()
    {
        String message = "Available ports:";
        String[] s = SerialPortList.getPortNames();
        for (String value : s) message += (value + " ");
        sendMessage(message);
        return s;
    }

    private boolean doesPortExist(String portName)
    {
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

    private synchronized boolean openPort(String portName)
    {
        //if it wasn'r set, trying to load from the config file
        String port = getConfig().getDevicePort();

        if (portName.equals("") && !doesPortExist(port))
        {
            sendMessage("Enter COM port name as an option");
            return false;
        }
        else
            {
            try
            {
                serialPort.closePort();
            }
            catch (Exception ignored) {}

            if (doesPortExist(portName) || doesPortExist(port)) {
                if (!doesPortExist(portName)) portName = port;
                try {
                    serialPort = new SerialPort(portName);
                    serialPort.openPort();
                    serialPort.setParams(SerialPort.BAUDRATE_9600,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    //!--it shouldn't exist in extands method!
                    //don't know why, but arduino  need these 2000
                    //Thread.sleep(2000);
                } catch (Exception ex) {
                    sendMessage("Port " + portName + " is busy");
                    return false;
                }
            } else {
                if (!isReconnectActive)
                    sendMessage(portName + " doesn't exist!");
                return false;
            }
            sendMessage(serialPort.getPortName() + " is opened");
            return true;
        }
    }

    private synchronized boolean closePort()
    {
        try
        {
            serialPort.closePort();
            if (!isReconnectActive) sendMessage(serialPort.getPortName()+" is closed");
            return true;
        }
        catch (SerialPortException ex)
        {
            sendMessage(ex.getLocalizedMessage());
            return false;
        }
    }

    synchronized  String readMessage()
    {
        try {
            String answer = "";
            long startTime = System.currentTimeMillis();
            while (!answer.contains("\n")) {
                //FIXME зависает в случае, если порт не отвечает, addlistener
                answer += (new String(serialPort.readBytes(1)));
                if (System.currentTimeMillis() - startTime > 2000) {
                    reconnect();
                    break;
                }
            }
            return answer;
        }
        catch (SerialPortException e)
        {
            sendMessage(e.getLocalizedMessage());
            reconnect();
            return "error";
        }
    }

    synchronized boolean writeMessage(String message)
    {
        try {
            return serialPort.writeString(message);
        }
        catch (SerialPortException e)
        {
            sendMessage(e.getLocalizedMessage());
            reconnect();
            return false;
        }
    }

    synchronized void reconnect() {
        reconnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String comPortName = serialPort.getPortName();
                    sendMessage(serialPort.getPortName() + " is lost. Reconnecting...");
                    isReconnectActive = true;
                    closePort();
                    while (!serialPort.isOpened()) {
                        openPort(comPortName);
                    }
                    sendMessage("Reconnected.");
                    isReconnectActive = false;
                    //rerunning command which caused the reconnection
                    //FIXME бесконечный цикл в случае неправильной команды
                    //runCommand(getReceivedDevice(), getReceivedCommand());
                } catch (NullPointerException e) {
                    isReconnectActive = false;
                    sendMessage("COM port name isn't set.");
                }
            }
        }
        );
        reconnectionThread.start();
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
