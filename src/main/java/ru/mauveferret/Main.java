package ru.mauveferret;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Terminal terminal = new Terminal("args[0]", "args[1]");
        Arduino arduino = new Arduino(1);
        terminal.AddDevice(terminal);
        terminal.AddDevice(arduino);
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
