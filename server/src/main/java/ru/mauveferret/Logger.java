package ru.mauveferret;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private String dataToLog = "";
    public String path;
    private FileWriter writer;
    private boolean append;
    private int columnLength = 20;

    public Logger(boolean append) {
        this.append = append;
    }

    //Getters and Setters


    public void setColumnLength(int columnLength) {
        this.columnLength = columnLength;
    }

    public void setAppend(boolean appendValue) {append = appendValue;}

    String getPath() {
        return path;
    }

    public void createFile(String path, String header) {
        this.path = path;
        try {
            File file = new File(new File(path).getParent());
            file.mkdirs();
            if (file.exists())
            {
                writer = new FileWriter(new File(path), append);
                if (!"".equals(header))
                    writer.write(header + "\n");
                writer.close();
            }
            else
                System.out.println("path "+path+" doesn't exist and can't be created");
        }
        catch (IOException ex)
        {
            System.out.println("Log path is incorrect.");
        }
    }


    private String fillLineByTabs(String line)
    {
        //TODO check how it works
        String[] lineArray = line.split(" ");
        String newLine = "";
        for (String message: lineArray) {
            String tabs = "";
            if (message.length()<columnLength) {
                for (int i = 0; i < columnLength - message.length(); i++) tabs += " ";
                {
                    newLine += message + tabs;
                }
            }
        }
        return newLine;
    }

    // TODO проверять на совпадение предыдущую строку и препредыдущую!
    public void write(String data) {
        if (!(data.equals(dataToLog) || data.equals(""))) {
            dataToLog = data;
            try {
                writer = new FileWriter(new File(path), append);
                String str = dataToLog.replaceAll("time", System.currentTimeMillis()+"");
                writer.write(str+"\n");
                writer.flush();
                writer.close();
            } catch (Exception e) {
                System.out.println("Data writing failed: "+e.getMessage());
            }
        }
    }


}

