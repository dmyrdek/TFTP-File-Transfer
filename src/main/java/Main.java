import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Making WRQ Packet:");
        Packet wrq = new Packet((byte)2, "christainasdfasdfasdfasfasdfsafsfasfewqq3 rfqsfasf23456789097654635678ygjkjhbvhkcgi.jpg");
        byte[] wrqArray = wrq.getByteArray();
        System.out.println("Byte array:");
        System.out.println(wrq.toString());
        System.out.println("Converting back to Packet:");
        Packet newPacket = wrq.convertToPacket(wrq.getByteArray());
    }


    public static void getIPAddress() throws IOException {
        String ipAddress = "";
        URL url_name = new URL("http://bot.whatismyipaddress.com");
        BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
        ipAddress = sc.readLine().trim();
        System.out.println("Public IP Address: " + ipAddress);
    }
}
