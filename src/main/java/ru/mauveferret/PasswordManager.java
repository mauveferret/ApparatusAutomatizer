package ru.mauveferret;

import java.util.TreeMap;

public class PasswordManager {

    private String login;
    private String passwordMD5;
    private long startOfAccessTime;
    private  long endOfAccessTime; //hours
    //load from file passwords and check if some password equals with entered one
    private TreeMap<String,String> passwords = new TreeMap<>();

    void createPassword()
    {

    }


    boolean hasAccess()
    {
        return System.currentTimeMillis() / (1000 * 60 * 60) > endOfAccessTime;
    }

    String getLogin()
    {
        return login;
    }

    boolean IsPasswordValid(String password)
    {
        return  false;
    }

}
