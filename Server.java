import java.io.File;

import HPT.*;
import others.*;

public class Server {
    // Attributes
    private static HPTServer server;
    private static Database database;

    // Constants
    private static final String CREDENTIAL_PATH = "./credentials.txt";

    public static void main(String args[]) throws Exception {
        // Exit the program if there are errors in args
        if (hasErrorInit(args)) {
            System.exit(1);
        }

        // Setup server socket and port number
        // socket timeout is 500
        int portNum = getPortNum(args[0]);
        System.out.println("Server port is " + portNum);
        server = new HPTServer(portNum);

        // Setup database
        database = new Database(CREDENTIAL_PATH);
        database.printCredentials();

        // Main program
        System.out.println("Server is starting...\n");
        while (true) {
            HPTPacket clientRequest = server.getRequest();

            ClientProcess clientProcess = new ClientProcess(server, clientRequest, database);
            clientProcess.start();
        }
    }

    // Check if there is error
    // i.e. Whether user has input args with lengh that is not 1
    private static boolean hasErrorInit(String args[]) {
        if (args.length != 1) {
            System.out.println("Program only accept one argument!");
            System.out.println("\nE.g.\njava Client <Port Number>");
            return true;
        }

        return false;
    }

    // Gets the port number
    // Exits if parsing is failed
    private static int getPortNum(String portStr) {
        // dummy value
        int portNum = 0;
        try {
            portNum = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.out.println("Argument must be an integer");
            System.exit(1);
        }
        return portNum;
    }
}

class ClientProcess extends Thread {
    private HPTServer server;
    private HPTPacket request;
    private Database database;

    // Constants
    private static final String INVALID_USAGE = "ERR Invalid command usage";

    public ClientProcess(HPTServer server, HPTPacket packet, Database database) throws Exception {
        this.server = server;
        this.request = packet;
        this.database = database;
    }

    @Override
    public void run() {
        super.run();

        try {
            processRequest();
        } catch (Exception e) {
            System.out.println("Exception occurred");
            e.printStackTrace();
        }
    }

    private void processRequest() throws Exception {
        // System.out.println(request.rawContent);
        String command = request.header;
        String body = request.content;

        System.out.print("\n");
        switch (command) {
            case "LOGIN":
                processLogin(body);
                break;
            case "PASSWORD":
                processLoginPassword(body);
                break;
            case "NEWUSER":
                processNewUser(body);
                break;
            case "CRT":
                processCreateThread(body);
                break;
            case "MSG":
                processPostMessage(body);
                break;
            case "DLT":
                processDeleteMessage(body);
                break;
            case "EDT":
                processEditMessage(body);
                break;
            case "LST":
                processListThread(body);
                break;
            case "RDT":
                processReadThread(body);
                break;
            case "UPD":
                processUploadFile(body);
                break;
            case "DWN":
                processDownloadFile(body);
                break;
            case "RMV":
                processRemoveThread(body);
                break;
            case "XIT":
                processExit(body);
                break;
            default:
                System.out.println("A user has inputted a wrong command");
                server.sendResponce("ERR Command Not Found", request);
        }
    }

    // ======================================================================
    // Login related functions
    private void processLogin(String username) throws Exception {
        System.out.println("Client authenticating...");

        if (database.isUserAlreadyLoggedIn(username)) {
            System.out.println("Username " + username + " already logged in");
            server.sendResponce("ERR Already logged in", request);
            return;
        }

        if (database.usrLogin(username)) {
            System.out.println(username + " entering password");
            server.sendResponce("UNAMEOK", request);
        } else {
            System.out.println("New user, entering password");
            server.sendResponce("ERR No username", request);
        }
    }

    private void processLoginPassword(String args) throws Exception {
        String[] credentials = args.split(" ");

        int userId = database.usrLoginPassword(credentials[0], credentials[1]);
        if (userId != -1) {
            System.out.println(credentials[0] + " successful login");
            server.sendResponce("LOGINOK " + userId, request);
            return;
        }

        System.out.println("Incorrect password");
        server.sendResponce("ERR No such user", request);
    }

    private void processNewUser(String args) throws Exception {
        int newId = database.addNewUser(args);
        System.out.println("New user created, successful login");
        server.sendResponce("LOGINOK " + newId, request);
    }
    // ======================================================================

    // ======================================================================
    // Thread related functions
    private void processCreateThread(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 2) {
            printCommandFailedUse("CRT");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        // Create and check if thread already exist
        String threadName = splittedArgs[1];
        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        if (database.createThread(usr.username, threadName)) {
            System.out.println(usr.username + " created thread " + threadName);
            server.sendResponce("OK Thread " + threadName + " created", request);
        } else {
            System.out.println(usr.username + " failed to create thread:");

            String errMsg = "Thread "+ threadName + " already exist";
            System.out.println(errMsg);
            server.sendResponce("ERR " + errMsg, request);
        }
    }

    private void processListThread(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 1) {
            printCommandFailedUse("LST");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadList = database.getThreadList();
        System.out.println(usr.username + " listed threads");
        if (threadList.equals("")) {
            server.sendResponce("OK No threads to list", request);
        } else {
            server.sendResponce("OK List of threads:\n" + threadList, request);
        }
    }

    private void processReadThread(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 2) {
            printCommandFailedUse("RDT");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadName = splittedArgs[1];
        if (!database.threadExist(threadName)) {
            String errMsg = "Thread " + threadName + " does not exist";
            System.out.println(usr.username + " failed to read thread:");
            System.out.println(errMsg);

            server.sendResponce("ERR " + errMsg, request);
            return;
        }

        System.out.println(usr.username + " is reading thread " + threadName);
        String threadContent = database.getThreadMsg(threadName);
        if (threadContent.equals("")) {
            server.sendResponce("OK No messages in thread", request);
            return;
        }

        server.sendResponce("OK " + threadContent, request);
    }

