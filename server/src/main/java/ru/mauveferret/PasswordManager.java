package ru.mauveferret;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class PasswordManager {

    private AES aes = new AES();
    private SimpleDateFormat formatForDate = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    // key == login, value == password
    private TreeMap<String,String> loginsAndPasswords = new TreeMap<>();
    private TreeMap<String, String> loginAndStartDates = new TreeMap<>();
    private TreeMap<String, String> loginAndExpireDates = new TreeMap<>();
    private TreeMap<String, Integer> loginAndAccessLevels = new TreeMap<>();
    private String path;

    PasswordManager(String key) {
        //FIXME universal path
        path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0,path.indexOf("ApparatusAutomatizer")+"ApparatusAutomatizer".length());
        path = path.replaceAll("/","\\\\");
        path+="\\resources\\passwords.txt";
        aes.setKey(key);
        loadLoginsAndPasswords();
    }

    //TODO crypt passwords.txt
    void writeLoginAndPassword(String login, String password, String dateStart, String dateExpiration, int accessLevel)
    {
        boolean valid = true;

        if (accessLevel>10)
        {
            valid=false;
            System.out.println("access level is too high!");
        }
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
                Date startDate = stringToDate(dateStart);
                Date expirationDate = stringToDate(dateExpiration);
                valid = expirationDate.after(date) && expirationDate.after(startDate);
                if (expirationDate.before(date))
                    System.out.println("ERROR: Date of access has expired. Pair not added.");
                if (startDate.after(expirationDate))
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
            addLoginAndPassword(login,password, dateStart,dateExpiration, accessLevel);
            System.out.println("Pair added successfully!");
        }
    }

    boolean loginExists(String login)
    {
        return loginsAndPasswords.containsKey(login);
    }

    boolean loginHasNotExpired(String login)
    {
        if (!loginsAndPasswords.containsKey(login))
        {
            System.out.println(login+" doesn't exist");
            return false;
        }
        try {
            Date currentDate = new Date();
            Date startDate = stringToDate(loginAndStartDates.get(login));
            Date expirationDate = stringToDate(loginAndExpireDates.get(login));
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
        String cryptedPassword = createHash(somePassword);
        if (!loginsAndPasswords.containsKey(someLogin))
        {
            System.out.println(someLogin+" doesn't exist");
            return false;
        }
        return  (loginsAndPasswords.get(someLogin).equals(cryptedPassword));
    }

    Date getExpireDate(String login)
    {
        loadLoginsAndPasswords();
        if (!loginsAndPasswords.containsKey(login))
        {
            System.out.println(login+" doesn't exist!");
            return new Date();
        }
        try {
            return stringToDate(loginAndExpireDates.get(login));
        }
        catch (Exception e)
        {
            System.out.println("ooh, that's terrible");
            return  new Date();
        }
    }

    int getAccessLevel(String login)
    {
        loadLoginsAndPasswords();
        if (!loginsAndPasswords.containsKey(login))
        {
            System.out.println(login+" doesn't exist!");
            return 0;
        }
        return loginAndAccessLevels.get(login);
    }

    String createRandom(int length)
    {
        SecureRandom random = new SecureRandom();
        String login = "";
        for (int i=0; i<length;i++)
            login+= ((char) (random.nextInt(26) + 'a'))+"";
        return login;
    }

    boolean createDecryptedFileVersion()
    {
        try
        {
            FileWriter writer = new FileWriter(new File(path.replace(".txt", "1.txt")), true);
            for (String login : loginsAndPasswords.keySet())
                writer.write(login + " " + loginAndStartDates.get(login) + " " +
                        loginAndExpireDates.get(login) + " " + loginAndAccessLevels.get(login) + "\n");
            writer.flush();
            writer.close();
            return true;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private Date stringToDate(String date) throws ParseException
    {
        return formatForDate.parse(date);
    }

    private void loadLoginsAndPasswords()
    {
        try {
            Scanner scanner = new Scanner(new FileReader(new File(path)));
            while (scanner.hasNextLine())
            {
                try {
                    String decryptedLine = aes.decrypt(scanner.nextLine());
                    String[] line = decryptedLine.split(" ");
                    if (line.length > 1) {
                        loginsAndPasswords.put(line[0], line[1]);
                        loginAndStartDates.put(line[0], line[2] + " " + line[3]);
                        loginAndExpireDates.put(line[0], line[4] + " " + line[5]);
                        loginAndAccessLevels.put(line[0], Integer.parseInt(line[6]));
                    } else
                        System.out.println("file was damaged!");
                }
                catch (Exception e)
                {
                    System.out.println("key is not valid or file was damaged");
                }
            }
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("FileNotFound!");
        }
    }

    private String createHash(String password)
    {
        return DigestUtils.sha256Hex(password);
    }


    private void addLoginAndPassword(String login, String password,
                                     String dateStart, String dateExpiration, int accessLevel)
    {
        try
        {
            FileWriter writer = new FileWriter(new File(path), true);
            String line = login;
            password = createHash(password) ;
            line+=" "+password+" "+dateStart+" "+dateExpiration+" "+accessLevel+"\n";
            try {
                writer.write(aes.encrypt(line));
            }
            catch (Exception e)
            {
                System.out.println("shit");
            }
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("FileIsBusy!");
        }

    }
}
