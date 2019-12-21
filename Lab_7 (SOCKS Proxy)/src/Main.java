public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Enter some port to wait at!");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[0]);
            PortForwarder portForwarder = new PortForwarder();
            portForwarder.start(port);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.err.println("Some event occurred!");
        }
    }
}
