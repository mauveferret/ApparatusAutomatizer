package ru.mauveferret;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Terminal terminal = new Terminal("C:\\Users\\mauve\\Git\\ApparatusAutomatizer\\resources\\terminal");
        Arduino arduino = new Arduino("C:\\Users\\mauve\\Git\\ApparatusAutomatizer\\resources\\Arduino");
        GuardianAngel angel = new GuardianAngel("C:\\Users\\mauve\\Git\\ApparatusAutomatizer\\resources\\angel");
        LeyboldTMP leyboldTMP = new LeyboldTMP("C:\\Users\\mauve\\Git\\ApparatusAutomatizer\\resources\\TMP");
        //leyboldTMP.getConfig().setDeviceName("TMP");
        ThyracontGauge thyracontGauge = new ThyracontGauge("C:\\Users\\mauve\\Git\\ApparatusAutomatizer\\resources\\Gauge");
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
        String command = scanner.nextLine();
        while (!command.equals("exit")) {
            terminal.launchCommand(command, false);
            command = scanner.nextLine();
        }
    }
}
