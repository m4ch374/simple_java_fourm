package others;

import java.io.*;
import java.util.*;

public class Database {
    // An array containing User object
    public ArrayList<User> users = new ArrayList<User>();
    public ArrayList<User> loggedInUsers = new ArrayList<User>();

    private String credFilePath;

    // Constants
    private static final String DIR_PATH = "./";
    private static final String[] OG_FILE_LIST = new String[] {
            ".gitignore", 
            "README.md", 
            "credentials.txt", 
            "Server.java",
            "Client.class",
            "Server.class",
            "Client.java"
        };

    public Database(String credFilePath) throws Exception{
        this.credFilePath = credFilePath;
        users = getUsers();
    }

    public boolean usrLogin(String username) {
        for (User usr : users) {
            if (usr.username.equals(username)) {
                return true;
            }
        }

        return false;
    }

    // Returns the user id
    public int usrLoginPassword(String usrname, String password) {
        for (int i = 0; i < users.size(); i++) {
            User usr = users.get(i);
            if (usr.username.equals(usrname) && usr.password.equals(password)) {
                loggedInUsers.add(usr);
                return i;
            }
        }
        return -1;
    }

    public boolean isUserAlreadyLoggedIn(String username) {
        for (User usr : loggedInUsers) {
            if (usr.username.equals(username)) {
                return true;
            }
        }

        return false;
    }

    public int addNewUser(String credentials) throws Exception {
        FileWriter fileWriter = new FileWriter(credFilePath, true);

        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        PrintWriter printWriter = new PrintWriter(bufferedWriter);

        printWriter.println(credentials);
        printWriter.close();

        int newId = users.size();
        String[] splittedCred = credentials.split(" ");
        User usr = new User(newId, splittedCred[0], splittedCred[1]);
        users.add(usr);
        loggedInUsers.add(usr);
        return newId;
    }

    public boolean createThread(String usrName, String threadName) throws Exception {
        String fileName = DIR_PATH + threadName;
        File thread = new File(fileName);
        if (!thread.createNewFile()) {
            return false;
        }
        
        FileWriter fileWriter = new FileWriter(fileName);
        fileWriter.write(usrName + "\n");
        fileWriter.close();
        return true;
    }

    public ArrayList<String> getThreadList() {
        ArrayList<String> threadList = new ArrayList<String>();

        File dir = new File(DIR_PATH);
        File[] fileList = dir.listFiles();
        for (File file : fileList) {
            if (file.isFile() && !Arrays.stream(OG_FILE_LIST).anyMatch(file.getName()::equals)) {
                threadList.add(file.getName());
            }
        }
        return threadList;
    }

    public void printCredentials() {
        for (User usr : users) {
            System.out.println("[" + usr.username + " , " + usr.password + "]");
        }
    }

    private ArrayList<User> getUsers() throws Exception {
        ArrayList<User> userList = new ArrayList<User>();
            
        File file = new File(credFilePath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        int u_id = 0;
        String credentials;
        while ((credentials = bufferedReader.readLine()) != null) {
            String[] credentialArray = credentials.split(" ");

            userList.add(new User(u_id, credentialArray[0], credentialArray[1]));
            u_id++;
        }

        bufferedReader.close();
        return userList;
    }
}
