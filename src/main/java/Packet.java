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

    public byte[] getByteArray(){
        return byteArray;
    }

    //WRQ Packet
    public Packet(byte opCode, String fileName){
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
    public Packet(byte opCode, byte block, byte[] data){
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

            if (i != data.length-1)
                position++;
        }
    }

    //ACK Packet
    public Packet(byte opCode, byte block, int make_this_null){
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
    public Packet(byte opCode, byte errorCode){
        this.opCode = opCode;
        String message = "";

        if (errorCode == 0)
            message = "Not defined, see error message (if any).";

        byteLength = 2 + 2  + message.length() + 1;
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
}
