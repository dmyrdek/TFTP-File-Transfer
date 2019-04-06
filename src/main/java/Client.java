import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    private String ip;
    private int port;

    private DatagramSocket socket;
    private InetAddress address;

    private DatagramPacket outputDatagramPacket;
    private DatagramPacket inputDatagramPacket;

    public Client(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        address = InetAddress.getByName(ip);
        socket = new DatagramSocket();
    }

    public byte[] sendPacket(Packet packet) throws IOException {
        byte [] response = new byte [4];
        outputDatagramPacket = new DatagramPacket(packet.getByteArray(), packet.getByteArray().length, address, port);
        socket.send(outputDatagramPacket);
        DatagramPacket resp = new DatagramPacket(response, response.length);
        socket.receive(resp);
        return resp.getData();
    }
}
