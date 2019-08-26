package ru.mauveferret;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Terminal terminal = new Terminal("C:\\Users\\dgbulgadaryan.2133-20115\\Git\\ApparatusAutomatizer\\resources\\terminal");
        Arduino arduino = new Arduino("C:\\Users\\dgbulgadaryan.2133-20115\\Git\\ApparatusAutomatizer\\resources\\Arduino");
        GuardianAngel angel = new GuardianAngel("C:\\Users\\dgbulgadaryan.2133-20115\\Git\\ApparatusAutomatizer\\resources\\angel");
        LeyboldTMP leyboldTMP = new LeyboldTMP("C:\\Users\\dgbulgadaryan.2133-20115\\Git\\ApparatusAutomatizer\\resources\\TMP");
        ThyracontGauge thyracontGauge = new ThyracontGauge("C:\\Users\\dgbulgadaryan.2133-20115\\Git\\ApparatusAutomatizer\\resources\\Gauge");
        //GuardianAngel guardianAngel = new GuardianAngel();
        //terminal.addDevice(guardianAngel);
        terminal.addDevice(thyracontGauge);
        terminal.addDevice(leyboldTMP);
        terminal.addDevice(terminal);
        terminal.addDevice(arduino);
        terminal.addDevice(angel);
        System.out.println("__________________________________________");
        for (String s :terminal.getCommandMap().keySet()) System.out.print(s+" ");
        System.out.println();
        System.out.println("__________________________________________");
        terminal.setName("terminalMainThread");
        terminal.start();
    }
}
