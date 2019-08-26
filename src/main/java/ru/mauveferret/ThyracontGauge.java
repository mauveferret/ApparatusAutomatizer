package ru.mauveferret;


import java.util.HashMap;
import java.util.TreeMap;

class ThyracontGauge extends SerialDevice {



    ThyracontGauge(String path) {
        super(path);
    }

    @Override
    void log() {

    }

    double[] pressure1 = new double[4];
//Getters

    public double[] getPressure() {
        return pressure1;
    }

    //TODO calibrate

    //gauge related commands

    private synchronized double measure(int gaugeNumber)
    {
        //FIXME CHECKSuM
        String message = "00"+gaugeNumber+"M";
        int checkSum=0;
        for (char c: message.toCharArray())
            checkSum+=c;
        writeMessage(message+(char) (checkSum % 64 +64)+"\r");
        message = readMessage();
        double mantissa =0;
        try {
            mantissa = Double.parseDouble(message.substring(4,8))/1000;
        }
        catch (Exception e)
        {
            System.out.println("hyi");
        }
        int order = 0;
                try {
                    order= Integer.parseInt(message.substring(8,10))-20;
                }
                catch (Exception e)
                {
                    System.out.println("sef");
                }
        double value = mantissa*0.75*Math.pow(10,order);
        try {
            pressure1[gaugeNumber] = value;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println(value);
        return  value;
    }

    //for commandline


    @Override
    void chooseCommand(String[] command) {
        if (command[1].equals("measure"))
        {
            measure(1);
        }
        super.chooseCommand(command);
    }

    @Override
    TreeMap<String, String> getCommands() {
        commands.put("measure", "measures pressure in mBar by some gauge in form: measure $gauge number$ ");
        commands.put("calibrate", "makes a calibration of the pirani or cold cathode in form: calibrate $type$");
        return super.getCommands();
    }
}

