package ru.mauveferret;

import javax.swing.*;
import java.awt.*;

public class SomeShit extends JFrame {

    public static void main(String[] args) {
        SomeShit app = new SomeShit();
        app.sayHello();
    }

    public SomeShit() throws HeadlessException {
        setVisible(false);
    }

    private void sayHello() {
        JOptionPane.showMessageDialog(this, "Hello world!", "Hello", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

}