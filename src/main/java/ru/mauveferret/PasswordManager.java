package ru.mauveferret;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class PasswordManager {


    private SimpleDateFormat formatForDate = new SimpleDateFormat("dd.MM.yyyy");
    private TreeMap<String,String> loginsAndPasswords = new TreeMap<>();
    private String path;
    private String secretKey;

    void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    PasswordManager() {
        //FIXME universal path
         path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0,path.indexOf("ApparatusAutomatizer")+"ApparatusAutomatizer".length());
        path = path.replaceAll("/","\\\\");
        path+="\\resources\\passwords.txt";
    }



    //FIXME make dates
    void writeLoginAndPassword(String login, String password, String dateStart, String dateExpiration)
    {

        if (login.equals(""))
            login = createRandomString();
        if (password.equals(""))
            password = createRandomString();
        //FIXME shouldn't appear in terminal
        loadLoginsAndPasswords();
        boolean valid = true;

        //check if the login already exists
        for (String someLogin: loginsAndPasswords.keySet())
        {
            if (someLogin.equals(login))
            {
                System.out.println("login already exists");
                valid = false;
                break;
            }
        }
        //if login doesn't exist, checking if date is valid
        if (valid)
        {
            try {
                Date date = new Date();
                Date startDate = StringToDate(dateStart);
                Date expirationDate = StringToDate(dateExpiration);
                valid = startDate.after(date) && expirationDate.after(startDate);
                System.out.println(expirationDate.toString()+"  "+startDate.toString());
                if (!valid) System.out.println("incorrect date! (it is past of expiration date is before start date)");
            }
            catch (ParseException ex)
            {
                System.out.println("wrong date format: "+ex.getMessage());
            }

        }
        if (valid)
        {
            //TODO add try block?!
            addLoginAndPassword(login,password, dateStart,dateExpiration);
            System.out.println("Pair added successfully!");
        }
    }

    private Date StringToDate(String date) throws ParseException
    {
        //TODO разобраться с датами
        //formatForDate.applyPattern("dd.mm.yyyy hh");
        return formatForDate.parse(date);
    }

    boolean hasAccess(String login)
    {
        return true;
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
            //FIXME куда запинуть даты?!
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

    private void addLoginAndPassword(String login, String password, String dateStart, String dateExpiration)
    {
        try
        {
            FileWriter writer = new FileWriter(new File(path), true);
            String line = login;

            password = AES.encrypt(password, secretKey) ;

            line+=" "+password+" "+dateStart+" "+dateExpiration+"\n";
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
