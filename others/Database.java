package others;

import java.io.*;
import java.util.*;

public class Database {
    // An array containing various data
    public ArrayList<User> users = new ArrayList<User>();
    public ArrayList<User> loggedInUsers = new ArrayList<User>();
    public ArrayList<String> threads = new ArrayList<String>();
    public ArrayList<String> uploadedFiles = new ArrayList<String>();

    private String credFilePath;

    // Constants
    private static final String DIR_PATH = "./";

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

    public boolean threadExist(String threadName) {
        return threads.contains(threadName);
    }

    public boolean createThread(String usrName, String threadName) throws Exception {
        if (threads.contains(threadName)) {
            return false;
        }

        FileWriter fileWriter = new FileWriter(threadName, false);
        fileWriter.write(usrName + "\n");
        fileWriter.close();
        threads.add(threadName);
        return true;
    }

    public String getThreadList() {
        String threadList = "";
        int threadLen = threads.size();
        for (int i = 0; i < threadLen; i++) {
            threadList += threads.get(i);
            
            if (i != threadLen - 1) {
                threadList += "\n";
            }
        }
        return threadList;
    }

    public String getThreadMsg(String threadName) throws Exception {
        File thread = new File(threadName);
        Scanner scanner = new Scanner(thread);
        scanner.nextLine(); // skips first line

        String content = "";
        while (scanner.hasNextLine()) {
            content += scanner.nextLine();

            if (scanner.hasNextLine()) {
                content += "\n";
            }
        }
        scanner.close();
        return content;
    }

    public boolean removeThread(String userName, String threadName) throws Exception {
        File thread = new File(threadName);

        Scanner scanner = new Scanner(thread);
        String owner = scanner.nextLine();
        scanner.close();

        if (!owner.equals(userName)) {
            return false;
        }

        // Remove thread
        thread.delete();
        threads.remove(threadName);

        //Remove files uploaded to the thread
        File dir = new File(DIR_PATH);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String[] splittedName = file.getName().split("-");
                if (splittedName.length == 2 && splittedName[0].equals(threadName)) {
                    file.delete();
                }
            }
        }

        return true;
    }

    public void postMsgToThread(String userName, String threadName, String msg) throws Exception {
        File thread = new File(threadName);
        FileWriter writer = new FileWriter(thread, true);
        writer.write(getMsgId(threadName) + " " + userName + ": " + msg + "\n");
        writer.close();
    }

    public boolean threadHasMsgId(String threadName, int msgId) throws Exception {
        String msg = getMsgInTread(threadName, msgId);

        if (msg == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteThreadMessage(String userName, String threadName, int msgId) throws Exception {
        if (!threadMsgIsOwner(userName, threadName, msgId)) {
            return false;
        }

        File thread = new File(threadName);
        Scanner scanner = new Scanner(thread);

        int startId = 0;
        String newContent = "";
        while(scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            String[] splittedMsg = msg.split(" ", 2);

            int currId = -1;
            try {
                currId = Integer.parseInt(splittedMsg[0]);
            } catch (Exception e) {
                newContent += msg + "\n";
                continue;
            }

            if (currId != msgId) {
                newContent += ++startId + " " + splittedMsg[1] + "\n";
            }
        }
        scanner.close();

        FileWriter fileWriter = new FileWriter(thread, false);
        fileWriter.write(newContent);
        fileWriter.close();

        return true;
    }

    public boolean editThreadMessage(String userName, String threadName, int msgId, String msg) throws Exception {
        if (!threadMsgIsOwner(userName, threadName, msgId)) {
            return false;
        }

        String originalMsg = getMsgInTread(threadName, msgId);
        String newMsg = originalMsg.split(" ")[0] + " " + userName + ": " + msg;
        editThreadContent(threadName, originalMsg, newMsg);
        
        return true;
    }

    public boolean fileNameExist(String convertedName) {
        for (String name : uploadedFiles) {
            if (name.equals(convertedName)) {
                return true;
            }
        }

        return false;
    }

    public void addTransferredFile(String userName, String threadName, String fileName, byte[] fileContent) throws Exception {
        // Create new file
        String convertedFileName = threadName + "-" + fileName;
        File file = new File(convertedFileName);
        file.createNewFile();

        // Write content to the file
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(fileContent);
        fileOutputStream.close();

        // internal processing
        uploadedFiles.add(convertedFileName);
        File thread = new File(threadName);
        FileWriter fileWriter = new FileWriter(thread, true);
        fileWriter.write(userName + " uploaded " + fileName + "\n");
        fileWriter.close();
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

    private int getMsgId(String threadName) throws Exception {
        File thread = new File(threadName);
        Scanner scanner = new Scanner(thread);
        scanner.nextLine(); // skips the first line

        int largest_id = 0;
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            String strNum = msg.split(" ")[0];
            try {
                largest_id = Integer.parseInt(strNum);
            } catch (Exception e) {
                continue;
            }
        }
        scanner.close();
        return largest_id + 1;
    }

    private String getMsgInTread(String threadName, int msgId) throws Exception {
        File thread = new File(threadName);
        Scanner scanner = new Scanner(thread);
        scanner.nextLine(); // Skips first line

        while (scanner.hasNextLine()) {
            int currId;
            String currMsg = scanner.nextLine();
            try {
                currId = Integer.parseInt(currMsg.split(" ")[0]);
            } catch (Exception e) {
                continue;
            }

            if (currId == msgId) {
                scanner.close();
                return currMsg;
            }
        }
        scanner.close();
        return null;
    }

    private boolean threadMsgIsOwner(String userName, String threadName, int msgId) throws Exception {
        String ogMessage = getMsgInTread(threadName, msgId);
        String owner = ogMessage.split(" ")[1];
        if (!owner.equals(userName + ":")) {
            return false;
        }

        return true;
    }

    private void editThreadContent(String threadName, String ogMsg, String newMsg) throws Exception {
        String threadContent = getThreadMsg(threadName) + "\n";
        threadContent = threadContent.replaceFirst(ogMsg, newMsg);

        File thread = new File(threadName);
        Scanner scanner = new Scanner(thread);
        String creator = scanner.nextLine();
        scanner.close();

        FileWriter writer = new FileWriter(thread, false);
        writer.write(creator + "\n" + threadContent);
        writer.close();
    }
}
