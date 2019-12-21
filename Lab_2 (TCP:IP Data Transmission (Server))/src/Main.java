import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server(Integer.parseInt(args[0]));
            server.listenClients();
        } catch (IOException e) {
            System.out.println("Incorrect port input!");
        }
    }
}
