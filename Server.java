import HPT.*;
import others.*;

public class Server {
    // Attributes
    private static HPTServer server;
    private static Database database;

    // Constants
    private static final String CREDENTIAL_PATH = "./credentials.txt";
    private static final String INVALID_USAGE = "ERR Invalid command usage";

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

            String response = processRequest(clientRequest);

            try {
                server.sendResponce(response);
            } catch (Exception e) {
                System.out.println("Exception occurred: \n");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
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

    private static String processRequest(HPTPacket request) throws Exception {
        // System.out.println(request.rawContent);
        String command = request.header;
        String body = request.content;

        System.out.print("\n");
        switch (command) {
            case "LOGIN":
                return processLogin(body);
            case "PASSWORD":
                return processLoginPassword(body);
            case "NEWUSER":
                return processNewUser(body);
            case "CRT":
                return processCreateThread(body);
            case "MSG":
                return processPostMessage(body);
            case "DLT":
                return processDeleteMessage(body);
            case "EDT":
                return processEditMessage(body);
            case "LST":
                return processListThread(body);
            case "RDT":
                return processReadThread(body);
            case "UPD":
                return processUploadFile(body);
            case "RMV":
                return processRemoveThread(body);
            case "XIT":
                return processExit(body);
            default:
                System.out.println("A user has inputted a wrong command");
                return "ERR Command Not Found";
        }
    }

    // ======================================================================
    // Login related functions
    private static String processLogin(String username) {
        System.out.println("Client authenticating...");

        if (database.isUserAlreadyLoggedIn(username)) {
            System.out.println("Username " + username + " already logged in");
            return "ERR Already logged in";
        }

        if (database.usrLogin(username)) {
            System.out.println(username + " entering password");
            return "UNAMEOK";
        } else {
            System.out.println("New user, entering password");
            return "ERR No username";
        }
    }

    private static String processLoginPassword(String args) {
        String[] credentials = args.split(" ");

        int userId = database.usrLoginPassword(credentials[0], credentials[1]);
        if (userId != -1) {
            System.out.println(credentials[0] + " successful login");
            return "LOGINOK " + userId;
        }

        System.out.println("Incorrect password");
        return "ERR No such user";
    }

    private static String processNewUser(String args) throws Exception {
        int newId = database.addNewUser(args);
        System.out.println("New user created, successful login");
        return "LOGINOK " + newId;
    }
    // ======================================================================

    // ======================================================================
    // Thread related functions
    private static String processCreateThread(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 2) {
            printCommandFailedUse("CRT");
            return INVALID_USAGE;
        }

        // Create and check if thread already exist
        String threadName = splittedArgs[1];
        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        if (database.createThread(usr.username, threadName)) {
            System.out.println(usr.username + " created thread " + threadName);
            return "OK Thread " + threadName + " created";
        } else {
            System.out.println(usr.username + " failed to create thread:");

            String errMsg = "Thread "+ threadName + " already exist";
            System.out.println(errMsg);
            return "ERR " + errMsg;
        }
    }

    private static String processListThread(String args) {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 1) {
            printCommandFailedUse("LST");
            return INVALID_USAGE;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadList = database.getThreadList();
        System.out.println(usr.username + " listed threads");
        if (threadList.equals("")) {
            return "OK No threads to list";
        } else {
            return "OK List of threads:\n" + threadList;
        }
    }

    private static String processReadThread(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 2) {
            printCommandFailedUse("RDT");
            return INVALID_USAGE;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadName = splittedArgs[1];
        if (!database.threadExist(threadName)) {
            String errMsg = "Thread " + threadName + " does not exist";
            System.out.println(usr.username + " failed to read thread:");
            System.out.println(errMsg);

            return "ERR " + errMsg;
        }

        String threadContent = database.getThreadMsg(threadName);
        if (threadContent.equals("")) {
            return "OK No messages in thread";
        }

        return "OK " + threadContent;
    }

    private static String processRemoveThread(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 2) {
            printCommandFailedUse("RMV");
            return INVALID_USAGE;
        }

