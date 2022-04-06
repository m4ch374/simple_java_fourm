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
        DatagramSocket serverSocket = new DatagramSocket(portNum);

        // Main program
        System.out.println("Server is starting...\n");
        while (true) {
            DatagramPacket clientRequest = new DatagramPacket(new byte[1024], 1024);
            serverSocket.receive(clientRequest);

            BufferedReader bufferedReader 
                = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(clientRequest.getData())));
            
            String requestContent = bufferedReader.readLine();
            System.out.println(requestContent);

            String responseContent = "OK";
            byte[] buffer = new byte[1024];
            buffer = responseContent.getBytes();
            DatagramPacket response 
                = new DatagramPacket(buffer, buffer.length, clientRequest.getAddress(), clientRequest.getPort());
            serverSocket.send(response);
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
