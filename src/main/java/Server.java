import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {

    int port;
    DatagramSocket socket;
    byte [] message;

    public Server(int port) {
        this.port = port;
        message = new byte[516];
    }

    public void startServer(Packet ackPacket) throws IOException {
        DatagramPacket packet = new DatagramPacket(message, message.length);
        socket = new DatagramSocket(port);
        socket.receive(packet);
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(ackPacket.getByteArray(), ackPacket.getByteArray().length, address, port);
        socket.send(packet);
    }
}
