import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    private String ip;
    private int port;
    private DatagramSocket socket;
    boolean ipv4, sequential, noDrops;
    final int window = 10;

    public Client(String ip, int port, boolean ipv4, boolean sequential, boolean noDrops) throws IOException {
        this.ip = ip;
        this.port = port;
        socket = new DatagramSocket();
        this.ipv4 = ipv4;
        this.sequential = sequential;
        this.noDrops = noDrops;
    }

    public double sendFile(File f) throws IOException {
        if (!sequential) {
            return sendSliding(f);
        }

        int dataSize = 0;
        long time = 0;
        try {

            //send WRQ
            socket = new DatagramSocket();
            byte[] data = Files.readAllBytes(f.toPath());
            Packet reqPacket = new Packet((byte) 2, f.getPath());
            DatagramPacket response = new DatagramPacket(new byte[516], 516);

            DatagramPacket packetForSend;


            if (ipv4) {
                packetForSend = new DatagramPacket(reqPacket.getByteArray(), reqPacket.getByteArray().length, Inet4Address.getByName(ip), port);
            } else {
                packetForSend = new DatagramPacket(reqPacket.getByteArray(), reqPacket.getByteArray().length, Inet6Address.getByName(ip), port);
            }

            socket.send(packetForSend);

            //Receive initial packet
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.convertToPacket(response.getData()));
            time = System.nanoTime();
            while (Packet.convertToPacket(response.getData()).getBlockNum() != 0) {
                socket.send(packetForSend);
                socket.receive(response);
            }

            //Send data and get ACK in between
            int dataLeft = data.length;
            dataSize = data.length;
            short blockNumber = 0;
            byte[] blockData = new byte[512];
            int dropLottery = 101;
            Packet nextData;
            if (!noDrops)
                dropLottery = ThreadLocalRandom.current().nextInt(100);
            do {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);
                nextData = new Packet((byte) 3, blockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
                System.out.println("Block num: " + blockNumber);
                packetForSend.setData(nextData.getByteArray());
                if (ThreadLocalRandom.current().nextInt(100) != dropLottery)
                    socket.send(packetForSend);
                //Receive Ack
                socket.receive(response);
                //Subtract data from dataLeft
                dataLeft -= 512;
                System.out.println(Packet.convertToPacket(response.getData()).getBlockNum());
                while (Packet.convertToPacket(response.getData()).getBlockNum() != nextData.getBlockNum()) {
                    System.out.println("uh oh");
                    socket.send(packetForSend);
                    socket.receive(response);
                }
                while (response.getData()[1] == (byte) 5) {
                    System.out.println("Error code: " + response.getData()[3]);
                    socket.send(packetForSend);
                    socket.receive(response);
                }

            } while (dataLeft >= 512);

            byte[] finalBlockData = new byte[dataLeft];
            System.arraycopy(data, blockNumber * 512, finalBlockData, 0, dataLeft);
            nextData = new Packet((byte) 3 , finalBlockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
            DatagramPacket finalPacket = new DatagramPacket(nextData.getByteArray(), nextData.getByteArray().length, packetForSend.getAddress(), port);
            socket.send(finalPacket);
            System.out.println("Data packet: " + nextData.getBlockNum());
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.convertToPacket(response.getData()).getBlockNum());

            while (response.getData()[1] == (byte) 5) {
                System.out.println("Error code: " + response.getData()[3]);
                socket.send(finalPacket);
                socket.receive(response);
            }

            time = System.nanoTime() - time;

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return calcTime(time, dataSize);
    }

    private double sendSliding(File f) {
        long time = 0;
        int dataSize = 0;
        try {
            socket = new DatagramSocket();
            byte[] data = Files.readAllBytes(f.toPath());
            Packet reqPacket = new Packet((byte) 2,f.getPath());
            DatagramPacket response = new DatagramPacket(new byte[516], 516);
            DatagramPacket packetForSend;

            if (ipv4) {
                packetForSend = new DatagramPacket(reqPacket.getByteArray(), reqPacket.getByteArray().length, Inet4Address.getByName(ip), port);
            } else {
                packetForSend = new DatagramPacket(reqPacket.getByteArray(), reqPacket.getByteArray().length, Inet6Address.getByName(ip), port);
            }

            //Send WRQ
            socket.send(packetForSend);

            //Receive initial ACK or ERR
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.convertToPacket(response.getData()));
            while (Packet.convertToPacket(response.getData()).getBlockNum() != 0) {
                socket.send(packetForSend);
                socket.receive(response);
            }

            socket.setSoTimeout(3000);
            int dataLeft = data.length;
            dataSize = data.length;
            short blockNumber = 0;
            int lastAckReceived = 1;
            byte[] blockData = new byte[512];
            Packet nextData;

            int dropLottery = 101;
            if (!noDrops) {
                dropLottery = ThreadLocalRandom.current().nextInt(100);
            }

            //Send first window
            time = System.nanoTime();
            for (int i = 0; i < window; i++) {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);
                nextData = new Packet((byte) 3, blockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
                packetForSend.setData(nextData.getByteArray());
                socket.send(packetForSend);
            }

            System.out.println("Initial window sent");

            socket.receive(response);

            do {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);

                nextData = new Packet((byte) 3, blockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
                packetForSend.setData(nextData.getByteArray());

                if (ThreadLocalRandom.current().nextInt(100) != dropLottery) {
                    socket.send(packetForSend);
                }


                try {
                    socket.receive(response);
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout, going back to: " + lastAckReceived);
                    blockNumber = (short) lastAckReceived;
                    continue;
                }
                lastAckReceived = Packet.convertToPacket(response.getData()).getBlockNum();
                dataLeft = data.length - (blockNumber * 512);
            }while (dataLeft > 512 && blockNumber - lastAckReceived < window);

            byte[] finalBlockData = new byte[dataLeft];
            System.arraycopy(data, blockNumber * 512, finalBlockData, 0, dataLeft);
            nextData = new Packet((byte) 3, finalBlockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
            DatagramPacket finalPacket = new DatagramPacket(nextData.getByteArray(), nextData.getByteArray().length, packetForSend.getAddress(), port);
            socket.send(finalPacket);
            System.out.println("Data packet: " + nextData.getBlockNum());

            while (Packet.convertToPacket(response.getData()).getBlockNum() < nextData.getBlockNum()) {
                socket.receive(response);
            }
            time = System.nanoTime() - time;

            System.out.println("ACK packet: " + Packet.convertToPacket(response.getData()).getBlockNum());
            socket.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return calcTime(time, dataSize);
    }


    private static double calcTime(long time, int size) {
        return Math.round(((((double) size * 8.0) / 100000)/ ((double) time)* 1_000_000));
    }


}
