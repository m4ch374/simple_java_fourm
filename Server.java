import java.io.*;
import java.net.*;

public class Server {
    public static void main(String args[]) throws Exception {
        // Exit the program if there are errors in args
        if (hasErrorInit(args)) {
            System.exit(1);
        }

        // Setup server socket and port number
        // socket timeout is 500
        int portNum = getPortNum(args[0]);
        System.out.println("Server port is " + portNum);
        HPTServer server = new HPTServer(portNum);

        // Main program
        System.out.println("Server is starting...\n");
        while (true) {
            DatagramPacket clientRequest = server.getRequest();
            String requestContent = HPTServer.getRequestContent(clientRequest);

            System.out.println(requestContent);

            try {
                server.sendResponce("OK");
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
}
