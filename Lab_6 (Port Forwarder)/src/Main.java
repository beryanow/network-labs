public class Main {
    public static void main(String[] args) {
        int lPort = Integer.parseInt(args[0]);
        String rHost = args[1];
        int rPort = Integer.parseInt(args[2]);

        Assistance assistance = new Assistance(lPort, rHost, rPort);
        PortForwarder portForwarder = new PortForwarder(assistance);
        portForwarder.initiate();
    }
}
