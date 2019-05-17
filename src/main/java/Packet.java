import java.io.IOException;

public class Packet {

    private byte[] byteArray;
    private int byteLength = 0;
    private static byte opCode;
    private byte zeroByte = 0;

    //Mode, always use octet
    private final String MODE = "octet";

    // Packet Size
    private final int PACKET_SIZE = 516;

    //WRQ variables
    private String fileName;

    //Data Variables
    private byte[] data;

    public byte[] getByteArray() {
        return byteArray;
    }

    public int getBlockNum() {
        if (byteArray[1] == 2)
            return 0;
        else
            return ((byteArray[2] & 0xff) << 8) | (byteArray[3] & 0xff);
    }

    //WRQ Packet
    public Packet(byte opCode, String fileName) {
        this.opCode = opCode;
        this.fileName = fileName;

        this.byteArray = new byte[4 + fileName.length() + MODE.length()];

        int packetByte = 0;
        this.byteArray[packetByte] = 0;
        this.byteArray[++packetByte] = this.opCode;

        System.arraycopy(fileName.getBytes(), 0, byteArray, ++packetByte, fileName.length());

        this.byteArray[packetByte += fileName.length()] = 0;

        System.arraycopy(MODE.getBytes(), 0, byteArray, ++packetByte, MODE.length());

        this.byteArray[packetByte += MODE.length()] = 0;
    }

    //Data Packet
    public Packet(byte opCode, byte[] data, byte[] block) {
        this.opCode = opCode;

        this.byteArray = new byte[4 + data.length];
        int packetByte = 0;
        this.byteArray[packetByte] = 0;
        this.byteArray[++packetByte] = this.opCode;

        System.arraycopy(block, 0, byteArray, ++packetByte, block.length);
        ++packetByte;

        System.arraycopy(data, 0, byteArray, ++packetByte, data.length);
    }

    //ACK Packet
    public Packet(byte opCode, byte[] block, int make_this_null) {
        this.opCode = opCode;

        this.byteArray = new byte[4];
        this.byteArray[0] = 0;
        this.byteArray[1] = this.opCode;

        System.arraycopy(block, 0, this.byteArray, 2, block.length);
    }

    //Error Packet
    public Packet(byte opCode, byte errorCode) {
        this.opCode = opCode;
        String message = "";

        if (errorCode == 0)
            message = "Not defined, see error message (if any).";

        byteLength = 2 + 2 + message.length() + 1;
        byteArray = new byte[byteLength];
        int position = 0;
        byteArray[position] = zeroByte;
        position++;
        byteArray[position] = opCode;
        position++;
        byteArray[position] = zeroByte;
        position++;
        byteArray[position] = errorCode;
        position++;
        for (int i = 0; i < message.length(); i++) {
            byteArray[position] = (byte) message.charAt(i);
            position++;
        }
        byteArray[position] = zeroByte;
    }

    public static Packet convertToPacket(byte[] byteArray) throws IOException {
        Packet packet;
        int postion = 1;

        //WRQ Packet
        if (byteArray[1] == 2) {
            StringBuffer buffer = new StringBuffer();
            int dataByte = 1;  // Start after opcode
            while ((int) byteArray[++dataByte] != 0) {
                buffer.append((char)byteArray[dataByte]);
            }

            return new Packet((byte) 2, buffer.toString());
        }

        //Data Packet
        else if (byteArray[1] == 3) {

            byte[] blockData = new byte[byteArray.length - 4];
            System.arraycopy(byteArray, 4, blockData, 0, byteArray.length - 4);
            byte[] blockNumber = {byteArray[2], byteArray[3]};
            return new Packet((byte) 3 ,blockData, blockNumber);

        }

        //ACK Packet
        else if (byteArray[1] == 4) {

            byte[] blockNumber = {byteArray[2], byteArray[3]};

            return new Packet((byte) 4, blockNumber, 9);
        }

        //Error Packet
        else if (byteArray[1] == 5) {
            opCode = byteArray[postion];
            postion++;
            postion++;
            int errorCode = byteArray[postion];
            postion++;

            packet = new Packet(opCode,(byte) errorCode);
            return packet;
        }

        return null;
    }
}
