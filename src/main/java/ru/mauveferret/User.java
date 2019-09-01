package ru.mauveferret;

public class User {

    private String login;
    private String passwordMD5;
    private  long endOfAccessTime; //hours

    User(String login, String password, long duration) {
        this.login = login;
        this.passwordMD5 = password+1000;
        endOfAccessTime = System.currentTimeMillis()/(1000*60*60)+duration;
    }

    boolean hasAccess()
    {
        return System.currentTimeMillis() / (1000 * 60 * 60) > endOfAccessTime;
    }

    String getLogin()
    {
        return login;
    }

    boolean passwordIsValid(String password)
    {
        return  false;
    }

}
