package ru.mauveferret;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Terminal terminal = new Terminal("args[0]", "args[1]");
        Arduino arduino = new Arduino();
        GuardianAngel angel = new GuardianAngel("ang");
        LeyboldTMP leyboldTMP = new LeyboldTMP("");
        leyboldTMP.setDeviceName("TMP");
        ThyracontGauge thyracontGauge = new ThyracontGauge("");
        GuardianAngel guardianAngel = new GuardianAngel("");
        terminal.addDevice(guardianAngel);
        terminal.addDevice(thyracontGauge);
        terminal.addDevice(leyboldTMP);
        terminal.addDevice(terminal);
        terminal.addDevice(arduino);
        terminal.addDevice(angel);
        for (String s :terminal.getCommandMap().keySet()) System.out.print(s+" ");
        System.out.println();
        System.out.println("__________________________________________");
        String command = scanner.nextLine();
        while (!command.equals("exit")) {
            terminal.launchCommand(command, false);
            command = scanner.nextLine();
        }
    }
}
