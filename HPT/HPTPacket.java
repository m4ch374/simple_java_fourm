package HPT;

import java.net.*;

public class HPTPacket {
    public String header;
    public String content;
    public String rawContent;
    public InetAddress sourceAddress;
    public int sourcePort;

    public HPTPacket(String rawContent, InetAddress sourAddress, int sourcePort) {
        this.rawContent = rawContent;
        this.sourceAddress = sourAddress;
        this.sourcePort = sourcePort;

        String[] splittedContent = rawContent.split(" ", 2);

        header = splittedContent[0];

        if (splittedContent.length == 1) {
            content = "";
        } else {
            content = splittedContent[1];
        }
    }

    public static String getUdpContent(DatagramPacket packet) throws Exception {
        return new String(packet.getData(), 0, packet.getLength());
    }

    public static HPTPacket generateFromUDP(DatagramPacket packet) throws Exception {
        String packetContent = getUdpContent(packet);
        return new HPTPacket(packetContent, packet.getAddress(), packet.getPort());
    }
}
