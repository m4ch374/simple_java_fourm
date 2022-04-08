package HPT;

import java.io.*;
import java.net.*;

public class HPTPacket {
    public String header;
    public String content;
    public String rawContent;

    public HPTPacket(String rawContent) {
        this.rawContent = rawContent;

        String[] splittedContent = rawContent.split(" ", 2);

        header = splittedContent[0];

        if (splittedContent.length == 1) {
            content = "";
        } else {
            content = splittedContent[1];
        }
    }

    public static String getUdpContent(DatagramPacket packet) throws Exception {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(packet.getData());

        InputStreamReader streamReader = new InputStreamReader(arrayInputStream);

        BufferedReader bufferedReader = new BufferedReader(streamReader);

        return bufferedReader.readLine();
    }

    public static HPTPacket generateFromUDP(DatagramPacket packet) throws Exception {
        String packetContent = getUdpContent(packet);
        return new HPTPacket(packetContent);
    }
}
