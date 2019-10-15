package ru.mauveferret;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

class ProgramAnalyzer extends Device {

    private long usedBytes;
    private double usedCpu;

     ProgramAnalyzer(String fileName) {
        super(fileName);
    }

    @Override
    protected void measureAndLog() {


        dataLog.createFile(config.dataPath, "time  memoryUse, MB cpuUse, %  ");
        log = new Thread(() -> {
            boolean stop = false;
            while (!stop)
            {
                    try {
                        usedBytes = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576;
                        usedCpu = getProcessCpuLoad();
                        dataLog.write("time "+usedBytes+" "+usedCpu);
                        stop = Thread.currentThread().isInterrupted();
                    }
                    catch (NullPointerException  e)
                    {
                        sendMessage("ERROR while measure RAM:\n ");
                        break;
                    }
                    //TODO very bad
                try {
                    Thread.sleep(500);
                }
                catch (Exception ignored){}
            }
        });
        log.setName(config.name);
        log.start();
    }


    private double getProcessCpuLoad()  {

        try {

            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (list.isEmpty()) return Double.NaN;

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            // usually takes a couple of seconds before we get real values
            if (value == -1.0) return Double.NaN;
            // returns a percentage value with 1 decimal point precision
            return ((int) (value * 1000) / 10.0);
        }
        catch (Exception ex)
        {
            return -1;
        }
    }
}
