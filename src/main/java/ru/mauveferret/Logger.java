package ru.mauveferret;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Logger extends Thread{

    private String dataToLog = "";
    private String pathToLog;
    private String pathToData;
    private FileWriter logWriter;
    private FileWriter dataWriter;

    //Getters and Setters

    public String getPathToLog() {
        return pathToLog;
    }

    public String getPathToData() {
        return pathToData;
    }

    Logger() {
    }

    void createLogFile(String pathToLog) {
        this.pathToLog = pathToLog;
        try {
            logWriter = new FileWriter(new File(pathToLog), true);
        }
        catch (IOException ex)
        {
            sendMessage("Log path is incorrect.");
        }
    }

    public void createDataFile(String pathToData) {
        this.pathToData = pathToData;
        try {
            dataWriter = new FileWriter(new File(pathToData), true);
        }
        catch (IOException ex)
        {
            sendMessage("Data path is incorrect.");
        }
    }



    abstract void log();

     void logData(String data) {
        if (!data.equals(dataToLog)) {
            dataToLog = data;
            try {
                dataWriter.write(dataToLog.replaceAll("time", System.currentTimeMillis()+"")+"\n");
                dataWriter.flush();
            } catch (Exception e) {
                sendMessage("Data writing failed: "+e.getMessage());
            }
        }
    }

    void sendMessage(String message) {
        try {
            message = System.currentTimeMillis() + " " + message;
            System.out.println(message);
            logWriter.write(message + "\n");
            logWriter.flush();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

    }
}

