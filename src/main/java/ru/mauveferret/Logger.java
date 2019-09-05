package ru.mauveferret;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private String dataToLog = "";
    private String path;
    private FileWriter writer;

    //Getters and Setters

    public String getPath() {
        return path;
    }

    void createFile(String path, String header) {
        this.path = path;
        try {
            writer = new FileWriter(new File(path), true);
            if (!"".equals(header))
                writer.write(header+"\n");
            writer.close();
        }
        catch (IOException ex)
        {
            System.out.println("Log path is incorrect.");
        }
    }

    //TODO выравнивание колонок

    // TODO проверять на совпадение предыдущую строку и препредыдущую!
     void write(String data) {
        if (!data.equals(dataToLog)) {
            dataToLog = data;
            try {
                writer = new FileWriter(new File(path), true);
                writer.write(dataToLog.replaceAll("time", System.currentTimeMillis()+"")+"\n");
                writer.flush();
                writer.close();
            } catch (Exception e) {
                System.out.println("Data writing failed: "+e.getMessage());
            }
        }
    }


}

