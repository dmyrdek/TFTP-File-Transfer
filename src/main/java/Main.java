import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Main {



    public static void getIPAddress() throws IOException {
        String ipAddress = "";
        URL url_name = new URL("http://bot.whatismyipaddress.com");
        BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
        ipAddress = sc.readLine().trim();
        System.out.println("Public IP Address: " + ipAddress);
    }
}
