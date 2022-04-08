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
            DatagramPacket response = client.sendRequest(content);

            String responseContent = HPTClient.getPacketContent(response);
            printResponse(responseContent);
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
            DatagramPacket response = client.sendRequest("LOGIN " + username + "\n");

            String loginResponse = HPTClient.getPacketContent(response);
            if (loginResponse.equals("ERR Already logged in")) {
                System.out.println("An user already logged in with this username, try again...");
                login_successful = false;
            } else if (loginResponse.equals("ERR No username")) {
                System.out.print("New user, enter password: ");
                String password = scanner.nextLine();
                DatagramPacket resp = client.sendRequest("NEWUSER " + username + " " + password + "\n");
                String respContent = HPTClient.getPacketContent(resp);
                login_successful = true;
                clientId = Integer.parseInt(respContent.split(" ")[1]);
            } else if (loginResponse.equals("OK")) {
                login_successful = loginWithPassword(username, scanner);
            } else {
                throw new Exception("Unknown error occurred");
            }
        }
        System.out.println("Client id is: " + clientId);
    }

    private static boolean loginWithPassword(String username, Scanner scanner) throws Exception {
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        DatagramPacket resp = client.sendRequest("PASSWORD " + username + " " + password + "\n");
        String respContent = HPTClient.getPacketContent(resp);
        
        if (!respContent.split(" ")[0].equals("LOGINOK")) {
            System.out.println("Incorrect password, try again...");
            return false;
        } else {
            clientId = Integer.parseInt(respContent.split(" ")[1]);
            return true;
        }
    }

    private static void printResponse(String response) {
        if (!response.equals("OK")) {
            String content = response.split(" ", 2)[1];
            System.out.println(content);
        }
    }
}
