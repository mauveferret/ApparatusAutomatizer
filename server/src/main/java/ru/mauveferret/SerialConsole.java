package ru.mauveferret;

public class SerialConsole extends SerialDevice {

    SerialConsole(String fileName) {
        super(fileName);
        deviceAccessLevel = 9;
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
