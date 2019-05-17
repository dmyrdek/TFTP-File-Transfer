import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        int side;
        boolean ipv4, sequential, nodrops;
        System.out.println("Enter 1 for Client or 2 for Server: ");
        Scanner scanner = new Scanner(System.in);
        side = Integer.parseInt(scanner.nextLine());

        if (side == 1) {
            System.out.println("Enter the ip address of the server: ");
            String dest = scanner.nextLine();

            System.out.println("Enter port of the server: ");
            int udpPort = Integer.parseInt(scanner.nextLine());

            System.out.println("Enter path to file you wish to send: ");
            String path = scanner.nextLine();

            while (true) {
                System.out.println("IPv4 or IPv6, enter 1 for IPv4 or 2 for IPv6:");
                String ipv4Choice = scanner.nextLine();
                if (ipv4Choice.equals("1")) {
                    ipv4 = true;
                    break;
                } else if (ipv4Choice.equals("2")) {
                    ipv4 = false;
                    break;
                } else {
                    System.out.println("Incorrect input please try again.");
                }
            }

            while (true) {
                System.out.println("Sequential or SlidingWindows, enter 1 for Sequential or 2 for SlidingWindows");
                String sequentialChoice = scanner.nextLine();
                if (sequentialChoice.equals("1")) {
                    sequential = true;
                    break;
                } else if (sequentialChoice.equals("2")) {
                    sequential = false;
                    break;
                } else {
                    System.out.println("Incorrect input please try again.");
                }
            }

            while (true) {
                System.out.println("No drops or 1% drops, enter 1 for no drops or 2 for 1% drops");
                String dropChoice = scanner.nextLine();
                if (dropChoice.equals("1")) {
                    nodrops = true;
                    break;
                } else if (dropChoice.equals("2")) {
                    nodrops = false;
                    break;
                } else {
                    System.out.println("Incorrect input please try again.");
                }
            }


            Client client = new Client(dest, udpPort, ipv4, sequential, nodrops);
            client.sendFile(new File(path));


        } else if (side == 2) {
            Server server = new Server();
            getIPAddress();
            server.getPort();
            System.out.println("Enter path to store file, with a file name: ");
            String filePath = scanner.nextLine();
            while (true) {
                System.out.println("Sequential or SlidingWindows, enter 1 for Sequential or 2 for SlidingWindows");
                String sequentialChoice = scanner.nextLine();
                if (sequentialChoice.equals("1")) {
                    sequential = true;
                    break;
                } else if (sequentialChoice.equals("2")) {
                    sequential = false;
                    break;
                } else {
                    System.out.println("Incorrect input please try again.");
                }
            }
            server.receive(filePath, sequential);
        }
    }


    public static void getIPAddress() throws IOException {
        String ipAddress = "";
        URL url_name = new URL("http://bot.whatismyipaddress.com");
        BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
        ipAddress = sc.readLine().trim();
        System.out.println("Public IP Address: " + ipAddress);
    }
}
