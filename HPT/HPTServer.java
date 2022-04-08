package HPT;

import java.io.*;
import java.net.*;

public class HPTServer {
    public int portNum;
    DatagramSocket serverSocket;

    // Dummy client value
    public InetAddress clientAddress = InetAddress.getLocalHost();
    public int clientPort = -1;

    // Constants
    private static final int PACKETLEN = 1024;

    public HPTServer(int portNum) throws Exception {
        this.portNum = portNum;
        serverSocket = new DatagramSocket(this.portNum);
    }

    public DatagramPacket getRequest() throws Exception {
        DatagramPacket clientRequest = new DatagramPacket(new byte[PACKETLEN], PACKETLEN);
        serverSocket.receive(clientRequest);

        clientAddress = clientRequest.getAddress();
        clientPort = clientRequest.getPort();
        return clientRequest;
    }

    public void sendResponce(String content) throws Exception {
        if (clientPort == -1) {
            throw new Exception("error: Packet did not recieve");
        }

        byte[] contentBuffer = content.getBytes();
        DatagramPacket responsePacket 
            = new DatagramPacket(contentBuffer, contentBuffer.length, clientAddress, clientPort);
        serverSocket.send(responsePacket);
    }

    public static String getRequestContent(DatagramPacket packet) throws Exception {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(packet.getData());

        InputStreamReader streamReader = new InputStreamReader(arrayInputStream);

        BufferedReader bufferedReader = new BufferedReader(streamReader);

        return bufferedReader.readLine();
    }
}
