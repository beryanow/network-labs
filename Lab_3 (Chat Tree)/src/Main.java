import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Node node;
        switch (args.length) {
            case 3:
                node = new Node(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                break;
            case 5:
                node = new Node(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
                break;
            default:
                System.err.println("Incorrect input!\n" + "Root: [root IP] [lost percentage] [root port]\n" + "Node: [node IP] [lost percentage] [node port] [parent IP] [parent port]");
                return;
        }
        node.enterMessages();
    }
}
