package HPT;

import java.io.*;
import java.net.*;

public class HPTServer {
    public int portNum;
    private DatagramSocket serverSocket;
    private ServerSocket fileServerSocket;

    // Dummy client value
    public InetAddress clientAddress = InetAddress.getLocalHost();
    public int clientPort = -1;

    // Constants
    private static final int PACKETLEN = 8192;

    public HPTServer(int portNum) throws Exception {
        this.portNum = portNum;
        serverSocket = new DatagramSocket(this.portNum);
        fileServerSocket = new ServerSocket(this.portNum);
    }

    public HPTPacket getRequest() throws Exception {
        DatagramPacket clientRequest = new DatagramPacket(new byte[PACKETLEN], PACKETLEN);
        serverSocket.receive(clientRequest);

        clientAddress = clientRequest.getAddress();
        clientPort = clientRequest.getPort();

        return HPTPacket.generateFromUDP(clientRequest);
    }

    public byte[] getTransferredByte() throws Exception {
        Socket fileSocket = fileServerSocket.accept();

        DataInputStream inputStream = new DataInputStream(fileSocket.getInputStream());

        byte[] content = inputStream.readAllBytes();
        inputStream.close();
        fileSocket.close();
        return content;
    }

    public void sendFileToClient(File file) throws Exception {
        Socket filSocket = fileServerSocket.accept();

        FileInputStream inputStream = new FileInputStream(file);
        byte[] fileContent = inputStream.readAllBytes();
        inputStream.close();

        DataOutputStream outputStream = new DataOutputStream(filSocket.getOutputStream());
        outputStream.write(fileContent);
        outputStream.close();

        filSocket.close();
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
}
