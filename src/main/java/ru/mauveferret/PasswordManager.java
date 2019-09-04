package ru.mauveferret;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

public class PasswordManager {

    private long startOfAccessTime;
    private  long endOfAccessTime; //hours
    private TreeMap<String,String> loginsAndPasswords = new TreeMap<>();
    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    void writeLoginAndPassword(String login, String password)
    {

        if (login.equals(""))
            login = createRandomString();
        if (password.equals(""))
            password = createRandomString();
        //FIXME shouldn't appear in terminal
        System.out.println(login+" "+password);
        loadLoginsAndPasswords();
        boolean valid = true;
        for (String someLogin: loginsAndPasswords.keySet())
        {
            if (someLogin.equals(login))
            {
                System.out.println("login already exists");
                valid = false;
                break;
            }
        }
        if (valid)
        {
            addLoginAndPassword(login,password);
            System.out.println("Pair added successfully!");
        }
    }

    boolean hasAccess(String login)
    {
        return System.currentTimeMillis() / (1000 * 60 * 60) > endOfAccessTime;
    }

    boolean IsPasswordValid(String login, String password)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            password = Arrays.toString(md.digest());
        }
        catch (NoSuchAlgorithmException ignored){}
        loadLoginsAndPasswords();
        boolean isValid = false;
        for (String somelogin: loginsAndPasswords.keySet())
        {
            if (login.equals(somelogin))
                if (loginsAndPasswords.get(somelogin).equals(password))
                {
                    isValid = true;
                    break;
                }
        }
        return false;
    }

    private String createRandomString()
    {
        Random random = new Random();
        String login = "";
        for (int i=0; i<6;i++)
            login+= ((char) (random.nextInt(26) + 'a'))+"";
        return login;
    }

    private void loadLoginsAndPasswords()
    {
        try {
            Scanner scanner = new Scanner(new FileReader(new File(path)));
            while (scanner.hasNextLine())
            {
                String[] line = scanner.nextLine().split(" ");
                if (line.length>1)
                    loginsAndPasswords.put(line[0],line[1]);
                else
                    System.out.println("file was damaged!");
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("FileNotFound!");
        }
    }

    private void addLoginAndPassword(String login, String password)
    {
        try
        {
            FileWriter writer = new FileWriter(new File(path));
            String line = login;
            try {
                MessageDigest passw = MessageDigest.getInstance("SHA-256");
                passw.update(password.getBytes());
                password = Arrays.toString(passw.digest());
                System.out.println(password);
            }
            catch (NoSuchAlgorithmException e)
            {
                System.out.println("no such algorithm");
            }
            line+=" "+password;
            System.out.println(line);
            writer.write(line);
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("FileIsBusy!");
        }

    }
}
