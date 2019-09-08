package ru.mauveferret;


public class Main {

    public static void main(String[] args) {

        PasswordManager passwordManager = new PasswordManager();
        passwordManager.writeLoginAndPassword("admin", "password", "14:30 01.01.2019", "13:00 01.01.2021");
        System.out.println(passwordManager.IsPasswordValid("admin", "password"));
        System.out.println(passwordManager.userHasAccess("admin"));

        Terminal terminal = new Terminal("terminal");
        Arduino arduino = new Arduino("arduino");
        LeyboldTMP leyboldTMP = new LeyboldTMP("tmp");
        ThyracontGauge thyracontGauge = new ThyracontGauge("gauge");
        GateControl gateControl = new GateControl("gateControl");
        Server server = new Server("server");
        //GuardianAngel guardianAngel = new GuardianAngel("angel");
        //terminal.addDevice(guardianAngel);
        terminal.addDevice(thyracontGauge);
        terminal.addDevice(leyboldTMP);
        terminal.addDevice(terminal);
        terminal.addDevice(arduino);
        terminal.addDevice(gateControl);
        terminal.addDevice(server);
        System.out.println("__________________________________________");
        for (String s :terminal.getCommandMap().keySet()) System.out.print(s+" ");
        System.out.println();
        System.out.println("__________________________________________");
        terminal.setName("terminalMainThread");
        terminal.start();
    }
}
