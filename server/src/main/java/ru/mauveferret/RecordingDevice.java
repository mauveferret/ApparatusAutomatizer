package ru.mauveferret;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

abstract public class RecordingDevice extends Device {



    //TODO must be filled!
     String pathForValueFile;
    protected String[] types = new String[50];

    //like columnNumber
    int deviceIndex;

    //used to write single pressure from every gauge/ Can be used by third party software
    private HashMap<Integer, Logger> loggerMap = new HashMap<>();

    //is filled by the initialize data from the logs
   protected String[] dataFromInitialize = new String[50];
    abstract void convertDataFromInitializeToArray(String[] initializeData);

    @Override
    protected void initialize() {
        super.initialize();
        String newPath = (new File(config.dataPath)).getParent();
        for (int number : config.elements) {
            loggerMap.put(number, new Logger(false));
            String sep = File.separator;
            String pressurePath = newPath + sep + "values" + sep + config.name + sep + types[number] + deviceIndex + ".txt";
            File singleValue = new File(pressurePath);
            if (singleValue.exists())
            {
               try {
                   Scanner reader = new Scanner(singleValue);
                   dataFromInitialize[number] = reader.nextLine();
                   reader.close();
               }
               catch (FileNotFoundException ignored){}
            }
                else
                    loggerMap.get(number).createFile(pressurePath, "");
        }
    }

    @Override
    protected void measureAndLog() {

    }
}
