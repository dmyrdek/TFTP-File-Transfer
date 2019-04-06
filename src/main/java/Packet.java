import java.io.IOException;

public class Packet {

    private byte[] byteArray;
    private int byteLength = 0;
    private byte opCode;
    private byte block;
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

    //WRQ Packet
    public Packet(byte opCode, String fileName) {
        this.opCode = opCode;
        this.fileName = fileName;
        byteLength = 2 + fileName.length() + 1 + MODE.length() + 1;
        byteArray = new byte[byteLength];
        int position = 0;
        byteArray[position] = zeroByte;
        position++;
        byteArray[position] = opCode;
        position++;
        for (int i = 0; i < fileName.length(); i++) {
            byteArray[position] = (byte) fileName.charAt(i);
            position++;
        }
        byteArray[position] = zeroByte;
        position++;
        for (int i = 0; i < MODE.length(); i++) {
            byteArray[position] = (byte) MODE.charAt(i);
            position++;
        }
        byteArray[position] = zeroByte;
    }

    //Data Packet
    public Packet(byte opCode, byte block, byte[] data) {
        this.opCode = opCode;
        this.data = data;

        byteLength = 2 + fileName.length() + 1 + MODE.length() + 1;
        byteArray = new byte[byteLength];
        int position = 0;

        byteArray[position] = zeroByte;
        position++;
        byteArray[position] = opCode;
        position++;
        byteArray[position] = zeroByte;
        position++;
        byteArray[position] = block;
        position++;
        for (int i = 0; i < data.length; i++) {
            byteArray[position] = data[i];

            if (i != data.length - 1)
                position++;
        }
    }

    //ACK Packet
    public Packet(byte opCode, byte block, int make_this_null) {
        this.opCode = opCode;
        this.block = block;

        byteLength = 4;
        byteArray = new byte[byteLength];
        int position = 0;

        byteArray[position] = zeroByte;
        position++;
        byteArray[position] = opCode;
        position++;
        byteArray[position] = zeroByte;
        position++;
        byteArray[position] = block;
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

    public Packet convertToPacket(byte[] byteArray) throws IOException {
        Packet packet;
        int postion = 1;

        //WRQ Packet
        if (byteArray[1] == 2) {
            String fileName;
            int fileNamePostion = 0;
            int fileNameSizeCounter = 1;

            opCode = byteArray[postion];
            postion++;

            while (byteArray[postion] != 0){
                fileNameSizeCounter++;
                postion++;
            }
            postion = postion - fileNameSizeCounter;
            byte[] filenameBytes = new byte[fileNameSizeCounter];
            while (byteArray[postion] != 0){
                filenameBytes[fileNamePostion] = byteArray[postion];
                fileNamePostion++;
                postion++;
            }
            fileName = new String(filenameBytes, "UTF-8");

            packet = new Packet(opCode, fileName);
            return packet;
        }

        //Data Packet
        else if (byteArray[1] == 3) {
            opCode = byteArray[postion];
            postion++;
            postion++;
            block = byteArray[postion];
            postion++;
            int dataSize = byteArray.length - postion;
            int dataPostion = 0;
            byte[] convertedData = null;
            while (postion <= byteArray.length) {
                convertedData = new byte[dataSize];
                convertedData[dataPostion] = byteArray[postion];
                postion++;
                dataPostion++;
            }

            packet = new Packet(opCode,block,convertedData);
            return packet;
        }

        //ACK Packet
        else if (byteArray[1] == 4) {
            opCode = byteArray[postion];
            postion++;
            postion++;
            block = byteArray[postion];
            packet = new Packet(opCode,block,9);
            return packet;
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
