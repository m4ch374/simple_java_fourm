package HPT;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class HPTClient {
    // Client attributes
    public InetAddress hostAddress;
    public int portNum;
    public DatagramSocket clientSocket;

    // Constants
    private static final int PACKETLEN = 1024;

    public HPTClient(InetAddress hostAddress, int portNum, int timeout) throws Exception {
        this.hostAddress = hostAddress;
        this.portNum = portNum;

        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(timeout);
    }

    public DatagramPacket sendRequest(String content) throws Exception {
        byte[] contentBuffer = content.getBytes();
        DatagramPacket request 
            = new DatagramPacket(contentBuffer, contentBuffer.length, hostAddress, portNum);

        DatagramPacket response = new DatagramPacket(new byte[PACKETLEN], PACKETLEN);
        
        boolean recieved = false;
        while (!recieved) {
            clientSocket.send(request);

            try {
                clientSocket.receive(response);
                recieved = true;
            } catch (SocketTimeoutException e) {
                System.out.println("\nPacket timed out, trying again.....\n");
                recieved = false;
            }
        }

        return response;
    }

    public static String getPacketContent(DatagramPacket packet) throws Exception {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(packet.getData());

        InputStreamReader streamReader = new InputStreamReader(arrayInputStream);

        BufferedReader bufferedReader = new BufferedReader(streamReader);

        return bufferedReader.readLine();
    }
}
