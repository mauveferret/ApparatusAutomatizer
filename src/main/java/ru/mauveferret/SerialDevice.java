package ru.mauveferret;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.TreeMap;

abstract class SerialDevice extends Device {


    abstract void type();

    //just to check if the Device works properly (for find ports method)
    abstract boolean callDevice();

    private SerialPort serialPort;
    //in this thread reconnection to the device happening
    private Thread reconnectionThread;
    //indicates if the reconnection is active
    private boolean isReconnectActive = false;

    SerialDevice(String path) {
        super(path);
    }

    //SerialDevice(){}

    //Getters

    boolean isReconnectActive() {
        return isReconnectActive;
    }

    //terminal related methods

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("ports", "shows available COM port's names");
        commands.put("open", "Open Arduino Port in form: OP $arduino number$ $COM port name$");
        commands.put("close", "close Arduino port");
        commands.put("break", "stops the reconnection to the Device");
        commands.put("connect", "trying to reconnect to the Device in case of the failure");
        commands.put("write", "write command on the port");
        commands.put("read", "read message from the port");
        return super.getCommands();
    }

    @Override
    void chooseTerminalCommand(String[] command) {
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
                writeMessage(command[2]);
                break;
            case "read":
                sendMessage(readMessage());
                break;
        }
    }

    @Override
    void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
        String[] command = line.split(" ");
        switch (command[0].toLowerCase()) {
            case "type": {
                config.deviceType = command[1];
                type();
            }
            break;
            case "port":
                config.devicePort = command[1];
                break;
            case "open":
                openPort("");
                break;
            case "baudrate":
                config.baudRate = Integer.parseInt(command[1]);
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
        String port = config.devicePort;
        if (!doesPortExist(portName) && !doesPortExist(port)) {
            sendMessage("Enter COM port name as an option");
            return false;
        } else {
            try {
                serialPort.closePort();
            } catch (Exception ignored) {
            }

            if (doesPortExist(portName) || doesPortExist(port)) {
                if (!doesPortExist(portName)) portName = port;
                try {
                    serialPort = new SerialPort(portName);
                    serialPort.openPort();
                    serialPort.setParams(config.baudRate,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    //!--it shouldn't exist in extands method!
                    //don't know why, but arduino  need these 2000
                    //Thread.sleep(2000);
                } catch (Exception ex) {
                    //FIXME is busy wouldnt ever appear is seems
                    if (!isReconnectActive)
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

    private synchronized boolean closePort() {
        try {
            serialPort.closePort();
            if (!isReconnectActive) sendMessage(serialPort.getPortName() + " is closed");
            return true;
        } catch (SerialPortException ex) {
            sendMessage(ex.getLocalizedMessage());
            return false;
        }
    }

    synchronized String readMessage() {
        try {
            String answer = "";
            long startTime = System.currentTimeMillis();
            //&& !answer.contains("\n")
            while (!answer.contains("\r")) {
                //FIXME зависает в случае, если порт не отвечает, addlistener
                if (serialPort.getInputBufferBytesCount() > 0) {
                    answer += (new String(serialPort.readBytes(1)));
                }
                if (System.currentTimeMillis() - startTime > 3000) {
                    sendMessage(config.deviceName + " message wasn't got.");
                    reconnect();
                    break;
                }
            }
            return answer;
        } catch (SerialPortException e) {
            sendMessage(e.getLocalizedMessage());
            reconnect();
            return "error";
        }
    }

    synchronized boolean writeMessage(String message) {
        try {
            boolean success = serialPort.writeString(message);
            return success;
        } catch (SerialPortException e) {
            sendMessage(e.getLocalizedMessage());
            reconnect();
            return false;
        }
    }

    synchronized void reconnect() {
        //sometime tis method is call without need so iv added if
        if (!serialPort.isOpened())
        {
        reconnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String comPortName = serialPort.getPortName();
                    sendMessage(serialPort.getPortName() + " is lost. Reconnecting...");
                    isReconnectActive = true;
                    closePort();
                    long t1 = System.currentTimeMillis();
                    while (!serialPort.isOpened()) {
                        openPort(comPortName);
                        if (System.currentTimeMillis() - t1 > 10000) {
                            //probably, device changed port
                            findPort();
                            break;
                        }
                    }
                    if (serialPort.isOpened()) sendMessage("Reconnected.");
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
        reconnectionThread.setName("reconnection");
        reconnectionThread.start();
        }
    }

    private void findPort()
    {
        for (String port: showAvailableCOMPorts())
        {
            try {
                openPort(port);
                if (callDevice())
                {
                    config.devicePort = port;
                    sendMessage("Device "+config.deviceName+" changed its port to "+port);
                    break;
                }
            }
            catch (Exception ignored) {}
        }
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
