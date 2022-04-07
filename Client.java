import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
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
        HPTClient client = new HPTClient(serverAddress, portNum, 500);

        // Main program
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Send content
            String content = scanner.nextLine();
            DatagramPacket response = client.sendRequest(content);

            String responseContent = HPTClient.getPacketContent(response);
            System.out.println(responseContent);
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
}
