import java.io.*;
import java.net.*;

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
            DatagramPacket clientRequest = server.getRequest();
            String requestContent = HPTServer.getRequestContent(clientRequest);

            System.out.println(requestContent);
            String response = processRequest(requestContent);
            System.out.println(response);

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

    private static String processRequest(String request) throws Exception {
        String[] splittedRequest = request.split(" ", 2);
        String command = splittedRequest[0];

        switch (command) {
            case "LOGIN":
                return processLogin(splittedRequest[1]);
            case "PASSWORD":
                return processLoginPassword(splittedRequest[1]);
            case "NEWUSER":
                return processNewUser(splittedRequest[1]);
            default:
                return "ERR Command Not Found\n";
        }
    }

    private static String processLogin(String username) {
        if (database.isUserAlreadyLoggedIn(username)) {
            return "ERR Already logged in\n";
        }

        if (database.usrLogin(username)) {
            return "OK\n";
        } else {
            return "ERR No username\n";
        }
    }

    private static String processLoginPassword(String args) {
        String[] credentials = args.split(" ");

        if (database.usrLoginPassword(credentials[0], credentials[1])) {
            return "OK\n";
        }

        return "ERR No such user\n";
    }

    private static String processNewUser(String args) throws Exception {
        database.addNewUser(args);
        return "OK\n";
    }
}
