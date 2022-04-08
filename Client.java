import java.net.*;
import java.util.Scanner;

import HPT.*;

public class Client {
    // Attributes
    private static int clientId;
    private static HPTClient client;

    public static void main(String args[]) throws Exception {
        // Exit the program if there are errors in args
        if (hasErrorInit(args)) {
            System.exit(1);
        }

        // Get Server address and port number
        InetAddress serverAddress = InetAddress.getLocalHost();
        int portNum = getPortNum(args[0]);
        System.out.println("Server port is " + portNum);

        // Setup client
        // Socket timeout is 500ms
        Scanner scanner = new Scanner(System.in);
        client = new HPTClient(serverAddress, portNum, 500);
        loginToServer(scanner);

        // Main program
        System.out.println("\nWelcome to the fourm\n");
        while (true) {
            // Send content
            System.out.println("Please enter a command: ");
            System.out.println("CRT, MSG, DLT, EDT, LST, RDT, UPD, DWN, RMV, XIT: ");
            String content = scanner.nextLine();
            HPTPacket response = client.sendRequest(content);

            printResponse(response);
            System.out.print("\n");
        }
    }

    // Check if there is error
    // i.e. Whether user has input args with lengh that is not 1
    private static boolean hasErrorInit(String args[]) {
        if (args.length != 1) {
            System.out.println("Program only accept one argument!");
            System.out.println("\nE.g.\njava Client <Server Port>");
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

    private static void loginToServer(Scanner scanner) throws Exception {
        boolean login_successful = false;
        while (!login_successful) {
            // Login with username
            System.out.print("Enter Username: ");
            String username = scanner.nextLine();
            HPTPacket response = client.sendRequest("LOGIN " + username + "\n");

            // Handle errors
            if (response.header.equals("ERR")) {
                // If user with said username is already logged in, 
                // client have to login with a new user name
                //
                // If the username isnt in the database,
                // client register a new user
                //
                // Unknown exception from the server otherwise
                if (response.content.equals("Already logged in")) {
                    System.out.println("An user already logged in with this username, try again...");
                    login_successful = false;
                } else if (response.content.equals("No username")) {
                    login_successful = registerNewUser(username, scanner);
                } else {
                    throw new Exception("Unknow error occurred from the server");
                }
            } else if (response.header.equals("UNAMEOK")) {
                // Login if UNAMEOK
                login_successful = loginWithPassword(username, scanner);
            } else {
                throw new Exception("Unknow error occurred");
            }
        }
        System.out.println("Client id is: " + clientId);
    }

    private static boolean registerNewUser(String username, Scanner scanner) throws Exception {
        System.out.print("New user, enter password: ");
        String password = scanner.nextLine();
        HPTPacket resp = client.sendRequest("NEWUSER " + username + " " + password + "\n");

        if (resp.header.equals("LOGINOK")) {
            clientId = Integer.parseInt(resp.content);
            return true;
        }

        throw new Exception("Unknown error occured from the server");
    }

    private static boolean loginWithPassword(String username, Scanner scanner) throws Exception {
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        HPTPacket resp = client.sendRequest("PASSWORD " + username + " " + password + "\n");
        
        if (!resp.header.equals("LOGINOK")) {
            System.out.println("Incorrect password, try again...");
            return false;
        } else {
            clientId = Integer.parseInt(resp.content);
            return true;
        }
    }

    private static void printResponse(HPTPacket response) {
        if (!response.header.equals("OK")) {
            System.out.println(response.content);
        }
    }
}
