import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {
    private String ip;
    private int port;

    private DatagramSocket socket;
    private InetAddress address;

    private DatagramPacket outputDatagramPacket;
    private DatagramPacket inputDatagramPacket;

    public Client(String ip, int port) throws UnknownHostException {
        this.ip = ip;
        this.port = port;
        address = InetAddress.getByName(ip);
    }

    public void sendPacket(Packet packet) throws IOException {
        socket = new DatagramSocket();
        outputDatagramPacket = new DatagramPacket(packet.getByteArray(), packet.getByteArray().length, address, port);

    }
}
