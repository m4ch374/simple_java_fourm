import HPT.*;
import others.*;

public class Server {
    // Attributes
    private static HPTServer server;
    private static Database database;

    // Constants
    private static final String credentialFilePath = "./credentials.txt";

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
        database = new Database(credentialFilePath);
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
            default:
                return "ERR Command Not Found\n";
        }
    }

    private static String processLogin(String username) {
        System.out.println("Client authenticating...");

        if (database.isUserAlreadyLoggedIn(username)) {
            System.out.println("Username " + username + "already logged in");
            return "ERR Already logged in\n";
        }

        if (database.usrLogin(username)) {
            System.out.println(username + " entering password");
            return "UNAMEOK\n";
        } else {
            System.out.println("New user, entering password");
            return "ERR No username\n";
        }
    }

    private static String processLoginPassword(String args) {
        String[] credentials = args.split(" ");

        int userId = database.usrLoginPassword(credentials[0], credentials[1]);
        if (userId != -1) {
            System.out.println(credentials[0] + " successful login");
            return "LOGINOK " + userId + "\n";
        }

        System.out.println("Incorrect password");
        return "ERR No such user\n";
    }

    private static String processNewUser(String args) throws Exception {
        int newId = database.addNewUser(args);
        System.out.println("New user created, successful login");
        return "LOGINOK " + newId + "\n";
    }
}