    private void processRemoveThread(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 2) {
            printCommandFailedUse("RMV");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        String threadName = splittedArgs[1];
        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));

        String failedPrompt = usr.username + " failed to remove thread:";

        if (!database.threadExist(threadName)) {
            String errorMsg = "Thread " + threadName + " does not exist";
            System.out.println(failedPrompt);
            System.out.println(errorMsg);

            server.sendResponce("ERR " + errorMsg, request);
            return;
        }

        if (!database.removeThread(usr.username, threadName)) {
            String errorMsg = "Not owner of original thread";
            System.out.println(failedPrompt);
            System.out.println(errorMsg);

            server.sendResponce("ERR " + errorMsg, request);
            return;
        }

        System.out.println(usr.username + " removed thread " + threadName);
        server.sendResponce("OK Removed thread " + threadName, request);
    }
    // ======================================================================

    // ======================================================================
    // Message related functions
    private void processPostMessage(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ", 3);
        if (splittedArgs.length != 3) {
            printCommandFailedUse("MSG");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        User usr = database.users.get(Integer.parseInt((splittedArgs[0])));
        String threadName = splittedArgs[1];
        String message = splittedArgs[2];

        if (!database.threadExist(threadName)) {
            String errMsg = "Thread " + threadName + " does not exist";
            System.out.println(usr.username + " failed to post message:");
            System.out.println(errMsg);

            server.sendResponce("ERR " + errMsg, request);
            return;
        }

        database.postMsgToThread(usr.username, threadName, message);
        System.out.println(usr.username + " posted message to thread " + threadName);
        server.sendResponce("OK Message posted to " + threadName, request);
    }

    private void processDeleteMessage(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ", 3);
        if (splittedArgs.length != 3) {
            printCommandFailedUse("DLT");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadName = splittedArgs[1];
        int msgId = Integer.parseInt(splittedArgs[2]);

        String errMsg = usr.username + " failed to delete message";
        if (!database.threadExist(threadName)) {
            String eString = "Thread " + threadName + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        if (!database.threadHasMsgId(threadName, msgId)) {
            String eString = "Message ID " + msgId + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        if (!database.deleteThreadMessage(usr.username, threadName, msgId)) {
            String eString = "Not sender of message";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        System.out.println(usr.username + " deleted a message");
        server.sendResponce("OK Message deleted", request);
    }

    private void processEditMessage(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ", 4);
        if (splittedArgs.length != 4) {
            printCommandFailedUse("EDT");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String thread = splittedArgs[1];
        int msgId = Integer.parseInt(splittedArgs[2]);
        String message = splittedArgs[3];

        String errMsg = usr.username + " failed to edit message:";
        if (!database.threadExist(thread)) {
            String eString = "Thread " + thread + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        if (!database.threadHasMsgId(thread, msgId)) {
            String eString = "Message ID " + msgId + " does not exist in thread";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        if (!database.editThreadMessage(usr.username, thread, msgId, message)) {
            String eString = "Not sender of message";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        System.out.println(usr.username + " edited a message");
        server.sendResponce("OK Message edited", request);
    }
    // ======================================================================

    // ======================================================================
    // File transfer related function
    private void processUploadFile(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 3) {
            printCommandFailedUse("UPD");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadName = splittedArgs[1];
        String fileName = splittedArgs[2];

        String errMsg = usr.username + " failed to upload file:";
        if (!database.threadExist(threadName)) {
            String eString = "Thread " + threadName + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        String convertedName = threadName + "-" + fileName;
        if (database.fileNameExist(convertedName)) {
            String eString = "File " + fileName + " exist";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        server.sendResponce("UPDOK " + fileName, request);
        processRcivFileFromUser(usr.username, threadName, fileName);
    }

    private void processDownloadFile(String args) throws Exception {
        // Initial error checking
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 3) {
            printCommandFailedUse("DWN");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadName = splittedArgs[1];
        String fileName = splittedArgs[2];

        String errMsg = usr.username + " failed to download file:";
        if (!database.threadExist(threadName)) {
            String eString = "Thread " + threadName + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        String convertedName = threadName + "-" + fileName;
        if (!database.fileNameExist(convertedName)) {
            String eString = "File " + fileName + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            server.sendResponce("ERR " + eString, request);
            return;
        }

        server.sendResponce("DWNOK " + fileName, request);

        File file = new File(convertedName);
        server.sendFileToClient(file);
        System.out.println(usr.username + " successfully downloaded a file");
    }
    // ======================================================================

    // ======================================================================
    // Exit
    private void processExit(String args) throws Exception {
        int userId;
        try {
            userId = Integer.parseInt(args);
        } catch (Exception e) {
            printCommandFailedUse("XIT");
            server.sendResponce(INVALID_USAGE, request);
            return;
        }
        User usr = database.users.get(userId);
        System.out.println(usr.username + " logged out");
        database.loggedInUsers.remove(usr);

        if (database.loggedInUsers.size() == 0) {
            System.out.println("\nWaiting for users");
        }

        server.sendResponce("XITOK Goodbye!", request);
    }
    // ======================================================================

    // ======================================================================
    // Helpers
    private void printCommandFailedUse(String command) {
        System.out.println("A user failed to use " + command + ":");
        System.out.println("Too many / too little arguments");
    }

    private void processRcivFileFromUser(String userName, String threadName, String fileName) throws Exception {
        byte[] fileContent = server.getTransferredByte();
        database.addTransferredFile(userName, threadName, fileName, fileContent);

        System.out.println(userName + " successfully uploaded a file");
    }
    // ======================================================================
}
