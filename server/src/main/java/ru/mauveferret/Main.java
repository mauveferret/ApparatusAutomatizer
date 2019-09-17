package ru.mauveferret;


public class Main {

    public static void main(String[] args) {

        //PasswordManager passwords = new PasswordManager("12345");
        //passwords.writeAccount("admin", "password", "14:30 01.01.2019", "13:00 01.01.2021", 10);
        //passwords.createDecryptedFileVersion();

        //PasswordManager passwordManager = new PasswordManager();
        //passwordManager.writeLoginAndPassword("admin", "password", "14:30 01.01.2019", "13:00 01.01.2021", 10);
        //System.out.println(passwordManager.IsPasswordValid("admin", "password"));
        //System.out.println(passwordManager.loginHasNotExpired("admin"));
        SerialConsole serialConsole = new SerialConsole("console");

        Terminal terminal = new Terminal("terminal");
        Arduino arduino = new Arduino("arduino");
        LeyboldTMP leyboldTMP = new LeyboldTMP("tmp");
        ThyracontGauge thyracontGauge = new ThyracontGauge("thyracontGauge");
        PfeifferGauge pfeifferGauge = new PfeifferGauge("pfeifferGauge");
        Bypass bypass = new Bypass("bypass");
        GateControl gateControl = new GateControl("gateControl");
        Server server = new Server("server");
        GuardianAngel guardianAngel = new GuardianAngel("angel");

        terminal.addDevice(server);
        terminal.addDevice(thyracontGauge);
        terminal.addDevice(pfeifferGauge);
        terminal.addDevice(leyboldTMP);
        terminal.addDevice(arduino);
        terminal.addDevice(bypass);
        terminal.addDevice(serialConsole);
        terminal.addDevice(gateControl);
        terminal.addDevice(guardianAngel);
        terminal.addDevice(terminal);
        try {
            Thread.sleep(500);
        }
        catch (Exception ignored){}
        System.out.println("__________________________________________");
        for (String s :terminal.getCommandMap().keySet()) System.out.print(s+" ");
        System.out.println();
        System.out.println("__________________________________________");
        terminal.startNewSession();
    }
}
