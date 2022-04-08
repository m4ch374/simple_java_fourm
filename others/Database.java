package others;

import java.io.*;
import java.util.*;

public class Database {
    // An array containing User object
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<User> loggedInUsers = new ArrayList<User>();

    private String credFilePath;

    public Database(String credFilePath) throws Exception{
        this.credFilePath = credFilePath;
        users = getUsers();
    }

    public boolean usrLogin(String username) {
        for (User usr : users) {
            if (usr.username.equals(username)) {
                System.out.println("hi");
                return true;
            }
        }

        return false;
    }

    public boolean usrLoginPassword(String usrname, String password) {
        for (User usr : users) {
            if (usr.username.equals(usrname) && usr.password.equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUserAlreadyLoggedIn(String username) {
        for (User usr : loggedInUsers) {
            if (usr.username.equals(username)) {
                return true;
            }
        }

        return false;
    }

    public void addNewUser(String credentials) throws Exception {
        FileWriter fileWriter = new FileWriter(credFilePath, true);

        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        PrintWriter printWriter = new PrintWriter(bufferedWriter);

        printWriter.println(credentials);
        printWriter.close();

        String[] splittedCred = credentials.split(" ");
        User usr = new User(splittedCred[0], splittedCred[1]);
        users.add(usr);
        loggedInUsers.add(usr);
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

        String credentials;
        while ((credentials = bufferedReader.readLine()) != null) {
            String[] credentialArray = credentials.split(" ");

            userList.add(new User(credentialArray[0], credentialArray[1]));
        }

        bufferedReader.close();
        return userList;
    }
}