        String threadName = splittedArgs[1];
        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));

        String failedPrompt = usr.username + " failed to remove thread:";

        if (!database.threadExist(threadName)) {
            String errorMsg = "Thread " + threadName + " does not exist";
            System.out.println(failedPrompt);
            System.out.println(errorMsg);

            return "ERR " + errorMsg;
        }

        if (!database.removeThread(usr.username, threadName)) {
            String errorMsg = "Not owner of original thread";
            System.out.println(failedPrompt);
            System.out.println(errorMsg);

            return "ERR " + errorMsg;
        }

        return "OK Removed thread " + threadName; 
    }
    // ======================================================================

    // ======================================================================
    // Message related functions
    private static String processPostMessage(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ", 3);
        if (splittedArgs.length != 3) {
            printCommandFailedUse("MSG");
            return INVALID_USAGE;
        }

        User usr = database.users.get(Integer.parseInt((splittedArgs[0])));
        String threadName = splittedArgs[1];
        String message = splittedArgs[2];

        if (!database.threadExist(threadName)) {
            String errMsg = "Thread " + threadName + " does not exist";
            System.out.println(usr.username + " failed to post message:");
            System.out.println(errMsg);

            return "ERR " + errMsg;
        }

        database.postMsgToThread(usr.username, threadName, message);
        System.out.println(usr.username + " posted message to thread " + threadName);
        return "OK Message posted to " + threadName;
    }

    private static String processDeleteMessage(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ", 3);
        if (splittedArgs.length != 3) {
            printCommandFailedUse("DLT");
            return INVALID_USAGE;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadName = splittedArgs[1];
        int msgId = Integer.parseInt(splittedArgs[2]);

        String errMsg = usr.username + " failed to delete message";
        if (!database.threadExist(threadName)) {
            String eString = "Thread " + threadName + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            return "ERR " + eString;
        }

        if (!database.threadHasMsgId(threadName, msgId)) {
            String eString = "Message ID " + msgId + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            return "ERR " + eString;
        }

        if (!database.deleteThreadMessage(usr.username, threadName, msgId)) {
            String eString = "Not sender of message";
            System.out.println(errMsg);
            System.out.println(eString);

            return "ERR " + eString;
        }

        System.out.println(usr.username + " deleted a message");
        return "OK Message deleted";
    }

    private static String processEditMessage(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ", 4);
        if (splittedArgs.length != 4) {
            printCommandFailedUse("EDT");
            return INVALID_USAGE;
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

            return "ERR " + eString;
        }

        if (!database.threadHasMsgId(thread, msgId)) {
            String eString = "Message ID " + msgId + " does not exist in thread";
            System.out.println(errMsg);
            System.out.println(eString);

            return "ERR " + eString;
        }

        if (!database.editThreadMessage(usr.username, thread, msgId, message)) {
            String eString = "Not sender of message";
            System.out.println(errMsg);
            System.out.println(eString);

            return "ERR " + eString;
        }

        System.out.println(usr.username + " edited a message");
        return "OK Message edited";
    }
    // ======================================================================

    // ======================================================================
    // File transfer related function
    private static String processUploadFile(String args) throws Exception {
        // Initial error handling
        String[] splittedArgs = args.split(" ");
        if (splittedArgs.length != 3) {
            printCommandFailedUse("UPD");
            return INVALID_USAGE;
        }

        User usr = database.users.get(Integer.parseInt(splittedArgs[0]));
        String threadName = splittedArgs[1];
        String fileName = splittedArgs[2];

        String errMsg = usr.username + " failed to upload file";
        if (!database.threadExist(threadName)) {
            String eString = "Thread " + threadName + " does not exist";
            System.out.println(errMsg);
            System.out.println(eString);

            return "ERR " + eString;
        }

        String convertedName = threadName + "-" + fileName;
        if (database.fileNameExist(convertedName)) {
            String eString = "File " + fileName + " exist";
            System.out.println(errMsg);
            System.out.println(eString);

            return "ERR " + eString;
        }

        server.sendResponce("UPDOK " + fileName);
        processRcivFileFromUser(usr.username, threadName, fileName);
        return "OK"; // will be dropped
    }
    // ======================================================================

    // ======================================================================
    // Exit
    private static String processExit(String args) {
        int userId;
        try {
            userId = Integer.parseInt(args);
        } catch (Exception e) {
            printCommandFailedUse("XIT");
            return INVALID_USAGE;
        }
        User usr = database.users.get(userId);
        System.out.println(usr.username + " logged out");
        database.loggedInUsers.remove(usr);

        if (database.loggedInUsers.size() == 0) {
            System.out.println("\nWaiting for users");
        }

        return "XITOK Goodbye!";
    }
    // ======================================================================

    // ======================================================================
    // Helpers
    private static void printCommandFailedUse(String command) {
        System.out.println("A user failed to use " + command + ":");
        System.out.println("Too many / too little arguments");
    }

    private static void processRcivFileFromUser(String userName, String threadName, String fileName) throws Exception {
        byte[] fileContent = server.getTransferredByte();
        database.addTransferredFile(userName, threadName, fileName, fileContent);

        System.out.println(userName + " successfully uploaded a file");
    }
    // ======================================================================
}
