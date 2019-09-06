package ru.mauveferret;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private String dataToLog = "";
    private String path;
    private FileWriter writer;
    private boolean append;

    Logger(boolean append) {
        this.append = append;
    }

    //Getters and Setters

    public String getPath() {
        return path;
    }

    void createFile(String path, String header) {
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

    private boolean createPath(String path)
    {
        return true;

    }

    //TODO выравнивание колонок

    // TODO проверять на совпадение предыдущую строку и препредыдущую!
     void write(String data) {
        if (!data.equals(dataToLog) && !data.equals("")) {
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

