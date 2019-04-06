import java.nio.ByteBuffer;

public class Packet {

    private ByteBuffer byteBuffer;
    private byte opCode;
    private byte block;

    //Mode, always use octet
    private static final String MODE = "octet";

    // Packet Size
    private final static int PACKET_SIZE = 516;

    //WRQ variables
    private String fileName;
    private int wrqByteLength = 0;
    private byte[] wrqByteArray;
    private byte zeroByte = 0;

    //Data Variables
    private byte[] data;
    private int dataByteLength = 0;
    private byte[] dataByteArray;

    //ACK Variables
    private int ackByteLength = 0;
    private byte[] ackByteArray;

    //Error Variables
    private int errorByteLength = 0;
    private byte[] errorByteArray;

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    //WRQ Packet:
    public Packet(byte opCode, String fileName){
        this.opCode = opCode;
        this.fileName = fileName;
        wrqByteLength = 2 + fileName.length() + 1 + MODE.length() + 1;
        wrqByteArray = new byte[wrqByteLength];
        int position = 0;
        wrqByteArray[position] = zeroByte;
        position++;
        wrqByteArray[position] = opCode;
        position++;
        for (int i = 0; i < fileName.length(); i++) {
            wrqByteArray[position] = (byte) fileName.charAt(i);
            position++;
        }
        wrqByteArray[position] = zeroByte;
        position++;
        for (int i = 0; i < MODE.length(); i++) {
            wrqByteArray[position] = (byte) MODE.charAt(i);
            position++;
        }
        wrqByteArray[position] = zeroByte;
        byteBuffer = ByteBuffer.wrap(wrqByteArray);
    }

    //Data Packet
    public Packet(byte opCode, byte block, byte[] data){
        this.opCode = opCode;
        this.data = data;

        dataByteLength = 2 + fileName.length() + 1 + MODE.length() + 1;
        dataByteArray = new byte[dataByteLength];
        int position = 0;

        dataByteArray[position] = zeroByte;
        position++;
        dataByteArray[position] = opCode;
        position++;
        dataByteArray[position] = zeroByte;
        position++;
        dataByteArray[position] = block;
        position++;
        for (int i = 0; i < data.length; i++) {
            dataByteArray[position] = data[i];

            if (i != data.length-1)
                position++;
        }
        byteBuffer = ByteBuffer.wrap(dataByteArray);
    }

    //ACK Packet
    public Packet(byte opCode, byte block, int make_this_null){
        this.opCode = opCode;
        this.block = block;

        ackByteLength = 4;
        ackByteArray = new byte[ackByteLength];
        int position = 0;

        wrqByteArray[position] = zeroByte;
        position++;
        ackByteArray[position] = opCode;
        position++;
        ackByteArray[position] = zeroByte;
        position++;
        ackByteArray[position] = block;
        byteBuffer = ByteBuffer.wrap(errorByteArray);

    }

    //Error Packet
    public Packet(byte opCode, byte errorCode){
        this.opCode = opCode;
        String message = "";

        if (errorCode == 0)
            message = "Not defined, see error message (if any).";

        errorByteLength = 2 + 2  + message.length() + 1;
        errorByteArray = new byte[errorByteLength];
        int position = 0;
        wrqByteArray[position] = zeroByte;
        position++;
        errorByteArray[position] = opCode;
        position++;
        wrqByteArray[position] = zeroByte;
        position++;
        errorByteArray[position] = errorCode;
        position++;
        for (int i = 0; i < message.length(); i++) {
            errorByteArray[position] = (byte) message.charAt(i);
            position++;
        }
        errorByteArray[position] = zeroByte;
        byteBuffer = ByteBuffer.wrap(errorByteArray);
    }
}
