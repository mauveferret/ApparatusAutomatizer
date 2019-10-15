package ru.mauveferret;

import org.apache.commons.codec.digest.DigestUtils;
import java.io.*;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class PasswordManager extends Device {

    private AES aes = new AES();
    private SimpleDateFormat formatForDate = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    // key == login, value == password
    private TreeMap<String,String> loginsAndPasswords = new TreeMap<>();
    private TreeMap<String, String> loginAndStartDates = new TreeMap<>();
    private TreeMap<String, String> loginAndExpireDates = new TreeMap<>();
    private TreeMap<String, Integer> loginAndAccessLevels = new TreeMap<>();
    private String path;

    public PasswordManager(String fileName) {
        super(fileName);
    }

    //TODO Make a part of the Device

    @Override
    protected void measureAndLog() {

    }

    @Override
    protected void initialize() {
        loadLoginsAndPasswords();
        super.initialize();
    }

    void setKey(String key)
    {
        aes.setKey(key);
    }

    synchronized void writeAccount(String login, String password, String dateStart, String dateExpiration, int accessLevel)
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
            addAccount(login,password, dateStart,dateExpiration, accessLevel);
            System.out.println("Pair added successfully!");
        }
    }

    synchronized void changePassword(String login, String oldPassword, String newPassword) {
        if (loginsAndPasswords.containsKey(login))
        {
            if (loginsAndPasswords.get(login).equals(oldPassword))
            {
                try {
                    if (stringToDate(loginAndExpireDates.get(login)).after(new Date()))
                    {
                        String startDate = loginAndStartDates.get(login);
                        String expireDate = loginAndExpireDates.get(login);
                        int accessLevel = loginAndAccessLevels.get(login);
                        removeAccount(login);
                        addAccount(login,newPassword,startDate,expireDate,accessLevel);
                    }
                }
                catch (Exception ignored){}
            }
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

    synchronized void createDecryptedFileVersion()
    {
        try
        {
            String pathForDecrypted = path.replace(".txt", "_decrypted.txt");
            FileWriter writer = new FileWriter(new File(pathForDecrypted), false);
            for (String login : loginsAndPasswords.keySet()) {
                writer.write(login + " " + loginAndStartDates.get(login) + " ******** " +
                        loginAndExpireDates.get(login) + " " + loginAndAccessLevels.get(login) + "\n");
                writer.flush();
            }
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    //Terminal related methods

    @Override
    protected TreeMap<String, String> getCommands() {
        commands.put("setkey", "sets password for passwords.txt file decryption");
        commands.put("addaccount", "adds account in form: addaccount $login$ $password$ $start date$ $expire date$ $access level$ ");
        commands.put("changepassword", "change accaoun password in form: changepassword $login$ $oldpassword$ $newpassword$");
        commands.put("getfile","returns decrypted version of the file with account data. Without passwords, obviously");
        return super.getCommands();
    }


    //Internal methods

    private Date stringToDate(String date) throws ParseException
    {
        return formatForDate.parse(date);
    }

    private String createHash(String password)
    {
        return DigestUtils.sha256Hex(password);
    }

    synchronized private void removeAccount(String login) {
        try {
            ArrayList<String> fileLines= new ArrayList<>();
            Scanner scanner = new Scanner(new FileReader(new File(path)));
            while (scanner.hasNextLine()) {
                try {
                    String line = aes.decrypt(scanner.nextLine());
                   if (!line.contains(login))
                       fileLines.add(aes.encrypt(line));

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("key is not valid or file was damaged");
                }
            }
            scanner.close();
            FileWriter writer= new FileWriter(new File(path), false);
            for (String line: fileLines)
            {
                writer.write(line);
                writer.flush();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized private void addAccount(String login, String password,
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
            loadLoginsAndPasswords();
        }
        catch (IOException e)
        {
            System.out.println("FileIsBusy!");
        }

    }

    synchronized private void loadLoginsAndPasswords()
    {
        loginsAndPasswords = new TreeMap<>();
        loginAndStartDates = new TreeMap<>();
        loginAndExpireDates = new TreeMap<>();
        loginAndAccessLevels = new TreeMap<>();
        try {
            //FIXME it shouldn't use config path
            Scanner scanner = new Scanner(new FileReader(new File(config.configPath)));
            while (scanner.hasNextLine())
            {
                try {
                    String decryptedLine = aes.decrypt(scanner.nextLine());
                    String[] line = decryptedLine.split(" ");
                    if (line.length > 1) {
                        loginsAndPasswords.put(line[0], line[1]);
                        loginAndStartDates.put(line[0], line[2] + " " + line[3]);
                        loginAndExpireDates.put(line[0], line[4] + " " + line[5]);
                        loginAndAccessLevels.put(line[0], Integer.parseInt(line[6].trim()));
                    } else
                        System.out.println("file was damaged!");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
}
