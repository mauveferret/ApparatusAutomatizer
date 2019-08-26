package ru.mauveferret;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        TempPathFinder tempPathFinder = new TempPathFinder(true);
        Terminal terminal = new Terminal(tempPathFinder.terminal);
        Arduino arduino = new Arduino(tempPathFinder.Arduino);
        GuardianAngel angel = new GuardianAngel(tempPathFinder.angel);
        LeyboldTMP leyboldTMP = new LeyboldTMP(tempPathFinder.TMP);
        ThyracontGauge thyracontGauge = new ThyracontGauge(tempPathFinder.Gauge);
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
