package HPT;

import java.io.*;
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

    public void uploadFile(String fileName) throws Exception {
        File uploadFile = new File(fileName);
        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] content = inputStream.readAllBytes();
        inputStream.close();

        Socket clientSocket = new Socket(hostAddress, portNum);
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
        outputStream.write(content);
        clientSocket.close();
    }

    public void downloadFile(String fileName) throws Exception {
        Socket clientSocket = new Socket(this.hostAddress, this.portNum);

        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
        byte[] fileContent = inputStream.readAllBytes();
        inputStream.close();

        clientSocket.close();

        File file = new File(fileName);
        file.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(fileContent);
        outputStream.close();
    }
}
