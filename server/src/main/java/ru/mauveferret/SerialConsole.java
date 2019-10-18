package ru.mauveferret;

import java.util.HashMap;

public class SerialConsole extends SerialDevice {

    SerialConsole(String fileName) {
        super(fileName);
        unitAccessLevel = 9;
    }

    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String, String> initializeData) {

    }

    void write(String message)
    {
        writeString(message+"\n");
    }

    void read()
    {
        sendMessage(readString("\n"));
    }

    @Override
    protected void chooseTerminalCommand(String[] command) {
        switch (command[1])
        {
            case "write":
            {
                write(command[2]);
                read();
            }
            break;
            case "read": read();
            break;
        }
        super.chooseTerminalCommand(command);
    }

    @Override
    protected void type() {

    }

    @Override
    protected boolean callDevice() {
        return false;
    }

    @Override
    protected void measureAndLog() {

    }
}
