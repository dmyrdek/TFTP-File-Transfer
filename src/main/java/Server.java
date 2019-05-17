import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server {

    private DatagramSocket udpSocket;

    public Server(){
        try {
            udpSocket = new DatagramSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive(String filePath, boolean sequential) {

        DatagramPacket packet = new DatagramPacket(new byte[516], 516);
        Packet ACK;
        ArrayList<Byte> fileData = new ArrayList<>();
        byte[] blockData;
        byte[] blockNumber = {0, 0};
        int dataSize = 516;
        int lpr = 0;
        int run = 0;

        try {
            System.out.println("Listening");
            udpSocket.receive(packet);
            if (packet.getData()[1] == 2) {//If packet was request, check protocol then send ACK with 0 block number
                ACK = new Packet((byte) 4, blockNumber, 9);
                udpSocket.send(new DatagramPacket(ACK.getByteArray(), 4, packet.getAddress(), packet.getPort()));
            }
            //Start loop for data packets
            do {
                lpr = Packet.convertToPacket(packet.getData()).getBlockNum();
                System.out.println(lpr);

                if (!sequential)
                    //Sliding windows
                    udpSocket.receive(packet);
                else {
                    try {
                        udpSocket.setSoTimeout(3000);
                        udpSocket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        //if there is a time out resend
                        System.out.println("Lost a packet, resending last ACK");
                        ACK = new Packet((byte) 4, blockNumber, null);
                        System.out.println(ACK);
                        udpSocket.send(new DatagramPacket(ACK.getByteArray(), 4, packet.getAddress(), packet.getPort()));
                        continue;
                    }
                }


                    while (Packet.convertToPacket(packet.getData()).getBlockNum() != lpr + 1) {
                        System.out.println(Packet.convertToPacket(packet.getData()).getBlockNum());//If received packet is beyond the next expected packet hold out until client goes back N and sends the correct one
                        udpSocket.receive(packet);
                        System.out.println(packet);
                    }

                blockNumber[0] = packet.getData()[2];
                blockNumber[1] = packet.getData()[3];

                dataSize = packet.getLength();
                System.out.println(dataSize);
                //Size of data in packet, ie. packet - 4 bytes for opcode and block number
                blockData = new byte[dataSize - 4];
                System.arraycopy(packet.getData(), 4, blockData, 0, dataSize - 4);
                for (byte b : blockData) {
                    fileData.add(b);
                }

                ACK = new Packet((byte) 4, blockNumber, 9);
                udpSocket.send(new DatagramPacket(ACK.getByteArray(), 4, packet.getAddress(), packet.getPort()));

                run++;
            } while (dataSize == 516);

            writeFile(fileData, filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        udpSocket.close();
    }



    private void writeFile(ArrayList<Byte> fileData, String filePath) {
        try {
            File file = new File(filePath);

            //Create file if it doesn't exist
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.out.println("Invalid path");
                    e.printStackTrace();
                }
                System.out.println("New file created");
            }
            //Convert array list to array so we can write it into the file
            byte[] bytesToWrite = new byte[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                bytesToWrite[i] = fileData.get(i);
            }

            FileOutputStream outputStream = new FileOutputStream(file);

            outputStream.write(bytesToWrite);
            outputStream.flush();
            outputStream.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File successfully written");
    }

    public void getPort() {
        System.out.println("UDP Port: " + udpSocket.getLocalPort());
    }
}
