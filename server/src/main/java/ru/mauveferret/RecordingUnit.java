package ru.mauveferret;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

abstract public class RecordingUnit extends Unit {

    public RecordingUnit(String fileName) {
        super(fileName);
    }

    //used to write single pressure from every gauge/ Can be used by third party software
    protected HashMap<String, Logger> loggerMap = new HashMap<>();


    //contains initial values for all devices, key = device, value = value (String)
    private HashMap<String,String> dataFromInitialize = new HashMap<>();
    protected abstract void convertDataFromInitializeToLocalType(HashMap<String,String> initializeData);

    @Override
    protected void initialize() {
        super.initialize();
        String newPath = (new File(config.dataPath)).getParent();
        for (String someDevice : config.devices) {
           //int deviceNumber = config.devices.indexOf(someDevice);
            loggerMap.put(someDevice, new Logger(true));
            String sep = File.separator;
            String singleValueLogPath = newPath + sep + "values" + sep + config.name +
                    sep + someDevice + config.unitNumber + ".txt";
            File singleValue = new File(singleValueLogPath);
            if (singleValue.exists())
            {
               try {
                   Scanner reader = new Scanner(singleValue);
                   dataFromInitialize.put(someDevice, reader.nextLine());
                   reader.close();
               }
               catch (FileNotFoundException ignored){}
               catch (NoSuchElementException e) {
                   try {
                       sendMessage(someDevice+" log is empty! Filliling by some gag");
                       FileWriter writer = new FileWriter(singleValue, true);
                       writer.write("0 0");
                       writer.close();
                       dataFromInitialize.put(someDevice, "0 0");
                   }
                   catch (Exception ex)
                   {
                       ex.printStackTrace();
                   }
               }
            }
                else
            {
                //FIXME 0 can cause problems for the first launch!
                dataFromInitialize.put(someDevice, "0 0");
            }
            loggerMap.get(someDevice).createFile(singleValueLogPath, "");
                //
                loggerMap.get(someDevice).setAppend(false);
        }
        convertDataFromInitializeToLocalType(dataFromInitialize);
        measureAndLog();
    }

    @Override
    protected void chooseTerminalCommand(String[] command) {
        super.chooseTerminalCommand(command);
    }

    @Override
    protected void chooseImportCommand(String line) {
        super.chooseImportCommand(line);
    }

    @Override
    protected void measureAndLog() {

    }
}
