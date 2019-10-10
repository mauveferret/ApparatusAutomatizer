package ru.mauveferret.Vacuum.Pumps;

import ru.mauveferret.Vacuum.TMP;

import java.util.Arrays;
import java.util.TreeMap;


public class LeyboldTMP extends TMP {



    private byte[] request = new byte[]{0x02,0x16,0x00,0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};;
    private byte[] response;
    private boolean[] statusRequest;
    private boolean[] statusResponse;

    private int  temperature = 0;
    private int frequency = 0;
    private double voltage = 0;
    private double current = 0;
    //for logging
    private String status;

    private boolean enabled = false;

    public LeyboldTMP(String fileName) {
        super(fileName);
        request= new byte[]{0x02,0x16,0x00,0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        response = new byte[24];
        statusRequest = new boolean[16];
        statusResponse = new boolean[16];
        temperature = 0;
        frequency = 0;
        voltage = 0;
        current = 0;
        status = "";

        deviceAccessLevel = 6;
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    protected void type() {

    }

    @Override
    protected boolean callDevice() {
        return false;
    }


//Getters

    public int getTemperature() {
        return temperature;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }

    public boolean isEnabled() {
        return enabled;
    }

    //Device related commands

    @Override
    protected void chooseTerminalCommand(String[] command) {
        switch (command[1])
        {
            //FIXME
            case "run": setEnabled(true);
            break;
            case "stop": setEnabled(false);
            break;
            case "data": sendMessage(temperature+" "+frequency+" "+voltage+" "+current);
            break;
        }
        super.chooseTerminalCommand(command);
    }

    private void setEnabled(boolean enable)
    {
        statusRequest[0] = enable;
    }

    private void  setControl (boolean control)
    {
        statusRequest[10] = control;
    }

    private void setStandBy(boolean standBy)
    {
        statusRequest[8] = standBy;
    }

    private void setCooling (boolean enable)
    {
        statusRequest[5] = enable;
    }


    //TMP internal methods

    //obviously only for two digit Strings (up to 255 in dec)
    private byte hexToByte(String hexInString) {
        if (hexInString.length()>2) return 0;
        return (byte) ((Character.digit(hexInString.charAt(0), 16) << 4)
                + Character.digit(hexInString.charAt(1), 16));
    }

    private void checkSum()
    {
        byte checkSum = request[0];
        for (int i=1; i<23;i++)
        {
            checkSum = (byte) (checkSum ^ request[i]);
        }
        request[23] = checkSum;
    }

    //creates HEX from statusRequest and put it in request[10] and request[11]
    private void setStatusRequest()
    {
        String requst = "";
        for (boolean bit: statusRequest) requst+=(bit) ? 1: 0;
        requst = new StringBuffer(requst).reverse().toString();
        String firstByte = Integer.toHexString(Integer.parseInt(requst.substring(0,8), 2)).toUpperCase();
        String secondByte = Integer.toHexString(Integer.parseInt(requst.substring(8,16), 2)).toUpperCase();
        if (firstByte.length()<2) firstByte="0"+firstByte;
        if (secondByte.length()<2) secondByte="0"+secondByte;
        request[11] = hexToByte(firstByte);
        request[12] = hexToByte(secondByte);
    }

    //fills statusResponse with the received from the TMP values
    private void getStatusResponse()
    {
        String binary = new StringBuffer(bytesToBinaryString(11)).reverse().toString();
        //for logging
        status = binary;
        for (int i=0; i<binary.length(); i++)
        {
            statusResponse[i] = (binary.charAt(i)+"").equals("1");
        }
    }

    //decryptes some TMP data from the receives message
    private void getData()
    {
        frequency = Integer.parseInt(bytesToBinaryString(13), 2);
        temperature = Integer.parseInt(bytesToBinaryString(15), 2);
        current = Integer.parseInt(bytesToBinaryString(17), 2) *0.1;
        voltage = Integer.parseInt(bytesToBinaryString(21), 2) * 0.1;
        System.out.println(frequency+" "+temperature+" "+current+" "+voltage);
    }

    private String bytesToBinaryString(int firstByteIndex)
    {
        int firstDecimal = Integer.parseInt(String.format("%02x", response[firstByteIndex]&0xff), 16);
        int secondDecimal = Integer.parseInt(String.format("%02x", response[firstByteIndex+1]&0xff), 16);
        String binary = fillBinaryByZeros(Integer.toBinaryString(firstDecimal),8);
        binary+=fillBinaryByZeros(Integer.toBinaryString(secondDecimal),8);
        return binary;
    }

    //probably you can change it with the repeat method
    private String fillBinaryByZeros(String binary, int length)
    {
        String returnString="";
        for (int i=0;i<length-binary.length();i++) returnString+="0";
        return returnString+binary;
    }

    private String display(byte[] b1) {
        StringBuilder strBuilder = new StringBuilder();
        for(byte val : b1) {
            strBuilder.append(String.format("%02x", val&0xff)+" ");
        }
        return strBuilder.toString();
    }


    private void measure()
    {

        setStatusRequest();
        checkSum();
        writeBytes(request);
        response = readBytes(24);
        getStatusResponse();
        getData();
    }

    //terminal related

    @Override
    protected void measureAndLog() {
        dataLog.createFile(config.dataPath, "time  temperature,C  frequency, Hz   voltage, 0.1V   current,A   status (see manual) ");
        Thread log = new Thread(() -> {
            boolean stop = true;
            try {
                while (stop)
                {
                    //FIXME very bad!
                    Thread.sleep(100);
                    measure();
                    dataLog.write("time "+temperature+" "+frequency+" "+voltage+" "+current+" "+status);
                    stop = Thread.currentThread().isInterrupted();
                }
            }
            catch (InterruptedException ignored) { }
        });
        log.setName(config.deviceName);
        log.start();
    }

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("run", "launches the TMP in form $run$");
        commands.put("stop","stops the TMP in form: $stop$");
        commands.put("measure","");
        commands.put("temperature", "returnes the temperature of the TMP in celsium in form: $temperature$");
        commands.put("frequency", "...");
        return super.getCommands();
    }

}
