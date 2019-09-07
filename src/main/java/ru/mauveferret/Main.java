package ru.mauveferret;


public class Main {

    public static void main(String[] args) {

        Terminal terminal = new Terminal("terminal");
        Arduino arduino = new Arduino("arduino");
        LeyboldTMP leyboldTMP = new LeyboldTMP("tmp");
        ThyracontGauge thyracontGauge = new ThyracontGauge("gauge");
        GateControl gateControl = new GateControl("gateControl");
        //GuardianAngel guardianAngel = new GuardianAngel("angel");
        //terminal.addDevice(guardianAngel);
        terminal.addDevice(thyracontGauge);
        terminal.addDevice(leyboldTMP);
        terminal.addDevice(terminal);
        terminal.addDevice(arduino);
        terminal.addDevice(gateControl);
        System.out.println("__________________________________________");
        for (String s :terminal.getCommandMap().keySet()) System.out.print(s+" ");
        System.out.println();
        System.out.println("__________________________________________");
        terminal.setName("terminalMainThread");
        terminal.start();
    }
}
