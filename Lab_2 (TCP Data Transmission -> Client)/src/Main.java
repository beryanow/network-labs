public class Main {
    public static void main(String[] args) {
        Client client = new Client(args[0]);
        client.connectToServer(args[1], Integer.parseInt(args[2]));
    }
}
