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

        // Setup client socket
        // Socket timeout is 500ms
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(500);

        // Main program
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Send content
            String content = scanner.nextLine();
            byte[] contentByte = new byte[1024];
            contentByte = content.getBytes();
            DatagramPacket request = new DatagramPacket(contentByte, contentByte.length, serverAddress, portNum);

            // Simple system to prevent packet loss
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
            boolean recievedResponse = false;
            String resopnceContent = "";
            while (!recievedResponse) {
                clientSocket.send(request);

                try {
                    clientSocket.receive(response);
                    recievedResponse = true;
                } catch (SocketTimeoutException e) {
                    System.out.println("Packet lost, retrying...");
                    recievedResponse = false;
                }
            }

            BufferedReader bufferedReader 
                    = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getData())));

            resopnceContent = bufferedReader.readLine();
            System.out.println(resopnceContent);
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
