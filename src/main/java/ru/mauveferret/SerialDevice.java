package ru.mauveferret;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialDevice extends Device {

    SerialPort serialPort;

    private String port="";

    private boolean isReconnectActive = false;

    boolean isReconnectActive() {
        return isReconnectActive;
    }


    @Override
    void runCommand(String[] command) {
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
                    //else sendMessage((openPort(command[2])) ? command[2]+" is opened" : command[2]+" isn't opened");
                else openPort(command[2]);
            }
            break;
            case "close":
            {
                closePort();
            }
            break;

            case "info": info();
                break;
        }
        super.runCommand(command);
    }

    private synchronized String[] showAvailableCOMPorts()
    {
        return SerialPortList.getPortNames();
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
        try {
            serialPort.closePort();
        } catch (Exception ignored) {}

        if (doesPortExist(portName) || doesPortExist(port))
        {
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
                sendMessage("Port "+portName+" is busy");
                return false;
            }
        }
        else {
            if (!isReconnectActive)
                sendMessage(portName+" doesn't exist!");
            return false;
        }
        sendMessage(serialPort.getPortName() + " is opened");
        return true;
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

    synchronized  String readMessage() throws SerialPortException
    {
        String answer="";
        long startTime = System.currentTimeMillis();
        while (!answer.contains("\n"))
        {
            answer+=(new String(serialPort.readBytes(1)));
            if (System.currentTimeMillis()-startTime>2000)
            {
                launchReconnectInThread();
                break;
            }

        }
        return answer;
    }

    void launchReconnectInThread()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }).start();
    }

    private synchronized void reconnect()
    {
        //TODO manual desactivation, threadlist

        try {
            String comPortName = serialPort.getPortName();
            sendMessage(serialPort.getPortName() + " is lost. Reconnecting...");
            isReconnectActive=true;
            closePort();
            while (!serialPort.isOpened()) {
                openPort(comPortName);
            }
            sendMessage("Reconnected.");
            isReconnectActive=false;
            //rerunning command which caused the reconnection
            analyzeCommand(getReceivedDevice(),getReceivedCommand());
        }
        catch (NullPointerException e)
        {
            isReconnectActive=false;
            sendMessage("COM port name isn't set.");
        }
    }

    @Override
    void info() {

    }
}
