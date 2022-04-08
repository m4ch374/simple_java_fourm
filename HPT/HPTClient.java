package HPT;

import java.net.*;

public class HPTClient {
    // Client attributes
    public InetAddress hostAddress;
    public int portNum;
    public DatagramSocket clientSocket;

    // Constants
    private static final int PACKETLEN = 8192;

    public HPTClient(InetAddress hostAddress, int portNum, int timeout) throws Exception {
        this.hostAddress = hostAddress;
        this.portNum = portNum;

        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(timeout);
    }

    public HPTPacket sendRequest(String content) throws Exception {
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

        return HPTPacket.generateFromUDP(response);
    }
}