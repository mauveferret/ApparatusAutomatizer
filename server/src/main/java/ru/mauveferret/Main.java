package ru.mauveferret;


import ru.mauveferret.Vacuum.*;

public class Main {

    public static void main(String[] args) {

        //PasswordManager passwords = new PasswordManager("12345");
        //passwords.writeAccount("admin", "password", "14:30 01.01.2019", "13:00 01.01.2021", 10);
        //passwords.createDecryptedFileVersion();

        //PasswordManager passwordManager = new PasswordManager();
        //passwordManager.writeLoginAndPassword("admin", "password", "14:30 01.01.2019", "13:00 01.01.2021", 10);
        //System.out.println(passwordManager.IsPasswordValid("admin", "password"));
        //System.out.println(passwordManager.loginHasNotExpired("admin"));

        //TODO it would be MUCH more comfortable if the filename equals deviceName
        //TODO  configPath should bi in directories  vacuum, discharge etc.

        SerialConsole serialConsole = new SerialConsole("console");
        Terminal terminal = new Terminal("terminal");
        terminal.addDevice(serialConsole);
        terminal.addDevice(terminal);
        
        new Vacuum(terminal);

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
