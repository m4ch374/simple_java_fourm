package HPT;

import java.io.*;
import java.net.*;

public class HPTServer {
    public int portNum;
    private DatagramSocket serverSocket;
    private ServerSocket fileServerSocket;

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

    public void sendResponce(String content, HPTPacket packet) throws Exception {
        byte[] contentBuffer = content.getBytes();
        DatagramPacket responsePacket 
            = new DatagramPacket(contentBuffer, contentBuffer.length, packet.sourceAddress, packet.sourcePort);
        serverSocket.send(responsePacket);
    }
}
