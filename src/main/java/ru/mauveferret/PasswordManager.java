package ru.mauveferret;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class PasswordManager {


    private SimpleDateFormat formatForDate = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    // key == login, value == password
    private TreeMap<String,String> loginsAndPasswords = new TreeMap<>();
    private TreeMap<String, String> loginAndStartDates = new TreeMap<>();
    private TreeMap<String, String> loginAndExpireDates = new TreeMap<>();
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

    void writeLoginAndPassword(String login, String password, String dateStart, String dateExpiration)
    {
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
                if (startDate.before(date))
                    System.out.println("ERROR: Date of access start has expired. Pair not added.");
                if (startDate.before(expirationDate))
                    System.out.println("ERROR: Expiration date is before start date. Pair not added");
            }
            catch (ParseException ex)
            {
                valid = false;
                System.out.println("wrong date format: "+ex.getMessage());
            }

        }
        if (valid)
        {
            addLoginAndPassword(login,password, dateStart,dateExpiration);
            System.out.println("Pair added successfully!");
        }
    }

    boolean userHasAccess(String login)
    {
        loadLoginsAndPasswords();
        if (!loginsAndPasswords.containsKey(login))
        {
            System.out.println(login+" doesn't exist");
            return false;
        }
        try {
            Date currentDate = new Date();
            Date startDate = StringToDate(loginAndStartDates.get(login));
            Date expirationDate = StringToDate(loginAndExpireDates.get(login));
            return  (startDate.before(currentDate) && expirationDate.after(currentDate));
        }
        catch (Exception e)
        {
            System.out.println("This error expected not to happen"+e.getMessage());
            return  false;
        }
    }

    boolean IsPasswordValid(String someLogin, String somePassword)
    {
        loadLoginsAndPasswords();
        String cryptedPassword = AES.encrypt(somePassword,secretKey);
        if (!loginsAndPasswords.containsKey(someLogin))
        {
            System.out.println(someLogin+" doesn't exist");
            return false;
        }
        return  (loginsAndPasswords.get(someLogin).equals(cryptedPassword));
    }

    String createRandom(int length)
    {
        Random random = new Random();
        String login = "";
        for (int i=0; i<length;i++)
            login+= ((char) (random.nextInt(26) + 'a'))+"";
        return login;
    }

    private Date StringToDate(String date) throws ParseException
    {
        return formatForDate.parse(date);
    }

    private void loadLoginsAndPasswords()
    {
        try {
            Scanner scanner = new Scanner(new FileReader(new File(path)));
            while (scanner.hasNextLine())
            {
                String[] line = scanner.nextLine().split(" ");
                if (line.length>1) {
                    loginsAndPasswords.put(line[0], line[1]);
                    loginAndStartDates.put(line[0], line[2]+" "+line[3]);
                    loginAndExpireDates.put(line[0], line[4]+" "+line[5]);
                }
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
