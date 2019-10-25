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



        SerialConsole serialConsole = new SerialConsole("console");
        ProgramAnalyzer analyzer = new ProgramAnalyzer("analyzer");
        //PasswordManager passwordManager = new PasswordManager();
        Terminal terminal = new Terminal("terminal");
        //terminal.addDevice(passwordManager);
        terminal.addDevice(serialConsole);
        terminal.addDevice(terminal);
        terminal.addDevice(analyzer);


        new VacuumUnits("vacuum");

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
